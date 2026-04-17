package org.lunskra.adapter.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.adapter.api.mapper.MemberDtoMapper;
import org.lunskra.adapter.utils.MemberDtoGenerator;
import org.lunskra.adapter.utils.MemberGenerator;
import org.lunskra.core.domain.GeonamesCity;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.MemberPage;
import org.lunskra.family_tree.api.model.GenderDto;
import org.lunskra.family_tree.api.model.MemberDto;
import org.lunskra.family_tree.api.model.MemberPageDto;
import org.lunskra.family_tree.api.model.UnresolvedLocationDto;
import org.lunskra.port.in.ValidateMemberUseCase;
import org.lunskra.port.out.GeocodingRepositoryPort;
import org.lunskra.port.out.ImageStoragePort;
import org.lunskra.port.out.MemberRepositoryPort;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberRepositoryPort memberRepositoryPort;

    @Mock
    ValidateMemberUseCase validateMemberUseCase;

    @Mock
    ImageStoragePort imageStoragePort;

    @Mock
    GeocodingRepositoryPort geocodingRepositoryPort;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MemberDtoMapper memberDtoMapper = Mappers.getMapper(MemberDtoMapper.class);
        memberService = new MemberService(memberRepositoryPort, memberDtoMapper, validateMemberUseCase, imageStoragePort, geocodingRepositoryPort);
    }

    // ──────────────────────────────────────────────
    // listMembers
    // ──────────────────────────────────────────────

    @Test
    void testListMembers_WhenTwoUsersExist_ThenReturnTwoUsers() {
        when(memberRepositoryPort.listMembers(any(), any(), any(), anyInt(), anyInt())).thenReturn(
                new MemberPage(
                        List.of(
                                MemberGenerator.createLivingMemberWithRequiredData(),
                                MemberGenerator.createLivingMemberWithAllData()
                        ),
                        2L
                )
        );
        MemberPageDto result = memberService.listMembers(null, null, null, 0, 20);
        Assertions.assertEquals(2, result.getContent().size());
        Assertions.assertEquals(2L, result.getTotalElements());
    }

    // ──────────────────────────────────────────────
    // getMember
    // ──────────────────────────────────────────────

    @Test
    void testGetMember_WhenIdExists_ThenReturnMember() {
        when(memberRepositoryPort.getMember(1)).thenReturn(MemberGenerator.createLivingMemberWithRequiredData());
        Assertions.assertEquals("John", memberService.getMember(1).getFirstName());
    }

    // ──────────────────────────────────────────────
    // deleteMember
    // ──────────────────────────────────────────────

    @Test
    void testDeleteMember_WhenCalled_ThenDelegatesToRepo() {
        when(memberRepositoryPort.getMember(1)).thenReturn(MemberGenerator.createLivingMemberWithRequiredData());
        memberService.deleteMember(1);

        verify(memberRepositoryPort).deleteMember(1);
    }

    // ──────────────────────────────────────────────
    // createMember / updateMember — basic
    // ──────────────────────────────────────────────

    @Test
    void testCreateMember_WhenValid_ThenValidatesAndReturnsDto() {
        when(memberRepositoryPort.createMember(any())).thenReturn(MemberGenerator.createLivingMemberWithRequiredData());
        MemberDto input = MemberDtoGenerator.createLivingMemberDtoWithRequiredData();

        MemberDto result = memberService.createMember(input, null);

        assertEquals("John", result.getFirstName());
        verify(validateMemberUseCase).validateNewMember(any());
        verify(memberRepositoryPort).createMember(any());
    }

    @Test
    void testUpdateMember_WhenValid_ThenValidatesAndReturnsDto() {
        when(memberRepositoryPort.updateMember(any())).thenReturn(MemberGenerator.createLivingMemberWithRequiredData());
        MemberDto input = MemberDtoGenerator.createLivingMemberDtoWithRequiredData();

        MemberDto result = memberService.updateMember(1, input, null);

        assertEquals("John", result.getFirstName());
        verify(validateMemberUseCase).validateExistingMember(any());
        verify(memberRepositoryPort).updateMember(any());
    }

    // ──────────────────────────────────────────────
    // createMember — geocoding / coordinate resolution
    // ──────────────────────────────────────────────

    @DisplayName("createMember: unique geo match → canonical city/country and coordinates written to persisted domain object")
    @Test
    void testCreateMember_WhenUniqueGeoMatch_ThenCoordinatesAndNamesResolved() {
        GeonamesCity munich = GeonamesCity.builder()
                .name("Munich").countryCode("DE").countryName("Germany")
                .lat(48.13).lng(11.57).build();
        when(geocodingRepositoryPort.findCountryCodeByName("Germany")).thenReturn(Optional.of("DE"));
        when(geocodingRepositoryPort.findCityByNameAndCountry("Munich", "DE")).thenReturn(Optional.of(munich));

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        when(memberRepositoryPort.createMember(captor.capture()))
                .thenReturn(MemberGenerator.createLivingMemberWithRequiredData());

        memberService.createMember(memberDtoWithBirthLocation("Munich", "Germany"), null);

        Member saved = captor.getValue();
        assertEquals("Munich", saved.getBirthCity());
        assertEquals("Germany", saved.getBirthCountry());
        assertEquals(48.13, saved.getBirthLat(), 0.001);
        assertEquals(11.57, saved.getBirthLng(), 0.001);
    }

    @DisplayName("createMember: ambiguous geo match → original birthCity and birthCountry kept, no coordinates set")
    @Test
    void testCreateMember_WhenAmbiguousGeoMatch_ThenOriginalBirthCityKept() {
        when(geocodingRepositoryPort.findCountryCodeByName("Germany")).thenReturn(Optional.of("DE"));
        when(geocodingRepositoryPort.findCityByNameAndCountry("Re", "DE")).thenReturn(Optional.empty());

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        when(memberRepositoryPort.createMember(captor.capture()))
                .thenReturn(MemberGenerator.createLivingMemberWithRequiredData());

        memberService.createMember(memberDtoWithBirthLocation("Re", "Germany"), null);

        Member saved = captor.getValue();
        assertEquals("Re", saved.getBirthCity());
        assertEquals("Germany", saved.getBirthCountry());
        assertNull(saved.getBirthLat());
        assertNull(saved.getBirthLng());
    }

    @DisplayName("createMember: no geo match → original birthCity kept, no coordinates set")
    @Test
    void testCreateMember_WhenNoGeoMatch_ThenOriginalBirthCityKept() {
        when(geocodingRepositoryPort.findCountryCodeByName("Germany")).thenReturn(Optional.of("DE"));
        when(geocodingRepositoryPort.findCityByNameAndCountry("Atlantis", "DE")).thenReturn(Optional.empty());

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        when(memberRepositoryPort.createMember(captor.capture()))
                .thenReturn(MemberGenerator.createLivingMemberWithRequiredData());

        memberService.createMember(memberDtoWithBirthLocation("Atlantis", "Germany"), null);

        Member saved = captor.getValue();
        assertEquals("Atlantis", saved.getBirthCity());
        assertNull(saved.getBirthLat());
    }

    @DisplayName("createMember: coordinates already present → geocoding repository never queried")
    @Test
    void testCreateMember_WhenCoordinatesAlreadyPresent_ThenGeocodingSkipped() {
        when(memberRepositoryPort.createMember(any()))
                .thenReturn(MemberGenerator.createLivingMemberWithRequiredData());

        MemberDto input = memberDtoWithBirthLocation("Munich", "Germany")
                .birthLat(48.13).birthLng(11.57);

        memberService.createMember(input, null);

        verifyNoInteractions(geocodingRepositoryPort);
    }

    // ──────────────────────────────────────────────
    // resolveLocations
    // ──────────────────────────────────────────────

    @DisplayName("resolveLocations: unique match → coordinates updated in DB, member not in residual list")
    @Test
    void testResolveLocations_WhenUniqueMatch_ThenCoordinatesUpdatedAndNotInResidual() {
        Member member = memberWithNullCoordinates(10, "Munich", "Germany");
        GeonamesCity munich = GeonamesCity.builder()
                .name("Munich").countryCode("DE").countryName("Germany")
                .lat(48.13).lng(11.57).build();

        when(memberRepositoryPort.findMembersWithoutCoordinates()).thenReturn(List.of(member));
        when(geocodingRepositoryPort.findCountryCodeByName("Germany")).thenReturn(Optional.of("DE"));
        when(geocodingRepositoryPort.findCityByNameAndCountry("Munich", "DE")).thenReturn(Optional.of(munich));

        List<UnresolvedLocationDto> residual = memberService.resolveLocations();

        assertTrue(residual.isEmpty());
        verify(memberRepositoryPort).updateMemberCoordinates(10, 48.13, 11.57, "Munich", "Germany");
    }

    @DisplayName("resolveLocations: ambiguous match → member in residual with city suggestions, coordinates NOT updated")
    @Test
    void testResolveLocations_WhenAmbiguousMatch_ThenMemberInResidualWithSuggestions() {
        Member member = memberWithNullCoordinates(11, "Re", "Germany");
        GeonamesCity regensburg = GeonamesCity.builder()
                .name("Regensburg").countryCode("DE").countryName("Germany")
                .lat(49.01).lng(12.10).build();
        GeonamesCity dresden = GeonamesCity.builder()
                .name("Dresden").countryCode("DE").countryName("Germany")
                .lat(51.05).lng(13.74).build();

        when(memberRepositoryPort.findMembersWithoutCoordinates()).thenReturn(List.of(member));
        when(geocodingRepositoryPort.findCountryCodeByName("Germany")).thenReturn(Optional.of("DE"));
        when(geocodingRepositoryPort.findCityByNameAndCountry("Re", "DE")).thenReturn(Optional.empty());
        when(geocodingRepositoryPort.searchCities("Re", "DE", 5)).thenReturn(List.of(dresden, regensburg));

        List<UnresolvedLocationDto> residual = memberService.resolveLocations();

        assertEquals(1, residual.size());
        assertEquals("Re", residual.get(0).getMember().getBirthCity());
        assertEquals(2, residual.get(0).getSuggestions().size());
        verify(memberRepositoryPort, never()).updateMemberCoordinates(anyInt(), any(Double.class), any(Double.class), anyString(), anyString());
    }

    @DisplayName("resolveLocations: member without birthCity/birthCountry → in residual with empty suggestions, no geocoding")
    @Test
    void testResolveLocations_WhenNoBirthCityOrCountry_ThenInResidualWithEmptySuggestions() {
        Member member = memberWithNullCoordinates(12, null, null);

        when(memberRepositoryPort.findMembersWithoutCoordinates()).thenReturn(List.of(member));

        List<UnresolvedLocationDto> residual = memberService.resolveLocations();

        assertEquals(1, residual.size());
        assertTrue(residual.get(0).getSuggestions().isEmpty());
        verifyNoInteractions(geocodingRepositoryPort);
        verify(memberRepositoryPort, never()).updateMemberCoordinates(anyInt(), any(Double.class), any(Double.class), anyString(), anyString());
    }

    @DisplayName("resolveLocations: mix of unique and ambiguous → correct residual count and update calls")
    @Test
    void testResolveLocations_WhenMixedResults_ThenOnlyUniqueMatchesResolved() {
        Member resolved = memberWithNullCoordinates(20, "Munich", "Germany");
        Member ambiguous = memberWithNullCoordinates(21, "Re", "Germany");
        GeonamesCity munich = GeonamesCity.builder()
                .name("Munich").countryCode("DE").countryName("Germany")
                .lat(48.13).lng(11.57).build();

        when(memberRepositoryPort.findMembersWithoutCoordinates()).thenReturn(List.of(resolved, ambiguous));
        when(geocodingRepositoryPort.findCountryCodeByName("Germany")).thenReturn(Optional.of("DE"));
        when(geocodingRepositoryPort.findCityByNameAndCountry("Munich", "DE")).thenReturn(Optional.of(munich));
        when(geocodingRepositoryPort.findCityByNameAndCountry("Re", "DE")).thenReturn(Optional.empty());
        when(geocodingRepositoryPort.searchCities(eq("Re"), eq("DE"), anyInt())).thenReturn(List.of());

        List<UnresolvedLocationDto> residual = memberService.resolveLocations();

        assertEquals(1, residual.size());
        verify(memberRepositoryPort).updateMemberCoordinates(20, 48.13, 11.57, "Munich", "Germany");
        verify(memberRepositoryPort, never()).updateMemberCoordinates(eq(21), any(Double.class), any(Double.class), anyString(), anyString());
    }

    // ──────────────────────────────────────────────
    // getUnresolvedLocations
    // ──────────────────────────────────────────────

    @DisplayName("getUnresolvedLocations: members without coordinates returned with empty suggestion list")
    @Test
    void testGetUnresolvedLocations_WhenMembersWithoutCoordinates_ThenReturnedWithEmptySuggestions() {
        when(memberRepositoryPort.findMembersWithoutCoordinates())
                .thenReturn(List.of(
                        memberWithNullCoordinates(1, "Munich", "Germany"),
                        memberWithNullCoordinates(2, "Re", "Germany")
                ));

        List<UnresolvedLocationDto> result = memberService.getUnresolvedLocations();

        assertEquals(2, result.size());
        result.forEach(dto -> assertTrue(dto.getSuggestions().isEmpty()));
    }

    // ──────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────

    private MemberDto memberDtoWithBirthLocation(String birthCity, String birthCountry) {
        return new MemberDto()
                .firstName("Hans").lastName("Test").gender(GenderDto.M)
                .birthDate(LocalDate.of(1980, 1, 1))
                .birthCity(birthCity).birthCountry(birthCountry);
    }

    private Member memberWithNullCoordinates(int id, String birthCity, String birthCountry) {
        return Member.builder()
                .id(id)
                .firstName("Hans").lastName("Test")
                .gender(org.lunskra.core.domain.Gender.M)
                .birthDate(LocalDate.of(1980, 1, 1))
                .birthCity(birthCity).birthCountry(birthCountry)
                .birthLat(null).birthLng(null)
                .build();
    }
}
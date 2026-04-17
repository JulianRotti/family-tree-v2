package org.lunskra.adapter.api.service;

import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.adapter.api.mapper.FieldErrorDtoMapper;
import org.lunskra.adapter.api.mapper.MemberDtoMapper;
import org.lunskra.adapter.utils.MemberDtoGenerator;
import org.lunskra.adapter.utils.MemberGenerator;
import org.lunskra.core.validation.DomainValidationException;
import org.lunskra.core.validation.FieldError;
import org.lunskra.family_tree.api.model.BulkUploadResultDto;
import org.lunskra.family_tree.api.model.FieldErrorDto;
import org.lunskra.family_tree.api.model.GenderDto;
import org.lunskra.family_tree.api.model.MemberBulkRequestDto;
import org.lunskra.family_tree.api.model.MemberDto;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkMemberServiceTest {

    @Mock
    MemberService memberService;

    @Mock
    MemberDtoMapper memberDtoMapper;

    @Mock
    FieldErrorDtoMapper fieldErrorDtoMapper;

    BulkMemberService bulkMemberService;

    @BeforeEach
    void setUp() {
        bulkMemberService = new BulkMemberService(memberService, memberDtoMapper, fieldErrorDtoMapper);
    }

    // ──────────────────────────────────────────────
    // happy path
    // ──────────────────────────────────────────────

    @DisplayName("bulkCreateMembers: all valid entries → all in created, failed list empty")
    @Test
    void testBulkCreateMembers_WhenAllValid_ThenAllInCreated() {
        MemberBulkRequestDto input1 = bulkRequest("Hans", "Müller");
        MemberBulkRequestDto input2 = bulkRequest("Erika", "Muster");
        MemberDto dto1 = MemberDtoGenerator.createLivingMemberDtoWithRequiredData();
        MemberDto dto2 = MemberDtoGenerator.createLivingMemberDtoWithAllData();
        MemberDto saved1 = MemberDtoGenerator.createLivingMemberDtoWithRequiredData();
        MemberDto saved2 = MemberDtoGenerator.createLivingMemberDtoWithAllData();

        when(memberDtoMapper.toMemberDto(input1)).thenReturn(dto1);
        when(memberDtoMapper.toMemberDto(input2)).thenReturn(dto2);
        when(memberService.createMember(dto1, null)).thenReturn(saved1);
        when(memberService.createMember(dto2, null)).thenReturn(saved2);

        BulkUploadResultDto result = bulkMemberService.bulkCreateMembers(List.of(input1, input2));

        assertEquals(2, result.getCreated().size());
        assertTrue(result.getFailed().isEmpty());
    }

    // ──────────────────────────────────────────────
    // partial failure — DomainValidationException
    // ──────────────────────────────────────────────

    @DisplayName("bulkCreateMembers: one entry fails with DomainValidationException → in failed with field errors, others succeed")
    @Test
    void testBulkCreateMembers_WhenOneDomainValidationFailure_ThenOneInFailedWithErrors() {
        MemberBulkRequestDto goodInput = bulkRequest("Hans", "Müller");
        MemberBulkRequestDto badInput = bulkRequest("", "");
        MemberDto goodDto = MemberDtoGenerator.createLivingMemberDtoWithRequiredData();
        MemberDto badDto = new MemberDto();
        MemberDto savedGood = MemberDtoGenerator.createLivingMemberDtoWithRequiredData();

        FieldError fieldError = new FieldError("firstName", "must not be blank");
        DomainValidationException validationEx = new DomainValidationException("invalid", List.of(fieldError));
        FieldErrorDto fieldErrorDto = new FieldErrorDto().field("firstName").message("must not be blank");

        when(memberDtoMapper.toMemberDto(goodInput)).thenReturn(goodDto);
        when(memberDtoMapper.toMemberDto(badInput)).thenReturn(badDto);
        when(memberService.createMember(goodDto, null)).thenReturn(savedGood);
        when(memberService.createMember(badDto, null)).thenThrow(validationEx);
        when(fieldErrorDtoMapper.toDto(List.of(fieldError))).thenReturn(List.of(fieldErrorDto));

        BulkUploadResultDto result = bulkMemberService.bulkCreateMembers(List.of(goodInput, badInput));

        assertEquals(1, result.getCreated().size());
        assertEquals(1, result.getFailed().size());
        assertEquals(1, result.getFailed().get(0).getErrors().size());
        assertEquals("firstName", result.getFailed().get(0).getErrors().get(0).getField());
    }

    // ──────────────────────────────────────────────
    // partial failure — unexpected exception
    // ──────────────────────────────────────────────

    @DisplayName("bulkCreateMembers: unexpected RuntimeException → entry in failed with empty error list")
    @Test
    void testBulkCreateMembers_WhenUnexpectedException_ThenInFailedWithEmptyErrors() {
        MemberBulkRequestDto input = bulkRequest("Hans", "Müller");
        MemberDto dto = MemberDtoGenerator.createLivingMemberDtoWithRequiredData();

        when(memberDtoMapper.toMemberDto(input)).thenReturn(dto);
        when(memberService.createMember(dto, null)).thenThrow(new RuntimeException("DB unavailable"));

        BulkUploadResultDto result = bulkMemberService.bulkCreateMembers(List.of(input));

        assertTrue(result.getCreated().isEmpty());
        assertEquals(1, result.getFailed().size());
        assertTrue(result.getFailed().get(0).getErrors().isEmpty());
    }

    // ──────────────────────────────────────────────
    // size limit
    // ──────────────────────────────────────────────

    @DisplayName("bulkCreateMembers: more than 1000 entries → BadRequestException, no entries processed")
    @Test
    void testBulkCreateMembers_WhenExceedsMaxSize_ThenBadRequestException() {
        List<MemberBulkRequestDto> oversized = new ArrayList<>();
        for (int i = 0; i <= BulkMemberService.MAX_BULK_SIZE; i++) {
            oversized.add(bulkRequest("Hans", "Test"));
        }

        assertThrows(BadRequestException.class,
                () -> bulkMemberService.bulkCreateMembers(oversized));

        verify(memberService, times(0)).createMember(any(), any());
    }

    // ──────────────────────────────────────────────
    // zero-coordinate normalisation
    // ──────────────────────────────────────────────

    @DisplayName("bulkCreateMembers: birthLat=0 / birthLng=0 in mapped DTO → normalised to null before createMember is called")
    @Test
    void testBulkCreateMembers_WhenZeroCoordinates_ThenNormalisedToNull() {
        MemberBulkRequestDto input = bulkRequest("Hans", "Müller");
        MemberDto dtoWithZeroCoords = new MemberDto()
                .firstName("Hans").lastName("Müller").gender(GenderDto.M)
                .birthDate(LocalDate.of(1980, 1, 1))
                .birthCity("Munich").birthCountry("Germany")
                .birthLat(0.0).birthLng(0.0);

        when(memberDtoMapper.toMemberDto(input)).thenReturn(dtoWithZeroCoords);

        ArgumentCaptor<MemberDto> captor = ArgumentCaptor.forClass(MemberDto.class);
        when(memberService.createMember(captor.capture(), any()))
                .thenReturn(MemberDtoGenerator.createLivingMemberDtoWithRequiredData());

        bulkMemberService.bulkCreateMembers(List.of(input));

        assertNull(captor.getValue().getBirthLat());
        assertNull(captor.getValue().getBirthLng());
    }

    // ──────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────

    private MemberBulkRequestDto bulkRequest(String firstName, String lastName) {
        return new MemberBulkRequestDto()
                .firstName(firstName)
                .lastName(lastName)
                .gender(GenderDto.M)
                .birthDate(LocalDate.of(1980, 1, 1))
                .birthCity("Munich")
                .birthCountry("Germany");
    }
}

package org.lunskra.adapter.api.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.api.mapper.MemberDtoMapper;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.MemberPage;
import org.lunskra.core.domain.GeonamesCity;
import org.lunskra.family_tree.api.model.CitySuggestionDto;
import org.lunskra.family_tree.api.model.MemberDto;
import org.lunskra.family_tree.api.model.MemberPageDto;
import org.lunskra.family_tree.api.model.UnresolvedLocationDto;
import org.lunskra.port.in.ValidateMemberUseCase;
import org.lunskra.port.out.GeocodingRepositoryPort;
import org.lunskra.port.out.ImageStoragePort;
import org.lunskra.port.out.MemberRepositoryPort;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Application-layer service that orchestrates member operations on behalf of the REST layer.
 * <p>
 * It coordinates domain validation (via {@link ValidateMemberUseCase}), image storage
 * (via {@link ImageStoragePort}), and persistence (via {@link MemberRepositoryPort}).
 * After every read or write the stored object-storage key on the DTO is replaced with
 * a short-lived presigned URL so the caller can download the image directly.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepositoryPort memberRepositoryPort;
    private final MemberDtoMapper memberDtoMapper;
    private final ValidateMemberUseCase validateMemberUseCase;
    private final ImageStoragePort imageStoragePort;
    private final GeocodingRepositoryPort geocodingRepositoryPort;


    public MemberPageDto listMembers(String firstName, String lastName, LocalDate birthDate, int page, int pageSize) {
        log.atDebug().addArgument(firstName).addArgument(lastName).addArgument(birthDate)
                .addArgument(page).addArgument(pageSize)
                .setMessage("Querying members with firstName={}, lastName={}, birthDate={}, page={}, pageSize={}").log();
        MemberPage memberPage = memberRepositoryPort.listMembers(firstName, lastName, birthDate, page, pageSize);
        List<MemberDto> members = memberDtoMapper.toDto(memberPage.members());
        members.forEach(this::resolveImageUrl);
        int totalPages = (int) Math.ceil((double) memberPage.totalElements() / pageSize);
        return new MemberPageDto()
                .content(members)
                .totalElements(memberPage.totalElements())
                .totalPages(totalPages)
                .page(page)
                .pageSize(pageSize);
    }

    public MemberDto getMember(Integer id) {
        log.atDebug().addArgument(id).setMessage("Fetching member with id={}").log();
        MemberDto member = memberDtoMapper.toDto(memberRepositoryPort.getMember(id));
        resolveImageUrl(member);
        return member;
    }

    @Transactional
    public void deleteMember(Integer id) {
        log.atInfo().addArgument(id).setMessage("Deleting member with id={}").log();
        Member existing = memberRepositoryPort.getMember(id);
        if (existing.getImagePath() != null) {
            imageStoragePort.deleteImage(existing.getImagePath());
        }
        memberRepositoryPort.deleteMember(id);
    }

    @Transactional
    public MemberDto createMember(MemberDto member, InputStream imageStream) {
        log.atInfo().addArgument(member.getFirstName()).addArgument(member.getLastName())
                .setMessage("Creating member {} {}").log();
        Member memberDomain = memberDtoMapper.toDomain(member);
        if (imageStream != null) {
            String objectKey = imageStoragePort.uploadImage(imageStream, -1);
            memberDomain.setImagePath(objectKey);
        }
        tryResolveMissingCoordinates(memberDomain);
        validateMemberUseCase.validateNewMember(memberDomain);
        MemberDto stored = memberDtoMapper.toDto(memberRepositoryPort.createMember(memberDomain));
        resolveImageUrl(stored);
        return stored;
    }

    @Transactional
    public MemberDto updateMember(Integer memberId, MemberDto member, InputStream imageStream) {
        log.atInfo().addArgument(memberId).setMessage("Updating member with id={}").log();
        member.setId(memberId);
        Member memberDomain = memberDtoMapper.toDomain(member);
        if (imageStream != null) {
            // Delete old image if present
            Member existing = memberRepositoryPort.getMember(memberId);
            if (existing.getImagePath() != null) {
                imageStoragePort.deleteImage(existing.getImagePath());
            }
            String objectKey = imageStoragePort.uploadImage(imageStream, -1);
            memberDomain.setImagePath(objectKey);
        }
        tryResolveMissingCoordinates(memberDomain);
        validateMemberUseCase.validateExistingMember(memberDomain);
        MemberDto updated = memberDtoMapper.toDto(memberRepositoryPort.updateMember(memberDomain));
        resolveImageUrl(updated);
        return updated;
    }

    public List<UnresolvedLocationDto> getUnresolvedLocations() {
        log.atDebug().setMessage("Fetching all members without birth coordinates").log();
        return memberRepositoryPort.findMembersWithoutCoordinates()
                .stream()
                .map(member -> {
                    MemberDto dto = memberDtoMapper.toDto(member);
                    resolveImageUrl(dto);
                    return new UnresolvedLocationDto().member(dto).suggestions(List.of());
                })
                .toList();
    }

    @Transactional
    public List<UnresolvedLocationDto> resolveLocations() {
        log.atInfo().setMessage("Attempting to auto-resolve birth coordinates for unresolved members").log();
        List<Member> unresolved = memberRepositoryPort.findMembersWithoutCoordinates();
        List<UnresolvedLocationDto> residual = new ArrayList<>();

        for (Member member : unresolved) {
            if (member.getBirthCity() == null || member.getBirthCountry() == null) {
                MemberDto dto = memberDtoMapper.toDto(member);
                resolveImageUrl(dto);
                residual.add(new UnresolvedLocationDto().member(dto).suggestions(List.of()));
                continue;
            }

            String countryCode = resolveCountryCode(member.getBirthCountry());
            var exact = geocodingRepositoryPort.findCityByNameAndCountry(
                    member.getBirthCity(), countryCode);

            if (exact.isPresent()) {
                GeonamesCity resolved = exact.get();
                log.atDebug().addArgument(member.getId()).addArgument(resolved.getName())
                        .addArgument(resolved.getCountryName())
                        .setMessage("Auto-resolved location for member id={}: city={}, country={}").log();
                memberRepositoryPort.updateMemberCoordinates(
                        member.getId(), resolved.getLat(), resolved.getLng(),
                        resolved.getName(), resolved.getCountryName());
            } else {
                List<CitySuggestionDto> suggestions = geocodingRepositoryPort
                        .searchCities(member.getBirthCity(), countryCode, 5)
                        .stream()
                        .map(this::toCitySuggestionDto)
                        .toList();
                MemberDto dto = memberDtoMapper.toDto(member);
                resolveImageUrl(dto);
                residual.add(new UnresolvedLocationDto().member(dto).suggestions(suggestions));
            }
        }

        log.atInfo().addArgument(unresolved.size() - residual.size()).addArgument(residual.size())
                .setMessage("resolve-locations: auto-resolved={}, residual={}").log();
        return residual;
    }

    /**
     * Returns an ISO 3166-1 alpha-2 country code for the given value.
     * If the value is already exactly 2 characters it is returned as-is (assumed to be a code).
     * Otherwise a case-insensitive lookup against {@code geonames_countries} is attempted;
     * when that also fails the original value is returned unchanged so the caller still gets
     * a best-effort result rather than a silent null.
     */
    private String resolveCountryCode(String birthCountry) {
        if (birthCountry == null || birthCountry.length() == 2) {
            return birthCountry;
        }
        return geocodingRepositoryPort.findCountryCodeByName(birthCountry)
                .orElse(birthCountry);
    }

    private CitySuggestionDto toCitySuggestionDto(GeonamesCity city) {
        return new CitySuggestionDto()
                .name(city.getName())
                .countryCode(city.getCountryCode())
                .countryName(city.getCountryName())
                .lat(city.getLat())
                .lng(city.getLng());
    }

    /**
     * If {@code birthLat}/{@code birthLng} are absent but {@code birthCity} and
     * {@code birthCountry} are present, attempts to resolve coordinates from the
     * GeoNames dataset (matching on {@code ascii_name} + {@code country_code},
     * highest population result). Silently skips when no match is found.
     */
    private void tryResolveMissingCoordinates(Member member) {
        if (member.getBirthLat() != null || member.getBirthLng() != null) {
            return;
        }
        if (member.getBirthCity() == null || member.getBirthCountry() == null) {
            return;
        }
        String countryCode = resolveCountryCode(member.getBirthCountry());
        geocodingRepositoryPort
                .findCityByNameAndCountry(member.getBirthCity(), countryCode)
                .ifPresent(city -> {
                    member.setBirthLat(city.getLat());
                    member.setBirthLng(city.getLng());
                    member.setBirthCity(city.getName());
                    member.setBirthCountry(city.getCountryName());
                });
    }

    /**
     * Replaces the stored object-storage key in the DTO with a presigned GET URL
     * valid for 60 minutes. Has no effect when {@code imageFilePath} is absent or blank.
     */
    public void resolveImageUrl(MemberDto member) {
        if (member.getImageFilePath() != null && !member.getImageFilePath().isBlank()) {
            String presignedUrl = imageStoragePort.generatePresignedUrl(member.getImageFilePath());
            member.setImageFilePath(presignedUrl);
        }
    }
}

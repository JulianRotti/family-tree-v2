package org.lunskra.adapter.api.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.api.mapper.MemberDtoMapper;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.MemberPage;
import org.lunskra.family_tree.api.model.MemberDto;
import org.lunskra.family_tree.api.model.MemberPageDto;
import org.lunskra.port.in.ValidateMemberUseCase;
import org.lunskra.port.out.ImageStoragePort;
import org.lunskra.port.out.MemberRepositoryPort;

import java.io.InputStream;
import java.time.LocalDate;
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
        validateMemberUseCase.validateExistingMember(memberDomain);
        MemberDto updated = memberDtoMapper.toDto(memberRepositoryPort.updateMember(memberDomain));
        resolveImageUrl(updated);
        return updated;
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

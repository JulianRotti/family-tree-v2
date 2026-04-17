package org.lunskra.adapter.api.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.api.mapper.FieldErrorDtoMapper;
import org.lunskra.adapter.api.mapper.MemberDtoMapper;
import org.lunskra.core.validation.DomainValidationException;
import org.lunskra.family_tree.api.model.BulkUploadFailureDto;
import org.lunskra.family_tree.api.model.BulkUploadResultDto;
import org.lunskra.family_tree.api.model.MemberBulkRequestDto;
import org.lunskra.family_tree.api.model.MemberDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Application-layer service for bulk member creation.
 * <p>
 * Processes each entry independently (best-effort): valid entries are persisted and
 * returned in {@code created}; entries that fail domain validation are returned in
 * {@code failed} together with their field errors.
 * <p>
 * This class is intentionally <em>not</em> {@code @Transactional}. Each call to
 * {@link MemberService#createMember} goes through the CDI proxy and therefore runs
 * in its own transaction, so a failure for one entry never rolls back the others.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class BulkMemberService {

    private final MemberService memberService;
    private final MemberDtoMapper memberDtoMapper;
    private final FieldErrorDtoMapper fieldErrorDtoMapper;

    static final int MAX_BULK_SIZE = 1_000;

    public BulkUploadResultDto bulkCreateMembers(List<MemberBulkRequestDto> inputs) {
        if (inputs.size() > MAX_BULK_SIZE) {
            throw new BadRequestException(
                    "Bulk upload exceeds maximum of " + MAX_BULK_SIZE + " entries (got " + inputs.size() + ")");
        }
        log.atInfo().addArgument(inputs.size()).setMessage("Starting bulk member creation for {} entries").log();

        List<MemberDto> created = new ArrayList<>();
        List<BulkUploadFailureDto> failed = new ArrayList<>();

        for (MemberBulkRequestDto input : inputs) {
            try {
                MemberDto dto = memberDtoMapper.toMemberDto(input);
                normalizeZeroCoordinates(dto);
                MemberDto saved = memberService.createMember(dto, null);
                created.add(saved);
            } catch (DomainValidationException e) {
                log.atDebug().addArgument(input.getFirstName()).addArgument(input.getLastName())
                        .setMessage("Bulk entry {}/{} failed domain validation").log();
                failed.add(new BulkUploadFailureDto()
                        .input(input)
                        .errors(fieldErrorDtoMapper.toDto(e.getErrors())));
            } catch (Exception e) {
                log.atWarn().addArgument(input.getFirstName()).addArgument(input.getLastName())
                        .addArgument(e.getMessage())
                        .setMessage("Bulk entry {}/{} failed unexpectedly: {}").log();
                failed.add(new BulkUploadFailureDto()
                        .input(input)
                        .errors(List.of()));
            }
        }

        log.atInfo().addArgument(created.size()).addArgument(failed.size())
                .setMessage("Bulk creation complete: created={}, failed={}").log();
        return new BulkUploadResultDto().created(created).failed(failed);
    }

    private static void normalizeZeroCoordinates(MemberDto dto) {
        if (dto.getBirthLat() != null && dto.getBirthLat() == 0.0) dto.setBirthLat(null);
        if (dto.getBirthLng() != null && dto.getBirthLng() == 0.0) dto.setBirthLng(null);
    }
}

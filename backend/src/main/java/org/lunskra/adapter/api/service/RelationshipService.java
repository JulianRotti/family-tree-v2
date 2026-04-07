package org.lunskra.adapter.api.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.api.mapper.RelationshipDtoMapper;
import org.lunskra.core.domain.Relationship;
import org.lunskra.family_tree.api.model.RelationshipDto;
import org.lunskra.family_tree.api.model.RelationshipUpdateRequestDto;
import org.lunskra.port.in.ValidateRelationshipUseCase;
import org.lunskra.port.out.RelationshipRepositoryPort;

import java.util.List;

/**
 * Application-layer service that orchestrates relationship operations on behalf of
 * the REST layer.
 * <p>
 * It coordinates domain validation (via {@link ValidateRelationshipUseCase}) and
 * persistence (via {@link RelationshipRepositoryPort}), and handles DTO ↔ domain
 * mapping through {@link RelationshipDtoMapper}.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class RelationshipService {

    private final RelationshipRepositoryPort relationshipRepositoryPort;
    private final RelationshipDtoMapper relationshipDtoMapper;
    private final ValidateRelationshipUseCase validateRelationshipUseCase;

    public List<RelationshipDto> listRelationships() {
        log.atDebug().setMessage("Fetching all relationships").log();
        return relationshipDtoMapper.toDto(relationshipRepositoryPort.listRelationships());
    }

    public RelationshipDto getRelationshipByMemberPair(Integer firstMemberId, Integer secondMemberId) {
        log.atDebug().addArgument(firstMemberId).addArgument(secondMemberId)
                .setMessage("Fetching relationship for firstMemberId={} and secondMemberId={}").log();
        return relationshipDtoMapper.toDto(
                relationshipRepositoryPort.getRelationshipByMemberPair(firstMemberId, secondMemberId));
    }

    @Transactional
    public RelationshipDto createRelationship(RelationshipDto dto) {
        log.atInfo().addArgument(dto.getFirstMemberId()).addArgument(dto.getSecondMemberId()).addArgument(dto.getRelationship())
                .setMessage("Creating relationship between firstMemberId={} and secondMemberId={} of type={}").log();
        Relationship relationship = relationshipDtoMapper.toDomain(dto);
        validateRelationshipUseCase.validateNewRelationship(relationship);
        return relationshipDtoMapper.toDto(
                relationshipRepositoryPort.createRelationship(relationship));
    }

    @Transactional
    public void deleteRelationshipByMemberPair(Integer firstMemberId, Integer secondMemberId) {
        log.atInfo().addArgument(firstMemberId).addArgument(secondMemberId)
                .setMessage("Deleting relationship between firstMemberId={} and secondMemberId={}").log();
        relationshipRepositoryPort.deleteRelationshipByMemberPair(firstMemberId, secondMemberId);
    }

    @Transactional
    public RelationshipDto updateRelationshipByMemberPair(Integer firstMemberId, Integer secondMemberId,
                                                          RelationshipUpdateRequestDto updateRequest) {
        log.atInfo().addArgument(firstMemberId).addArgument(secondMemberId).addArgument(updateRequest.getRelationship())
                .setMessage("Updating relationship between firstMemberId={} and secondMemberId={} to type={}").log();
        RelationshipDto dto = new RelationshipDto(firstMemberId, secondMemberId, updateRequest.getRelationship());
        Relationship relationship = relationshipDtoMapper.toDomain(dto);
        validateRelationshipUseCase.validateExistingRelationship(relationship);
        return relationshipDtoMapper.toDto(
                relationshipRepositoryPort.updateRelationshipByMemberPair(
                        firstMemberId, secondMemberId, relationship.getRelationshipType()));
    }
}

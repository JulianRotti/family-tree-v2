package org.lunskra.adapter.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.adapter.api.mapper.RelationshipDtoMapper;
import org.lunskra.adapter.utils.RelationshipDtoGenerator;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;
import org.lunskra.family_tree.api.model.RelationshipDto;
import org.lunskra.family_tree.api.model.RelationshipTypeDto;
import org.lunskra.port.in.ValidateRelationshipUseCase;
import org.lunskra.port.out.RelationshipRepositoryPort;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelationshipServiceTest {

    @Mock
    RelationshipRepositoryPort relationshipRepositoryPort;

    @Mock
    ValidateRelationshipUseCase validateRelationshipUseCase;

    private RelationshipService relationshipService;

    @BeforeEach
    void setUp() {
        RelationshipDtoMapper mapper = Mappers.getMapper(RelationshipDtoMapper.class);
        relationshipService = new RelationshipService(relationshipRepositoryPort, mapper, validateRelationshipUseCase);
    }

    @Test
    void listRelationships_WhenTwoRelationshipsExist_ThenReturnTwo() {
        when(relationshipRepositoryPort.listRelationships()).thenReturn(List.of(
                new Relationship(1, 2, RelationshipType.PARENT),
                new Relationship(3, 4, RelationshipType.EX_SPOUSE)
        ));

        assertEquals(2, relationshipService.listRelationships().size());
    }

    @Test
    void listRelationships_WhenNoRelationshipsExist_ThenReturnEmptyList() {
        when(relationshipRepositoryPort.listRelationships()).thenReturn(List.of());

        assertEquals(0, relationshipService.listRelationships().size());
    }

    @Test
    void getRelationshipByMemberPair_WhenPairExists_ThenReturnDto() {
        when(relationshipRepositoryPort.getRelationshipByMemberPair(1, 2))
                .thenReturn(new Relationship(1, 2, RelationshipType.PARENT));

        RelationshipDto result = relationshipService.getRelationshipByMemberPair(1, 2);

        assertEquals(1, result.getFirstMemberId());
        assertEquals(2, result.getSecondMemberId());
    }

    @Test
    void createRelationship_WhenValid_ThenValidatesAndReturnsDto() {
        when(relationshipRepositoryPort.createRelationship(any()))
                .thenReturn(new Relationship(1, 2, RelationshipType.PARENT));

        RelationshipDto result = relationshipService.createRelationship(
                RelationshipDtoGenerator.createParentRelationshipDto(1, 2));

        assertEquals(1, result.getFirstMemberId());
        assertEquals(2, result.getSecondMemberId());
        verify(validateRelationshipUseCase).validateNewRelationship(any());
        verify(relationshipRepositoryPort).createRelationship(any());
    }

    @Test
    void deleteRelationshipByMemberPair_WhenCalled_ThenDelegatesToRepo() {
        relationshipService.deleteRelationshipByMemberPair(1, 2);

        verify(relationshipRepositoryPort).deleteRelationshipByMemberPair(1, 2);
    }

    @Test
    void updateRelationshipByMemberPair_WhenValid_ThenValidatesAndReturnsDto() {
        when(relationshipRepositoryPort.updateRelationshipByMemberPair(eq(1), eq(2), any()))
                .thenReturn(new Relationship(1, 2, RelationshipType.CURRENT_MARRIED_SPOUSE));

        RelationshipDto result = relationshipService.updateRelationshipByMemberPair(
                1, 2, RelationshipDtoGenerator.createUpdateRequest(RelationshipTypeDto.CURRENT_MARRIED_SPOUSE));

        assertEquals(1, result.getFirstMemberId());
        assertEquals(2, result.getSecondMemberId());
        verify(validateRelationshipUseCase).validateExistingRelationship(any());
        verify(relationshipRepositoryPort).updateRelationshipByMemberPair(eq(1), eq(2), any());
    }
}

package org.lunskra.core.validation.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.core.utils.RelationshipGenerator;
import org.lunskra.core.validation.FieldError;
import org.lunskra.port.out.RelationshipRepositoryPort;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniqueCurrentSpouseRuleTest {

    @Mock
    RelationshipRepositoryPort relationshipRepositoryPort;

    @InjectMocks
    UniqueCurrentSpouseRule rule;

    @Test
    void apply_WhenTypeIsNotSpouse_ThenReturnEmptyWithoutQueryingRepo() {
        Optional<FieldError> result = rule.apply(RelationshipGenerator.parentRelationship(1, 2));

        assertTrue(result.isEmpty());
        verifyNoInteractions(relationshipRepositoryPort);
    }

    @Test
    void apply_WhenTypeIsExSpouse_ThenReturnEmptyWithoutQueryingRepo() {
        Optional<FieldError> result = rule.apply(RelationshipGenerator.exSpouseRelationship(1, 2));

        assertTrue(result.isEmpty());
        verifyNoInteractions(relationshipRepositoryPort);
    }

    @Test
    void apply_WhenCurrentMarriedSpouseAndNeitherHasExistingSpouse_ThenReturnEmpty() {
        when(relationshipRepositoryPort.listRelationships()).thenReturn(List.of(
                RelationshipGenerator.parentRelationship(1, 3)
        ));

        Optional<FieldError> result = rule.apply(RelationshipGenerator.marriedSpouseRelationship(1, 2));

        assertTrue(result.isEmpty());
    }

    @Test
    void apply_WhenCurrentSpouseAndNeitherHasExistingSpouse_ThenReturnEmpty() {
        when(relationshipRepositoryPort.listRelationships()).thenReturn(List.of());

        Optional<FieldError> result = rule.apply(RelationshipGenerator.currentSpouseRelationship(1, 2));

        assertTrue(result.isEmpty());
    }

    @Test
    void apply_WhenFirstMemberAlreadyHasCurrentSpouse_ThenReturnErrorForFirstMemberId() {
        // member 1 already has a current spouse (member 3)
        when(relationshipRepositoryPort.listRelationships()).thenReturn(List.of(
                RelationshipGenerator.marriedSpouseRelationship(1, 3)
        ));

        Optional<FieldError> result = rule.apply(RelationshipGenerator.marriedSpouseRelationship(1, 2));

        assertTrue(result.isPresent());
        assertEquals("firstMemberId", result.get().getField());
    }

    @Test
    void apply_WhenSecondMemberAlreadyHasCurrentSpouse_ThenReturnErrorForSecondMemberId() {
        // member 2 already has a current spouse (member 4)
        when(relationshipRepositoryPort.listRelationships()).thenReturn(List.of(
                RelationshipGenerator.currentSpouseRelationship(4, 2)
        ));

        Optional<FieldError> result = rule.apply(RelationshipGenerator.currentSpouseRelationship(1, 2));

        assertTrue(result.isPresent());
        assertEquals("secondMemberId", result.get().getField());
    }

    @Test
    void apply_WhenUpdatingExistingSpouseRelationshipBetweenSamePair_ThenReturnEmpty() {
        // The same pair already has a spouse relationship — this is an update, not a duplicate
        when(relationshipRepositoryPort.listRelationships()).thenReturn(List.of(
                RelationshipGenerator.currentSpouseRelationship(1, 2)
        ));

        Optional<FieldError> result = rule.apply(RelationshipGenerator.marriedSpouseRelationship(1, 2));

        assertTrue(result.isEmpty());
    }

    @Test
    void apply_WhenUpdatingExistingSpouseRelationshipReversedOrder_ThenReturnEmpty() {
        // Same pair stored in reverse order in the repo
        when(relationshipRepositoryPort.listRelationships()).thenReturn(List.of(
                RelationshipGenerator.currentSpouseRelationship(2, 1)
        ));

        Optional<FieldError> result = rule.apply(RelationshipGenerator.marriedSpouseRelationship(1, 2));

        assertTrue(result.isEmpty());
    }
}

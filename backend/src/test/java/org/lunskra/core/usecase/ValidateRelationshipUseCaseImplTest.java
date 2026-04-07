package org.lunskra.core.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.core.utils.RelationshipGenerator;
import org.lunskra.core.validation.DomainValidationException;
import org.lunskra.core.validation.FieldError;
import org.lunskra.core.validation.RelationshipValidator;
import org.lunskra.core.validation.rules.RuleName;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateRelationshipUseCaseImplTest {

    @Mock
    RelationshipValidator relationshipValidator;

    @InjectMocks
    ValidateRelationshipUseCaseImpl useCase;

    @Test
    void validateNewRelationship_WhenNoErrors_ThenNoExceptionThrown() {
        when(relationshipValidator.validate(any(), any())).thenReturn(List.of());

        assertDoesNotThrow(() -> useCase.validateNewRelationship(RelationshipGenerator.parentRelationship(1, 2)));
    }

    @Test
    void validateNewRelationship_WhenValidatorReturnsErrors_ThenThrowDomainValidationException() {
        when(relationshipValidator.validate(any(), any())).thenReturn(
                List.of(new FieldError("firstMemberId", "some error"))
        );

        assertThrows(DomainValidationException.class,
                () -> useCase.validateNewRelationship(RelationshipGenerator.parentRelationship(1, 2)));
    }

    @Test
    void validateNewRelationship_WhenCalled_ThenPassesNewRuleNames() {
        when(relationshipValidator.validate(any(), any())).thenReturn(List.of());
        ArgumentCaptor<List<RuleName>> ruleNamesCaptor = ArgumentCaptor.captor();

        useCase.validateNewRelationship(RelationshipGenerator.parentRelationship(1, 2));

        verify(relationshipValidator).validate(any(), ruleNamesCaptor.capture());
        List<RuleName> passedRules = ruleNamesCaptor.getValue();
        assertTrue(passedRules.contains(RuleName.CREATE_RELATIONSHIP_EXIST));
        assertTrue(passedRules.contains(RuleName.SELF_RELATIONSHIP));
        assertTrue(passedRules.contains(RuleName.RELATIONSHIP_MEMBERS_EXIST));
        assertTrue(passedRules.contains(RuleName.UNIQUE_CURRENT_SPOUSE));
        assertTrue(passedRules.contains(RuleName.PARENT_NOT_YOUNGER_THAN_CHILD));
    }

    @Test
    void validateExistingRelationship_WhenNoErrors_ThenNoExceptionThrown() {
        when(relationshipValidator.validate(any(), any())).thenReturn(List.of());

        assertDoesNotThrow(() -> useCase.validateExistingRelationship(RelationshipGenerator.marriedSpouseRelationship(1, 2)));
    }

    @Test
    void validateExistingRelationship_WhenValidatorReturnsErrors_ThenThrowDomainValidationException() {
        when(relationshipValidator.validate(any(), any())).thenReturn(
                List.of(new FieldError("firstMemberId", "some error"))
        );

        assertThrows(DomainValidationException.class,
                () -> useCase.validateExistingRelationship(RelationshipGenerator.marriedSpouseRelationship(1, 2)));
    }

    @Test
    void validateExistingRelationship_WhenCalled_ThenDoesNotPassCreateRelationshipExistRule() {
        when(relationshipValidator.validate(any(), any())).thenReturn(List.of());
        ArgumentCaptor<List<RuleName>> ruleNamesCaptor = ArgumentCaptor.captor();

        useCase.validateExistingRelationship(RelationshipGenerator.marriedSpouseRelationship(1, 2));

        verify(relationshipValidator).validate(any(), ruleNamesCaptor.capture());
        List<RuleName> passedRules = ruleNamesCaptor.getValue();
        assertTrue(passedRules.contains(RuleName.SELF_RELATIONSHIP));
        assertTrue(passedRules.contains(RuleName.RELATIONSHIP_MEMBERS_EXIST));
        assertTrue(passedRules.contains(RuleName.UNIQUE_CURRENT_SPOUSE));
        assertTrue(passedRules.contains(RuleName.PARENT_NOT_YOUNGER_THAN_CHILD));
        assertTrue(passedRules.stream().noneMatch(r -> r == RuleName.CREATE_RELATIONSHIP_EXIST));
    }
}

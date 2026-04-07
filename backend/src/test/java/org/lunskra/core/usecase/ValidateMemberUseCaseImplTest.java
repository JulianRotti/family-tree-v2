package org.lunskra.core.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.adapter.utils.MemberGenerator;
import org.lunskra.core.validation.DomainValidationException;
import org.lunskra.core.validation.FieldError;
import org.lunskra.core.validation.MemberValidator;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateMemberUseCaseImplTest {

    @Mock
    MemberValidator memberValidator;

    @InjectMocks
    ValidateMemberUseCaseImpl useCase;

    @Test
    void validateNewMember_WhenNoErrors_ThenNoExceptionThrown() {
        when(memberValidator.validate(any(), any())).thenReturn(List.of());

        assertDoesNotThrow(() -> useCase.validateNewMember(MemberGenerator.createLivingMemberWithRequiredData()));
    }

    @Test
    void validateNewMember_WhenValidatorReturnsErrors_ThenThrowDomainValidationException() {
        when(memberValidator.validate(any(), any())).thenReturn(
                List.of(new FieldError("firstName, lastName, birthDate", "Member with this data already exists"))
        );

        assertThrows(DomainValidationException.class,
                () -> useCase.validateNewMember(MemberGenerator.createLivingMemberWithRequiredData()));
    }

    @Test
    void validateNewMember_WhenCalled_ThenPassesNewMemberRuleNames() {
        when(memberValidator.validate(any(), any())).thenReturn(List.of());
        ArgumentCaptor<List<RuleName>> ruleNamesCaptor = ArgumentCaptor.captor();

        useCase.validateNewMember(MemberGenerator.createLivingMemberWithRequiredData());

        verify(memberValidator).validate(any(), ruleNamesCaptor.capture());
        List<RuleName> passedRules = ruleNamesCaptor.getValue();
        assertTrue(passedRules.contains(RuleName.CREATE_MEMBER_EXIST));
        assertTrue(passedRules.contains(RuleName.BIRTH_DEATH_DATE));
    }

    @Test
    void validateExistingMember_WhenNoErrors_ThenNoExceptionThrown() {
        when(memberValidator.validate(any(), any())).thenReturn(List.of());

        assertDoesNotThrow(() -> useCase.validateExistingMember(MemberGenerator.createLivingMemberWithRequiredData()));
    }

    @Test
    void validateExistingMember_WhenValidatorReturnsErrors_ThenThrowDomainValidationException() {
        when(memberValidator.validate(any(), any())).thenReturn(
                List.of(new FieldError("birthDate, deathDate", "Deathdate cannot be before birthdate"))
        );

        assertThrows(DomainValidationException.class,
                () -> useCase.validateExistingMember(MemberGenerator.createLivingMemberWithAllData()));
    }

    @Test
    void validateExistingMember_WhenCalled_ThenDoesNotPassCreateMemberExistRule() {
        when(memberValidator.validate(any(), any())).thenReturn(List.of());
        ArgumentCaptor<List<RuleName>> ruleNamesCaptor = ArgumentCaptor.captor();

        useCase.validateExistingMember(MemberGenerator.createLivingMemberWithAllData());

        verify(memberValidator).validate(any(), ruleNamesCaptor.capture());
        List<RuleName> passedRules = ruleNamesCaptor.getValue();
        assertTrue(passedRules.contains(RuleName.BIRTH_DEATH_DATE));
        assertTrue(passedRules.stream().noneMatch(r -> r == RuleName.CREATE_MEMBER_EXIST));
    }
}

package org.lunskra.core.validation.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.adapter.utils.MemberGenerator;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.MemberPage;
import org.lunskra.core.validation.FieldError;
import org.lunskra.port.out.MemberRepositoryPort;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateMemberExistsRuleTest {

    @Mock
    MemberRepositoryPort memberRepositoryPort;

    @InjectMocks
    CreateMemberExistsRule rule;

    @Test
    void apply_WhenNoMemberWithSameDataExists_ThenReturnEmpty() {
        when(memberRepositoryPort.listMembers(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new MemberPage(List.of(), 0L));

        Optional<FieldError> result = rule.apply(MemberGenerator.createLivingMemberWithRequiredData());

        assertTrue(result.isEmpty());
    }

    @Test
    void apply_WhenMemberWithSameFirstNameLastNameAndBirthDateExists_ThenReturnError() {
        when(memberRepositoryPort.listMembers(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new MemberPage(List.of(MemberGenerator.createLivingMemberWithRequiredData()), 1L));

        Optional<FieldError> result = rule.apply(MemberGenerator.createLivingMemberWithRequiredData());

        assertTrue(result.isPresent());
        assertEquals("firstName, lastName, birthDate", result.get().getField());
    }
}

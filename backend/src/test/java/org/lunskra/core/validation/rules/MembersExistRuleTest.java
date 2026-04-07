package org.lunskra.core.validation.rules;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lunskra.core.utils.RelationshipGenerator;
import org.lunskra.core.validation.FieldError;
import org.lunskra.port.out.MemberRepositoryPort;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.lunskra.adapter.utils.MemberGenerator.createLivingMemberWithRequiredData;
import static org.lunskra.adapter.utils.MemberGenerator.createLivingMemberWithAllData;

class MembersExistRuleTest {

    MemberRepositoryPort memberRepositoryPort;
    MembersExistRule rule;

    @BeforeEach
    void setup() {
        memberRepositoryPort = Mockito.mock(MemberRepositoryPort.class);
        rule = new MembersExistRule(memberRepositoryPort);
    }

    @Test
    void apply_WhenBothMembersExist_ThenReturnEmpty() {
        when(memberRepositoryPort.getMember(1)).thenReturn(createLivingMemberWithRequiredData());
        when(memberRepositoryPort.getMember(2)).thenReturn(createLivingMemberWithAllData());

        Optional<FieldError> result = rule.apply(RelationshipGenerator.parentRelationship(1, 2));

        assertTrue(result.isEmpty());
    }

    @Test
    void apply_WhenFirstMemberDoesNotExist_ThenReturnErrorForFirstMemberId() {
        doThrow(new EntityNotFoundException()).when(memberRepositoryPort).getMember(1);

        Optional<FieldError> result = rule.apply(RelationshipGenerator.parentRelationship(1, 2));

        assertTrue(result.isPresent());
        assertEquals("firstMemberId", result.get().getField());
    }

    @Test
    void apply_WhenSecondMemberDoesNotExist_ThenReturnErrorForSecondMemberId() {
        when(memberRepositoryPort.getMember(1)).thenReturn(createLivingMemberWithRequiredData());
        doThrow(new EntityNotFoundException()).when(memberRepositoryPort).getMember(2);

        Optional<FieldError> result = rule.apply(RelationshipGenerator.parentRelationship(1, 2));

        assertTrue(result.isPresent());
        assertEquals("secondMemberId", result.get().getField());
    }
}

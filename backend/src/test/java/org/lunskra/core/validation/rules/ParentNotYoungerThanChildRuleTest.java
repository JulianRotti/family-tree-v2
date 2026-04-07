package org.lunskra.core.validation.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.core.domain.Member;
import org.lunskra.core.utils.RelationshipGenerator;
import org.lunskra.core.validation.FieldError;
import org.lunskra.port.out.MemberRepositoryPort;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParentNotYoungerThanChildRuleTest {

    @Mock
    MemberRepositoryPort memberRepositoryPort;

    @InjectMocks
    ParentNotYoungerThanChildRule rule;

    @Test
    void apply_WhenTypeIsNotParent_ThenReturnEmptyWithoutQueryingRepo() {
        Optional<FieldError> result = rule.apply(RelationshipGenerator.marriedSpouseRelationship(1, 2));

        assertTrue(result.isEmpty());
        verifyNoInteractions(memberRepositoryPort);
    }

    @Test
    void apply_WhenParentIsBornBeforeChild_ThenReturnEmpty() {
        when(memberRepositoryPort.getMember(1)).thenReturn(memberWithBirthDate(1, LocalDate.of(1960, 1, 1)));
        when(memberRepositoryPort.getMember(2)).thenReturn(memberWithBirthDate(2, LocalDate.of(1990, 6, 15)));

        Optional<FieldError> result = rule.apply(RelationshipGenerator.parentRelationship(1, 2));

        assertTrue(result.isEmpty());
    }

    @Test
    void apply_WhenParentIsBornAfterChild_ThenReturnError() {
        when(memberRepositoryPort.getMember(1)).thenReturn(memberWithBirthDate(1, LocalDate.of(1995, 1, 1)));
        when(memberRepositoryPort.getMember(2)).thenReturn(memberWithBirthDate(2, LocalDate.of(1980, 1, 1)));

        Optional<FieldError> result = rule.apply(RelationshipGenerator.parentRelationship(1, 2));

        assertTrue(result.isPresent());
        assertEquals("firstMemberId", result.get().getField());
    }

    @Test
    void apply_WhenParentAndChildBornOnSameDay_ThenReturnError() {
        LocalDate sameDay = LocalDate.of(1990, 5, 20);
        when(memberRepositoryPort.getMember(1)).thenReturn(memberWithBirthDate(1, sameDay));
        when(memberRepositoryPort.getMember(2)).thenReturn(memberWithBirthDate(2, sameDay));

        Optional<FieldError> result = rule.apply(RelationshipGenerator.parentRelationship(1, 2));

        assertTrue(result.isPresent());
        assertEquals("firstMemberId", result.get().getField());
    }

    @Test
    void apply_WhenParentBirthDateIsNull_ThenReturnEmpty() {
        when(memberRepositoryPort.getMember(1)).thenReturn(memberWithBirthDate(1, null));
        when(memberRepositoryPort.getMember(2)).thenReturn(memberWithBirthDate(2, LocalDate.of(1990, 1, 1)));

        Optional<FieldError> result = rule.apply(RelationshipGenerator.parentRelationship(1, 2));

        assertTrue(result.isEmpty());
    }

    @Test
    void apply_WhenChildBirthDateIsNull_ThenReturnEmpty() {
        when(memberRepositoryPort.getMember(1)).thenReturn(memberWithBirthDate(1, LocalDate.of(1960, 1, 1)));
        when(memberRepositoryPort.getMember(2)).thenReturn(memberWithBirthDate(2, null));

        Optional<FieldError> result = rule.apply(RelationshipGenerator.parentRelationship(1, 2));

        assertTrue(result.isEmpty());
    }

    private Member memberWithBirthDate(int id, LocalDate birthDate) {
        Member member = new Member();
        member.setId(id);
        member.setBirthDate(birthDate);
        return member;
    }
}

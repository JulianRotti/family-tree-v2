package org.lunskra.core.validation.rules;

import org.junit.jupiter.api.Test;
import org.lunskra.core.domain.Member;
import org.lunskra.core.validation.FieldError;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BirthBeforeDeathRuleTest {

    private final BirthBeforeDeathRule rule = new BirthBeforeDeathRule();

    @Test
    void apply_WhenDeathDateIsNull_ThenReturnEmpty() {
        Member member = memberWithDates(LocalDate.of(1990, 1, 1), null);

        assertTrue(rule.apply(member).isEmpty());
    }

    @Test
    void apply_WhenBirthDateIsBeforeDeathDate_ThenReturnEmpty() {
        Member member = memberWithDates(LocalDate.of(1990, 1, 1), LocalDate.of(2060, 6, 15));

        assertTrue(rule.apply(member).isEmpty());
    }

    @Test
    void apply_WhenBirthDateEqualsDeathDate_ThenReturnEmpty() {
        LocalDate sameDay = LocalDate.of(1990, 5, 20);
        Member member = memberWithDates(sameDay, sameDay);

        assertTrue(rule.apply(member).isEmpty());
    }

    @Test
    void apply_WhenBirthDateIsAfterDeathDate_ThenReturnError() {
        Member member = memberWithDates(LocalDate.of(2000, 1, 1), LocalDate.of(1999, 12, 31));

        Optional<FieldError> result = rule.apply(member);

        assertTrue(result.isPresent());
        assertEquals("birthDate, deathDate", result.get().getField());
    }

    private Member memberWithDates(LocalDate birthDate, LocalDate deathDate) {
        Member member = new Member();
        member.setBirthDate(birthDate);
        member.setDeathDate(deathDate);
        return member;
    }
}

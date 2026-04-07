package org.lunskra.core.validation.rules;

import jakarta.enterprise.context.ApplicationScoped;
import org.lunskra.core.domain.Member;
import org.lunskra.core.validation.FieldError;

import java.util.Optional;

/**
 * Validation rule ({@link RuleName#BIRTH_DEATH_DATE}) that ensures a member's
 * death date, when present, is not earlier than their birth date.
 */
@ApplicationScoped
public class BirthBeforeDeathRule implements Rule<Member> {
    @Override
    public RuleName getRuleName() {
        return RuleName.BIRTH_DEATH_DATE;
    }

    @Override
    public Optional<FieldError> apply(Member object) {
        // Birthdate > Deathdate
        if (object.getDeathDate() != null && object.getBirthDate().isAfter(object.getDeathDate())) {
            return Optional.of(new FieldError("birthDate, deathDate","Deathdate cannot be before birthdate"));
        }
        return Optional.empty();
    }
}

package org.lunskra.core.validation.rules;

import jakarta.enterprise.context.ApplicationScoped;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.validation.FieldError;

import java.util.Optional;

/**
 * Validation rule ({@link RuleName#SELF_RELATIONSHIP}) that prevents a member from
 * being related to themselves, i.e. it rejects any relationship where
 * {@code firstMemberId} and {@code secondMemberId} are equal.
 */
@ApplicationScoped
public class SelfRelationshipRule implements Rule<Relationship> {

    @Override
    public RuleName getRuleName() {
        return RuleName.SELF_RELATIONSHIP;
    }

    @Override
    public Optional<FieldError> apply(Relationship relationship) {
        if (relationship.getFirstMemberId().equals(relationship.getSecondMemberId())) {
            return Optional.of(new FieldError("firstMemberId", "A member cannot have a relationship with themselves"));
        }
        return Optional.empty();
    }
}

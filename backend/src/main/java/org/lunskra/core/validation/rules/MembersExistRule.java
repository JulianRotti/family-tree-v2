package org.lunskra.core.validation.rules;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.validation.FieldError;
import org.lunskra.port.out.MemberRepositoryPort;

import java.util.Optional;

/**
 * Validation rule ({@link RuleName#RELATIONSHIP_MEMBERS_EXIST}) that ensures both
 * members referenced by a relationship actually exist in the system. Each member is
 * checked independently so that the error can pinpoint which ID is invalid.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class MembersExistRule implements Rule<Relationship> {

    private final MemberRepositoryPort memberRepositoryPort;

    @Override
    public RuleName getRuleName() {
        return RuleName.RELATIONSHIP_MEMBERS_EXIST;
    }

    @Override
    public Optional<FieldError> apply(Relationship relationship) {
        try {
            memberRepositoryPort.getMember(relationship.getFirstMemberId());
        } catch (EntityNotFoundException e) {
            return Optional.of(new FieldError("firstMemberId", "Member with id " + relationship.getFirstMemberId() + " does not exist"));
        }
        try {
            memberRepositoryPort.getMember(relationship.getSecondMemberId());
        } catch (EntityNotFoundException e) {
            return Optional.of(new FieldError("secondMemberId", "Member with id " + relationship.getSecondMemberId() + " does not exist"));
        }
        return Optional.empty();
    }
}

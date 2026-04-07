package org.lunskra.core.validation.rules;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;
import org.lunskra.core.validation.FieldError;
import org.lunskra.port.out.MemberRepositoryPort;

import java.util.Optional;

/**
 * Validation rule ({@link RuleName#PARENT_NOT_YOUNGER_THAN_CHILD}) that ensures the
 * designated parent in a {@link RelationshipType#PARENT} relationship was born before
 * the child. The rule is skipped for other relationship types and also when either
 * member's birth date is unknown.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ParentNotYoungerThanChildRule implements Rule<Relationship> {

    private final MemberRepositoryPort memberRepositoryPort;

    @Override
    public RuleName getRuleName() {
        return RuleName.PARENT_NOT_YOUNGER_THAN_CHILD;
    }

    @Override
    public Optional<FieldError> apply(Relationship relationship) {
        if (relationship.getRelationshipType() != RelationshipType.PARENT) {
            return Optional.empty();
        }

        Member parent = memberRepositoryPort.getMember(relationship.getFirstMemberId());
        Member child = memberRepositoryPort.getMember(relationship.getSecondMemberId());

        if (parent.getBirthDate() == null || child.getBirthDate() == null) {
            return Optional.empty();
        }

        if (!parent.getBirthDate().isBefore(child.getBirthDate())) {
            return Optional.of(new FieldError("firstMemberId", "Parent (id " + relationship.getFirstMemberId() + ") must be born before the child (id " + relationship.getSecondMemberId() + ")"));
        }

        return Optional.empty();
    }
}

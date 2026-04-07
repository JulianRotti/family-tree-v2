package org.lunskra.core.validation.rules;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;
import org.lunskra.core.domain.RelationshipTypeSets;
import org.lunskra.core.validation.FieldError;
import org.lunskra.port.out.RelationshipRepositoryPort;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Validation rule ({@link RuleName#UNIQUE_CURRENT_SPOUSE}) that ensures each member
 * has at most one current spouse at any point in time. The rule only applies to
 * relationships of type {@link RelationshipTypeSets#TYPE_CURRENT_SPOUSE}; ex-spouse
 * and parent relationships are ignored. When validating an update the existing
 * relationship between the same pair is excluded from the check.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class UniqueCurrentSpouseRule implements Rule<Relationship> {

    private final RelationshipRepositoryPort relationshipRepositoryPort;

    @Override
    public RuleName getRuleName() {
        return RuleName.UNIQUE_CURRENT_SPOUSE;
    }

    @Override
    public Optional<FieldError> apply(Relationship relationship) {
        if (!RelationshipTypeSets.TYPE_CURRENT_SPOUSE.contains(relationship.getRelationshipType())) {
            return Optional.empty();
        }

        // Exclude the relationship between these two members themselves (relevant for updates)
        List<Relationship> existingSpouseRelationships = relationshipRepositoryPort.listRelationships().stream()
                .filter(r -> RelationshipTypeSets.TYPE_CURRENT_SPOUSE.contains(r.getRelationshipType()))
                .filter(r -> !isCurrentPair(r, relationship))
                .toList();

        boolean firstAlreadyHasSpouse = existingSpouseRelationships.stream()
                .anyMatch(r -> r.getFirstMemberId().equals(relationship.getFirstMemberId())
                        || r.getSecondMemberId().equals(relationship.getFirstMemberId()));

        if (firstAlreadyHasSpouse) {
            return Optional.of(new FieldError("firstMemberId", "Member with id " + relationship.getFirstMemberId() + " already has a current spouse"));
        }

        boolean secondAlreadyHasSpouse = existingSpouseRelationships.stream()
                .anyMatch(r -> r.getFirstMemberId().equals(relationship.getSecondMemberId())
                        || r.getSecondMemberId().equals(relationship.getSecondMemberId()));

        if (secondAlreadyHasSpouse) {
            return Optional.of(new FieldError("secondMemberId", "Member with id " + relationship.getSecondMemberId() + " already has a current spouse"));
        }

        return Optional.empty();
    }

    /**
     * Returns {@code true} if {@code r} represents the same member pair as {@code current},
     * regardless of which member is stored as first or second.
     */
    private boolean isCurrentPair(Relationship r, Relationship current) {
        return (r.getFirstMemberId().equals(current.getFirstMemberId()) && r.getSecondMemberId().equals(current.getSecondMemberId()))
                || (r.getFirstMemberId().equals(current.getSecondMemberId()) && r.getSecondMemberId().equals(current.getFirstMemberId()));
    }
}

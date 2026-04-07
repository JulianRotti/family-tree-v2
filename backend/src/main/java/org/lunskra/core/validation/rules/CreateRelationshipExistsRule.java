package org.lunskra.core.validation.rules;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.validation.FieldError;
import org.lunskra.port.out.RelationshipRepositoryPort;

import java.util.Optional;

/**
 * Validation rule ({@link RuleName#CREATE_RELATIONSHIP_EXIST}) that prevents creating
 * a duplicate relationship. A relationship already exists when the repository returns
 * a result for the given member pair (order of IDs is irrelevant).
 */
@ApplicationScoped
@RequiredArgsConstructor
public class CreateRelationshipExistsRule implements Rule<Relationship> {

    private final RelationshipRepositoryPort relationshipRepositoryPort;
    @Override
    public RuleName getRuleName() {
        return RuleName.CREATE_RELATIONSHIP_EXIST;
    }

    @Override
    public Optional<FieldError> apply(Relationship object) {
        try {
            relationshipRepositoryPort.getRelationshipByMemberPair(
                    object.getFirstMemberId(), object.getSecondMemberId());
            return Optional.of(
                    new FieldError("firstMemberId, secondMemberId", "Relationship for these members already exists")
            );
        } catch (EntityNotFoundException e) {
            return Optional.empty();
        }
    }
}

package org.lunskra.port.in;

import org.lunskra.core.domain.Relationship;

/**
 * Input port for validating {@link Relationship} objects against domain rules.
 */
public interface ValidateRelationshipUseCase {

    /**
     * Validates a relationship that is about to be created.
     * Applies self-reference, member-existence, duplicate-detection,
     * unique-current-spouse, and parent-age rules.
     *
     * @param relationship the relationship to validate; must not be {@code null}
     * @throws org.lunskra.core.validation.DomainValidationException if any rule fails
     */
    void validateNewRelationship(Relationship relationship);

    /**
     * Validates a relationship that already exists and is about to be updated.
     * Same as {@link #validateNewRelationship} but skips the duplicate-detection rule.
     *
     * @param relationship the relationship to validate; must contain valid member IDs
     * @throws org.lunskra.core.validation.DomainValidationException if any rule fails
     */
    void validateExistingRelationship(Relationship relationship);

}

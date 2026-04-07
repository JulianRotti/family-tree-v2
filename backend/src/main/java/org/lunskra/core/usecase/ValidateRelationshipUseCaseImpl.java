package org.lunskra.core.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.validation.DomainValidationException;
import org.lunskra.core.validation.FieldError;
import org.lunskra.core.validation.RelationshipValidator;
import org.lunskra.core.validation.rules.RuleName;
import org.lunskra.port.in.ValidateRelationshipUseCase;

import java.util.List;

/**
 * Applies domain validation rules to {@link org.lunskra.core.domain.Relationship} objects.
 * <p>
 * Two validation scenarios are supported:
 * <ul>
 *   <li><b>New relationship</b> – checks self-reference, member existence, duplicate
 *       detection, unique-current-spouse constraint, and parent-age constraint.</li>
 *   <li><b>Existing relationship</b> – same rules except duplicate detection, which is
 *       not relevant when updating an already persisted relationship.</li>
 * </ul>
 * A {@link DomainValidationException} is thrown when any rule fails.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ValidateRelationshipUseCaseImpl implements ValidateRelationshipUseCase {

    /** Rules applied when creating a new relationship. */
    private static final List<RuleName> NEW_RELATIONSHIP_RULE_NAMES = List.of(
            RuleName.SELF_RELATIONSHIP,
            RuleName.RELATIONSHIP_MEMBERS_EXIST,
            RuleName.CREATE_RELATIONSHIP_EXIST,
            RuleName.UNIQUE_CURRENT_SPOUSE,
            RuleName.PARENT_NOT_YOUNGER_THAN_CHILD
    );
    /** Rules applied when updating an existing relationship. */
    private static final List<RuleName> EXISTING_RELATIONSHIP_RULE_NAMES = List.of(
            RuleName.SELF_RELATIONSHIP,
            RuleName.RELATIONSHIP_MEMBERS_EXIST,
            RuleName.UNIQUE_CURRENT_SPOUSE,
            RuleName.PARENT_NOT_YOUNGER_THAN_CHILD
    );

    private final RelationshipValidator relationshipValidator;

    @Override
    public void validateNewRelationship(Relationship relationship) {
        log.atDebug().addArgument(relationship.getFirstMemberId()).addArgument(relationship.getSecondMemberId())
                .addArgument(relationship.getRelationshipType()).addArgument(NEW_RELATIONSHIP_RULE_NAMES)
                .setMessage("Validating new relationship between memberId={} and memberId={} of type={} against rules: {}").log();
        List<FieldError> errors = relationshipValidator.validate(relationship, NEW_RELATIONSHIP_RULE_NAMES);
        if (!errors.isEmpty()) {
            logErrors(errors, relationship, "new");
            throw new DomainValidationException("New relationship does not adhere to domain validation rules.", errors);
        }
    }

    @Override
    public void validateExistingRelationship(Relationship relationship) {
        log.atDebug().addArgument(relationship.getFirstMemberId()).addArgument(relationship.getSecondMemberId())
                .addArgument(relationship.getRelationshipType()).addArgument(EXISTING_RELATIONSHIP_RULE_NAMES)
                .setMessage("Validating existing relationship between memberId={} and memberId={} of type={} against rules: {}").log();
        List<FieldError> errors = relationshipValidator.validate(relationship, EXISTING_RELATIONSHIP_RULE_NAMES);
        if (!errors.isEmpty()) {
            logErrors(errors, relationship, "existing");
            throw new DomainValidationException("Existing relationship does not adhere to domain validation rules.", errors);
        }
    }

    private void logErrors(List<FieldError> errors, Relationship relationship, String detail) {
        for (FieldError error : errors) {
            log.atWarn().addArgument(detail).addArgument(relationship.getFirstMemberId())
                    .addArgument(relationship.getSecondMemberId()).addArgument(error.getField()).addArgument(error.getMessage())
                    .setMessage("Validation failed for {} relationship between memberId={} and memberId={}: ({}) {}").log();
        }
    }
}

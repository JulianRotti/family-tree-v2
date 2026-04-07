package org.lunskra.core.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.core.domain.Member;
import org.lunskra.core.validation.DomainValidationException;
import org.lunskra.core.validation.FieldError;
import org.lunskra.core.validation.MemberValidator;
import org.lunskra.core.validation.rules.RuleName;
import org.lunskra.port.in.ValidateMemberUseCase;

import java.util.List;

/**
 * Applies domain validation rules to {@link org.lunskra.core.domain.Member} objects.
 * <p>
 * Two validation scenarios are supported:
 * <ul>
 *   <li><b>New member</b> – checks uniqueness (no duplicate name + birth date) and
 *       date consistency (death date ≥ birth date).</li>
 *   <li><b>Existing member</b> – only checks date consistency, since uniqueness is
 *       irrelevant for updates.</li>
 * </ul>
 * A {@link DomainValidationException} is thrown when any rule fails.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ValidateMemberUseCaseImpl implements ValidateMemberUseCase {

    /** Rules applied when creating a new member. */
    private static final List<RuleName> NEW_MEMBER_RULE_NAMES = List.of(
            RuleName.CREATE_MEMBER_EXIST,
            RuleName.BIRTH_DEATH_DATE
    );
    /** Rules applied when updating an existing member. */
    private static final List<RuleName> EXISTING_MEMBER_RULE_NAMES = List.of(
            RuleName.BIRTH_DEATH_DATE
    );
    private final MemberValidator memberValidator;

    @Override
    public void validateNewMember(Member member) {
        log.atDebug().addArgument(member.getFirstName()).addArgument(member.getLastName()).addArgument(NEW_MEMBER_RULE_NAMES)
                .setMessage("Validating new member {} {} against rules: {}").log();
        List<FieldError> errors = memberValidator.validate(member, NEW_MEMBER_RULE_NAMES);
        if (!errors.isEmpty()) {
            logErrors(errors, member, "new");
            throw new DomainValidationException("New member does not adhere do domain validation rules.", errors);
        }
    }

    @Override
    public void validateExistingMember(Member member) {
        log.atDebug().addArgument(member.getId()).addArgument(EXISTING_MEMBER_RULE_NAMES)
                .setMessage("Validating existing member with id={} against rules: {}").log();
        List<FieldError> errors = memberValidator.validate(member, EXISTING_MEMBER_RULE_NAMES);
        if (!errors.isEmpty()) {
            logErrors(errors, member, "existing");
            throw new DomainValidationException("Existing member does not adhere do domain validation rules.", errors);
        }
    }

    private void logErrors(List<FieldError> errors, Member member, String detail) {
        for (FieldError error : errors) {
            log.atWarn().addArgument(detail).addArgument(member.getFirstName()).addArgument(member.getLastName())
                    .addArgument(error.getField()).addArgument(error.getMessage())
                    .setMessage("Validation failed for {} member {} {}: ({}) {}").log();
        }
    }
}

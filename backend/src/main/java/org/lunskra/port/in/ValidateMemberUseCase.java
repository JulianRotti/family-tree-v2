package org.lunskra.port.in;

import org.lunskra.core.domain.Member;

/**
 * Input port for validating {@link Member} objects against domain rules.
 */
public interface ValidateMemberUseCase {

    /**
     * Validates a member that is about to be created.
     * Applies uniqueness and date-consistency rules.
     *
     * @param member the member to validate; must not be {@code null}
     * @throws org.lunskra.core.validation.DomainValidationException if any rule fails
     */
    void validateNewMember(Member member);

    /**
     * Validates a member that already exists and is about to be updated.
     * Only applies date-consistency rules (uniqueness is skipped for updates).
     *
     * @param member the member to validate; must contain a valid {@code id}
     * @throws org.lunskra.core.validation.DomainValidationException if any rule fails
     */
    void validateExistingMember(Member member);
}

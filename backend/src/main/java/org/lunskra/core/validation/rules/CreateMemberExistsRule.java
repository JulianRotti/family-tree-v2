package org.lunskra.core.validation.rules;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.lunskra.core.domain.Member;
import org.lunskra.core.validation.FieldError;
import org.lunskra.port.out.MemberRepositoryPort;

import java.util.Optional;

/**
 * Validation rule ({@link RuleName#CREATE_MEMBER_EXIST}) that prevents creating a
 * duplicate member. A duplicate is defined as an existing member with the same
 * first name, last name, and birth date.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class CreateMemberExistsRule implements Rule<Member> {

    private final MemberRepositoryPort memberRepositoryPort;

    @Override
    public RuleName getRuleName() {
        return RuleName.CREATE_MEMBER_EXIST;
    }

    @Override
    public Optional<FieldError> apply(Member object) {
        // Member with firstName, lastName, birthDate already exists
        if (memberRepositoryPort.listMembers(object.getFirstName(), object.getLastName(), object.getBirthDate(), 0, 1).totalElements() > 0) {
            return Optional.of(new FieldError("firstName, lastName, birthDate","Member with this data already exists"));
        }
        return Optional.empty();
    }
}

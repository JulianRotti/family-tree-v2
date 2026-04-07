package org.lunskra.port.out;

import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.MemberPage;

import java.time.LocalDate;

public interface MemberRepositoryPort {

    /**
     * Returns a paginated list of {@link Member Members} matching the given optional filter parameters.
     *
     * @param firstName optional substring to match against the member's first name (case-insensitive), or {@code null} to ignore
     * @param lastName optional substring to match against the member's last name (case-insensitive), or {@code null} to ignore
     * @param birthDate optional birthdate to match exactly, or {@code null} to ignore
     * @param page zero-based page index
     * @param pageSize number of members per page; must be &gt;= 1
     * @return a {@link MemberPage} containing matching members for the requested page plus the total element count
     */
    MemberPage listMembers(String firstName, String lastName, LocalDate birthDate, int page, int pageSize);

    /**
     * Returns the {@link Member} with the given unique identifier.
     *
     * @param id the unique identifier of the member; must not be {@code null}
     * @return the corresponding {@link Member}
     * @throws jakarta.persistence.EntityNotFoundException if no member with the given {@code id} exists
     */
    Member getMember(Integer id);

    /**
     * Deletes the {@link Member} with the given unique identifier.
     *
     * @param id the unique identifier of the member; must not be {@code null}
     * @throws jakarta.persistence.EntityNotFoundException if no member with the given {@code id} exists
     */
    void deleteMember(Integer id);

    /**
     * Creates the {@link Member} in the persistence layer.
     *
     * @param member the {@link Member} to be created; must not contain an {@code id}
     * @return the stored {@link Member} enriched by the generated identifier
     */
    Member createMember(Member member);

    /**
     * Updates an existing {@link Member}.
     *
     * @param member the member containing the new state; must contain a valid {@code id}
     * @return the updated {@link Member}
     * @throws jakarta.persistence.EntityNotFoundException if the member does not exist
     */
    Member updateMember(Member member);
}

package org.lunskra.adapter.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.security.TenantContext;
import org.lunskra.core.domain.FamilyTreeComponents;
import org.lunskra.core.domain.Gender;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;
import org.lunskra.port.out.FamilyTreeRepositoryPort;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistence adapter that implements {@link FamilyTreeRepositoryPort} by calling the
 * MySQL stored procedure {@code get_family_tree}.
 * <p>
 * The procedure returns two result sets in a fixed order:
 * <ol>
 *   <li>All relationships connecting the members reachable from the head-of-family.</li>
 *   <li>All reachable members (id, first name, last name only).</li>
 * </ol>
 * Because Hibernate does not map stored-procedure result sets to entities automatically,
 * the adapter unwraps the underlying JDBC connection and maps the
 * {@link java.sql.ResultSet rows} manually.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FamilyTreeRepositoryPortImpl implements FamilyTreeRepositoryPort {

    private final EntityManager em;
    private final TenantContext tenantContext;

    @Override
    @Transactional
    public FamilyTreeComponents getFamilyTreeComponents(Integer headOfFamilyId, Integer maxDepth) {
        return callProcedure(headOfFamilyId, maxDepth, "{CALL get_family_tree(?, ?, ?)}");
    }

    @Override
    public FamilyTreeComponents getFamilyTreeAncestorComponents(Integer childOfFamilyId, Integer maxDepth) {
        return callProcedure(childOfFamilyId, maxDepth, "{CALL get_family_tree_reverse(?, ?, ?)}");
    }

    public FamilyTreeComponents callProcedure(Integer memberId, Integer maxDepth, String procedure) {
        List<Member> members = new ArrayList<>();
        List<Relationship> relationships = new ArrayList<>();
        String currentTenantId = tenantContext.getTenantId();

        log.debug("Calling stored procedure {} for memberId={}, maxDepth={}", procedure, memberId, maxDepth);
        em.unwrap(org.hibernate.Session.class).doWork(connection -> {
            try (CallableStatement stmt = connection.prepareCall(procedure)) {

                stmt.setInt(1, memberId);
                if (maxDepth != null) {
                    stmt.setInt(2, maxDepth);
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                stmt.setString(3, currentTenantId);

                boolean hasResults = stmt.execute();

                // ---- RESULT SET 1: relationships ----
                if (hasResults) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        while (rs.next()) {
                            relationships.add(mapRelationship(rs));
                        }
                    }
                }

                // ---- RESULT SET 2: members ----
                if (stmt.getMoreResults()) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        while (rs.next()) {
                            members.add(mapMember(rs));
                        }
                    }
                }
            }
        });

        if (members.isEmpty()) {
            throw new EntityNotFoundException("Member with id " + memberId + " not found");
        }

        return new FamilyTreeComponents(members, relationships);
    }

    /**
     * Maps a single row from the first result set of {@code get_family_tree} to a
     * {@link Relationship} domain object.
     */
    private Relationship mapRelationship(ResultSet rs) throws SQLException {
        return new Relationship(
                rs.getInt("member_1_id"),
                rs.getInt("member_2_id"),
                RelationshipType.valueOf(rs.getString("relationship"))
        );
    }

    /**
     * Maps a single row from the second result set of {@code get_family_tree} to a
     * {@link Member} domain object. Only fields needed for visualisation in the frontend are filled.
     */
    private Member mapMember(ResultSet rs) throws SQLException {
        return Member.builder()
                .id(rs.getInt("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .initialLastName(rs.getString("initial_last_name"))
                .gender(Gender.valueOf(rs.getString("gender")))
                .birthDate(rs.getDate("birth_date") != null ? rs.getDate("birth_date").toLocalDate() : null)
                .deathDate(rs.getDate("death_date") != null ? rs.getDate("death_date").toLocalDate() : null)
                .birthCity(rs.getString("birth_city"))
                .birthCountry(rs.getString("birth_country"))
                .imagePath(rs.getString("image_path"))
                .build();
    }
}

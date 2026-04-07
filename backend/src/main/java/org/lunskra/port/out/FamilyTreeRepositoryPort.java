package org.lunskra.port.out;

import org.lunskra.core.domain.FamilyTreeComponents;

/**
 * Output port for retrieving the raw data required to build a family tree.
 */
public interface FamilyTreeRepositoryPort {

    /**
     * Returns all members and relationships reachable from the given head-of-family
     * member. Delegates to the {@code get_family_tree} stored procedure which performs
     * the recursive traversal in the database.
     *
     * @param headOfFamilyId ID of the root member
     * @return a {@link FamilyTreeComponents} containing all reachable members (with id,
     *         first name and last name populated) and all connecting relationships
     * @throws jakarta.persistence.EntityNotFoundException if no member with the given id exists
     */
    FamilyTreeComponents getFamilyTreeComponents(Integer headOfFamilyId);
}

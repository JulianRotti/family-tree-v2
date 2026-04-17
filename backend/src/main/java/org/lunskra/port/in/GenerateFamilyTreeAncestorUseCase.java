package org.lunskra.port.in;

import org.lunskra.core.domain.FamilyTreeAncestor;

/**
 * Input port for building a hierarchical {@link FamilyTreeAncestor} rooted at a given member.
 */
public interface GenerateFamilyTreeAncestorUseCase {

    /**
     * Generates a complete family tree starting at the given child-of-family member.
     * The spacing parameters are forwarded to the subtree-length calculation so the
     * frontend can use the pre-computed values for layout without traversing the tree again.
     *
     * @param childOfFamilyId             ID of the member at the root of the tree
     * @param maxDepth                   maximum number of ascendant generations to include,
     *                                   or {@code null} for all generations
     * @param widthOfMemberNode          width of a single member node in the UI layout unit
     * @param spaceBetweenMemberNodes horizontal gap between a member and their spouse
     * @return the fully assembled {@link FamilyTreeAncestor}
     * @throws jakarta.persistence.EntityNotFoundException if the head-of-family member does not exist
     */
    FamilyTreeAncestor generateFamilyTreeAncestor(
            Integer childOfFamilyId,
            Integer maxDepth,
            Float widthOfMemberNode,
            Float spaceBetweenMemberNodes
    );

}

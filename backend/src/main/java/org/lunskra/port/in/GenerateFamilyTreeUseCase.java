package org.lunskra.port.in;

import org.lunskra.core.domain.FamilyTree;

/**
 * Input port for building a hierarchical {@link FamilyTree} rooted at a given member.
 */
public interface GenerateFamilyTreeUseCase {

    /**
     * Generates a complete family tree starting at the given head-of-family member.
     * The spacing parameters are forwarded to the subtree-length calculation so the
     * frontend can use the pre-computed values for layout without traversing the tree again.
     *
     * @param headOfFamilyId             ID of the member at the root of the tree
     * @param maxDepth                   maximum number of descendant generations to include,
     *                                   or {@code null} for all generations
     * @param widthOfMemberNode          width of a single member node in the UI layout unit
     * @param spaceBetweenMemberAndSpouse horizontal gap between a member and their spouse
     * @param spaceBetweenChildren        horizontal gap between sibling nodes
     * @return the fully assembled {@link FamilyTree}
     * @throws jakarta.persistence.EntityNotFoundException if the head-of-family member does not exist
     */
    FamilyTree generateFamilyTree(
            Integer headOfFamilyId,
            Integer maxDepth,
            Float widthOfMemberNode,
            Float spaceBetweenMemberAndSpouse,
            Float spaceBetweenChildren
    );
}

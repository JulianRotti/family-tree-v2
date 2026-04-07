package org.lunskra.core.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.core.domain.FamilyTree;
import org.lunskra.core.domain.FamilyTreeComponents;
import org.lunskra.core.domain.FamilyTreeOfMember;
import org.lunskra.core.domain.FamilyUnit;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.service.RelationshipFinder;
import org.lunskra.port.in.GenerateFamilyTreeUseCase;
import org.lunskra.port.out.FamilyTreeRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lunskra.core.service.RelationshipFinder.Spouse;

/**
 * Generates a hierarchical {@link FamilyTree} starting from a given head-of-family member.
 * <p>
 * The use case fetches all members and relationships reachable from the root via the
 * {@code get_family_tree} stored procedure, then recursively builds the tree structure.
 * For each node it also pre-computes a {@code subtreeLength} value — the total horizontal
 * width of that node's subtree — which the frontend uses for layout calculations. The
 * measurement is derived from the UI spacing parameters passed by the caller.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GenerateFamilyTreeUseCaseImpl implements GenerateFamilyTreeUseCase {

    private final FamilyTreeRepositoryPort familyTreePort;

    @Override
    public FamilyTree generateFamilyTree(
            Integer headOfFamilyId,
            Float widthOfMemberNode,
            Float spaceBetweenMemberAndSpouse,
            Float spaceBetweenChildren
    ) {
        log.atInfo()
                .addArgument(headOfFamilyId)
                .addArgument(widthOfMemberNode)
                .addArgument(spaceBetweenMemberAndSpouse)
                .addArgument(spaceBetweenChildren)
                .setMessage("Computing family tree for memberId={}. Measurements for subtreeLength computation: " +
                        "widthOfMemberNode={}, spaceBetweenMemberAndSpouse={}, spaceBetweenChildren={}").log();

        FamilyTreeComponents components = familyTreePort.getFamilyTreeComponents(headOfFamilyId);

        List<Member> members = components.members();
        List<Relationship> relationships = components.relationships();

        FamilyTreeOfMember famTreeOfHead = generateFamilyTreeOfMember(
                headOfFamilyId,
                relationships,
                new Measures(widthOfMemberNode, spaceBetweenMemberAndSpouse, spaceBetweenChildren)
        );

        return new FamilyTree(
                headOfFamilyId,
                famTreeOfHead,
                members
        );
    }

    /**
     * Recursively builds the subtree for a single member.
     * <p>
     * For each spouse found via {@link RelationshipFinder#findSpousesWithChildren}, a
     * {@link FamilyUnit} is created and its children are resolved recursively. After the
     * full subtree is assembled the appropriate {@code subtreeLength} formula is applied.
     *
     * @param memberId      the member whose subtree is to be built
     * @param relationships all relationships in the family tree
     * @param measures      UI spacing values used to compute {@code subtreeLength}
     * @return the fully populated subtree node for the given member
     */
    private FamilyTreeOfMember generateFamilyTreeOfMember(
            Integer memberId,
            List<Relationship> relationships,
            Measures measures
    ) {

        FamilyTreeOfMember famTreeOfMember = new FamilyTreeOfMember();
        famTreeOfMember.setMemberId(memberId);
        famTreeOfMember.setFamily(new ArrayList<>());

        Map<Spouse, List<Integer>> spouseChildrenMap = RelationshipFinder.findSpousesWithChildren(memberId, relationships);

        for (Spouse spouse : spouseChildrenMap.keySet()) {
            FamilyUnit unit = new FamilyUnit();
            unit.setSpouseId(spouse.memberId());
            unit.setRelationship(spouse.type());
            unit.setChildren(new ArrayList<>());

            for (Integer childId : spouseChildrenMap.get(spouse)) {
                unit.getChildren().add(generateFamilyTreeOfMember(childId, relationships, measures));
            }
            famTreeOfMember.getFamily().add(unit);
        }

        float subtreeLength = 0F;

        // Member doesn't have any family
        if (famTreeOfMember.getFamily().isEmpty()) {
            subtreeLength = getSubtreeLengthNoFamily(measures);
        }

        // Member does have exactly one family
        else if (famTreeOfMember.getFamily().size() == 1) {
            subtreeLength = getSubtreeLengthOneFamily(famTreeOfMember.getFamily().getFirst(), measures);
        }

        // Member does have several families
        else {
            subtreeLength = getSubtreeLengthMultipleFamilies(famTreeOfMember.getFamily(), measures);
        }

        famTreeOfMember.setSubtreeLength(subtreeLength);

        return famTreeOfMember;
    }

    /**
     * Subtree length when the member has no spouse and no children.
     * The width is {@code widthOfMemberNode + spaceBetweenChildren}.
     */
    private Float getSubtreeLengthNoFamily(Measures measures) {

        // Member doesn't have family
        // -> subtree consists only of member node, which is a child of previous generation
        // -> spaceBetweenChildren/2 | widthOfMemberNode | widthOfMemberNode/2
        return measures.widthOfMemberNode() + measures.spaceBetweenChildren();
    }

    /**
     * Subtree length when the member has exactly one family unit.
     * Returns the larger of: (a) the combined width of the member and spouse nodes with
     * surrounding spacing, and (b) the sum of all children's subtree lengths.
     */
    private Float getSubtreeLengthOneFamily(FamilyUnit unit, Measures measures) {

        // -> spaceBetweenChildren/2 + widthOfMemberNode + spaceBetweenMemberAndSpouse + widthOfMemberNode + spaceBetweenChildren/2
        float lengthOfMemberAndSpouse = 2 * measures.widthOfMemberNode() + measures.spaceBetweenMemberAndSpouse() + measures.spaceBetweenChildren();

        // The family consists only of a spouse without children
        // -> spaceBetweenChildren/2 | widthOfMemberNode | spaceBetweenMemberAndSpouse | widthOfMemberNode | spaceBetweenChildren/2
        if (unit.getChildren().isEmpty()) {
            return lengthOfMemberAndSpouse;
        }

        // The family has children, then the subtree length is the maximum of length of the parents generation
        // -> spaceBetweenChildren/2 | widthOfMemberNode | spaceBetweenMemberAndSpouse | widthOfMemberNode | spaceBetweenChildren/2
        // or the childs generation (=subtreelength of the childs tree)
        return Math.max(
                lengthOfMemberAndSpouse,
                unit.getChildren().stream()
                    .map(FamilyTreeOfMember::getSubtreeLength)
                    .reduce(0F, Float::sum)
        );
    }

    /**
     * Subtree length when the member has more than one family unit.
     * In the visual layout each spouse is placed to the right of the member, with
     * children dangling below each spouse column. The total width is the member's
     * section plus the sum of each family unit's column width.
     */
    private Float getSubtreeLengthMultipleFamilies(List<FamilyUnit> units, Measures measures) {

        // -> spaceBetweenChildren/2 | widthOfMemberNode | spaceBetweenMemberAndSpouse/2
        float lengthOfMemberWithSpacings = measures.widthOfMemberNode() + measures.spaceBetweenMemberAndSpouse() / 2 + measures.spaceBetweenChildren() / 2;

        // In final visualisation the children dangle under the spouses nodes and the member node is separated from that
        // -> member | spouse1 | spouse2 | ...
        float subtreeLength = lengthOfMemberWithSpacings;

        for (FamilyUnit unit : units) {
            // The family consists only of a spouse without children
            // member (already added) | spouse
            if (unit.getChildren().isEmpty()) {
                subtreeLength += lengthOfMemberWithSpacings;
            }

            // The family has children, then the subtree length is
            // -> member (already added) | max( spouse, subtreeLength of child)
            else {
                subtreeLength += Math.max(
                        lengthOfMemberWithSpacings,
                        unit.getChildren().stream()
                            .map(FamilyTreeOfMember::getSubtreeLength)
                            .reduce(0F, Float::sum)
                );
            }
        }

        return subtreeLength;
    }

    /**
     * Groups the three UI spacing values passed into the use case so they can be
     * threaded through the recursive calls without a long parameter list.
     *
     * @param widthOfMemberNode          width of a single member node in the frontend layout
     * @param spaceBetweenMemberAndSpouse horizontal gap between a member and their spouse
     * @param spaceBetweenChildren        horizontal gap between sibling nodes
     */
    private record Measures(Float widthOfMemberNode,
                            Float spaceBetweenMemberAndSpouse,
                            Float spaceBetweenChildren) {};
}

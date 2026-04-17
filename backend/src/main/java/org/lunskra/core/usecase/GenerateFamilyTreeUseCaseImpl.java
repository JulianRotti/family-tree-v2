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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.lunskra.core.service.RelationshipFinder.SpouseRecord;

/**
 * Generates a hierarchical {@link FamilyTree} starting from a given head-of-family member.
 * <p>
 * Members and relationships are fetched in one shot via the repository (backed by the
 * {@code get_family_tree} stored procedure). The tree is then built by
 * {@link #generateFamilyTreeOfMember} which recurses through parent-child edges while
 * collecting three aggregate counts in a shared {@link Counts} instance:
 * <ul>
 *   <li>{@code numberTotal} — every person in the tree, including spouses</li>
 *   <li>{@code numberLiving} — every person whose {@code deathDate} is {@code null}</li>
 *   <li>{@code numberGenerations} — depth of the deepest generation reached, or
 *       {@code maxDepth} when the caller supplied one</li>
 * </ul>
 * Each {@link FamilyTreeOfMember} node also carries a pre-computed {@code subtreeLength}
 * — the total horizontal width of its subtree — which the frontend uses for layout
 * calculations without needing to recurse the tree a second time.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GenerateFamilyTreeUseCaseImpl implements GenerateFamilyTreeUseCase {

    private final FamilyTreeRepositoryPort familyTreePort;

    @Override
    public FamilyTree generateFamilyTree(
            Integer headOfFamilyId,
            Integer maxDepth,
            Float widthOfMemberNode,
            Float spaceBetweenMemberAndSpouse,
            Float spaceBetweenChildren
    ) {
        log.atInfo()
                .addArgument(headOfFamilyId)
                .addArgument(maxDepth)
                .addArgument(widthOfMemberNode)
                .addArgument(spaceBetweenMemberAndSpouse)
                .addArgument(spaceBetweenChildren)
                .setMessage("Computing family tree for memberId={}. maxDepth={}. Measurements for subtreeLength computation: " +
                        "widthOfMemberNode={}, spaceBetweenMemberAndSpouse={}, spaceBetweenChildren={}").log();

        FamilyTreeComponents components = familyTreePort.getFamilyTreeComponents(headOfFamilyId, maxDepth);

        List<Member> members = components.members();
        List<Relationship> relationships = components.relationships();

        Map<Integer, Member> membersById = members.stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        Counts counts = new Counts();

        FamilyTreeOfMember famTreeOfHead = generateFamilyTreeOfMember(
                headOfFamilyId,
                1,
                counts,
                membersById,
                relationships,
                new Measures(widthOfMemberNode, spaceBetweenMemberAndSpouse, spaceBetweenChildren)
        );

        return new FamilyTree(
                headOfFamilyId,
                counts.numberTotal,
                counts.numberLiving,
                counts.numberGenerations,
                famTreeOfHead,
                members
        );
    }

    /**
     * Recursively builds the subtree rooted at {@code memberId}.
     * <p>
     * Before descending, the member and each of their spouses are counted in {@code counts}.
     * For each spouse found via {@link RelationshipFinder#findSpousesWithChildren} a
     * {@link FamilyUnit} is created and its children are resolved by recursive calls with
     * {@code currentGeneration + 1}. After the full subtree is assembled the appropriate
     * {@code subtreeLength} formula is applied based on how many family units the member has.
     *
     * @param memberId          the member whose subtree is to be built
     * @param currentGeneration 1-based depth of this node relative to the tree root
     * @param counts            shared mutable counters updated in-place during recursion
     * @param membersById       lookup map for member details (used to check {@code deathDate})
     * @param relationships     all relationships in the family tree
     * @param measures          UI spacing values used to compute {@code subtreeLength}
     * @return the fully populated subtree node for the given member
     */
    private FamilyTreeOfMember generateFamilyTreeOfMember(
            Integer memberId,
            int currentGeneration,
            Counts counts,
            Map<Integer, Member> membersById,
            List<Relationship> relationships,
            Measures measures
    ) {

        counts.incrementFor(memberId, membersById);
        counts.numberGenerations = Math.max(counts.numberGenerations, currentGeneration);

        FamilyTreeOfMember famTreeOfMember = new FamilyTreeOfMember();
        famTreeOfMember.setMemberId(memberId);
        famTreeOfMember.setGeneration(currentGeneration);
        famTreeOfMember.setFamily(new ArrayList<>());

        Map<SpouseRecord, List<Integer>> spouseChildrenMap = RelationshipFinder.findSpousesWithChildren(memberId, relationships);

        for (SpouseRecord spouseRecord : spouseChildrenMap.keySet()) {
            counts.incrementFor(spouseRecord.memberId(), membersById);

            FamilyUnit unit = new FamilyUnit();
            unit.setSpouseId(spouseRecord.memberId());
            unit.setRelationship(spouseRecord.type());
            unit.setChildren(new ArrayList<>());

            for (Integer childId : spouseChildrenMap.get(spouseRecord)) {
                unit.getChildren().add(
                        generateFamilyTreeOfMember(childId, currentGeneration + 1, counts, membersById, relationships, measures)
                );
            }
            famTreeOfMember.getFamily().add(unit);
        }

        float subtreeLength;

        if (famTreeOfMember.getFamily().isEmpty()) {
            subtreeLength = getSubtreeLengthNoFamily(measures);
        } else if (famTreeOfMember.getFamily().size() == 1) {
            subtreeLength = getSubtreeLengthOneFamily(famTreeOfMember.getFamily().getFirst(), measures);
        } else {
            subtreeLength = getSubtreeLengthMultipleFamilies(famTreeOfMember.getFamily(), measures);
        }

        famTreeOfMember.setSubtreeLength(subtreeLength);

        return famTreeOfMember;
    }

    /**
     * Subtree length when the member has no spouse and no children.
     * <p>
     * Layout: {@code spaceBetweenChildren/2 | widthOfMemberNode | spaceBetweenChildren/2}
     * @param measures the distance measures to compute the length
     * @return the subtree length if the member does not have any family
     */
    private Float getSubtreeLengthNoFamily(Measures measures) {
        return measures.widthOfMemberNode() + measures.spaceBetweenChildren();
    }

    /**
     * Subtree length when the member has exactly one family unit.
     * <p>
     * Parent row layout: {@code spaceBetweenChildren/2 | member | spaceBetweenMemberAndSpouse | spouse | spaceBetweenChildren/2}
     * <p>
     * @param unit the {@link FamilyUnit}
     * @param measures the distance measures to compute the length
     * @return the wider of the parent row and the sum of children's subtree lengths.
     */
    private Float getSubtreeLengthOneFamily(FamilyUnit unit, Measures measures) {
        float parentRowLength = 2 * measures.widthOfMemberNode() + measures.spaceBetweenMemberAndSpouse() + measures.spaceBetweenChildren();

        if (unit.getChildren().isEmpty()) {
            return parentRowLength;
        }

        float childRowLength = unit.getChildren().stream()
                .map(FamilyTreeOfMember::getSubtreeLength)
                .reduce(0F, Float::sum);

        return Math.max(parentRowLength, childRowLength);
    }

    /**
     * Subtree length when the member has more than one family unit.
     * <p>
     * Each spouse is placed to the right of the member, with children dangling below
     * their own spouse column. One "slot" is {@code spaceBetweenChildren/2 | member | spaceBetweenMemberAndSpouse/2},
     * and the member occupies the first slot. Each family unit then contributes a further
     * slot, whose width is the wider of the spouse slot and the sum of its children's subtree lengths.
     * @param units List of {@link FamilyUnit}
     * @param measures the distance measures to compute the length
     * @return the subtree length of the combined family units
     */
    private Float getSubtreeLengthMultipleFamilies(List<FamilyUnit> units, Measures measures) {
        // Width of one slot: spaceBetweenChildren/2 | widthOfMemberNode | spaceBetweenMemberAndSpouse/2
        float slotLength = measures.widthOfMemberNode() + measures.spaceBetweenMemberAndSpouse() / 2 + measures.spaceBetweenChildren() / 2;

        // The member occupies the leftmost slot; each spouse adds another slot to the right.
        float subtreeLength = slotLength;

        for (FamilyUnit unit : units) {
            if (unit.getChildren().isEmpty()) {
                subtreeLength += slotLength;
            } else {
                float childRowLength = unit.getChildren().stream()
                        .map(FamilyTreeOfMember::getSubtreeLength)
                        .reduce(0F, Float::sum);
                subtreeLength += Math.max(slotLength, childRowLength);
            }
        }

        return subtreeLength;
    }

}

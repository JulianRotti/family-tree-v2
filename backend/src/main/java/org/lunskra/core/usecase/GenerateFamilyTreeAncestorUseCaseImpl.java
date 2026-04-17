package org.lunskra.core.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.core.domain.FamilyTreeAncestor;
import org.lunskra.core.domain.FamilyTreeAncestorOfMember;
import org.lunskra.core.domain.FamilyTreeComponents;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.Parents;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.service.RelationshipFinder;
import org.lunskra.port.in.GenerateFamilyTreeAncestorUseCase;
import org.lunskra.port.out.FamilyTreeRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GenerateFamilyTreeAncestorUseCaseImpl implements GenerateFamilyTreeAncestorUseCase {

    private final FamilyTreeRepositoryPort port;

    @Override
    public FamilyTreeAncestor generateFamilyTreeAncestor(Integer childOfFamilyId, Integer maxDepth, Float widthOfMemberNode, Float spaceBetweenMembers) {

        FamilyTreeComponents components = port.getFamilyTreeAncestorComponents(childOfFamilyId, maxDepth);

        List<Member> members = components.members();
        List<Relationship> relationships = components.relationships();

        Map<Integer, Member> membersById = members.stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        Counts counts = new Counts();

        FamilyTreeAncestorOfMember famTreeOfChild = generateFamilyTreeAncestorOfMember(
                childOfFamilyId,
                1,
                counts,
                membersById,
                relationships,
                new Measures(widthOfMemberNode, spaceBetweenMembers, null)
        );

        return new FamilyTreeAncestor(
                childOfFamilyId,
                counts.numberTotal,
                counts.numberLiving,
                counts.numberGenerations,
                famTreeOfChild,
                members
        );
    }

    private FamilyTreeAncestorOfMember generateFamilyTreeAncestorOfMember(
            Integer memberId,
            int currentGeneration,
            Counts counts,
            Map<Integer, Member> membersById,
            List<Relationship> relationships,
            Measures measures
    ) {
        counts.incrementFor(memberId, membersById);
        counts.numberGenerations = Math.max(counts.numberGenerations, currentGeneration);

        FamilyTreeAncestorOfMember famTreeOfMember = new FamilyTreeAncestorOfMember();

        famTreeOfMember.setMemberId(memberId);
        famTreeOfMember.setGeneration(currentGeneration);

        RelationshipFinder.ParentRecord parentRecord = RelationshipFinder.findParents(
                memberId,
                relationships
        );

        Parents parents = new Parents();
        parents.setRelationship(parentRecord.type());
        parents.setParents(new ArrayList<>());

        float subTreeLength = 0;

        for (int parentId : parentRecord.parentIds()) {
            FamilyTreeAncestorOfMember famTree = generateFamilyTreeAncestorOfMember(
                    parentId, currentGeneration + 1, counts, membersById, relationships, measures
            );

            parents.getParents().add(famTree);

            subTreeLength += famTree.getSubtreeLength();
        }

        // If no parents, then subtreelength is the space one node with spacing needs
        subTreeLength = Math.max(measures.widthOfMemberNode() + measures.spaceBetweenMemberAndSpouse(), subTreeLength);
        famTreeOfMember.setSubtreeLength(subTreeLength);
        famTreeOfMember.setParents(parents);

        return famTreeOfMember;
    }

}

package org.lunskra.core.usecase;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.adapter.utils.MemberGenerator;
import org.lunskra.core.domain.FamilyTree;
import org.lunskra.core.domain.FamilyTreeComponents;
import org.lunskra.core.domain.FamilyTreeOfMember;
import org.lunskra.core.domain.FamilyUnit;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.utils.RelationshipGenerator;
import org.lunskra.port.out.FamilyTreeRepositoryPort;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class GenerateFamilyTreeUseCaseImplTest {

    @Mock
    FamilyTreeRepositoryPort port;

    GenerateFamilyTreeUseCaseImpl useCase;

    @BeforeEach
    void setup() {
        useCase = new GenerateFamilyTreeUseCaseImpl(port);
    }

    @Test
    void testGenerateFamilyTree_WhenMemberDoesNotExist_ThenThrowEntityNotFoundException() {
        // Given
        Mockito.when(port.getFamilyTreeComponents(999)).thenThrow(
                new EntityNotFoundException("Member with id 999 not found")
        );

        // When / Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> useCase.generateFamilyTree(999, 1F, 3F, 7F)
        );
    }

    @Test
    void testGenerateFamilyTreeOfMember_ParentOneChildFamily() {
        // Given
        Member head = MemberGenerator.createLivingMemberWithRequiredData();
        head.setId(1);

        Member spouse = MemberGenerator.createLivingMemberWithRequiredData();
        spouse.setId(2);

        Member child1 = MemberGenerator.createLivingMemberWithRequiredData();
        child1.setId(3);

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(head.getId(), spouse.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child1.getId())
        );

        Mockito.when(port.getFamilyTreeComponents(head.getId())).thenReturn(
                new FamilyTreeComponents(List.of(head, spouse, child1), relationships)
        );

        float widthOfMemberNode = 1F;
        float spaceBetweenMemberAndSpouse = 3F;
        float spaceBetweenChildren = 7F;

        // When
        FamilyTree familyTree = useCase.generateFamilyTree(head.getId(), widthOfMemberNode, spaceBetweenMemberAndSpouse, spaceBetweenChildren);

        // Then
        Assertions.assertEquals(head.getId(), familyTree.getHeadOfFamilyId());

        FamilyTreeOfMember famOfHead = familyTree.getTree();
        Assertions.assertEquals(1, famOfHead.getFamily().size());
        Assertions.assertEquals(spaceBetweenChildren + 2 * widthOfMemberNode + spaceBetweenMemberAndSpouse, famOfHead.getSubtreeLength());
    }

    @Test
    void testGenerateFamilyTreeOfMember_ParentTwoChildrenFamily() {
        // Given
        Member head = MemberGenerator.createLivingMemberWithRequiredData();
        head.setId(1);

        Member spouse = MemberGenerator.createLivingMemberWithRequiredData();
        spouse.setId(2);

        Member child1 = MemberGenerator.createLivingMemberWithRequiredData();
        child1.setId(3);

        Member child2 = MemberGenerator.createLivingMemberWithRequiredData();
        child2.setId(4);

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(head.getId(), spouse.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child2.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child2.getId())
        );

        Mockito.when(port.getFamilyTreeComponents(head.getId())).thenReturn(
                new FamilyTreeComponents(List.of(head, spouse, child1, child2), relationships)
        );

        float widthOfMemberNode = 1F;
        float spaceBetweenMemberAndSpouse = 3F;
        float spaceBetweenChildren = 7F;

        // When
        FamilyTree familyTree = useCase.generateFamilyTree(head.getId(), widthOfMemberNode, spaceBetweenMemberAndSpouse, spaceBetweenChildren);

        // Then
        FamilyTreeOfMember famOfHead = familyTree.getTree();
        Assertions.assertEquals(1, famOfHead.getFamily().size());
        Assertions.assertEquals(2 * spaceBetweenChildren + 2 * widthOfMemberNode, famOfHead.getSubtreeLength());

        FamilyUnit unit = famOfHead.getFamily().getFirst();
        Assertions.assertEquals(spouse.getId(), unit.getSpouseId());
        Assertions.assertEquals(2, unit.getChildren().size());

        FamilyTreeOfMember famOfChild1 = unit.getChildren().get(0);
        FamilyTreeOfMember famOfChild2 = unit.getChildren().get(1);

        Assertions.assertEquals(spaceBetweenChildren + widthOfMemberNode, famOfChild1.getSubtreeLength());
        Assertions.assertEquals(spaceBetweenChildren + widthOfMemberNode, famOfChild2.getSubtreeLength());
    }

    @Test
    void testGenerateFamilyTreeOfMember_ParentTwoChildrenWithChildrenFamily() {
        // Given
        Member head = MemberGenerator.createLivingMemberWithRequiredData();
        head.setId(1);

        Member spouse = MemberGenerator.createLivingMemberWithRequiredData();
        spouse.setId(2);

        Member child1 = MemberGenerator.createLivingMemberWithRequiredData();
        child1.setId(3);

        Member child2 = MemberGenerator.createLivingMemberWithRequiredData();
        child2.setId(4);

        Member child3 = MemberGenerator.createLivingMemberWithRequiredData();
        child3.setId(5);

        Member spouseOfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        spouseOfChild2.setId(6);

        Member child1OfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        child1OfChild2.setId(7);

        Member child2OfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        child2OfChild2.setId(8);

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(head.getId(), spouse.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child2.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child3.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child2.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child3.getId()),
                RelationshipGenerator.currentSpouseRelationship(child2.getId(), spouseOfChild2.getId()),
                RelationshipGenerator.parentRelationship(child2.getId(), child1OfChild2.getId()),
                RelationshipGenerator.parentRelationship(child2.getId(), child2OfChild2.getId()),
                RelationshipGenerator.parentRelationship(spouseOfChild2.getId(), child1OfChild2.getId()),
                RelationshipGenerator.parentRelationship(spouseOfChild2.getId(), child2OfChild2.getId())
        );

        Mockito.when(port.getFamilyTreeComponents(head.getId())).thenReturn(
                new FamilyTreeComponents(
                        List.of(head, spouse, child1, child2, child3, spouseOfChild2, child1OfChild2, child2OfChild2),
                        relationships
                )
        );

        float widthOfMemberNode = 1F;
        float spaceBetweenMemberAndSpouse = 3F;
        float spaceBetweenChildren = 7F;

        // When
        FamilyTree familyTree = useCase.generateFamilyTree(head.getId(), widthOfMemberNode, spaceBetweenMemberAndSpouse, spaceBetweenChildren);

        FamilyTreeOfMember famOfHead = familyTree.getTree();
        Assertions.assertEquals(1, famOfHead.getFamily().size());
        Assertions.assertEquals(4 * spaceBetweenChildren + 4 * widthOfMemberNode, famOfHead.getSubtreeLength());
    }

    @Test
    void testGenerateFamilyTreeOfMember_ParentTwoChildrenWithChildrenFamilyMultipleSpouses() {
        // Given
        Member head = MemberGenerator.createLivingMemberWithRequiredData();
        head.setId(1);

        Member spouse = MemberGenerator.createLivingMemberWithRequiredData();
        spouse.setId(2);

        Member child1 = MemberGenerator.createLivingMemberWithRequiredData();
        child1.setId(3);

        Member child2 = MemberGenerator.createLivingMemberWithRequiredData();
        child2.setId(4);

        Member child3 = MemberGenerator.createLivingMemberWithRequiredData();
        child3.setId(5);

        Member spouseOfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        spouseOfChild2.setId(6);

        Member child1OfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        child1OfChild2.setId(7);

        Member child2OfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        child2OfChild2.setId(8);

        Member spouse2OfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        spouse2OfChild2.setId(9);

        Member child3OfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        child3OfChild2.setId(10);

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(head.getId(), spouse.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child2.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child3.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child2.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child3.getId()),
                RelationshipGenerator.currentSpouseRelationship(child2.getId(), spouseOfChild2.getId()),
                RelationshipGenerator.parentRelationship(child2.getId(), child1OfChild2.getId()),
                RelationshipGenerator.parentRelationship(child2.getId(), child2OfChild2.getId()),
                RelationshipGenerator.parentRelationship(spouseOfChild2.getId(), child1OfChild2.getId()),
                RelationshipGenerator.parentRelationship(spouseOfChild2.getId(), child2OfChild2.getId()),
                RelationshipGenerator.exSpouseRelationship(child2.getId(), spouse2OfChild2.getId()),
                RelationshipGenerator.parentRelationship(child2.getId(), child3OfChild2.getId()),
                RelationshipGenerator.parentRelationship(spouse2OfChild2.getId(), child3OfChild2.getId())
        );

        Mockito.when(port.getFamilyTreeComponents(head.getId())).thenReturn(
                new FamilyTreeComponents(
                        List.of(head, spouse, child1, child2, child3, spouseOfChild2, child1OfChild2, child2OfChild2, spouse2OfChild2, child3OfChild2),
                        relationships
                )
        );

        float widthOfMemberNode = 1F;
        float spaceBetweenMemberAndSpouse = 3F;
        float spaceBetweenChildren = 7F;

        // When
        FamilyTree familyTree = useCase.generateFamilyTree(head.getId(), widthOfMemberNode, spaceBetweenMemberAndSpouse, spaceBetweenChildren);

        FamilyTreeOfMember famOfHead = familyTree.getTree();
        Assertions.assertEquals(1, famOfHead.getFamily().size());
        Assertions.assertEquals(11 * spaceBetweenChildren / 2 + spaceBetweenMemberAndSpouse / 2 + 6 * widthOfMemberNode, famOfHead.getSubtreeLength());
    }

    @Test
    void testGenerateFamilyTreeOfMember_ParentTwoChildrenWithChildrenFamilyMultipleSpousesDominatingSpaceBetweenSpouses() {
        // Given
        Member head = MemberGenerator.createLivingMemberWithRequiredData();
        head.setId(1);

        Member spouse = MemberGenerator.createLivingMemberWithRequiredData();
        spouse.setId(2);

        Member child1 = MemberGenerator.createLivingMemberWithRequiredData();
        child1.setId(3);

        Member child2 = MemberGenerator.createLivingMemberWithRequiredData();
        child2.setId(4);

        Member child3 = MemberGenerator.createLivingMemberWithRequiredData();
        child3.setId(5);

        Member spouseOfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        spouseOfChild2.setId(6);

        Member child1OfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        child1OfChild2.setId(7);

        Member child2OfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        child2OfChild2.setId(8);

        Member spouse2OfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        spouse2OfChild2.setId(9);

        Member child3OfChild2 = MemberGenerator.createLivingMemberWithRequiredData();
        child3OfChild2.setId(10);

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(head.getId(), spouse.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child2.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child3.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child2.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child3.getId()),
                RelationshipGenerator.currentSpouseRelationship(child2.getId(), spouseOfChild2.getId()),
                RelationshipGenerator.parentRelationship(child2.getId(), child1OfChild2.getId()),
                RelationshipGenerator.parentRelationship(child2.getId(), child2OfChild2.getId()),
                RelationshipGenerator.parentRelationship(spouseOfChild2.getId(), child1OfChild2.getId()),
                RelationshipGenerator.parentRelationship(spouseOfChild2.getId(), child2OfChild2.getId()),
                RelationshipGenerator.exSpouseRelationship(child2.getId(), spouse2OfChild2.getId()),
                RelationshipGenerator.parentRelationship(child2.getId(), child3OfChild2.getId()),
                RelationshipGenerator.parentRelationship(spouse2OfChild2.getId(), child3OfChild2.getId())
        );

        Mockito.when(port.getFamilyTreeComponents(head.getId())).thenReturn(
                new FamilyTreeComponents(
                        List.of(head, spouse, child1, child2, child3, spouseOfChild2, child1OfChild2, child2OfChild2, spouse2OfChild2, child3OfChild2),
                        relationships
                )
        );

        float widthOfMemberNode = 1F;
        float spaceBetweenMemberAndSpouse = 13F;
        float spaceBetweenChildren = 1F;

        // When
        FamilyTree familyTree = useCase.generateFamilyTree(head.getId(), widthOfMemberNode, spaceBetweenMemberAndSpouse, spaceBetweenChildren);

        FamilyTreeOfMember famOfHead = familyTree.getTree();
        Assertions.assertEquals(1, famOfHead.getFamily().size());
        Assertions.assertEquals(5 * widthOfMemberNode + 7 * spaceBetweenChildren / 2 + 3 * spaceBetweenMemberAndSpouse / 2, famOfHead.getSubtreeLength());
    }
}
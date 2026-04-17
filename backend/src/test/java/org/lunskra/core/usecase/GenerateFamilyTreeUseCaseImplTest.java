package org.lunskra.core.usecase;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class GenerateFamilyTreeUseCaseImplTest {

    @Mock
    FamilyTreeRepositoryPort port;

    GenerateFamilyTreeUseCaseImpl useCase;

    // Fixed measures used across most tests
    static final float WIDTH = 1F;
    static final float SPACE_SPOUSE = 3F;
    static final float SPACE_CHILDREN = 7F;

    @BeforeEach
    void setup() {
        useCase = new GenerateFamilyTreeUseCaseImpl(port);
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Test
    void testGenerateFamilyTree_WhenMemberDoesNotExist_ThenThrowEntityNotFoundException() {
        // Given
        Mockito.when(port.getFamilyTreeComponents(999, null)).thenThrow(
                new EntityNotFoundException("Member with id 999 not found")
        );

        // When / Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> useCase.generateFamilyTree(999, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN)
        );
    }

    // -------------------------------------------------------------------------
    // subtreeLength — one family unit
    // -------------------------------------------------------------------------

    @Test
    void testSubtreeLength_HeadWithSpouseAndOneChild() {
        // Given
        Member head = member(1);
        Member spouse = member(2);
        Member child1 = member(3);

        mockComponents(head.getId(), null, List.of(head, spouse, child1), List.of(
                RelationshipGenerator.currentSpouseRelationship(head.getId(), spouse.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child1.getId())
        ));

        // When
        FamilyTree familyTree = useCase.generateFamilyTree(head.getId(), null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

        // Then
        FamilyTreeOfMember famOfHead = familyTree.getTree();
        Assertions.assertEquals(1, famOfHead.getFamily().size());
        // parent row = 2*1+3+7=12; child row = 1+7=8 → max = 12
        Assertions.assertEquals(SPACE_CHILDREN + 2 * WIDTH + SPACE_SPOUSE, famOfHead.getSubtreeLength());
    }

    @Test
    void testSubtreeLength_HeadWithSpouseAndTwoChildren() {
        // Given
        Member head = member(1);
        Member spouse = member(2);
        Member child1 = member(3);
        Member child2 = member(4);

        mockComponents(head.getId(), null, List.of(head, spouse, child1, child2), List.of(
                RelationshipGenerator.currentSpouseRelationship(head.getId(), spouse.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child2.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child2.getId())
        ));

        // When
        FamilyTree familyTree = useCase.generateFamilyTree(head.getId(), null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

        // Then
        FamilyTreeOfMember famOfHead = familyTree.getTree();
        Assertions.assertEquals(1, famOfHead.getFamily().size());
        // parent row = 12; child row = 2*(1+7)=16 → max = 16
        Assertions.assertEquals(2 * SPACE_CHILDREN + 2 * WIDTH, famOfHead.getSubtreeLength());

        FamilyUnit unit = famOfHead.getFamily().getFirst();
        Assertions.assertEquals(spouse.getId(), unit.getSpouseId());
        Assertions.assertEquals(2, unit.getChildren().size());

        FamilyTreeOfMember famOfChild1 = unit.getChildren().get(0);
        FamilyTreeOfMember famOfChild2 = unit.getChildren().get(1);

        Assertions.assertEquals(SPACE_CHILDREN + WIDTH, famOfChild1.getSubtreeLength());
        Assertions.assertEquals(SPACE_CHILDREN + WIDTH, famOfChild2.getSubtreeLength());
    }

    @Test
    void testSubtreeLength_HeadWithSpouseAndTwoChildrenWhereOneHasOwnFamily() {
        // Given
        Member head = member(1);
        Member spouse = member(2);
        Member child1 = member(3);
        Member child2 = member(4);
        Member child3 = member(5);
        Member spouseOfChild2 = member(6);
        Member grandchild1 = member(7);
        Member grandchild2 = member(8);

        mockComponents(head.getId(), null,
                List.of(head, spouse, child1, child2, child3, spouseOfChild2, grandchild1, grandchild2),
                List.of(
                        RelationshipGenerator.currentSpouseRelationship(head.getId(), spouse.getId()),
                        RelationshipGenerator.parentRelationship(head.getId(), child1.getId()),
                        RelationshipGenerator.parentRelationship(head.getId(), child2.getId()),
                        RelationshipGenerator.parentRelationship(head.getId(), child3.getId()),
                        RelationshipGenerator.parentRelationship(spouse.getId(), child1.getId()),
                        RelationshipGenerator.parentRelationship(spouse.getId(), child2.getId()),
                        RelationshipGenerator.parentRelationship(spouse.getId(), child3.getId()),
                        RelationshipGenerator.currentSpouseRelationship(child2.getId(), spouseOfChild2.getId()),
                        RelationshipGenerator.parentRelationship(child2.getId(), grandchild1.getId()),
                        RelationshipGenerator.parentRelationship(child2.getId(), grandchild2.getId()),
                        RelationshipGenerator.parentRelationship(spouseOfChild2.getId(), grandchild1.getId()),
                        RelationshipGenerator.parentRelationship(spouseOfChild2.getId(), grandchild2.getId())
                )
        );

        // When
        FamilyTree familyTree = useCase.generateFamilyTree(head.getId(), null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

        // Then: child row = child1 + child2 + child3
        // child1 = 1+7=8; child3 = 1+7=8; child2 has 2 grandchildren → 2*(1+7)=16 → max(12,16)=16
        // total = 8+16+8 = 32 = 4*(1+7)
        FamilyTreeOfMember famOfHead = familyTree.getTree();
        Assertions.assertEquals(1, famOfHead.getFamily().size());
        Assertions.assertEquals(4 * SPACE_CHILDREN + 4 * WIDTH, famOfHead.getSubtreeLength());
    }

    // -------------------------------------------------------------------------
    // subtreeLength — multiple family units
    // -------------------------------------------------------------------------

    @Test
    void testSubtreeLength_HeadWithTwoSpousesAndChildren_ChildrenDominate() {
        // Given – child widths dominate over the spouse slots
        Member head = member(1);
        Member spouse = member(2);
        Member child1 = member(3);
        Member child2 = member(4);
        Member child3 = member(5);
        Member spouseOfChild2 = member(6);
        Member grandchild1 = member(7);
        Member grandchild2 = member(8);
        Member spouse2OfChild2 = member(9);
        Member grandchild3 = member(10);

        List<Relationship> rels = twoSpouseRelationships(head, spouse, child1, child2, child3,
                spouseOfChild2, grandchild1, grandchild2, spouse2OfChild2, grandchild3);

        mockComponents(head.getId(), null,
                List.of(head, spouse, child1, child2, child3, spouseOfChild2, grandchild1, grandchild2, spouse2OfChild2, grandchild3),
                rels
        );

        // When
        FamilyTree familyTree = useCase.generateFamilyTree(head.getId(), null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

        // Then
        Assertions.assertEquals(1, familyTree.getTree().getFamily().size());
        Assertions.assertEquals(
                11 * SPACE_CHILDREN / 2 + SPACE_SPOUSE / 2 + 6 * WIDTH,
                familyTree.getTree().getSubtreeLength()
        );
    }

    @Test
    void testSubtreeLength_HeadWithTwoSpousesAndChildren_SpouseDominates() {
        // Given – large spaceBetweenMemberAndSpouse makes spouse slots wider than child rows
        Member head = member(1);
        Member spouse = member(2);
        Member child1 = member(3);
        Member child2 = member(4);
        Member child3 = member(5);
        Member spouseOfChild2 = member(6);
        Member grandchild1 = member(7);
        Member grandchild2 = member(8);
        Member spouse2OfChild2 = member(9);
        Member grandchild3 = member(10);

        List<Relationship> rels = twoSpouseRelationships(head, spouse, child1, child2, child3,
                spouseOfChild2, grandchild1, grandchild2, spouse2OfChild2, grandchild3);

        mockComponents(head.getId(), null,
                List.of(head, spouse, child1, child2, child3, spouseOfChild2, grandchild1, grandchild2, spouse2OfChild2, grandchild3),
                rels
        );

        float largeSpouseSpace = 13F;
        float smallChildSpace = 1F;

        // When
        FamilyTree familyTree = useCase.generateFamilyTree(head.getId(), null, WIDTH, largeSpouseSpace, smallChildSpace);

        // Then
        Assertions.assertEquals(
                5 * WIDTH + 7 * smallChildSpace / 2 + 3 * largeSpouseSpace / 2,
                familyTree.getTree().getSubtreeLength()
        );
    }

    // -------------------------------------------------------------------------
    // maxDepth — port delegation and tree structure
    // -------------------------------------------------------------------------

    @Test
    void testMaxDepth_PortIsCalledWithSpecifiedDepth() {
        // Given
        Member head = member(1);
        Member spouse = member(2);
        Member child1 = member(3);
        Member child2 = member(4);

        mockComponents(head.getId(), 1, List.of(head, spouse, child1, child2), List.of(
                RelationshipGenerator.currentSpouseRelationship(head.getId(), spouse.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child2.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child2.getId())
        ));

        // When
        useCase.generateFamilyTree(head.getId(), 1, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

        // Then
        Mockito.verify(port).getFamilyTreeComponents(head.getId(), 1);
    }

    // -------------------------------------------------------------------------
    // Counts — numberTotal
    // -------------------------------------------------------------------------

    @Nested
    class NumberTotal {

        @Test
        void headOnly() {
            mockComponents(1, null, List.of(member(1)), List.of());

            FamilyTree tree = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            Assertions.assertEquals(1, tree.getNumberTotal());
        }

        @Test
        void headWithSpouseNoChildren() {
            Member head = member(1);
            Member spouse = member(2);

            mockComponents(1, null, List.of(head, spouse), List.of(
                    RelationshipGenerator.currentSpouseRelationship(1, 2)
            ));

            FamilyTree tree = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            Assertions.assertEquals(2, tree.getNumberTotal());
        }

        @Test
        void headWithSpouseAndTwoChildren() {
            Member head = member(1);
            Member spouse = member(2);
            Member child1 = member(3);
            Member child2 = member(4);

            mockComponents(1, null, List.of(head, spouse, child1, child2), List.of(
                    RelationshipGenerator.currentSpouseRelationship(1, 2),
                    RelationshipGenerator.parentRelationship(1, 3),
                    RelationshipGenerator.parentRelationship(1, 4),
                    RelationshipGenerator.parentRelationship(2, 3),
                    RelationshipGenerator.parentRelationship(2, 4)
            ));

            FamilyTree tree = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            Assertions.assertEquals(4, tree.getNumberTotal());
        }

        @Test
        void headWithCurrentSpouseAndExSpouseWithChild_CountsBothSpouses() {
            // ex-spouse is included only because they share a child
            Member head = member(1);
            Member currentSpouse = member(2);
            Member exSpouse = member(3);
            Member childOfEx = member(4);

            mockComponents(1, null, List.of(head, currentSpouse, exSpouse, childOfEx), List.of(
                    RelationshipGenerator.currentSpouseRelationship(1, 2),
                    RelationshipGenerator.exSpouseRelationship(1, 3),
                    RelationshipGenerator.parentRelationship(1, 4),
                    RelationshipGenerator.parentRelationship(3, 4)
            ));

            FamilyTree tree = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            // head + currentSpouse + exSpouse + childOfEx
            Assertions.assertEquals(4, tree.getNumberTotal());
        }
    }

    // -------------------------------------------------------------------------
    // Counts — numberLiving
    // -------------------------------------------------------------------------

    @Nested
    class NumberLiving {

        @Test
        void allMembersLiving() {
            Member head = member(1);
            Member spouse = member(2);
            Member child = member(3);

            mockComponents(1, null, List.of(head, spouse, child), List.of(
                    RelationshipGenerator.currentSpouseRelationship(1, 2),
                    RelationshipGenerator.parentRelationship(1, 3),
                    RelationshipGenerator.parentRelationship(2, 3)
            ));

            FamilyTree tree = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            Assertions.assertEquals(3, tree.getNumberLiving());
        }

        @Test
        void deceasedHead_IsNotCountedAsLiving() {
            Member head = deceasedMember(1);
            Member spouse = member(2);

            mockComponents(1, null, List.of(head, spouse), List.of(
                    RelationshipGenerator.currentSpouseRelationship(1, 2)
            ));

            FamilyTree tree = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            Assertions.assertEquals(2, tree.getNumberTotal());
            Assertions.assertEquals(1, tree.getNumberLiving());
        }

        @Test
        void deceasedSpouse_IsNotCountedAsLiving() {
            Member head = member(1);
            Member spouse = deceasedMember(2);

            mockComponents(1, null, List.of(head, spouse), List.of(
                    RelationshipGenerator.currentSpouseRelationship(1, 2)
            ));

            FamilyTree tree = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            Assertions.assertEquals(2, tree.getNumberTotal());
            Assertions.assertEquals(1, tree.getNumberLiving());
        }

        @Test
        void deceasedChild_IsNotCountedAsLiving() {
            Member head = member(1);
            Member spouse = member(2);
            Member livingChild = member(3);
            Member deceasedChild = deceasedMember(4);

            mockComponents(1, null, List.of(head, spouse, livingChild, deceasedChild), List.of(
                    RelationshipGenerator.currentSpouseRelationship(1, 2),
                    RelationshipGenerator.parentRelationship(1, 3),
                    RelationshipGenerator.parentRelationship(1, 4),
                    RelationshipGenerator.parentRelationship(2, 3),
                    RelationshipGenerator.parentRelationship(2, 4)
            ));

            FamilyTree tree = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            Assertions.assertEquals(4, tree.getNumberTotal());
            Assertions.assertEquals(3, tree.getNumberLiving());
        }
    }

    // -------------------------------------------------------------------------
    // Counts — numberGenerations and generation field
    // -------------------------------------------------------------------------

    @Nested
    class NumberGenerations {

        @Test
        void headOnly_OneGeneration() {
            mockComponents(1, null, List.of(member(1)), List.of());

            FamilyTree tree = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            Assertions.assertEquals(1, tree.getNumberGenerations());
            Assertions.assertEquals(1, tree.getTree().getGeneration());
        }

        @Test
        void headWithSpouseAndChild_TwoGenerations() {
            mockComponents(1, null, List.of(member(1), member(2), member(3)), List.of(
                    RelationshipGenerator.currentSpouseRelationship(1, 2),
                    RelationshipGenerator.parentRelationship(1, 3),
                    RelationshipGenerator.parentRelationship(2, 3)
            ));

            FamilyTree tree = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            Assertions.assertEquals(2, tree.getNumberGenerations());
            // Spouse shares generation with the head (generation field only lives on tree nodes)
            Assertions.assertEquals(1, tree.getTree().getGeneration());
            FamilyTreeOfMember child = tree.getTree().getFamily().getFirst().getChildren().getFirst();
            Assertions.assertEquals(2, child.getGeneration());
        }

        @Test
        void threeGenerations() {
            // head → child → grandchild
            mockComponents(1, null, List.of(member(1), member(2), member(3), member(4)), List.of(
                    RelationshipGenerator.currentSpouseRelationship(1, 2),
                    RelationshipGenerator.parentRelationship(1, 3),
                    RelationshipGenerator.parentRelationship(2, 3),
                    RelationshipGenerator.currentSpouseRelationship(3, 4)
            ));

            FamilyTree tree = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            // child (id=3) has spouse (id=4) but no shared children → still generation 2
            Assertions.assertEquals(2, tree.getNumberGenerations());

            // Extend: add a grandchild under child and their spouse
            mockComponents(1, null, List.of(member(1), member(2), member(3), member(4), member(5)), List.of(
                    RelationshipGenerator.currentSpouseRelationship(1, 2),
                    RelationshipGenerator.parentRelationship(1, 3),
                    RelationshipGenerator.parentRelationship(2, 3),
                    RelationshipGenerator.currentSpouseRelationship(3, 4),
                    RelationshipGenerator.parentRelationship(3, 5),
                    RelationshipGenerator.parentRelationship(4, 5)
            ));

            FamilyTree tree2 = useCase.generateFamilyTree(1, null, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            Assertions.assertEquals(3, tree2.getNumberGenerations());
            FamilyTreeOfMember grandchild = tree2.getTree()
                    .getFamily().getFirst()  // family unit of head
                    .getChildren().getFirst() // child (id=3)
                    .getFamily().getFirst()   // family unit of child
                    .getChildren().getFirst();// grandchild (id=5)
            Assertions.assertEquals(3, grandchild.getGeneration());
        }

        @Test
        void maxDepthLargerThanActualDepth_UsesActualDepth() {
            // Actual tree has 2 generations, but caller said maxDepth=5
            mockComponents(1, 5, List.of(member(1), member(2), member(3)), List.of(
                    RelationshipGenerator.currentSpouseRelationship(1, 2),
                    RelationshipGenerator.parentRelationship(1, 3),
                    RelationshipGenerator.parentRelationship(2, 3)
            ));

            FamilyTree tree = useCase.generateFamilyTree(1, 5, WIDTH, SPACE_SPOUSE, SPACE_CHILDREN);

            Assertions.assertEquals(2, tree.getNumberGenerations());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Member member(int id) {
        Member m = MemberGenerator.createLivingMemberWithRequiredData();
        m.setId(id);
        return m;
    }

    private Member deceasedMember(int id) {
        Member m = member(id);
        m.setDeathDate(LocalDate.of(2000, 1, 1));
        return m;
    }

    private void mockComponents(int headId, Integer maxDepth, List<Member> members, List<Relationship> relationships) {
        Mockito.when(port.getFamilyTreeComponents(headId, maxDepth))
                .thenReturn(new FamilyTreeComponents(members, relationships));
    }

    /**
     * Builds the relationship list for the two-spouse scenario used in the
     * subtreeLength multiple-families tests: head with one current spouse and three
     * children, where the second child has two spouses of their own each with children.
     */
    private List<Relationship> twoSpouseRelationships(
            Member head, Member spouse,
            Member child1, Member child2, Member child3,
            Member spouseOfChild2, Member grandchild1, Member grandchild2,
            Member spouse2OfChild2, Member grandchild3
    ) {
        return List.of(
                RelationshipGenerator.currentSpouseRelationship(head.getId(), spouse.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child2.getId()),
                RelationshipGenerator.parentRelationship(head.getId(), child3.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child1.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child2.getId()),
                RelationshipGenerator.parentRelationship(spouse.getId(), child3.getId()),
                RelationshipGenerator.currentSpouseRelationship(child2.getId(), spouseOfChild2.getId()),
                RelationshipGenerator.parentRelationship(child2.getId(), grandchild1.getId()),
                RelationshipGenerator.parentRelationship(child2.getId(), grandchild2.getId()),
                RelationshipGenerator.parentRelationship(spouseOfChild2.getId(), grandchild1.getId()),
                RelationshipGenerator.parentRelationship(spouseOfChild2.getId(), grandchild2.getId()),
                RelationshipGenerator.exSpouseRelationship(child2.getId(), spouse2OfChild2.getId()),
                RelationshipGenerator.parentRelationship(child2.getId(), grandchild3.getId()),
                RelationshipGenerator.parentRelationship(spouse2OfChild2.getId(), grandchild3.getId())
        );
    }
}

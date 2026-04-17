package org.lunskra.core.usecase;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.adapter.utils.MemberGenerator;
import org.lunskra.core.domain.FamilyTreeAncestor;
import org.lunskra.core.domain.FamilyTreeAncestorOfMember;
import org.lunskra.core.domain.FamilyTreeComponents;
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
class GenerateFamilyTreeAncestorUseCaseImplTest {

    @Mock
    FamilyTreeRepositoryPort port;

    GenerateFamilyTreeAncestorUseCaseImpl useCase;

    // Fixed measures used across most tests
    static final float WIDTH = 1F;
    static final float SPACE_MEMBERS = 3F;

    @BeforeEach
    void setup() {
        useCase = new GenerateFamilyTreeAncestorUseCaseImpl(port);
    }

    // Tree used across most tests:
    //
    //         20          21,22
    //          \           /
    //     16,17(ex)    18,19(current)
    //           \       /
    //           14,15(married)
    //                |
    //                1
    //
    //  1  – living
    // 14  – living,  15 – dead:  married, parents of 1
    // 16  – living,  17 – living: ex-spouses, parents of 14
    // 20  – dead:    single parent of 17
    // 18  – living,  19 – dead:  current spouses, parents of 15
    // 21  – dead,    22 – dead:  married, parents of 19

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Test
    void testGenerateFamilyTreeAncestor_WhenMemberDoesNotExist_ThenThrowEntityNotFoundException() {
        // Given
        Mockito.when(port.getFamilyTreeAncestorComponents(999, null)).thenThrow(
                new EntityNotFoundException("Member with id 999 not found")
        );

        // When / Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> useCase.generateFamilyTreeAncestor(999, null, WIDTH, SPACE_MEMBERS)
        );
    }

    // -------------------------------------------------------------------------
    // subtreeLength
    // -------------------------------------------------------------------------

    @Test
    void testSubtreeLength_FullTree_FiveLeafPaths() {
        // Given – childId=1, maxDepth=null
        // Leaf paths: 16, 20→17, 18, 21, 22 → each contributes (WIDTH + SPACE_MEMBERS)
        mockFullTree();

        // When
        FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, null, WIDTH, SPACE_MEMBERS);

        // Then: 5 leaf paths × (1 + 3) = 20
        Assertions.assertEquals(5 * WIDTH + 5 * SPACE_MEMBERS, result.getTree().getSubtreeLength());
    }

    @Test
    void testSubtreeLength_MaxDepthOne_TwoLeafParents() {
        // Given – childId=1, maxDepth=1 → only parents 14 and 15 (both leaves at this depth)
        mockComponents(1, 1,
                List.of(member(1), member(14), deceasedMember(15)),
                List.of(
                        RelationshipGenerator.parentRelationship(14, 1),
                        RelationshipGenerator.parentRelationship(15, 1),
                        RelationshipGenerator.marriedSpouseRelationship(14, 15)
                )
        );

        // When
        FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, 1, WIDTH, SPACE_MEMBERS);

        // Then: 2 leaf parents × (1 + 3) = 8
        Assertions.assertEquals(2 * WIDTH + 2 * SPACE_MEMBERS, result.getTree().getSubtreeLength());
    }

    @Test
    void testSubtreeLength_MaxDepthTwo_FourLeafPaths() {
        // Given – childId=1, maxDepth=2 → 16, 17, 18, 19 are leaves (20, 21, 22 excluded)
        mockComponents(1, 2,
                List.of(member(1), member(14), deceasedMember(15), member(16), member(17), member(18), deceasedMember(19)),
                List.of(
                        RelationshipGenerator.parentRelationship(14, 1),
                        RelationshipGenerator.parentRelationship(15, 1),
                        RelationshipGenerator.marriedSpouseRelationship(14, 15),
                        RelationshipGenerator.parentRelationship(16, 14),
                        RelationshipGenerator.parentRelationship(17, 14),
                        RelationshipGenerator.exSpouseRelationship(16, 17),
                        RelationshipGenerator.parentRelationship(18, 15),
                        RelationshipGenerator.parentRelationship(19, 15),
                        RelationshipGenerator.currentSpouseRelationship(18, 19)
                )
        );

        // When
        FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, 2, WIDTH, SPACE_MEMBERS);

        // Then: 4 leaf paths × (1 + 3) = 16
        Assertions.assertEquals(4 * WIDTH + 4 * SPACE_MEMBERS, result.getTree().getSubtreeLength());
    }

    @Test
    void testSubtreeLength_StartFromMidTreeMember_SingleParent() {
        // Given – childId=17, maxDepth=null → only member 17 and its single parent 20
        mockComponents(17, null,
                List.of(member(17), deceasedMember(20)),
                List.of(
                        RelationshipGenerator.parentRelationship(20, 17)
                )
        );

        // When
        FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(17, null, WIDTH, SPACE_MEMBERS);

        // Then: 1 leaf path × (1 + 3) = 4
        Assertions.assertEquals(WIDTH + SPACE_MEMBERS, result.getTree().getSubtreeLength());
    }

    // -------------------------------------------------------------------------
    // maxDepth — port delegation
    // -------------------------------------------------------------------------

    @Test
    void testMaxDepth_PortIsCalledWithSpecifiedDepth() {
        // Given
        mockComponents(1, 2,
                List.of(member(1), member(14), deceasedMember(15)),
                List.of(
                        RelationshipGenerator.parentRelationship(14, 1),
                        RelationshipGenerator.parentRelationship(15, 1),
                        RelationshipGenerator.marriedSpouseRelationship(14, 15)
                )
        );

        // When
        useCase.generateFamilyTreeAncestor(1, 2, WIDTH, SPACE_MEMBERS);

        // Then
        Mockito.verify(port).getFamilyTreeAncestorComponents(1, 2);
    }

    // -------------------------------------------------------------------------
    // Counts — numberTotal
    // -------------------------------------------------------------------------

    @Nested
    class NumberTotal {

        @Test
        void fullTree_TenMembers() {
            mockFullTree();

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, null, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(10, result.getNumberTotal());
        }

        @Test
        void maxDepthOne_ThreeMembers() {
            mockComponents(1, 1,
                    List.of(member(1), member(14), deceasedMember(15)),
                    List.of(
                            RelationshipGenerator.parentRelationship(14, 1),
                            RelationshipGenerator.parentRelationship(15, 1),
                            RelationshipGenerator.marriedSpouseRelationship(14, 15)
                    )
            );

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, 1, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(3, result.getNumberTotal());
        }

        @Test
        void maxDepthTwo_SevenMembers() {
            mockComponents(1, 2,
                    List.of(member(1), member(14), deceasedMember(15), member(16), member(17), member(18), deceasedMember(19)),
                    List.of(
                            RelationshipGenerator.parentRelationship(14, 1),
                            RelationshipGenerator.parentRelationship(15, 1),
                            RelationshipGenerator.marriedSpouseRelationship(14, 15),
                            RelationshipGenerator.parentRelationship(16, 14),
                            RelationshipGenerator.parentRelationship(17, 14),
                            RelationshipGenerator.exSpouseRelationship(16, 17),
                            RelationshipGenerator.parentRelationship(18, 15),
                            RelationshipGenerator.parentRelationship(19, 15),
                            RelationshipGenerator.currentSpouseRelationship(18, 19)
                    )
            );

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, 2, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(7, result.getNumberTotal());
        }

        @Test
        void startFromMidTreeMember_TwoMembers() {
            mockComponents(17, null,
                    List.of(member(17), deceasedMember(20)),
                    List.of(
                            RelationshipGenerator.parentRelationship(20, 17)
                    )
            );

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(17, null, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(2, result.getNumberTotal());
        }
    }

    // -------------------------------------------------------------------------
    // Counts — numberLiving
    // -------------------------------------------------------------------------

    @Nested
    class NumberLiving {

        @Test
        void fullTree_FiveLivingMembers() {
            // Living: 1, 14, 16, 17, 18  — Dead: 15, 19, 20, 21, 22
            mockFullTree();

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, null, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(5, result.getNumberLiving());
        }

        @Test
        void maxDepthOne_TwoLivingMembers() {
            // Living: 1, 14  — Dead: 15
            mockComponents(1, 1,
                    List.of(member(1), member(14), deceasedMember(15)),
                    List.of(
                            RelationshipGenerator.parentRelationship(14, 1),
                            RelationshipGenerator.parentRelationship(15, 1),
                            RelationshipGenerator.marriedSpouseRelationship(14, 15)
                    )
            );

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, 1, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(2, result.getNumberLiving());
        }

        @Test
        void maxDepthTwo_FiveLivingMembers() {
            // Living: 1, 14, 16, 17, 18  — Dead: 15, 19
            mockComponents(1, 2,
                    List.of(member(1), member(14), deceasedMember(15), member(16), member(17), member(18), deceasedMember(19)),
                    List.of(
                            RelationshipGenerator.parentRelationship(14, 1),
                            RelationshipGenerator.parentRelationship(15, 1),
                            RelationshipGenerator.marriedSpouseRelationship(14, 15),
                            RelationshipGenerator.parentRelationship(16, 14),
                            RelationshipGenerator.parentRelationship(17, 14),
                            RelationshipGenerator.exSpouseRelationship(16, 17),
                            RelationshipGenerator.parentRelationship(18, 15),
                            RelationshipGenerator.parentRelationship(19, 15),
                            RelationshipGenerator.currentSpouseRelationship(18, 19)
                    )
            );

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, 2, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(5, result.getNumberLiving());
        }

        @Test
        void startFromMidTreeMember_OneLivingMember() {
            // Living: 17  — Dead: 20
            mockComponents(17, null,
                    List.of(member(17), deceasedMember(20)),
                    List.of(
                            RelationshipGenerator.parentRelationship(20, 17)
                    )
            );

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(17, null, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(1, result.getNumberLiving());
        }
    }

    // -------------------------------------------------------------------------
    // Counts — numberGenerations and generation field
    // -------------------------------------------------------------------------

    @Nested
    class NumberGenerations {

        @Test
        void fullTree_FourGenerations() {
            // Gen 1: 1 — Gen 2: 14, 15 — Gen 3: 16, 17, 18, 19 — Gen 4: 20, 21, 22
            mockFullTree();

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, null, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(4, result.getNumberGenerations());
        }

        @Test
        void maxDepthOne_TwoGenerations() {
            mockComponents(1, 1,
                    List.of(member(1), member(14), deceasedMember(15)),
                    List.of(
                            RelationshipGenerator.parentRelationship(14, 1),
                            RelationshipGenerator.parentRelationship(15, 1),
                            RelationshipGenerator.marriedSpouseRelationship(14, 15)
                    )
            );

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, 1, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(2, result.getNumberGenerations());
            // Root is generation 1, parents are generation 2
            Assertions.assertEquals(1, result.getTree().getGeneration());
            List<FamilyTreeAncestorOfMember> parents = result.getTree().getParents().getParents();
            Assertions.assertTrue(parents.stream().allMatch(p -> p.getGeneration() == 2));
        }

        @Test
        void maxDepthTwo_ThreeGenerations() {
            mockComponents(1, 2,
                    List.of(member(1), member(14), deceasedMember(15), member(16), member(17), member(18), deceasedMember(19)),
                    List.of(
                            RelationshipGenerator.parentRelationship(14, 1),
                            RelationshipGenerator.parentRelationship(15, 1),
                            RelationshipGenerator.marriedSpouseRelationship(14, 15),
                            RelationshipGenerator.parentRelationship(16, 14),
                            RelationshipGenerator.parentRelationship(17, 14),
                            RelationshipGenerator.exSpouseRelationship(16, 17),
                            RelationshipGenerator.parentRelationship(18, 15),
                            RelationshipGenerator.parentRelationship(19, 15),
                            RelationshipGenerator.currentSpouseRelationship(18, 19)
                    )
            );

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(1, 2, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(3, result.getNumberGenerations());
        }

        @Test
        void startFromMidTreeMember_TwoGenerations() {
            mockComponents(17, null,
                    List.of(member(17), deceasedMember(20)),
                    List.of(
                            RelationshipGenerator.parentRelationship(20, 17)
                    )
            );

            FamilyTreeAncestor result = useCase.generateFamilyTreeAncestor(17, null, WIDTH, SPACE_MEMBERS);

            Assertions.assertEquals(2, result.getNumberGenerations());
            Assertions.assertEquals(1, result.getTree().getGeneration());
            Assertions.assertEquals(2, result.getTree().getParents().getParents().getFirst().getGeneration());
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

    private void mockComponents(int childId, Integer maxDepth, List<Member> members, List<Relationship> relationships) {
        Mockito.when(port.getFamilyTreeAncestorComponents(childId, maxDepth))
                .thenReturn(new FamilyTreeComponents(members, relationships));
    }

    /**
     * Mocks the full ancestor tree rooted at member 1:
     *
     *         20          21,22
     *          \           /
     *     16,17(ex)    18,19(current)
     *           \       /
     *           14,15(married)
     *                |
     *                1
     */
    private void mockFullTree() {
        mockComponents(1, null,
                List.of(
                        member(1),
                        member(14), deceasedMember(15),
                        member(16), member(17),
                        deceasedMember(20),
                        member(18), deceasedMember(19),
                        deceasedMember(21), deceasedMember(22)
                ),
                List.of(
                        RelationshipGenerator.parentRelationship(14, 1),
                        RelationshipGenerator.parentRelationship(15, 1),
                        RelationshipGenerator.marriedSpouseRelationship(14, 15),
                        RelationshipGenerator.parentRelationship(16, 14),
                        RelationshipGenerator.parentRelationship(17, 14),
                        RelationshipGenerator.exSpouseRelationship(16, 17),
                        RelationshipGenerator.parentRelationship(20, 17),
                        RelationshipGenerator.parentRelationship(18, 15),
                        RelationshipGenerator.parentRelationship(19, 15),
                        RelationshipGenerator.currentSpouseRelationship(18, 19),
                        RelationshipGenerator.parentRelationship(21, 19),
                        RelationshipGenerator.parentRelationship(22, 19),
                        RelationshipGenerator.marriedSpouseRelationship(21, 22)
                )
        );
    }
}

package org.lunskra.core.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;
import org.lunskra.core.utils.RelationshipGenerator;

import java.util.List;
import java.util.Map;
import java.util.Set;

class RelationshipFinderTest {

    // -------------------------------------------------------------------------
    // findSpousesWithChildren
    // -------------------------------------------------------------------------

    @Test
    void findSpousesWithChildren_SpouseHasSharedChildren_SpousesUnrelatedChildNotIncluded() {
        // Given
        int memberId = 1;
        int spouseId = 2;
        int child1 = 3;
        int child2 = 4;
        int childOtherRel = 5;

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(memberId, spouseId),
                RelationshipGenerator.parentRelationship(memberId, child1),
                RelationshipGenerator.parentRelationship(memberId, child2),
                RelationshipGenerator.parentRelationship(spouseId, child1),
                RelationshipGenerator.parentRelationship(spouseId, child2),
                RelationshipGenerator.parentRelationship(spouseId, childOtherRel)
        );

        RelationshipFinder.Spouse spouse = new RelationshipFinder.Spouse(spouseId, RelationshipType.CURRENT_SPOUSE);

        // When
        Map<RelationshipFinder.Spouse, List<Integer>> result =
                RelationshipFinder.findSpousesWithChildren(memberId, relationships);

        List<Integer> children = result.get(spouse);

        // Then
        Assertions.assertTrue(children.contains(child1));
        Assertions.assertTrue(children.contains(child2));
        System.out.println(children);
        Assertions.assertFalse(children.contains(childOtherRel));

    }

    @Test
    void findSpousesWithChildren_MemberHasNoSpouse_ReturnsEmptyMap() {
        // Given
        int member = 1;
        int othermember = 2;
        int spouseOfOtherMember = 3;
        int childOfOtherCouple = 4;

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(othermember, spouseOfOtherMember),
                RelationshipGenerator.parentRelationship(othermember, childOfOtherCouple),
                RelationshipGenerator.parentRelationship(spouseOfOtherMember, childOfOtherCouple)
        );

        // When
        Map<RelationshipFinder.Spouse, List<Integer>> result =
                RelationshipFinder.findSpousesWithChildren(member, relationships);

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findSpousesWithChildren_MultipleSpousesEachWithChildren() {
        // Given
        int member = 1;
        int currentSpouse = 2;
        int exSpouse = 3;
        int childOfCurrent1 = 4;
        int childOfCurrent2 = 5;
        int childOfEx = 6;

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(member, currentSpouse),
                RelationshipGenerator.exSpouseRelationship(member, exSpouse),
                RelationshipGenerator.parentRelationship(member, childOfCurrent1),
                RelationshipGenerator.parentRelationship(member, childOfCurrent2),
                RelationshipGenerator.parentRelationship(member, childOfEx),
                RelationshipGenerator.parentRelationship(currentSpouse, childOfCurrent1),
                RelationshipGenerator.parentRelationship(currentSpouse, childOfCurrent2),
                RelationshipGenerator.parentRelationship(exSpouse, childOfEx)
        );

        RelationshipFinder.Spouse spouseCurrent = new RelationshipFinder.Spouse(currentSpouse, RelationshipType.CURRENT_SPOUSE);
        RelationshipFinder.Spouse spouseEx = new RelationshipFinder.Spouse(exSpouse, RelationshipType.EX_SPOUSE);

        // When
        Map<RelationshipFinder.Spouse, List<Integer>> result =
                RelationshipFinder.findSpousesWithChildren(member, relationships);

        // Then
        Assertions.assertNotNull(result.get(spouseCurrent));
        Assertions.assertTrue(result.get(spouseCurrent).contains(childOfCurrent1));
        Assertions.assertTrue(result.get(spouseCurrent).contains(childOfCurrent2));

        Assertions.assertNotNull(result.get(spouseEx));
        Assertions.assertTrue(result.get(spouseEx).contains(childOfEx));
    }

    @Test
    void findSpousesWithChildren_ExSpouseWithoutChildren_IsExcluded() {
        // Given
        int member = 1;
        int exSpouse = 2;

        List<Relationship> relationships = List.of(
                RelationshipGenerator.exSpouseRelationship(member, exSpouse)
        );

        RelationshipFinder.Spouse spouse = new RelationshipFinder.Spouse(exSpouse, RelationshipType.EX_SPOUSE);

        // When
        Map<RelationshipFinder.Spouse, List<Integer>> result =
                RelationshipFinder.findSpousesWithChildren(member, relationships);

        // Then
        Assertions.assertFalse(result.containsKey(spouse));
    }

    @Test
    void findSpousesWithChildren_CurrentSpouseNoChildren_ExSpouseWithChildren() {
        // Given
        int member = 1;
        int currentSpouse = 2;
        int exSpouse = 3;
        int childEx1 = 4;
        int childEx2 = 5;

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(member, currentSpouse),
                RelationshipGenerator.exSpouseRelationship(member, exSpouse),
                RelationshipGenerator.parentRelationship(member, childEx1),
                RelationshipGenerator.parentRelationship(member, childEx2),
                RelationshipGenerator.parentRelationship(exSpouse, childEx1),
                RelationshipGenerator.parentRelationship(exSpouse, childEx1)
        );

        // When
        Map<RelationshipFinder.Spouse, List<Integer>> result =
                RelationshipFinder.findSpousesWithChildren(member, relationships);

        RelationshipFinder.Spouse spouseCurrent = new RelationshipFinder.Spouse(currentSpouse, RelationshipType.CURRENT_SPOUSE);
        RelationshipFinder.Spouse spouseEx = new RelationshipFinder.Spouse(exSpouse, RelationshipType.EX_SPOUSE);

        // Then
        Assertions.assertTrue(result.containsKey(spouseCurrent));
        Assertions.assertTrue(result.get(spouseCurrent).isEmpty());
        Assertions.assertTrue(result.containsKey(spouseEx));
    }

    @Test
    void findSpousesWithChildren_MemberHasChildrenButNoSpouseInList_ReturnsEmptyMap() {
        // Given
        int member = 1;
        int child1 = 2;
        int child2 = 3;

        List<Relationship> relationships = List.of(
                RelationshipGenerator.parentRelationship(member, child1),
                RelationshipGenerator.parentRelationship(member, child2)
            );

        // When
        Map<RelationshipFinder.Spouse, List<Integer>> result =
                RelationshipFinder.findSpousesWithChildren(member, relationships);

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // findChildren
    // -------------------------------------------------------------------------

    @Test
    void findChildren_BothParentsListed_ReturnsSharedChildren() {
        // Given
        int member = 1;
        int spouse = 2;
        int child1 = 3;
        int child2 = 4;

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(member, spouse),
                RelationshipGenerator.parentRelationship(member, child1),
                RelationshipGenerator.parentRelationship(member, child2),
                RelationshipGenerator.parentRelationship(spouse, child1),
                RelationshipGenerator.parentRelationship(spouse, child2)
        );

        // When
        List<Integer> result = RelationshipFinder.findChildren(member, spouse, relationships);

        // Then
        Assertions.assertTrue(result.contains(child1));
        Assertions.assertTrue(result.contains(child2));
    }

    @Test
    void findChildren_OrderDoesNotMatter() {
        // Given
        int member = 1;
        int spouse = 2;
        int child1 = 3;
        int child2 = 4;

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(member, spouse),
                RelationshipGenerator.parentRelationship(member, child1),
                RelationshipGenerator.parentRelationship(spouse, child2),
                RelationshipGenerator.parentRelationship(spouse, child2)
        );

        // When
        List<Integer> result = RelationshipFinder.findChildren(member, spouse, relationships);
        List<Integer> resultOrderSwitch = RelationshipFinder.findChildren(spouse, member, relationships);

        // Then
        Assertions.assertEquals(Set.of(result), Set.of(resultOrderSwitch));
    }

    @Test
    void findChildren_SpouseHasExtraChild_ExtraChildDoesNotAppearInResult() {
        // Given
        int member = 1;
        int spouse = 2;
        int spouseOfSpouse = 3;
        int child1 = 4;
        int child2 = 5;
        int childSpouseSpouse = 6;

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(member, spouse),
                RelationshipGenerator.exSpouseRelationship(spouse, spouseOfSpouse),
                RelationshipGenerator.parentRelationship(member, child1),
                RelationshipGenerator.parentRelationship(member, child2),
                RelationshipGenerator.parentRelationship(spouse, child2),
                RelationshipGenerator.parentRelationship(spouse, child2),
                RelationshipGenerator.parentRelationship(spouse, childSpouseSpouse),
                RelationshipGenerator.parentRelationship(spouseOfSpouse, childSpouseSpouse)
        );

        // When
        List<Integer> result = RelationshipFinder.findChildren(member, spouse, relationships);

        // Then
        Assertions.assertFalse(result.contains(childSpouseSpouse));
    }

    @Test
    void findChildren_NoSharedChildren_ReturnsEmptyList() {
        // Given
        int member = 1;
        int spouse = 2;
        int exSpouseOfSpouse = 3;
        int exSpouseOfMember = 4;
        int childExSpouse = 5;
        int childSpouseSpouse = 6;

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(member, spouse),
                RelationshipGenerator.exSpouseRelationship(spouse, exSpouseOfSpouse),
                RelationshipGenerator.exSpouseRelationship(member, exSpouseOfMember),
                RelationshipGenerator.parentRelationship(member, childExSpouse),
                RelationshipGenerator.parentRelationship(exSpouseOfMember, childExSpouse),
                RelationshipGenerator.parentRelationship(spouse, childSpouseSpouse),
                RelationshipGenerator.parentRelationship(exSpouseOfSpouse, childSpouseSpouse)
        );

        // When
        List<Integer> result = RelationshipFinder.findChildren(member, spouse, relationships);

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findChildren_MembersNotInRelationships_ReturnsEmptyList() {
        // Given
        int member = 1;
        int spouse = 2;
        int child1 = 3;
        int child2 = 4;

        List<Relationship> relationships = List.of(
                RelationshipGenerator.currentSpouseRelationship(member, spouse),
                RelationshipGenerator.parentRelationship(member, child1),
                RelationshipGenerator.parentRelationship(spouse, child2),
                RelationshipGenerator.parentRelationship(spouse, child2)
        );

        int memberNotInList = 100;
        int SpouseNotInList = 101;

        // When
        List<Integer> result = RelationshipFinder.findChildren(memberNotInList, SpouseNotInList, relationships);

        // Then
        Assertions.assertTrue(result.isEmpty());
    }
}

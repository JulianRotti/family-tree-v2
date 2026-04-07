package org.lunskra.adapter.persistence.repository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lunskra.adapter.persistence.testcontainer.MySQLTestContainerResource;
import org.lunskra.core.domain.FamilyTreeComponents;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusTest
@QuarkusTestResource(value = MySQLTestContainerResource.class, restrictToAnnotatedClass = true)
class FamilyTreeRepositoryPortImplTest {

    @Inject
    FamilyTreeRepositoryPortImpl port;

    @DisplayName("getFamilyTreeComponents: throws EntityNotFound if member does not exist")
    @Test
    void getFamilyTreeComponents_whenMemberDoesNotExist_thenThrowEntityNotFound() {
        // Given
        int nonExistingId = 999;

        // When & Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> port.getFamilyTreeComponents(nonExistingId)
        );
    }

    @DisplayName("getFamilyTreeComponents: returns one member and no relationships when member has no relationships")
    @Test
    void getFamilyTreeComponents_whenMemberHasNoRelationships_thenReturnSingleMemberAndEmptyRelationships() {
        // given
        // pick a member that is isolated (adjust if needed)
        int memberId = 13;

        // when
        FamilyTreeComponents result = port.getFamilyTreeComponents(memberId);

        // then
        assertNotNull(result);
        assertEquals(1, result.members().size());
        assertTrue(result.relationships().isEmpty());
        assertEquals(memberId, result.members().getFirst().getId());
    }

    @DisplayName("getFamilyTreeComponents: returns full exact family tree for Kenji Horn")
    @Test
    void getFamilyTreeComponents_whenKenjiHorn_thenReturnExactMembersAndRelationships() {
        // given
        int memberId = 6;

        // when
        FamilyTreeComponents result = port.getFamilyTreeComponents(memberId);

        // then
        assertNotNull(result);

        // ---- EXPECTED RELATIONSHIPS ----
        Set<Relationship> expectedRelationships = Set.of(
                new Relationship(6, 7, RelationshipType.CURRENT_SPOUSE),
                new Relationship(6, 12, RelationshipType.EX_SPOUSE),
                new Relationship(6, 13, RelationshipType.EX_SPOUSE),
                new Relationship(6, 10, RelationshipType.PARENT),
                new Relationship(6, 11, RelationshipType.PARENT),
                new Relationship(12, 9, RelationshipType.PARENT),
                new Relationship(12, 10, RelationshipType.PARENT),
                new Relationship(6, 9, RelationshipType.PARENT),
                new Relationship(7, 11, RelationshipType.PARENT)
        );

        Set<RelationshipKey> actualRelKeys = getRelationshipkeys(new HashSet<>(result.relationships()));
        Set<RelationshipKey> expectedRelKeys = getRelationshipkeys(expectedRelationships);

        assertEquals(expectedRelKeys, actualRelKeys, "Relationships should match exactly");

        // ---- EXPECTED MEMBERS (derived from relationships + root) ----
        Set<Integer> expectedMemberIds = Set.of(
                6, 7, 9, 10, 11, 12, 13
        );

        Set<Integer> actualMemberIds = result.members().stream()
                .map(Member::getId)
                .collect(Collectors.toSet());

        assertEquals(expectedMemberIds, actualMemberIds,
                "Members should match exactly");
    }

    @DisplayName("getFamilyTreeComponents: returns full exact family tree for Hans Horn")
    @Test
    void getFamilyTreeComponents_whenHansHorn_thenReturnExactMembersAndRelationships() {
        // given
        int memberId = 1;

        // when
        FamilyTreeComponents result = port.getFamilyTreeComponents(memberId);

        // then
        assertNotNull(result);

        // ---- EXPECTED RELATIONSHIPS ----
        Set<Relationship> expectedRelationships = Set.of(
                new Relationship(1, 8, RelationshipType.CURRENT_MARRIED_SPOUSE),
                new Relationship(1, 4, RelationshipType.PARENT), // Relationship(firstMemberId=1, secondMemberId=4, relationshipType=PARENT)
                new Relationship(8, 4, RelationshipType.PARENT),
                new Relationship(4, 3, RelationshipType.CURRENT_SPOUSE),
                new Relationship(3, 2, RelationshipType.PARENT),
                new Relationship(3, 5, RelationshipType.PARENT),
                new Relationship(3, 6, RelationshipType.PARENT),
                new Relationship(4, 2, RelationshipType.PARENT), // Relationship(firstMemberId=4, secondMemberId=2, relationshipType=PARENT)
                new Relationship(4, 5, RelationshipType.PARENT), // Relationship(firstMemberId=4, secondMemberId=5, relationshipType=PARENT)
                new Relationship(4, 6, RelationshipType.PARENT), // Relationship(firstMemberId=4, secondMemberId=6, relationshipType=PARENT)
                new Relationship(6, 7, RelationshipType.CURRENT_SPOUSE),
                new Relationship(6, 12, RelationshipType.EX_SPOUSE),
                new Relationship(6, 13, RelationshipType.EX_SPOUSE),
                new Relationship(6, 10, RelationshipType.PARENT), // Relationship(firstMemberId=6, secondMemberId=10, relationshipType=PARENT)
                new Relationship(6, 11, RelationshipType.PARENT), // Relationship(firstMemberId=6, secondMemberId=11, relationshipType=PARENT)
                new Relationship(12, 9, RelationshipType.PARENT),
                new Relationship(12, 10, RelationshipType.PARENT),
                new Relationship(6, 9, RelationshipType.PARENT), // Relationship(firstMemberId=6, secondMemberId=9, relationshipType=PARENT)
                new Relationship(7, 11, RelationshipType.PARENT)
        );

        Set<RelationshipKey> actualRelKeys = getRelationshipkeys(new HashSet<>(result.relationships()));
        Set<RelationshipKey> expectedRelKeys = getRelationshipkeys(expectedRelationships);

        assertEquals(expectedRelKeys, actualRelKeys, "Relationships should match exactly");

        // ---- EXPECTED MEMBERS (derived from relationships + root) ----
        Set<Integer> expectedMemberIds = Set.of(
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13
        );

        Set<Integer> actualMemberIds = result.members().stream()
                .map(Member::getId)
                .collect(Collectors.toSet());



        assertEquals(expectedMemberIds, actualMemberIds,
                "Members should match exactly");
    }

    public record RelationshipKey(int a, int b, RelationshipType type) {};

    Set<RelationshipKey> getRelationshipkeys(Set<Relationship> rels) {
        return rels.stream().map(r -> new RelationshipKey(
                Math.min(r.getFirstMemberId(), r.getSecondMemberId()),
                Math.max(r.getFirstMemberId(), r.getSecondMemberId()),
                r.getRelationshipType()
        )).collect(Collectors.toSet());
    }
}
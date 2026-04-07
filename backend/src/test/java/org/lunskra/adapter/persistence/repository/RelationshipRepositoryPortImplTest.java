package org.lunskra.adapter.persistence.repository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.lunskra.adapter.persistence.testcontainer.MySQLTestContainerResource;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;

import java.util.List;

@QuarkusTest
@QuarkusTestResource(value = MySQLTestContainerResource.class, restrictToAnnotatedClass = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RelationshipRepositoryPortImplTest {

    @Inject
    RelationshipRepositoryPortImpl relationshipRepositoryPort;

    private final static Integer MEMBER_ID_NON_EXISTENT = 1000;

    @Order(1)
    @DisplayName("Should return all relationships")
    @Test
    void listRelationships() {
        // Given
        Integer totalRelationships = 19;

        // When
        List<Relationship> relationships = relationshipRepositoryPort.listRelationships();

        // Then
        Assertions.assertEquals(totalRelationships, relationships.size());
    }

    @Order(2)
    @DisplayName("Should return existing relationship")
    @Test
    void testGetRelationship_WhenRelationshipExists_ThenReturnRelationship() {
        // Given
        Integer firstMemberId = 8;
        Integer secondMemberId = 1;
        RelationshipType type = RelationshipType.CURRENT_MARRIED_SPOUSE;

        // When
        Relationship relationship = relationshipRepositoryPort.getRelationshipByMemberPair(firstMemberId, secondMemberId);

        // Then
        Assertions.assertEquals(type, relationship.getRelationshipType());
    }

    @Order(3)
    @DisplayName("Should throw error when member id does not exist")
    @Test
    void testGetRelationship_WhenMemberIdDoesNotExist_ThenThrowError() {
        // Given
        Integer firstMemberId = 1;
        Integer secondMemberId = MEMBER_ID_NON_EXISTENT;

        // When & Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> relationshipRepositoryPort.getRelationshipByMemberPair(firstMemberId, secondMemberId)
        );

    }

    @Order(4)
    @DisplayName("Should throw error when member id exist but no relationship between them")
    @Test
    void testGetRelationship_WhenMemberIdExistButNoRelationship_ThenThrowError() {
        // Given
        Integer firstMemberId = 1;
        Integer secondMemberId = 9;

        // When & Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> relationshipRepositoryPort.getRelationshipByMemberPair(firstMemberId, secondMemberId)
        );

    }

    @Order(5)
    @DisplayName("Should create relationship when members exist")
    @Test
    void testCreateRelationship_WhenMemberIdExist_ThenCreateRelationship() {
        // Given
        Integer firstMemberId = 1;
        Integer secondMemberId = 9;
        RelationshipType type = RelationshipType.EX_SPOUSE;
        Relationship relationship = new Relationship(firstMemberId, secondMemberId, type);

        // When
        relationshipRepositoryPort.createRelationship(relationship);

        // Then
        Relationship relationshipFromDb = Assertions.assertDoesNotThrow(
                () -> relationshipRepositoryPort.getRelationshipByMemberPair(firstMemberId, secondMemberId)
        );
        Assertions.assertEquals(type, relationshipFromDb.getRelationshipType());
    }

    @Order(7)
    @DisplayName("Should delete relationship when members and relationship exists")
    @Test
    void testDeleteRelationship_WhenMembersAndRelExists_ThenDeleteRel() {
        // Given
        Integer firstMemberId = 1;
        Integer secondMemberId = 9;

        // When
        relationshipRepositoryPort.deleteRelationshipByMemberPair(firstMemberId, secondMemberId);

        // Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> relationshipRepositoryPort.getRelationshipByMemberPair(firstMemberId, secondMemberId)
        );
    }

    @Order(8)
    @DisplayName("Should throw error  when members exist but no relationship")
    @Test
    void testDeleteRelationship_WhenMembersButNoRelExists_ThenThrowError() {
        // Given
        Integer firstMemberId = 1;
        Integer secondMemberId = 9;

        // When & Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> relationshipRepositoryPort.deleteRelationshipByMemberPair(firstMemberId, secondMemberId)
        );
    }

    @Order(9)
    @DisplayName("Should throw error when member does not exist")
    @Test
    void testDeleteRelationship_WhenMemberDoesNotExist_ThenThrowError() {
        // Given
        Integer firstMemberId = 1;

        // When & Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> relationshipRepositoryPort.deleteRelationshipByMemberPair(firstMemberId, MEMBER_ID_NON_EXISTENT)
        );
    }

    @Order(10)
    @DisplayName("Should update relationship when members and relationship exist")
    @Test
    void testUpdateRelationship_WhenMembersAndRelExist_ThenUpdateRel() {
        // Given
        Integer firstMemberId = 1;
        Integer secondMemberId = 8;
        RelationshipType type = RelationshipType.EX_SPOUSE;

        // When
        Relationship relationship = relationshipRepositoryPort.updateRelationshipByMemberPair(
                firstMemberId,
                secondMemberId,
                type
        );

        // Then
        Assertions.assertEquals(type, relationship.getRelationshipType());
    }

    @Order(11)
    @DisplayName("Should throw error when members exist but no relationship")
    @Test
    void testUpdateRelationship_WhenMembersButNoRelExists_ThenThrowError() {
        // Given
        Integer firstMemberId = 1;
        Integer secondMemberId = 9;
        RelationshipType type = RelationshipType.EX_SPOUSE;

        // When & Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> relationshipRepositoryPort.updateRelationshipByMemberPair(firstMemberId, secondMemberId, type)
        );
    }

    @Order(12)
    @DisplayName("Should throw error when member does not exist")
    @Test
    void testUpdateRelationship_WhenMemberDoesNotExist_ThenThrowError() {
        // Given
        Integer firstMemberId = 1;
        RelationshipType type = RelationshipType.EX_SPOUSE;

        // When & Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> relationshipRepositoryPort.updateRelationshipByMemberPair(firstMemberId, MEMBER_ID_NON_EXISTENT, type)
        );
    }
}
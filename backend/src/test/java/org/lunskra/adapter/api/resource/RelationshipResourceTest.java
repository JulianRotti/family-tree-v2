package org.lunskra.adapter.api.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lunskra.adapter.api.service.RelationshipService;
import org.lunskra.adapter.utils.RelationshipDtoGenerator;
import org.lunskra.family_tree.api.model.RelationshipDto;
import org.lunskra.family_tree.api.model.RelationshipTypeDto;
import org.mockito.Mockito;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
@TestSecurity(user = "testuser", roles = {"view", "create", "edit", "delete"})
class RelationshipResourceTest {

    @InjectMock
    RelationshipService relationshipService;

    private static final String API_RELATIONSHIPS = "/api/relationships/";

    RelationshipDto parentRelationship;
    RelationshipDto spouseRelationship;

    @BeforeEach
    void setUp() {
        parentRelationship = RelationshipDtoGenerator.createParentRelationshipDto(1, 2);
        spouseRelationship = RelationshipDtoGenerator.createMarriedSpouseRelationshipDto(3, 4);
    }

    @DisplayName("Should return list with two relationships when no filters are set.")
    @Test
    void testListRelationships_WhenTwoRelationshipsExist_ThenReturnTwo() {
        Mockito.when(relationshipService.listRelationships()).thenReturn(List.of(parentRelationship, spouseRelationship));

        given()
            .accept(ContentType.JSON)
        .when()
            .get(API_RELATIONSHIPS)
        .then()
            .statusCode(200)
            .body("size()", is(2));
    }

    @DisplayName("Should return empty list when no relationships exist.")
    @Test
    void testListRelationships_WhenNoRelationshipsExist_ThenReturnEmptyList() {
        Mockito.when(relationshipService.listRelationships()).thenReturn(List.of());

        given()
            .accept(ContentType.JSON)
        .when()
            .get(API_RELATIONSHIPS)
        .then()
            .statusCode(200)
            .body("size()", is(0));
    }

    @DisplayName("Should return relationship when valid member pair is given.")
    @Test
    void testGetRelationshipByMemberPair_WhenPairExists_ThenReturnRelationship() {
        Mockito.when(relationshipService.getRelationshipByMemberPair(1, 2)).thenReturn(parentRelationship);

        given()
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", 1)
            .pathParam("secondMemberId", 2)
        .when()
            .get(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(200)
            .body("firstMemberId", is(1))
            .body("secondMemberId", is(2));
    }

    @DisplayName("Should return 404 when relationship for given member pair does not exist.")
    @Test
    void testGetRelationshipByMemberPair_WhenPairDoesNotExist_ThenReturn404() {
        Mockito.when(relationshipService.getRelationshipByMemberPair(1, 2))
                .thenThrow(new EntityNotFoundException());

        given()
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", 1)
            .pathParam("secondMemberId", 2)
        .when()
            .get(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(404);
    }

    @DisplayName("Should return 201 with location header when relationship is created.")
    @Test
    void testCreateRelationship_WhenValid_ThenReturn201() {
        Mockito.when(relationshipService.createRelationship(any())).thenReturn(parentRelationship);

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(parentRelationship)
        .when()
            .post(API_RELATIONSHIPS)
        .then()
            .statusCode(201)
            .body("firstMemberId", is(1))
            .body("secondMemberId", is(2));
    }

    @DisplayName("Should return 204 when relationship is deleted.")
    @Test
    void testDeleteRelationshipByMemberPair_WhenPairExists_ThenReturn204() {
        given()
            .pathParam("firstMemberId", 1)
            .pathParam("secondMemberId", 2)
        .when()
            .delete(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(204);

        Mockito.verify(relationshipService).deleteRelationshipByMemberPair(1, 2);
    }

    @DisplayName("Should return 404 when relationship to delete does not exist.")
    @Test
    void testDeleteRelationshipByMemberPair_WhenPairDoesNotExist_ThenReturn404() {
        Mockito.doThrow(new EntityNotFoundException())
                .when(relationshipService)
                .deleteRelationshipByMemberPair(1, 2);

        given()
            .pathParam("firstMemberId", 1)
            .pathParam("secondMemberId", 2)
        .when()
            .delete(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(404);
    }

    @DisplayName("Should return updated relationship when update request is valid.")
    @Test
    void testUpdateRelationshipByMemberPair_WhenValid_ThenReturn200() {
        Mockito.when(relationshipService.updateRelationshipByMemberPair(eq(1), eq(2), any()))
                .thenReturn(spouseRelationship);

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", 1)
            .pathParam("secondMemberId", 2)
            .body(RelationshipDtoGenerator.createUpdateRequest(RelationshipTypeDto.CURRENT_MARRIED_SPOUSE))
        .when()
            .put(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(200)
            .body("firstMemberId", is(3))
            .body("secondMemberId", is(4));
    }

    @DisplayName("Should return 404 when relationship to update does not exist.")
    @Test
    void testUpdateRelationshipByMemberPair_WhenPairDoesNotExist_ThenReturn404() {
        Mockito.when(relationshipService.updateRelationshipByMemberPair(eq(1), eq(2), any()))
                .thenThrow(new EntityNotFoundException());

        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", 1)
            .pathParam("secondMemberId", 2)
            .body(RelationshipDtoGenerator.createUpdateRequest(RelationshipTypeDto.CURRENT_MARRIED_SPOUSE))
        .when()
            .put(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(404);
    }
}

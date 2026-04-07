package org.lunskra.adapter.api.resource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.lunskra.adapter.persistence.testcontainer.MySQLTestContainerResource;
import org.lunskra.adapter.utils.RelationshipDtoGenerator;
import org.lunskra.family_tree.api.model.ProblemDetailsDto;
import org.lunskra.family_tree.api.model.RelationshipDto;
import org.lunskra.family_tree.api.model.RelationshipTypeDto;
import org.lunskra.port.out.RelationshipRepositoryPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(value = MySQLTestContainerResource.class, restrictToAnnotatedClass = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestSecurity(user = "testuser", roles = {"view", "create", "edit", "delete"})
public class RelationshipResourceIntegrationTest {

    @Inject
    RelationshipRepositoryPort relationshipRepositoryPort;

    private static final String API_RELATIONSHIPS = "/api/relationships/";

    // Seeded pair used for create → update → delete lifecycle across tests
    private static final Integer FIRST_ID = 5;
    private static final Integer SECOND_ID = 9;

    // Seeded pair for an already existing relationship
    private static final Integer FIRST_ID_EXISTING = 1;
    private static final Integer SECOND_ID_EXISTING = 8;

    // Seeded pair for a younger parent <-> older child relationship
    private static final Integer YOUNG_PARENT_ID = 11;
    private static final Integer OLD_CHILD_ID = 2;

    // Greta (3) already has CURRENT_SPOUSE with Ahmed (4); Fatima (2) is her child via PARENT
    private static final Integer MEMBER_ALREADY_HAS_SPOUSE_ID = 3;
    private static final Integer SECOND_SPOUSE_CANDIDATE_ID = 2;

    @Order(1)
    @DisplayName("Should return all 19 seeded relationships")
    @Test
    void testListRelationships_WhenSeededDataLoaded_ThenReturnAll19() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get(API_RELATIONSHIPS)
        .then()
            .statusCode(200)
            .body("size()", is(19));
    }

    @Order(2)
    @DisplayName("Should return Hans–Amina relationship when their ids are passed")
    @Test
    void testGetRelationshipByMemberPair_WhenPairExists_ThenReturnRelationship() {
        given()
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", FIRST_ID_EXISTING)
            .pathParam("secondMemberId", SECOND_ID_EXISTING)
        .when()
            .get(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(200)
            .body("firstMemberId", is(FIRST_ID_EXISTING))
            .body("secondMemberId", is(SECOND_ID_EXISTING))
            .body("relationship", is(RelationshipTypeDto.CURRENT_MARRIED_SPOUSE.toString()));
    }

    @Order(3)
    @DisplayName("Should return 404 when member pair does not exist")
    @Test
    void testGetRelationshipByMemberPair_WhenPairDoesNotExist_ThenReturn404() {
        given()
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", 999)
            .pathParam("secondMemberId", 1000)
        .when()
            .get(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json");
    }

    @Order(4)
    @DisplayName("Should create PARENT relationship when Chantal (5) is parent of Luca (9)")
    @Test
    void testCreateRelationship_WhenValid_ThenReturn201AndPersist() {
        RelationshipDto dto = RelationshipDtoGenerator.createParentRelationshipDto(FIRST_ID, SECOND_ID);

        RelationshipDto created = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(dto)
        .when()
            .post(API_RELATIONSHIPS)
        .then()
            .statusCode(201)
            .extract().body().as(RelationshipDto.class);

        Assertions.assertEquals(FIRST_ID, created.getFirstMemberId());
        Assertions.assertEquals(SECOND_ID, created.getSecondMemberId());
        Assertions.assertNotNull(
                relationshipRepositoryPort.getRelationshipByMemberPair(FIRST_ID, SECOND_ID)
        );
    }

    @Order(5)
    @DisplayName("Should return 409 when relationship already exists")
    @Test
    void testCreateRelationship_WhenRelationshipAlreadyExists_ThenReturn409() {
        ProblemDetailsDto problem = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(new RelationshipDto(FIRST_ID_EXISTING, SECOND_ID_EXISTING, RelationshipTypeDto.CURRENT_MARRIED_SPOUSE))
        .when()
            .post(API_RELATIONSHIPS)
        .then()
            .statusCode(409)
            .contentType("application/problem+json")
            .extract().body().as(ProblemDetailsDto.class);

        Assertions.assertEquals("firstMemberId, secondMemberId", problem.getErrors().getFirst().getField());
    }

    @Order(6)
    @DisplayName("Should return 409 when first and second member id are the same")
    @Test
    void testCreateRelationship_WhenSelfRelationship_ThenReturn409() {
        ProblemDetailsDto problem = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(new RelationshipDto(1, 1, RelationshipTypeDto.PARENT))
        .when()
            .post(API_RELATIONSHIPS)
        .then()
            .statusCode(409)
            .contentType("application/problem+json")
            .extract().body().as(ProblemDetailsDto.class);

        Assertions.assertEquals("firstMemberId", problem.getErrors().getFirst().getField());
    }

    @Order(7)
    @DisplayName("Should return 409 when parent (Miguel, born 2000) is younger than child (Fatima, born 1990)")
    @Test
    void testCreateRelationship_WhenParentYoungerThanChild_ThenReturn409() {
        // member 11 = Miguel (born 2000) as parent of member 2 = Fatima (born 1990)
        ProblemDetailsDto problem = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(new RelationshipDto(YOUNG_PARENT_ID, OLD_CHILD_ID, RelationshipTypeDto.PARENT))
        .when()
            .post(API_RELATIONSHIPS)
        .then()
            .statusCode(409)
            .contentType("application/problem+json")
            .extract().body().as(ProblemDetailsDto.class);

        Assertions.assertEquals("firstMemberId", problem.getErrors().getFirst().getField());
    }

    @Order(10)
    @DisplayName("Should update relationship type when pair exists")
    @Test
    void testUpdateRelationshipByMemberPair_WhenPairExists_ThenUpdateAndReturn200() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", FIRST_ID)
            .pathParam("secondMemberId", SECOND_ID)
            .body(RelationshipDtoGenerator.createUpdateRequest(RelationshipTypeDto.EX_SPOUSE))
        .when()
            .put(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(200)
            .body("firstMemberId", is(FIRST_ID))
            .body("secondMemberId", is(SECOND_ID));
    }

    @Order(11)
    @DisplayName("Should return 404 when updating a non-existing relationship")
    @Test
    void testUpdateRelationshipByMemberPair_WhenPairDoesNotExist_ThenReturn404() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", 1)
            .pathParam("secondMemberId", 11)
            .body(RelationshipDtoGenerator.createUpdateRequest(RelationshipTypeDto.EX_SPOUSE))
        .when()
            .put(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json");
    }

    @Order(12)
    @DisplayName("Should return 409 when update would give Greta (3) a second current spouse")
    @Test
    void testUpdateRelationshipByMemberPair_WhenResultsInDuplicateCurrentSpouse_ThenReturn409() {
        // Greta (3) already has CURRENT_SPOUSE with Ahmed (4).
        // Updating the existing PARENT relationship (3, 2) to CURRENT_SPOUSE would give her two current spouses.
        ProblemDetailsDto problem = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", MEMBER_ALREADY_HAS_SPOUSE_ID)
            .pathParam("secondMemberId", SECOND_SPOUSE_CANDIDATE_ID)
            .body(RelationshipDtoGenerator.createUpdateRequest(RelationshipTypeDto.CURRENT_SPOUSE))
        .when()
            .put(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(409)
            .contentType("application/problem+json")
            .extract().body().as(ProblemDetailsDto.class);

        Assertions.assertEquals("firstMemberId", problem.getErrors().getFirst().getField());
    }

    @Order(20)
    @DisplayName("Should delete the relationship when pair exists")
    @Test
    void testDeleteRelationshipByMemberPair_WhenPairExists_ThenReturn204AndRemove() {
        given()
            .pathParam("firstMemberId", FIRST_ID)
            .pathParam("secondMemberId", SECOND_ID)
        .when()
            .delete(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(204);

        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> relationshipRepositoryPort.getRelationshipByMemberPair(FIRST_ID, SECOND_ID)
        );
    }

    @Order(21)
    @DisplayName("Should return 404 when deleting a non-existing relationship")
    @Test
    void testDeleteRelationshipByMemberPair_WhenPairDoesNotExist_ThenReturn404() {
        given()
            .pathParam("firstMemberId", 999)
            .pathParam("secondMemberId", 1000)
        .when()
            .delete(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json");
    }
}

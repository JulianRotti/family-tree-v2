package org.lunskra.adapter.api.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lunskra.adapter.api.service.RelationshipService;

import static io.restassured.RestAssured.given;

@QuarkusTest
class RelationshipResourceAuthorizationTest {

    @InjectMock
    RelationshipService relationshipService;

    private static final String API_RELATIONSHIPS = "/api/relationships/";

    @DisplayName("Should return 401 when no authentication is provided for GET /relationships")
    @Test
    void testListRelationships_WhenUnauthenticated_ThenReturn401() {
        given()
        .when()
            .get(API_RELATIONSHIPS)
        .then()
            .statusCode(401);
    }

    @DisplayName("Should return 403 when authenticated user lacks view role for GET /relationships")
    @Test
    @TestSecurity(user = "restricted", roles = {"create"})
    void testListRelationships_WhenMissingViewRole_ThenReturn403() {
        given()
        .when()
            .get(API_RELATIONSHIPS)
        .then()
            .statusCode(403);
    }
}

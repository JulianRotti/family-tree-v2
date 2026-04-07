package org.lunskra.adapter.api.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lunskra.port.in.GenerateFamilyTreeUseCase;

import static io.restassured.RestAssured.given;

@QuarkusTest
class FamilyTreeResourceAuthorizationTest {

    @InjectMock
    GenerateFamilyTreeUseCase generateFamilyTreeUseCase;

    private static final String API_FAMILY_TREE = "/api/family-tree/{memberId}";

    @DisplayName("Should return 401 when no authentication is provided for GET /family-tree")
    @Test
    void testGetFamilyTree_WhenUnauthenticated_ThenReturn401() {
        given()
            .pathParam("memberId", 1)
        .when()
            .get(API_FAMILY_TREE)
        .then()
            .statusCode(401);
    }

    @DisplayName("Should return 403 when authenticated user lacks view role for GET /family-tree")
    @Test
    @TestSecurity(user = "restricted", roles = {"create"})
    void testGetFamilyTree_WhenMissingViewRole_ThenReturn403() {
        given()
            .pathParam("memberId", 1)
        .when()
            .get(API_FAMILY_TREE)
        .then()
            .statusCode(403);
    }
}

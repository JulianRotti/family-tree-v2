package org.lunskra.adapter.api.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lunskra.adapter.api.service.MemberService;

import static io.restassured.RestAssured.given;

@QuarkusTest
class MemberResourceAuthorizationTest {

    @InjectMock
    MemberService memberService;

    private static final String API_MEMBERS = "/api/members/";

    @DisplayName("Should return 401 when no authentication is provided for GET /members")
    @Test
    void testListMembers_WhenUnauthenticated_ThenReturn401() {
        given()
        .when()
            .get(API_MEMBERS)
        .then()
            .statusCode(401);
    }

    @DisplayName("Should return 403 when authenticated user lacks view role for GET /members")
    @Test
    @TestSecurity(user = "restricted", roles = {"create"})
    void testListMembers_WhenMissingViewRole_ThenReturn403() {
        given()
        .when()
            .get(API_MEMBERS)
        .then()
            .statusCode(403);
    }
}

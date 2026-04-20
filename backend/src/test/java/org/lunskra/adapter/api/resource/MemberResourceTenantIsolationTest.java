package org.lunskra.adapter.api.resource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.SecurityAttribute;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lunskra.adapter.TestTenantConstants;
import org.lunskra.adapter.persistence.testcontainer.MySQLTestContainerResource;
import org.lunskra.adapter.utils.MemberRequestGenerator;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

/**
 * Verifies that the discriminator-based multitenancy correctly isolates data between tenants.
 * Tenant A owns members 1–24 and 34 relationships.
 * Tenant B owns members 25–26 and 1 relationship.
 */
@QuarkusTest
@QuarkusTestResource(value = MySQLTestContainerResource.class, restrictToAnnotatedClass = true)
class MemberResourceTenantIsolationTest {

    private static final String API_MEMBERS = "/api/members/";

    @Test
    @DisplayName("TENANT_B list: should return only 2 members belonging to TENANT_B")
    @TestSecurity(user = "tenant-b-user", roles = {"view"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_B_ID)})
    void testListMembers_WhenTenantB_ThenReturnOnlyTenantBMembers() {
        given()
            .accept(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("pageSize", 100)
        .when()
            .get(API_MEMBERS)
        .then()
            .statusCode(200)
            .body("totalElements", is(2))
            .body("content.size()", is(2));
    }

    @Test
    @DisplayName("TENANT_A cannot GET member 25 owned by TENANT_B — expects 404")
    @TestSecurity(user = "tenant-a-user", roles = {"view"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_A_ID)})
    void testGetMember_WhenTenantAAccessesTenantBMember_ThenReturn404() {
        given()
            .accept(ContentType.JSON)
            .pathParam("memberId", TestTenantConstants.TENANT_B_MEMBER_1_ID)
        .when()
            .get(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json");
    }

    @Test
    @DisplayName("TENANT_B cannot GET member 1 owned by TENANT_A — expects 404")
    @TestSecurity(user = "tenant-b-user", roles = {"view"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_B_ID)})
    void testGetMember_WhenTenantBAccessesTenantAMember_ThenReturn404() {
        given()
            .accept(ContentType.JSON)
            .pathParam("memberId", 1)
        .when()
            .get(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json");
    }

    @Test
    @DisplayName("TENANT_B cannot DELETE member 1 owned by TENANT_A — expects 404")
    @TestSecurity(user = "tenant-b-user", roles = {"delete"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_B_ID)})
    void testDeleteMember_WhenTenantBDeletesTenantAMember_ThenReturn404() {
        given()
            .pathParam("memberId", 1)
        .when()
            .delete(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json");
    }

    @Test
    @DisplayName("TENANT_A cannot UPDATE member 25 owned by TENANT_B — expects 404")
    @TestSecurity(user = "tenant-a-user", roles = {"edit"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_A_ID)})
    void testUpdateMember_WhenTenantAUpdatesTenantBMember_ThenReturn404() {
        Map<String, Object> body = MemberRequestGenerator.createLivingMemberRequestWithAllDataAndNoId();
        MemberRequestGenerator.createMultiPartRequestWithMemberRequestFields(given(), body)
            .accept(ContentType.JSON)
            .pathParam("memberId", TestTenantConstants.TENANT_B_MEMBER_1_ID)
        .when()
            .put(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json");
    }

    @Test
    @DisplayName("TENANT_A list pagination: totalElements reflects only 24 TENANT_A members")
    @TestSecurity(user = "tenant-a-user", roles = {"view"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_A_ID)})
    void testListMembers_WhenTenantA_ThenTotalElementsIs24() {
        given()
            .accept(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("pageSize", 5)
        .when()
            .get(API_MEMBERS)
        .then()
            .statusCode(200)
            .body("totalElements", is(24));
    }
}

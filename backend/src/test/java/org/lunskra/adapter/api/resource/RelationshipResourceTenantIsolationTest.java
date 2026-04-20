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
import org.lunskra.family_tree.api.model.RelationshipDto;
import org.lunskra.family_tree.api.model.RelationshipTypeDto;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

/**
 * Verifies that relationship endpoints are correctly scoped to the requesting tenant.
 * Tenant A owns 34 relationships. Tenant B owns 1 relationship (members 25–26).
 */
@QuarkusTest
@QuarkusTestResource(value = MySQLTestContainerResource.class, restrictToAnnotatedClass = true)
class RelationshipResourceTenantIsolationTest {

    private static final String API_RELATIONSHIPS = "/api/relationships/";

    @Test
    @DisplayName("TENANT_B list: should return only 1 relationship belonging to TENANT_B")
    @TestSecurity(user = "tenant-b-user", roles = {"view"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_B_ID)})
    void testListRelationships_WhenTenantB_ThenReturnOnlyTenantBRelationships() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get(API_RELATIONSHIPS)
        .then()
            .statusCode(200)
            .body("size()", is(1));
    }

    @Test
    @DisplayName("TENANT_A cannot GET relationship between TENANT_B members 25 and 26 — expects 404")
    @TestSecurity(user = "tenant-a-user", roles = {"view"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_A_ID)})
    void testGetRelationship_WhenTenantAAccessesTenantBRelationship_ThenReturn404() {
        given()
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", TestTenantConstants.TENANT_B_MEMBER_1_ID)
            .pathParam("secondMemberId", TestTenantConstants.TENANT_B_MEMBER_2_ID)
        .when()
            .get(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json");
    }

    @Test
    @DisplayName("TENANT_B cannot GET relationship between TENANT_A members 1 and 8 — expects 404")
    @TestSecurity(user = "tenant-b-user", roles = {"view"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_B_ID)})
    void testGetRelationship_WhenTenantBAccessesTenantARelationship_ThenReturn404() {
        given()
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", 1)
            .pathParam("secondMemberId", 8)
        .when()
            .get(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json");
    }

    @Test
    @DisplayName("TENANT_B cannot DELETE relationship between TENANT_A members — expects 404")
    @TestSecurity(user = "tenant-b-user", roles = {"delete"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_B_ID)})
    void testDeleteRelationship_WhenTenantBDeletesTenantARelationship_ThenReturn404() {
        given()
            .pathParam("firstMemberId", 1)
            .pathParam("secondMemberId", 8)
        .when()
            .delete(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json");
    }

    @Test
    @DisplayName("TENANT_B cannot UPDATE relationship between TENANT_A members — members invisible to TENANT_B, expects 409 from member validation")
    @TestSecurity(user = "tenant-b-user", roles = {"edit"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_B_ID)})
    void testUpdateRelationship_WhenTenantBUpdatesTenantARelationship_ThenReturn409() {
        // The update path validates member existence before looking up the relationship.
        // Members 1 and 8 belong to TENANT_A and are invisible to TENANT_B, so
        // MembersExistRule fails with DomainValidationException (409) before the
        // relationship lookup (which would yield 404) is ever reached.
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .pathParam("firstMemberId", 1)
            .pathParam("secondMemberId", 8)
            .body(new RelationshipDto(1, 8, RelationshipTypeDto.EX_SPOUSE))
        .when()
            .put(API_RELATIONSHIPS + "{firstMemberId}/{secondMemberId}")
        .then()
            .statusCode(409)
            .contentType("application/problem+json");
    }

    @Test
    @DisplayName("TENANT_A list: totalElements reflects only 34 TENANT_A relationships")
    @TestSecurity(user = "tenant-a-user", roles = {"view"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_A_ID)})
    void testListRelationships_WhenTenantA_ThenReturn34() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get(API_RELATIONSHIPS)
        .then()
            .statusCode(200)
            .body("size()", is(34));
    }
}

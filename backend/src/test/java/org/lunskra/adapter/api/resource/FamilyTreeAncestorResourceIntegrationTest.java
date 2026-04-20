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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(value = MySQLTestContainerResource.class, restrictToAnnotatedClass = true)
@TestSecurity(user = "testuser", roles = {"view", "create", "edit", "delete"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_A_ID)})
class FamilyTreeAncestorResourceIntegrationTest {

    private static final String API_FAMILY_TREE = "/api/family-tree-ancestor/{memberId}";

    private static final float WIDTH_OF_MEMBER_NODE   = 1f;
    private static final float SPACE_BETWEEN_MEMBERS  = 3f;

    @Test
    @DisplayName("Should return subtreeLength 20 for Hans (1) with the given spacing params")
    void getFamilyTreeByHeadMemberId_WhenMemberIdIs1_ThenSubtreeLengthIs20() {
        given()
                .accept(ContentType.JSON)
                .pathParam("memberId", 1)
                .queryParam("widthOfMemberNode", WIDTH_OF_MEMBER_NODE)
                .queryParam("spaceBetweenMemberNodes", SPACE_BETWEEN_MEMBERS)
                .when()
                .get(API_FAMILY_TREE)
                .then()
                .statusCode(200)
                .body("childMemberId", is(1))
                .body("tree.subtreeLength", is(20.0f))
                .body("numberTotal", is(10))
                .body("numberLiving", is(4))
                .body("numberGenerations", is(4));
    }
    @Test
    @DisplayName("Should return subtreeLength 8 for Hans (1) with the given spacing params")
    void getFamilyTreeByHeadMemberId_WhenMemberIdIs1AndMaxDepth1_ThenSubtreeLengthIs8() {
        given()
                .accept(ContentType.JSON)
                .pathParam("memberId", 1)
                .queryParam("widthOfMemberNode", WIDTH_OF_MEMBER_NODE)
                .queryParam("spaceBetweenMemberNodes", SPACE_BETWEEN_MEMBERS)
                .queryParam("maxDepth", 1)
                .when()
                .get(API_FAMILY_TREE)
                .then()
                .statusCode(200)
                .body("childMemberId", is(1))
                .body("tree.subtreeLength", is(8.0f))
                .body("numberTotal", is(3))
                .body("numberLiving", is(2))
                .body("numberGenerations", is(2));
    }

    @Test
    @DisplayName("Should return subtreeLength 16 for Hans (1) with the given spacing params")
    void getFamilyTreeByHeadMemberId_WhenMemberIdIs1AndMaxDepth2_ThenSubtreeLengthIs16() {
        given()
                .accept(ContentType.JSON)
                .pathParam("memberId", 1)
                .queryParam("widthOfMemberNode", WIDTH_OF_MEMBER_NODE)
                .queryParam("spaceBetweenMemberNodes", SPACE_BETWEEN_MEMBERS)
                .queryParam("maxDepth", 2)
                .when()
                .get(API_FAMILY_TREE)
                .then()
                .statusCode(200)
                .body("childMemberId", is(1))
                .body("tree.subtreeLength", is(16.0f))
                .body("numberTotal", is(7))
                .body("numberLiving", is(4))
                .body("numberGenerations", is(3));
    }

    @Test
    @DisplayName("Should return subtreeLength 4 for (16) with the given spacing params")
    void getFamilyTreeByHeadMemberId_WhenMemberIdIs16_ThenSubtreeLengthIs4() {
        given()
                .accept(ContentType.JSON)
                .pathParam("memberId", 16)
                .queryParam("widthOfMemberNode", WIDTH_OF_MEMBER_NODE)
                .queryParam("spaceBetweenMemberNodes", SPACE_BETWEEN_MEMBERS)
                .when()
                .get(API_FAMILY_TREE)
                .then()
                .statusCode(200)
                .body("childMemberId", is(16))
                .body("tree.subtreeLength", is(4.0f))
                .body("numberTotal", is(2))
                .body("numberLiving", is(0))
                .body("numberGenerations", is(2));
    }
    @Test
    @DisplayName("Should return 404 when memberId does not exist")
    void getFamilyTreeByHeadMemberId_WhenMemberDoesNotExist_ThenReturn404() {
        given()
                .accept(ContentType.JSON)
                .pathParam("memberId", 9999)
                .queryParam("widthOfMemberNode", WIDTH_OF_MEMBER_NODE)
                .queryParam("spaceBetweenMemberNodes", SPACE_BETWEEN_MEMBERS)
                .when()
                .get(API_FAMILY_TREE)
                .then()
                .statusCode(404)
                .contentType("application/problem+json");
    }
}
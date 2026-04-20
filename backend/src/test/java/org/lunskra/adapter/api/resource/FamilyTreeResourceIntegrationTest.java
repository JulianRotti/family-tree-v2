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
class FamilyTreeResourceIntegrationTest {

    private static final String API_FAMILY_TREE = "/api/family-tree/{memberId}";

    private static final float WIDTH_OF_MEMBER_NODE         = 1f;
    private static final float SPACE_BETWEEN_MEMBER_SPOUSE  = 3f;
    private static final float SPACE_BETWEEN_CHILDREN       = 7f;

    @Test
    @DisplayName("Should return subtreeLength 46 for Hans (1) with the given spacing params")
    void getFamilyTreeByHeadMemberId_WhenMemberIdIs1_ThenSubtreeLengthIs46() {
        given()
            .accept(ContentType.JSON)
            .pathParam("memberId", 1)
            .queryParam("widthOfMemberNode", WIDTH_OF_MEMBER_NODE)
            .queryParam("spaceBetweenMemberAndSpouse", SPACE_BETWEEN_MEMBER_SPOUSE)
            .queryParam("spaceBetweenChildren", SPACE_BETWEEN_CHILDREN)
        .when()
            .get(API_FAMILY_TREE)
        .then()
            .statusCode(200)
            .body("headMemberId", is(1))
            .body("tree.subtreeLength", is(46.0f))
            .body("numberTotal", is(12))
            .body("numberLiving", is(10))
            .body("numberGenerations", is(4));
    }

    @Test
    @DisplayName("Should return subtreeLength 30 for Kenji (6) with the given spacing params")
    void getFamilyTreeByHeadMemberId_WhenMemberIdIs6_ThenSubtreeLengthIs30() {
        given()
            .accept(ContentType.JSON)
            .pathParam("memberId", 6)
            .queryParam("widthOfMemberNode", WIDTH_OF_MEMBER_NODE)
            .queryParam("spaceBetweenMemberAndSpouse", SPACE_BETWEEN_MEMBER_SPOUSE)
            .queryParam("spaceBetweenChildren", SPACE_BETWEEN_CHILDREN)
        .when()
            .get(API_FAMILY_TREE)
        .then()
            .statusCode(200)
            .body("headMemberId", is(6))
            .body("tree.subtreeLength", is(30.0f))
            .body("numberTotal", is(6))
            .body("numberLiving", is(6))
            .body("numberGenerations", is(2));;
    }

    @Test
    @DisplayName("Should return subtreeLength 12 for Hans (1) with maxDepth=1 (only first-generation descendants)")
    void getFamilyTreeByHeadMemberId_WhenMemberIdIs1AndMaxDepthIs1_ThenSubtreeLengthIs24() {
        given()
            .accept(ContentType.JSON)
            .pathParam("memberId", 1)
            .queryParam("widthOfMemberNode", WIDTH_OF_MEMBER_NODE)
            .queryParam("spaceBetweenMemberAndSpouse", SPACE_BETWEEN_MEMBER_SPOUSE)
            .queryParam("spaceBetweenChildren", SPACE_BETWEEN_CHILDREN)
            .queryParam("maxDepth", 1)
        .when()
            .get(API_FAMILY_TREE)
        .then()
            .statusCode(200)
            .body("headMemberId", is(1))
            .body("tree.subtreeLength", is(12.0F))
            .body("numberTotal", is(4))
            .body("numberLiving", is(2))
            .body("numberGenerations", is(2));
    }

    @Test
    @DisplayName("Should return 404 when memberId does not exist")
    void getFamilyTreeByHeadMemberId_WhenMemberDoesNotExist_ThenReturn404() {
        given()
            .accept(ContentType.JSON)
            .pathParam("memberId", 9999)
            .queryParam("widthOfMemberNode", WIDTH_OF_MEMBER_NODE)
            .queryParam("spaceBetweenMemberAndSpouse", SPACE_BETWEEN_MEMBER_SPOUSE)
            .queryParam("spaceBetweenChildren", SPACE_BETWEEN_CHILDREN)
        .when()
            .get(API_FAMILY_TREE)
        .then()
            .statusCode(404)
            .contentType("application/problem+json");
    }
}
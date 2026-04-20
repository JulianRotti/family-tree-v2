package org.lunskra.adapter.api.resource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.SecurityAttribute;
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
import org.lunskra.adapter.TestTenantConstants;
import org.lunskra.adapter.persistence.testcontainer.MySQLTestContainerResource;
import org.lunskra.adapter.utils.MemberRequestGenerator;
import org.lunskra.family_tree.api.model.MemberDto;
import org.lunskra.family_tree.api.model.ProblemDetailsDto;
import org.lunskra.port.out.MemberRepositoryPort;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(value = MySQLTestContainerResource.class, restrictToAnnotatedClass = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestSecurity(user = "testuser", roles = {"view", "create", "edit", "delete"}, attributes = {@SecurityAttribute(key = TestTenantConstants.TENANT_ID_KEY, value = TestTenantConstants.TENANT_A_ID)})
public class MemberResourceIntegrationTest {

    @Inject
    MemberRepositoryPort memberRepositoryPort;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String API_MEMBERS = "/api/members/";
    private static final Integer ID_EXISTING = 1; // Gets deleted in one of the tests
    private static final Integer ID_EXISTING_FOR_UPDATE = 2;
    private static final Integer ID_NON_EXISTING = 1000;

    @Order(0)
    @DisplayName("Should return first page with 5 members and correct metadata, when pageSize=5 is given")
    @Test
    void testListMembers_WhenPaginationGiven_ThenReturnPagedResult() {
        // Test data contains 13 members; 13 / 5 = ceil(2.6) = 3 pages
        given()
            .accept(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("pageSize", 5)
        .when()
            .get(API_MEMBERS)
        .then()
            .statusCode(200)
            .body("content.size()", is(5))
            .body("totalElements", is(24))
            .body("totalPages", is(5))
            .body("page", is(0))
            .body("pageSize", is(5));
    }

    @Order(1)
    @DisplayName("Should return Hans, when Hans id is passed")
    @Test
    void testGetMemberById_WhenIdExists_ThenReturnHans() {
        given()
                .accept(ContentType.JSON)
                .pathParam("memberId", ID_EXISTING)
                .when()
                .get(API_MEMBERS + "{memberId}")
                .then()
                .statusCode(200)
                .body("firstName", is("Hans"));
    }

    @Order(2)
    @DisplayName("Should return NOT_FOUND http response, when non-existing id is passed")
    @Test
    void testGetMemberById_WhenIdOfNonExistingMemberGiven_ThenReturnNotFoundError() {
        given()
            .accept(ContentType.JSON)
            .pathParam("memberId", ID_NON_EXISTING)
        .when()
            .get(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json")
            .body("instance", is(API_MEMBERS + ID_NON_EXISTING));
    }

    @Order(3)
    @DisplayName("Should create member, when valid member request without id is passed")
    @Test
    void testCreateMember_WhenValidMemberWithoutIdGiven_ThenCreateMember() {
        Map<String, Object> memberRequestBody = MemberRequestGenerator.createLivingMemberRequestWithAllDataAndNoId();
        MemberDto member = MemberRequestGenerator.createMultiPartRequestWithMemberRequestFields(given(), memberRequestBody)
            .accept(ContentType.JSON)
        .when()
            .post(API_MEMBERS)
        .then()
            .statusCode(201)
            .extract()
            .body()
            .as(MemberDto.class);

        Integer memberId = member.getId();
        Assertions.assertNotNull(memberId);
        Assertions.assertEquals(memberRequestBody.get("firstName"), memberRepositoryPort.getMember(memberId).getFirstName());
    }

    @Order(4)
    @DisplayName("Should return Validation error, when member already exists")
    @Test
    void testCreateMember_WhenMemberAlreadyExists_ThenReturnConstraintValidationError() {
        Map<String, Object> memberRequestBody = MemberRequestGenerator.createLivingMemberRequestWithAllDataAndNoId();
        ProblemDetailsDto dto = MemberRequestGenerator.createMultiPartRequestWithMemberRequestFields(given(), memberRequestBody)
            .accept(ContentType.JSON)
        .when()
            .post(API_MEMBERS)
        .then()
            .statusCode(409).log().all()
            .extract()
            .body()
            .as(ProblemDetailsDto.class);

        Assertions.assertEquals("firstName, lastName, birthDate", dto.getErrors().getFirst().getField());
    }

    @Order(5)
    @DisplayName("Should return Validation error, when deathdate before birthdate")
    @Test
    void testCreateMember_WhenDeathDateBeforeBirthDate_ThenReturnConstraintValidationError() {
        Map<String, Object> memberRequestBody = MemberRequestGenerator.createLivingMemberRequestWithAllDataAndNoId();
        memberRequestBody.put("birthDate", "1990-01-01");
        memberRequestBody.put("deathDate", "1989-01-01");
        ProblemDetailsDto dto = MemberRequestGenerator.createMultiPartRequestWithMemberRequestFields(given(), memberRequestBody)
            .accept(ContentType.JSON)
        .when()
            .post(API_MEMBERS)
        .then()
            .statusCode(409).log().all()
            .extract()
            .body()
            .as(ProblemDetailsDto.class);

        Assertions.assertEquals("birthDate, deathDate", dto.getErrors().getFirst().getField());
    }

    @Order(10)
    @DisplayName("Should delete Hans, when Hans id is passed")
    @Test
    void testDeleteMemberById_WhenIdExists_ThenDeleteHans() {
        given()
            .pathParam("memberId", ID_EXISTING)
        .when()
            .delete(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(204);

        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> memberRepositoryPort.getMember(ID_EXISTING)
        );
    }

    @Order(11)
    @DisplayName("Should return NOT_FOUND http response, when non-existing id is passed")
    @Test
    void testDeleteMemberById_WhenIdOfNonExistingMemberGiven_ThenReturnNotFoundError() {
        given()
            .pathParam("memberId", ID_NON_EXISTING)
        .when()
            .delete(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json")
            .body("instance", is(API_MEMBERS + ID_NON_EXISTING));
    }

    @Order(12)
    @DisplayName("Should return NOT_FOUND http response, when non-existing id is passed")
    @Test
    void testUpdateMemberById_WhenNonExistentIdGiven_ThenReturnNotFoundError() {
        Map<String, Object> memberRequestBody = MemberRequestGenerator.createLivingMemberRequestWithAllDataAndNoId();
        MemberRequestGenerator.createMultiPartRequestWithMemberRequestFields(given(), memberRequestBody)
            .accept(ContentType.JSON)
            .pathParam("memberId", ID_NON_EXISTING)
        .when()
            .put(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json")
            .body("instance", is(API_MEMBERS + ID_NON_EXISTING));
    }

    @Order(13)
    @DisplayName("Should update Fatima, when existing id is passed")
    @Test
    void testUpdateMemberById_WhenExistensIdGiven_ThenUpdateMember() {
        String firstNamePostUpdate = "Postupdate";

        Map<String, Object> memberRequestBody = MemberRequestGenerator.createLivingMemberRequestWithAllDataAndNoId();
        memberRequestBody.put("firstName", firstNamePostUpdate);
        MemberRequestGenerator.createMultiPartRequestWithMemberRequestFields(given(), memberRequestBody)
            .accept(ContentType.JSON)
            .pathParam("memberId", ID_EXISTING_FOR_UPDATE)
                .log().all()
        .when()
            .put(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(201).log().all();
        Assertions.assertEquals(firstNamePostUpdate, memberRepositoryPort.getMember(ID_EXISTING_FOR_UPDATE).getFirstName());
    }
}

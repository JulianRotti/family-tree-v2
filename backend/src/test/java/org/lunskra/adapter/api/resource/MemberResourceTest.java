package org.lunskra.adapter.api.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lunskra.adapter.api.service.MemberService;
import org.lunskra.adapter.utils.MemberDtoGenerator;
import org.lunskra.adapter.utils.MemberRequestGenerator;
import org.lunskra.family_tree.api.model.MemberDto;
import org.lunskra.family_tree.api.model.MemberPageDto;
import org.lunskra.family_tree.api.model.ProblemDetailsDto;
import org.mockito.Mockito;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
@TestSecurity(user = "testuser", roles = {"view", "create", "edit", "delete"})
class MemberResourceTest {

    @InjectMock
    MemberService memberService;

    MemberDto memberJohn;
    MemberDto memberWithId;
    MemberDto memberWithoutId;

    List<MemberDto> members;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String API_MEMBERS = "/api/members/";

    @BeforeEach
    void setUp() {
        memberJohn = MemberDtoGenerator.createLivingMemberDtoWithRequiredData();
        memberWithId = MemberDtoGenerator.createLivingMemberDtoWithAllData();
        memberWithoutId = MemberDtoGenerator.createLivingMemberDtoWithAllDataWithoutId();
        members = List.of(
                MemberDtoGenerator.createLivingMemberDtoWithAllData(),
                memberJohn
        );

        MemberPageDto emptyPage = new MemberPageDto()
                .content(List.of()).totalElements(0L).totalPages(0).page(0).pageSize(20);
        MemberPageDto allMembersPage = new MemberPageDto()
                .content(members).totalElements(2L).totalPages(1).page(0).pageSize(20);
        MemberPageDto johnPage = new MemberPageDto()
                .content(List.of(memberJohn)).totalElements(1L).totalPages(1).page(0).pageSize(20);

        // default: no members are found
        Mockito.when(memberService.listMembers(any(), any(), any(), anyInt(), anyInt())).thenReturn(emptyPage);

        // no filters set: return all members
        Mockito.when(memberService.listMembers(eq(null), eq(null), eq(null), anyInt(), anyInt())).thenReturn(allMembersPage);

        // filter for John set: return John
        Mockito.when(memberService.listMembers(
                eq(memberJohn.getFirstName()),
                eq(memberJohn.getLastName()),
                eq(memberJohn.getBirthDate()),
                anyInt(), anyInt())).thenReturn(johnPage);
    }

    @DisplayName("Should return list with all two members, when no filters are set.")
    @Test
    void testListMembers_WhenTwoMembersExistAndNoQuery_ThenTwoMembersShouldBeInResponse() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get(API_MEMBERS)
        .then()
            .statusCode(200)
            .body("content.size()", is(2))
            .body("content.firstName", hasItem("John"));
    }

    @DisplayName("Should return list with John, when filters with John's data are set.")
    @Test
    void testListMembers_WhenOneMemberFitsQuery_ThenOneMemberShouldBeInResponse() {
        given()
            .accept(ContentType.JSON)
            .queryParam("firstName", memberJohn.getFirstName())
            .queryParam("lastName", memberJohn.getLastName())
            .queryParam("birthDate", memberJohn.getBirthDate().format(dateTimeFormatter))
        .when()
            .get(API_MEMBERS)
        .then()
            .statusCode(200)
            .body("content.size()", is(1))
            .body("content.firstName", hasItem("John"));
    }

    @DisplayName("Should return empty list, when filters do not match John's data.")
    @Test
    void testListMembers_WhenNoMemberFitsQuery_ThenNoMemberShouldBeInResponse() {
        given()
            .accept(ContentType.JSON)
            .queryParam("firstName", "Notjohn")
            .queryParam("lastName", memberJohn.getLastName())
            .queryParam("birthDate", memberJohn.getBirthDate().format(dateTimeFormatter))
        .when()
            .get(API_MEMBERS)
        .then()
            .statusCode(200)
            .body("content.size()", is(0));
    }

    @DisplayName("Should return pagination metadata, when page and pageSize are given.")
    @Test
    void testListMembers_WhenPaginationParamsGiven_ThenReturnPaginationMetadata() {
        MemberPageDto pageOne = new MemberPageDto()
                .content(List.of(memberJohn))
                .totalElements(2L)
                .totalPages(2)
                .page(0)
                .pageSize(1);
        Mockito.when(memberService.listMembers(eq(null), eq(null), eq(null), eq(0), eq(1))).thenReturn(pageOne);

        given()
            .accept(ContentType.JSON)
            .queryParam("page", 0)
            .queryParam("pageSize", 1)
        .when()
            .get(API_MEMBERS)
        .then()
            .statusCode(200)
            .body("content.size()", is(1))
            .body("totalElements", is(2))
            .body("totalPages", is(2))
            .body("page", is(0))
            .body("pageSize", is(1));
    }

    @DisplayName("Should return second page, when page=1 and pageSize=1 are given.")
    @Test
    void testListMembers_WhenSecondPageRequested_ThenReturnSecondMember() {
        MemberPageDto pageTwo = new MemberPageDto()
                .content(List.of(memberWithId))
                .totalElements(2L)
                .totalPages(2)
                .page(1)
                .pageSize(1);
        Mockito.when(memberService.listMembers(eq(null), eq(null), eq(null), eq(1), eq(1))).thenReturn(pageTwo);

        given()
            .accept(ContentType.JSON)
            .queryParam("page", 1)
            .queryParam("pageSize", 1)
        .when()
            .get(API_MEMBERS)
        .then()
            .statusCode(200)
            .body("content.size()", is(1))
            .body("content.firstName", hasItem("Katharina"))
            .body("page", is(1))
            .body("pageSize", is(1));
    }

    @DisplayName("Should delete John, when John's id is given")
    @Test
    void testDeleteMemberById_WhenMemberidExists_ThenReturn204() {
        Mockito.doNothing()
                .when(memberService)
                .deleteMember(1);
        given()
            .pathParam("memberId", 1)
        .when()
            .delete(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(204);
    }

    @DisplayName("Should return NOT_FOUND http response, when non-existing id is passed")
    @Test
    void testDeleteMemberById_WhenMemberidDoesNotExist_ThenReturnEntityNotFound() {
        Mockito.doThrow(EntityNotFoundException.class)
                .when(memberService)
                .deleteMember(1);
        given()
            .pathParam("memberId", 1)
        .when()
            .delete(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json")
            .body("title", is("Not found"))
            .body("instance", is(API_MEMBERS + "1"));
    }

    @DisplayName("Should return John, when John's id is given.")
    @Test
    void testGetMemberById_WhenIdOfExistingUserGiven_ThenReturnExistingUser() {
        Mockito.when(memberService.getMember(1)).thenReturn(memberJohn);
        given()
            .accept(ContentType.JSON)
            .pathParam("memberId", 1)
        .when()
            .get(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(200)
            .body("firstName", is("John"));
    }

    @DisplayName("Should return NOT_FOUND http response, when id is not an integer (=path not found)")
    @Test
    void testGetMemberById_WhenIdNotInteger_ThenReturnPathNotFound() {
        given()
            .accept(ContentType.JSON)
            .pathParam("memberId", "abc")
        .when()
            .get(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json")
            .body("title", is("Not found"))
            .body("instance", is(API_MEMBERS + "abc"));
    }


    @DisplayName("Should return NOT_FOUND http response, when non-existing id is passed")
    @Test
    void testGetMemberById_WhenNonExistingId_ThenReturnNotFound() {
        Mockito.when(memberService.getMember(1)).thenThrow(EntityNotFoundException.class);
        given()
            .accept(ContentType.JSON)
            .pathParam("memberId", 1)
        .when()
            .get(API_MEMBERS + "{memberId}")
        .then()
            .statusCode(404)
            .contentType("application/problem+json")
            .body("instance", is(API_MEMBERS + "1"));
    }

    @Test
    void updateMemberById() {
    }

    @DisplayName("Should return member, when member is passed")
    @Test
    void testCreateMember_WhenMemberValid_ThenReturnMember() {
        Mockito.when(memberService.createMember(any(), any())).thenReturn(memberWithId);
        Map<String, Object> memberRequestBody = MemberRequestGenerator.createLivingMemberRequestWithAllDataAndNoId();

        MemberRequestGenerator.createMultiPartRequestWithMemberRequestFields(given(), memberRequestBody)
            .accept(ContentType.JSON)
        .when()
            .post(API_MEMBERS)
        .then()
            .statusCode(201)
            .body("id", notNullValue());
    }


    @DisplayName("Should return Validation error, when required fields are missing for member")
    @Test
    void testCreateMember_WhenRequiredFieldsMissing_ThenConstraintViolationError() {
        Map<String, Object> memberRequestBody = MemberRequestGenerator.createLivingMemberRequestWithAllDataAndNoId();
        memberRequestBody.remove("firstName");
        memberRequestBody.remove("lastName");

        ProblemDetailsDto dto = MemberRequestGenerator.createMultiPartRequestWithMemberRequestFields(given(), memberRequestBody)
            .accept(ContentType.JSON)
        .when()
            .post(API_MEMBERS)
        .then()
            .statusCode(400).log().all()
            .extract()
            .body()
            .as(ProblemDetailsDto.class);

        Assertions.assertEquals("Constraint Violation", dto.getTitle());
        Assertions.assertEquals(2, dto.getErrors().size());
    }

    @DisplayName("Should return Validation error, when field wrong pattern")
    @Test
    void testCreateMember_WhenFieldPatternMalformed_ThenConstraintViolationError() {
        Map<String, Object> memberRequestBody = MemberRequestGenerator.createLivingMemberRequestWithAllDataAndNoId();
        memberRequestBody.put("email", "abc");

        ProblemDetailsDto dto = MemberRequestGenerator.createMultiPartRequestWithMemberRequestFields(given(), memberRequestBody)
            .accept(ContentType.JSON)
        .when()
            .post(API_MEMBERS)
        .then()
            .statusCode(400)
            .extract()
            .body()
            .as(ProblemDetailsDto.class);

        Assertions.assertEquals("Constraint Violation", dto.getTitle());
        Assertions.assertEquals(1, dto.getErrors().size());
    }

    @DisplayName("Should return Bad Request error, when field wrong type")
    @Test
    void testCreateMember_WhenFieldTypeMalformed_ThenBadRequestError() {
        Map<String, Object> memberRequestBody = MemberRequestGenerator.createLivingMemberRequestWithAllDataAndNoId();
        memberRequestBody.put("birthDate", "abc");

        ProblemDetailsDto dto = MemberRequestGenerator.createMultiPartRequestWithMemberRequestFields(given(), memberRequestBody)
            .accept(ContentType.JSON)
        .when()
            .post(API_MEMBERS)
        .then()
            .log().all()
            .statusCode(400)
            .extract()
            .body()
            .as(ProblemDetailsDto.class);

        Assertions.assertEquals("Bad Request", dto.getTitle());
    }
}
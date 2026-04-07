package org.lunskra.adapter.persistence.repository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.lunskra.adapter.persistence.testcontainer.MySQLTestContainerResource;
import org.lunskra.adapter.utils.MemberGenerator;
import org.lunskra.core.domain.Member;
import org.lunskra.core.domain.MemberPage;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


@QuarkusTest
@QuarkusTestResource(value = MySQLTestContainerResource.class, restrictToAnnotatedClass = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemberRepositoryPortImplTest {

    @Inject
    MemberRepositoryPortImpl memberRepositoryPort;

    @Order(1)
    @DisplayName("Should return all 13 members, when no filter is given")
    @Test
    void listMembers() {
        // Given
        Integer expectedTotalFamilyMembers = 13;

        // When
        MemberPage page = memberRepositoryPort.listMembers(null, null, null, 0, 100);

        // Then
        Assertions.assertEquals(expectedTotalFamilyMembers, page.members().size());
        Assertions.assertEquals(13L, page.totalElements());
    }

    @Order(2)
    @DisplayName("Should return all members with family name Horn, when filter 'Horn' for lastName is set")
    @Test
    void testListMembers_WhenFilterForFamilyNameHorn_ReturnAllHornMembers() {
        // Given
        Integer expectedHornFamilyMembers = 8;
        String lastName = "Horn";

        // When
        MemberPage page = memberRepositoryPort.listMembers(null, lastName, null, 0, 100);
        Member firstMember = page.members().getFirst();

        // Then
        Assertions.assertEquals(expectedHornFamilyMembers, page.members().size());
        Assertions.assertEquals(lastName, firstMember.getLastName());
    }

    @Order(3)
    @DisplayName("Should return all members with family name Friedenthal, when filter 'ieden' for lastName is set")
    @Test
    void testListMembers_WhenFilterForFamilyContainingIeden_ReturnAllFriedenthalMembers() {
        // Given
        Integer expectedFriedenthalFamilyMembers = 3;
        String lastNameSubstring = "ieden";
        String expectedLastName = "Friedenthal";

        // When
        MemberPage page = memberRepositoryPort.listMembers(null, lastNameSubstring, null, 0, 100);
        Member firstMember = page.members().getFirst();

        // Then
        Assertions.assertEquals(expectedFriedenthalFamilyMembers, page.members().size());
        Assertions.assertEquals(expectedLastName, firstMember.getLastName());
    }

    @Order(4)
    @DisplayName("Should return empty list, when filter 'xxx' for lastName is set")
    @Test
    void testListMembers_WhenFilterForLastnameContainingXXX_ReturnEmptyList() {
        // Given
        String lastName = "xxx";

        // When
        MemberPage page = memberRepositoryPort.listMembers(null, lastName, null, 0, 100);

        // Then
        Assertions.assertEquals(List.of(), page.members());
        Assertions.assertEquals(0L, page.totalElements());
    }

    @Order(5)
    @DisplayName("Should return Hans Horn, when filter 'Hans' for firstName is set")
    @Test
    void testListMembers_WhenFilterForFirstnameFritz_ReturnAllFriedenthalMembers() {
        // Given
        String firstName = "Hans";

        // When
        MemberPage page = memberRepositoryPort.listMembers(firstName, null, null, 0, 100);

        // Then
        Assertions.assertEquals(1, page.members().size());
        Assertions.assertEquals(firstName, page.members().getFirst().getFirstName());
    }

    @Order(6)
    @DisplayName("Should return members Sofia and Maria, when filter 'ia' for firstName is set")
    @Test
    void testListMembers_WhenFilterForFirstnameContainingIa_ReturnSofiaMaria() {
        // Given
        String firstNameSubstring = "ia";
        String expectedFirstName = "Sofia";

        // When
        MemberPage page = memberRepositoryPort.listMembers(firstNameSubstring, null, null, 0, 100);

        // Then
        Assertions.assertEquals(2, page.members().size());
        Assertions.assertTrue(
                page.members().stream().anyMatch(member -> Objects.equals(expectedFirstName, member.getFirstName()))
        );
    }

    @Order(7)
    @DisplayName("Should return empty list, when filter 'xxx' for firstName is set")
    @Test
    void testListMembers_WhenFilterForFirstnameContainingXXX_ReturnEmptyList() {
        // Given
        String firstName = "xxx";

        // When
        MemberPage page = memberRepositoryPort.listMembers(firstName, null, null, 0, 100);

        // Then
        Assertions.assertEquals(List.of(), page.members());
    }

    @Order(8)
    @DisplayName("Should return Sofia and Naomi, when filter '1990-01-01' for birthDate is set")
    @Test
    void testListMembers_WhenFilterForBirthdate19900101_ReturnSofiaNaomi() {
        // Given
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        String expectedFirstName = "Naomi";

        // When
        MemberPage page = memberRepositoryPort.listMembers(null, null, birthDate, 0, 100);

        // Then
        Assertions.assertEquals(2, page.members().size());
        Assertions.assertTrue(
                page.members().stream().anyMatch(member -> Objects.equals(expectedFirstName, member.getFirstName()))
        );
    }

    @Order(9)
    @DisplayName("Should return Maria, when filter 'Maria', 'Cortez', '1991-05-29' for firstName, lastName, birthDate is set")
    @Test
    void testListMembers_WhenFilterForMariaCortez19910529_ReturnMaria() {
        // Given
        String firstName = "Maria";
        String lastName = "Cortez";
        LocalDate birthDate = LocalDate.of(1991, 5, 29);

        // When
        MemberPage page = memberRepositoryPort.listMembers(firstName, lastName, birthDate, 0, 100);

        // Then
        Assertions.assertEquals(1, page.members().size());
        Assertions.assertEquals(firstName, page.members().getFirst().getFirstName());
    }

    @Order(9)
    @DisplayName("Should return first page of 5 members with correct metadata, when pageSize=5 is given")
    @Test
    void testListMembers_WhenPaginationGiven_ThenReturnPageWithMetadata() {
        // Given: 13 total members, pageSize=5 → 3 pages; page 0 has 5 members
        MemberPage page = memberRepositoryPort.listMembers(null, null, null, 0, 5);

        Assertions.assertEquals(5, page.members().size());
        Assertions.assertEquals(13L, page.totalElements());
    }

    @Order(9)
    @DisplayName("Should return last page of 3 members, when page=2 and pageSize=5 is given")
    @Test
    void testListMembers_WhenLastPageRequested_ThenReturnRemainingMembers() {
        // Given: 13 total members, pageSize=5 → page 2 (0-based) has 3 members
        MemberPage page = memberRepositoryPort.listMembers(null, null, null, 2, 5);

        Assertions.assertEquals(3, page.members().size());
        Assertions.assertEquals(13L, page.totalElements());
    }

    @Order(10)
    @DisplayName("Should return Omar, when memberId 12 is given")
    @Test
    void testGetMember_WhenMemberIdExists_ThenReturnMember() {
        // Given
        Integer memberId = 10;
        String firstName = "Omar";
        String birthCity = "Hamburg";

        // When
        Member member = memberRepositoryPort.getMember(memberId);

        // Then
        Assertions.assertEquals(
                firstName,
                member.getFirstName()
        );
        Assertions.assertEquals(
                birthCity,
                member.getBirthCity()
        );
    }

    @Order(11)
    @DisplayName("Should return EntityNotFoundException, when member with id does not exist")
    @Test
    void testGetMember_WhenMemberIdDoesNotExist_ThenReturnEntityNotFound() {
        // Given
        Integer memberId = 1000;
        String expectedExceptionMessage = "Member with id 1000 not found";

        // When & Then
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> memberRepositoryPort.getMember(memberId));
        Assertions.assertEquals(
                expectedExceptionMessage,
                exception.getMessage()
        );
    }

    @Order(12)
    @DisplayName("Should create the member and return it with its new id")
    @Test
    void testCreateMember_WhenMemberGiven_ThenCreateMember() {
        // Given
        Member member = MemberGenerator.createLivingMemberWithAllDataAndNoId();

        // When
        Member memberStored = memberRepositoryPort.createMember(member);

        // Then
        Integer id = memberStored.getId();
        Assertions.assertEquals(
                member.getFirstName(),
                memberRepositoryPort.getMember(id).getFirstName()
        );
    }

    @Order(13)
    @DisplayName("Should return error, when member with id is tried to be stored")
    @Test
    void testCreateMember_WhenMemberToBeStoredHasId_ThenXXX() {
        // Given
        Member member = MemberGenerator.createLivingMemberWithAllData();

        // When
        Assertions.assertThrows(
                RuntimeException.class,
                () -> memberRepositoryPort.createMember(member)
        );
    }

    @Order(14)
    @DisplayName("Should delete Hans, when memberId 1 is given")
    @Test
    void testDeleteMember_WhenMemberIdExists_ThenDeleteHans() {
        // Given
        Integer memberId = 1;

        // When
        memberRepositoryPort.deleteMember(1);

        // Then
        Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> memberRepositoryPort.getMember(memberId)
        );
    }

    @Order(15)
    @DisplayName("Should return EntityNotFoundException, when member with id does not exist")
    @Test
    void testDeleteMember_WhenMemberIdDoesNotExist_ThenReturnEntityNotFound() {
        // Given
        Integer memberId = 1000;
        String expectedExceptionMessage = "Member with id 1000 not found";

        // When & Then
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> memberRepositoryPort.deleteMember(memberId));
        Assertions.assertEquals(
                expectedExceptionMessage,
                exception.getMessage()
        );
    }
}

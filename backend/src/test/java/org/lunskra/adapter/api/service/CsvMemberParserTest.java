package org.lunskra.adapter.api.service;

import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lunskra.family_tree.api.model.GenderDto;
import org.lunskra.family_tree.api.model.MemberBulkRequestDto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvMemberParserTest {

    CsvMemberParser parser;

    @BeforeEach
    void setUp() {
        parser = new CsvMemberParser();
    }

    // ──────────────────────────────────────────────
    // happy path — comma delimiter
    // ──────────────────────────────────────────────

    @DisplayName("parse: comma-delimited CSV with all fields → correctly mapped DTO")
    @Test
    void testParse_WhenCommaDelimited_ThenAllFieldsMapped() {
        String csv = """
                id,firstName,lastName,initialLastName,gender,birthDate,deathDate,birthCity,birthCountry,email,telephone,streetAndNumber,postcode,city,occupation,notes
                ,Hans,Müller,Bauer,M,1950-06-15,,Munich,DE,hans@example.com,+49123,Main St 1,80331,Munich,Teacher,Some note
                """;

        List<MemberBulkRequestDto> result = parser.parse(stream(csv));

        assertEquals(1, result.size());
        MemberBulkRequestDto dto = result.get(0);
        assertNull(dto.getId());
        assertEquals("Hans", dto.getFirstName());
        assertEquals("Müller", dto.getLastName());
        assertEquals("Bauer", dto.getInitialLastName());
        assertEquals(GenderDto.M, dto.getGender());
        assertEquals(LocalDate.of(1950, 6, 15), dto.getBirthDate());
        assertNull(dto.getDeathDate());
        assertEquals("Munich", dto.getBirthCity());
        assertEquals("DE", dto.getBirthCountry());
        assertEquals("hans@example.com", dto.getEmail());
        assertEquals("+49123", dto.getTelephone());
        assertEquals("Main St 1", dto.getStreetAndNumber());
        assertEquals("80331", dto.getPostcode());
        assertEquals("Munich", dto.getCity());
        assertEquals("Teacher", dto.getOccupation());
        assertEquals("Some note", dto.getNotes());
    }

    @DisplayName("parse: comma-delimited CSV with explicit id → id mapped")
    @Test
    void testParse_WhenIdProvided_ThenIdMapped() {
        String csv = """
                id,firstName,lastName,gender,birthDate,birthCity,birthCountry
                42,Hans,Müller,M,1950-06-15,Munich,DE
                """;

        List<MemberBulkRequestDto> result = parser.parse(stream(csv));

        assertEquals(42, result.get(0).getId());
    }

    @DisplayName("parse: multiple rows → one DTO per data row")
    @Test
    void testParse_WhenMultipleRows_ThenOneDtoPerRow() {
        String csv = """
                firstName,lastName,gender,birthDate,birthCity,birthCountry
                Hans,Müller,M,1950-06-15,Munich,DE
                Erika,Muster,F,1960-03-01,Berlin,DE
                """;

        List<MemberBulkRequestDto> result = parser.parse(stream(csv));

        assertEquals(2, result.size());
        assertEquals("Hans", result.get(0).getFirstName());
        assertEquals("Erika", result.get(1).getFirstName());
    }

    // ──────────────────────────────────────────────
    // semicolon delimiter auto-detection
    // ──────────────────────────────────────────────

    @DisplayName("parse: semicolon-delimited CSV → delimiter auto-detected, fields correctly mapped")
    @Test
    void testParse_WhenSemicolonDelimited_ThenParsedCorrectly() {
        String csv = """
                firstName;lastName;gender;birthDate;birthCity;birthCountry
                Hans;Müller;M;1950-06-15;Munich;DE
                """;

        List<MemberBulkRequestDto> result = parser.parse(stream(csv));

        assertEquals(1, result.size());
        assertEquals("Hans", result.get(0).getFirstName());
        assertEquals("Munich", result.get(0).getBirthCity());
    }

    // ──────────────────────────────────────────────
    // optional / blank fields → null
    // ──────────────────────────────────────────────

    @DisplayName("parse: blank optional fields → null in DTO")
    @Test
    void testParse_WhenOptionalFieldsBlank_ThenNull() {
        String csv = """
                firstName,lastName,initialLastName,gender,birthDate,deathDate,birthCity,birthCountry,email,telephone,streetAndNumber,postcode,city,occupation,notes
                Hans,Müller,,M,1950-06-15,,Munich,DE,,,,,,,
                """;

        MemberBulkRequestDto dto = parser.parse(stream(csv)).get(0);

        assertNull(dto.getInitialLastName());
        assertNull(dto.getDeathDate());
        assertNull(dto.getEmail());
        assertNull(dto.getTelephone());
        assertNull(dto.getStreetAndNumber());
        assertNull(dto.getPostcode());
        assertNull(dto.getCity());
        assertNull(dto.getOccupation());
        assertNull(dto.getNotes());
    }

    // ──────────────────────────────────────────────
    // gender parsing
    // ──────────────────────────────────────────────

    @DisplayName("parse: gender F → GenderDto.F")
    @Test
    void testParse_WhenGenderF_ThenGenderDtoF() {
        String csv = "firstName,lastName,gender,birthDate,birthCity,birthCountry\nErika,Muster,F,1960-01-01,Berlin,DE\n";
        assertEquals(GenderDto.F, parser.parse(stream(csv)).get(0).getGender());
    }

    @DisplayName("parse: gender D → GenderDto.D")
    @Test
    void testParse_WhenGenderD_ThenGenderDtoD() {
        String csv = "firstName,lastName,gender,birthDate,birthCity,birthCountry\nHans,Test,D,1970-01-01,Hamburg,DE\n";
        assertEquals(GenderDto.D, parser.parse(stream(csv)).get(0).getGender());
    }

    @DisplayName("parse: unrecognised gender value → null")
    @Test
    void testParse_WhenUnknownGender_ThenNull() {
        String csv = "firstName,lastName,gender,birthDate,birthCity,birthCountry\nHans,Test,X,1970-01-01,Hamburg,DE\n";
        assertNull(parser.parse(stream(csv)).get(0).getGender());
    }

    @DisplayName("parse: gender column absent → null")
    @Test
    void testParse_WhenGenderColumnAbsent_ThenNull() {
        String csv = "firstName,lastName,birthDate,birthCity,birthCountry\nHans,Test,1970-01-01,Hamburg,DE\n";
        assertNull(parser.parse(stream(csv)).get(0).getGender());
    }

    // ──────────────────────────────────────────────
    // id parsing
    // ──────────────────────────────────────────────

    @DisplayName("parse: id column present but blank → null")
    @Test
    void testParse_WhenIdBlank_ThenNull() {
        String csv = "id,firstName,lastName,gender,birthDate,birthCity,birthCountry\n,Hans,Müller,M,1950-01-01,Munich,DE\n";
        assertNull(parser.parse(stream(csv)).get(0).getId());
    }

    @DisplayName("parse: id is not an integer → BadRequestException")
    @Test
    void testParse_WhenIdNotInteger_ThenBadRequest() {
        String csv = "id,firstName,lastName,gender,birthDate,birthCity,birthCountry\nabc,Hans,Müller,M,1950-01-01,Munich,DE\n";
        assertThrows(BadRequestException.class, () -> parser.parse(stream(csv)));
    }

    @DisplayName("parse: id is zero → BadRequestException")
    @Test
    void testParse_WhenIdIsZero_ThenBadRequest() {
        String csv = "id,firstName,lastName,gender,birthDate,birthCity,birthCountry\n0,Hans,Müller,M,1950-01-01,Munich,DE\n";
        assertThrows(BadRequestException.class, () -> parser.parse(stream(csv)));
    }

    // ──────────────────────────────────────────────
    // date parsing errors
    // ──────────────────────────────────────────────

    @DisplayName("parse: birthDate in wrong format → BadRequestException")
    @Test
    void testParse_WhenBirthDateWrongFormat_ThenBadRequest() {
        String csv = "firstName,lastName,gender,birthDate,birthCity,birthCountry\nHans,Müller,M,15.06.1950,Munich,DE\n";
        assertThrows(BadRequestException.class, () -> parser.parse(stream(csv)));
    }

    @DisplayName("parse: deathDate in wrong format → BadRequestException")
    @Test
    void testParse_WhenDeathDateWrongFormat_ThenBadRequest() {
        String csv = "firstName,lastName,gender,birthDate,deathDate,birthCity,birthCountry\nHans,Müller,M,1950-06-15,31/12/2020,Munich,DE\n";
        assertThrows(BadRequestException.class, () -> parser.parse(stream(csv)));
    }

    // ──────────────────────────────────────────────
    // file size limit
    // ──────────────────────────────────────────────

    @DisplayName("parse: file exceeds 10 MB → BadRequestException")
    @Test
    void testParse_WhenFileTooLarge_ThenBadRequest() {
        // CsvMemberParser.MAX_CSV_BYTES == 10 * 1024 * 1024; exceed by one byte
        byte[] oversized = new byte[CsvMemberParser.MAX_CSV_BYTES + 1];
        assertThrows(BadRequestException.class,
                () -> parser.parse(new ByteArrayInputStream(oversized)));
    }

    // ──────────────────────────────────────────────
    // empty input
    // ──────────────────────────────────────────────

    @DisplayName("parse: header-only CSV (no data rows) → empty list")
    @Test
    void testParse_WhenHeaderOnly_ThenEmptyList() {
        String csv = "firstName,lastName,gender,birthDate,birthCity,birthCountry\n";
        assertTrue(parser.parse(stream(csv)).isEmpty());
    }

    // ──────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────

    private InputStream stream(String csv) {
        return new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
    }

    private void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }
}

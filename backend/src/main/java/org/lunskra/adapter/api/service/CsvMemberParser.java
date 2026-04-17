package org.lunskra.adapter.api.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.lunskra.family_tree.api.model.GenderDto;
import org.lunskra.family_tree.api.model.MemberBulkRequestDto;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a CSV file into a list of {@link MemberBulkRequestDto} objects.
 * <p>
 * The first row must contain column headers (case-insensitive).
 * Both comma ({@code ,}) and semicolon ({@code ;}) delimiters are accepted —
 * the delimiter is detected automatically from the header line.
 * <p>
 * Expected columns (order does not matter):
 * <pre>
 * firstName, lastName, initialLastName, gender, birthDate, deathDate,
 * birthCity, birthCountry, email, telephone, streetAndNumber, postcode,
 * city, occupation, notes
 * </pre>
 * lat/lng are intentionally excluded — they are resolved from GeoNames after import.
 */
@Slf4j
@ApplicationScoped
public class CsvMemberParser {

    public static final String TEMPLATE_HEADERS =
            "id,firstName,lastName,initialLastName,gender,birthDate,deathDate," +
            "birthCity,birthCountry,email,telephone,streetAndNumber,postcode," +
            "city,occupation,notes";

    /** id is intentionally empty in the example — leave blank to let the DB auto-generate one. */
    public static final String TEMPLATE_EXAMPLE =
            ",Hans,Mustermann,,M,1950-06-15,,Munich,DE,,,,,,Teacher,";

    static final int MAX_CSV_BYTES = 10 * 1024 * 1024; // 10 MB

    public List<MemberBulkRequestDto> parse(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readNBytes(MAX_CSV_BYTES + 1);
            if (bytes.length > MAX_CSV_BYTES) {
                throw new BadRequestException("CSV file exceeds the maximum allowed size of 10 MB");
            }
            String content = new String(bytes, StandardCharsets.UTF_8);
            char delimiter = detectDelimiter(content);

            log.atDebug().addArgument(delimiter).setMessage("Detected CSV delimiter: '{}'").log();

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setDelimiter(delimiter)
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .setIgnoreHeaderCase(true)
                    .build();

            List<MemberBulkRequestDto> result = new ArrayList<>();
            try (Reader reader = new InputStreamReader(
                    new java.io.ByteArrayInputStream(bytes), StandardCharsets.UTF_8)) {
                for (CSVRecord record : format.parse(reader)) {
                    result.add(toDto(record));
                }
            }

            log.atDebug().addArgument(result.size()).setMessage("Parsed {} CSV rows").log();
            return result;
        } catch (IOException e) {
            log.atWarn().setCause(e).setMessage("Failed to read CSV input stream").log();
            throw new BadRequestException("Failed to read CSV file");
        }
    }

    private MemberBulkRequestDto toDto(CSVRecord record) {
        return new MemberBulkRequestDto()
                .id(parseId(record, "id"))
                .firstName(get(record, "firstName"))
                .lastName(get(record, "lastName"))
                .initialLastName(get(record, "initialLastName"))
                .gender(parseGender(get(record, "gender")))
                .birthDate(parseDate(record, "birthDate"))
                .deathDate(parseDate(record, "deathDate"))
                .birthCity(get(record, "birthCity"))
                .birthCountry(get(record, "birthCountry"))
                .email(get(record, "email"))
                .telephone(get(record, "telephone"))
                .streetAndNumber(get(record, "streetAndNumber"))
                .postcode(get(record, "postcode"))
                .city(get(record, "city"))
                .occupation(get(record, "occupation"))
                .notes(get(record, "notes"));
    }

    /**
     * Reads a cell value; returns {@code null} when the column is absent or blank.
     */
    private static String get(CSVRecord record, String column) {
        if (!record.isMapped(column)) {
            return null;
        }
        String value = record.get(column);
        return (value == null || value.isBlank()) ? null : value;
    }

    private static Integer parseId(CSVRecord record, String column) {
        String value = get(record, column);
        if (value == null) return null;
        try {
            int id = Integer.parseInt(value);
            if (id < 1) {
                throw new BadRequestException(
                        "Invalid id at row " + record.getRecordNumber() + ": must be >= 1, got " + id);
            }
            return id;
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    "Invalid id at row " + record.getRecordNumber() + ": '" + value + "' is not a valid integer");
        }
    }

    private static GenderDto parseGender(String value) {
        if (value == null) return null;
        return switch (value.trim().toUpperCase()) {
            case "M" -> GenderDto.M;
            case "F" -> GenderDto.F;
            case "D" -> GenderDto.D;
            default -> null;
        };
    }

    private static LocalDate parseDate(CSVRecord record, String column) {
        String value = get(record, column);
        if (value == null) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BadRequestException(
                    "Invalid date format in column '" + column + "' at row " + record.getRecordNumber()
                    + ": '" + value + "'. Expected ISO format yyyy-MM-dd.");
        }
    }

    /**
     * Determines the delimiter by counting commas vs semicolons in the first line.
     * Falls back to comma if counts are equal or the file has no first line.
     */
    private static char detectDelimiter(String content) {
        String firstLine = content.lines().findFirst().orElse("");
        long commas = firstLine.chars().filter(c -> c == ',').count();
        long semicolons = firstLine.chars().filter(c -> c == ';').count();
        return semicolons > commas ? ';' : ',';
    }
}

package org.lunskra.adapter.api.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.lunskra.family_tree.api.model.GenderDto;
import org.lunskra.family_tree.api.model.MemberDto;

import java.time.LocalDate;

/**
 * Assembles a {@code MemberDto} from individual multipart form parameters.
 * <p>
 * Bean Validation annotations on the method parameters enforce format constraints
 * (name characters, telephone format, street-number format, e-mail) before the DTO
 * is constructed. Violations result in a {@link jakarta.validation.ConstraintViolationException}
 * which is mapped to an HTTP 400 response.
 */
@ApplicationScoped
public final class MemberDtoAssembler {
    private final String NAME_PATTERN = "^$|^[A-Za-zÄÖÜäöüß-]+$";
    private final String NAME_MESSAGE = "Only letters, hyphens and German umlauts are allowed";

    private final String CITY_PATTERN = "^$|^[A-Za-zÄÖÜäöüß()\\s-]+$";
    private final String CITY_MESSAGE = "Only letters, hyphens, spaces, parentheses, and umlauts are allowed";

    private final String TELEPHONE_PATTERN = "^$|^[0-9+\\s()-]{7,20}$";
    private final String TELEPHONE_MESSAGE = "Only digits, spaces, +, (), - (7-20 characters) allowed";

    private static final String STREET_NUMBER_PATTERN = "^$|^[\\p{L} .'-]+\\s+\\d+[\\p{L}\\d/ -]*$";
    private final String STREET_NUMBER_MESSAGE = "Must be like Müllerstraße 12, Hauptstr. 5, Goethe-Straße 7A";

    private final String OCCUPATION_PATTERN = "^$|^[A-Za-zÄÖÜäöüß()\\s-]{1,100}$";
    private final String OCCUPATION_MESSAGE = "Only letters, hyphens, spaces, parentheses, and umlauts are allowed (max. 100 characters)";

    private final String NOTES_PATTERN = "^$|^[A-Za-zÄÖÜäöüß0-9\\s.,!?;:()\\-\"'/@#&+=%]{1,500}$";
    private final String NOTES_MESSAGE = "Only letters, digits, common punctuation, and umlauts are allowed (max. 500 characters)";

    private static boolean isZeroCoordinate(Double value) {
        return value != null && value == 0.0;
    }

    public MemberDto fromFormParams(
        @NotBlank @Pattern(regexp = NAME_PATTERN, message = NAME_MESSAGE) String firstName,
        @NotBlank @Pattern(regexp = NAME_PATTERN, message = NAME_MESSAGE) String lastName,
        @Pattern(regexp = NAME_PATTERN, message = NAME_MESSAGE) String initialLastName,
        @NotNull GenderDto gender,
        @NotNull LocalDate birthDate,
        @NotBlank @Pattern(regexp = CITY_PATTERN, message = CITY_MESSAGE) String birthCity,
        @NotBlank @Pattern(regexp = CITY_PATTERN, message = CITY_MESSAGE) String birthCountry,
        LocalDate deathDate,
        @Email String email,
        @Pattern(regexp = TELEPHONE_PATTERN, message = TELEPHONE_MESSAGE) String telephone,
        @Pattern(regexp = STREET_NUMBER_PATTERN, message = STREET_NUMBER_MESSAGE) String streetAndNumber,
        String postcode,
        @Pattern(regexp = CITY_PATTERN, message = CITY_MESSAGE) String city,
        @Pattern(regexp = OCCUPATION_PATTERN, message = OCCUPATION_MESSAGE) String occupation,
        @Pattern(regexp = NOTES_PATTERN, message = NOTES_MESSAGE) String notes,
        Double birthLat,
        Double birthLng
    ) {
        return new MemberDto()
            .firstName(firstName)
            .lastName(lastName)
            .initialLastName(initialLastName)
            .gender(gender)
            .birthDate(birthDate)
            .birthCity(birthCity)
            .birthCountry(birthCountry)
            .deathDate(deathDate)
            .email(email)
            .telephone(telephone)
            .streetAndNumber(streetAndNumber)
            .postcode(postcode)
            .city(city)
            .occupation(occupation)
            .notes(notes)
            .birthLat(isZeroCoordinate(birthLat) ? null : birthLat)
            .birthLng(isZeroCoordinate(birthLng) ? null : birthLng);
    }
}

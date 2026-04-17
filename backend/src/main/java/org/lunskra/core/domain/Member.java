package org.lunskra.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Domain object representing a person in the family tree.
 * <p>
 * A member holds all personal information needed to identify and display a person,
 * including birth and death dates, contact details, address, and an optional reference
 * to an image stored in object storage.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Builder
public class Member {
    /** Database-generated identifier; {@code null} before the member is persisted. */
    private Integer id;
    private String firstName;
    private String lastName;
    /** Birth surname (e.g. maiden name), optional. */
    private String initialLastName;
    private Gender gender;
    private LocalDate birthDate;
    /** {@code null} if the person is still alive. */
    private LocalDate deathDate;
    private String birthCity;
    private String birthCountry;
    private Double birthLat;
    private Double birthLng;
    private String email;
    private String telephone;
    private String streetAndNumber;
    private String postcode;
    private String city;
    private String occupation;
    private String notes;
    /** Object-storage key of the member's profile image; {@code null} if no image was uploaded. */
    private String imagePath;
}

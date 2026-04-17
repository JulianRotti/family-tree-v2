package org.lunskra.adapter.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.lunskra.core.domain.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity mapped to the {@code members} database table.
 * <p>
 * This class is the persistence representation of the {@link org.lunskra.core.domain.Member}
 * domain object. Conversion between the two is handled by
 * {@link org.lunskra.adapter.persistence.mapper.MemberJpaMapper}.
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "members")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer id;

    @Column(name = "first_name", nullable = false, length = 50)
    public String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    public String lastName;

    @Column(name = "initial_last_name", length = 50)
    public String initialLastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 1)
    public Gender gender;

    @Column(name = "birth_date")
    public LocalDate birthDate;

    @Column(name = "death_date")
    public LocalDate deathDate;

    @Column(name = "birth_city", length = 50)
    public String birthCity;

    @Column(name = "birth_country", length = 50)
    public String birthCountry;

    @Column(name = "birth_lat", precision = 9, scale = 6)
    public BigDecimal birthLat;

    @Column(name = "birth_lng", precision = 9, scale = 6)
    public BigDecimal birthLng;

    @Column(name = "email", length = 50)
    public String email;

    @Column(name = "telephone", length = 20)
    public String telephone;

    @Column(name = "street_number", length = 100)
    public String streetAndNumber;

    @Column(name = "plz", length = 10)
    public String postcode;

    @Column(name = "city", length = 50)
    public String city;

    @Column(name = "occupation", length = 100)
    public String occupation;

    @Column(name = "notes", length = 500)
    public String notes;

    @Column(name = "image_path", length = 255)
    public String imagePath;
}

package org.lunskra.adapter.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity mapped to the read-only {@code geonames_cities} reference table.
 */
@Getter
@Setter
@Entity
@Table(name = "geonames_cities")
public class GeonamesCityEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "ascii_name", length = 200, nullable = false)
    private String asciiName;

    @Column(name = "lat", precision = 9, scale = 6, nullable = false)
    private BigDecimal lat;

    @Column(name = "lng", precision = 9, scale = 6, nullable = false)
    private BigDecimal lng;

    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Column(name = "population", nullable = false)
    private Integer population;
}

package org.lunskra.adapter.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity mapped to the read-only {@code geonames_countries} reference table.
 */
@Getter
@Setter
@Entity
@Table(name = "geonames_countries")
public class GeonamesCountryEntity {

    @Id
    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Column(name = "country_name", length = 100, nullable = false)
    private String countryName;
}

package org.lunskra.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Domain object representing a city from the GeoNames reference dataset.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class GeonamesCity {
    /** City display name. */
    private String name;
    /** ISO 3166-1 alpha-2 country code. */
    private String countryCode;
    /** Human-readable country name. */
    private String countryName;
    /** Latitude in decimal degrees. */
    private Double lat;
    /** Longitude in decimal degrees. */
    private Double lng;
}

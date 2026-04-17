package org.lunskra.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Domain object representing a country from the GeoNames reference dataset.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class GeonamesCountry {
    /** ISO 3166-1 alpha-2 country code (e.g. "DE"). */
    private String code;
    /** Human-readable country name (e.g. "Germany"). */
    private String name;
}

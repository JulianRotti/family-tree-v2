package org.lunskra.port.out;

import org.lunskra.core.domain.GeonamesCity;
import org.lunskra.core.domain.GeonamesCountry;

import java.util.List;
import java.util.Optional;

/**
 * Output port for querying the offline GeoNames reference dataset.
 * <p>
 * Implementations must not call any external geocoding service; all data is
 * resolved from the local {@code geonames_countries} and {@code geonames_cities} tables.
 */
public interface GeocodingRepositoryPort {

    /**
     * Returns up to {@code limit} countries whose name starts with {@code queryPrefix}
     * (case-insensitive).
     *
     * @param queryPrefix prefix to match against {@code country_name}; must not be blank
     * @param limit       maximum number of results to return
     * @return matching countries, possibly empty
     */
    List<GeonamesCountry> searchCountries(String queryPrefix, int limit);

    /**
     * Returns up to {@code limit} cities whose ASCII name starts with {@code queryPrefix}
     * (case-insensitive), ordered by population descending.
     *
     * @param queryPrefix prefix to match against {@code ascii_name}; must not be blank
     * @param countryCode optional ISO 3166-1 alpha-2 code to restrict results; {@code null} means no filter
     * @param limit       maximum number of results to return
     * @return matching cities with country name resolved, possibly empty
     */
    List<GeonamesCity> searchCities(String queryPrefix, String countryCode, int limit);

    /**
     * Resolves coordinates for a city whose ASCII name contains {@code asciiName} as a
     * case-insensitive substring and whose country code equals {@code countryCode}.
     * Returns a result <em>only</em> when the match is unique (exactly one city matches).
     * When zero or multiple entries match, {@link Optional#empty()} is returned so that
     * callers do not silently overwrite ambiguous input.
     *
     * @param asciiName   ASCII city name or partial substring (case-insensitive)
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @return the uniquely matching city, or empty if no match or multiple matches found
     */
    Optional<GeonamesCity> findCityByNameAndCountry(String asciiName, String countryCode);

    /**
     * Resolves an ISO 3166-1 alpha-2 country code from a country name that contains
     * {@code countryName} as a case-insensitive substring. When multiple countries match,
     * the one with the shortest name is returned (closest match).
     * Partial inputs such as {@code "German"} or {@code "ermany"} both resolve to {@code "DE"}.
     *
     * @param countryName full or partial country name, e.g. {@code "German"}
     * @return the matching country code (e.g. {@code "DE"}), or empty if not found
     */
    Optional<String> findCountryCodeByName(String countryName);
}

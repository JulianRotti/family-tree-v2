package org.lunskra.adapter.api.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.family_tree.api.model.CitySuggestionDto;
import org.lunskra.family_tree.api.model.CountrySuggestionDto;
import org.lunskra.port.out.GeocodingRepositoryPort;

import java.util.List;

/**
 * Application-layer service for the GeoNames autocomplete endpoints.
 * <p>
 * Validates query parameters and delegates to {@link GeocodingRepositoryPort}.
 * All data is resolved from the offline GeoNames reference tables; no external
 * geocoding service is called at runtime.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GeocodeService {

    static final int COUNTRY_LIMIT = 20;
    static final int CITY_LIMIT = 8;

    private final GeocodingRepositoryPort geocodingRepositoryPort;

    /**
     * Returns up to 20 countries whose name starts with {@code q} (case-insensitive).
     *
     * @param q name prefix; must be at least 1 character
     * @return matching country suggestions
     */
    public List<CountrySuggestionDto> searchCountries(String q) {
        if (q == null || q.isBlank()) {
            throw new BadRequestException(new IllegalArgumentException("Query parameter 'q' is required and must not be blank"));
        }
        log.atDebug().addArgument(q).setMessage("Searching countries with q='{}'").log();
        return geocodingRepositoryPort.searchCountries(q, COUNTRY_LIMIT)
                .stream()
                .map(c -> new CountrySuggestionDto().code(c.getCode()).name(c.getName()))
                .toList();
    }

    /**
     * Returns up to 8 cities whose ASCII name starts with {@code q} (case-insensitive),
     * ordered by population descending.
     *
     * @param q           ASCII name prefix; must be at least 2 characters
     * @param countryCode optional ISO 3166-1 alpha-2 code to restrict results; {@code null} means no filter
     * @return matching city suggestions
     */
    public List<CitySuggestionDto> searchCities(String q, String countryCode) {
        if (q == null || q.length() < 2) {
            throw new BadRequestException(new IllegalArgumentException("Query parameter 'q' must be at least 2 characters"));
        }
        log.atDebug().addArgument(q).addArgument(countryCode)
                .setMessage("Searching cities with q='{}', countryCode='{}'").log();
        return geocodingRepositoryPort.searchCities(q, countryCode, CITY_LIMIT)
                .stream()
                .map(c -> new CitySuggestionDto()
                        .name(c.getName())
                        .countryCode(c.getCountryCode())
                        .countryName(c.getCountryName())
                        .lat(c.getLat())
                        .lng(c.getLng()))
                .toList();
    }
}

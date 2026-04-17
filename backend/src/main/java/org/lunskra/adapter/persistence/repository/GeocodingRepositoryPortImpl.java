package org.lunskra.adapter.persistence.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.persistence.jpa.GeonamesCityEntity;
import org.lunskra.adapter.persistence.jpa.GeonamesCountryEntity;
import org.lunskra.core.domain.GeonamesCity;
import org.lunskra.core.domain.GeonamesCountry;
import org.lunskra.port.out.GeocodingRepositoryPort;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Persistence adapter that implements {@link GeocodingRepositoryPort} using Quarkus Panache.
 * <p>
 * All queries run against the read-only {@code geonames_countries} and
 * {@code geonames_cities} reference tables loaded from the offline GeoNames dataset.
 * No external geocoding service is called at runtime.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GeocodingRepositoryPortImpl implements GeocodingRepositoryPort {

    private final GeonamesCountryPanacheRepository countryRepo;
    private final GeonamesCityPanacheRepository cityRepo;

    /** {@inheritDoc} */
    @Override
    public List<GeonamesCountry> searchCountries(String queryPrefix, int limit) {
        log.atDebug().addArgument(queryPrefix).addArgument(limit)
                .setMessage("Searching countries with prefix='{}', limit={}").log();
        return countryRepo
                .find("lower(countryName) like lower(concat(:q,'%'))",
                        Parameters.with("q", queryPrefix))
                .page(Page.ofSize(limit))
                .list()
                .stream()
                .map(e -> GeonamesCountry.builder()
                        .code(e.getCountryCode())
                        .name(e.getCountryName())
                        .build())
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<GeonamesCity> searchCities(String queryPrefix, String countryCode, int limit) {
        log.atDebug().addArgument(queryPrefix).addArgument(countryCode).addArgument(limit)
                .setMessage("Searching cities with prefix='{}', countryCode='{}', limit={}").log();

        List<GeonamesCityEntity> cities = cityRepo
                .find("lower(asciiName) like lower(concat(:q,'%')) and (:cc is null or countryCode = :cc) order by population desc",
                        Parameters.with("q", queryPrefix).and("cc", countryCode))
                .page(Page.ofSize(limit))
                .list();

        Map<String, String> countryNames = resolveCountryNames(
                cities.stream().map(GeonamesCityEntity::getCountryCode).collect(Collectors.toSet()));

        return cities.stream()
                .map(c -> GeonamesCity.builder()
                        .name(c.getName())
                        .countryCode(c.getCountryCode())
                        .countryName(countryNames.getOrDefault(c.getCountryCode(), c.getCountryCode()))
                        .lat(c.getLat().doubleValue())
                        .lng(c.getLng().doubleValue())
                        .build())
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<GeonamesCity> findCityByNameAndCountry(String asciiName, String countryCode) {
        log.atDebug().addArgument(asciiName).addArgument(countryCode)
                .setMessage("Resolving city coordinates for asciiName='{}', countryCode='{}'").log();

        List<GeonamesCityEntity> results = cityRepo
                .find("lower(asciiName) like lower(concat('%', :name, '%')) and countryCode = :cc order by population desc",
                        Parameters.with("name", asciiName).and("cc", countryCode))
                .page(Page.ofSize(2))
                .list();

        if (results.size() != 1) {
            return Optional.empty();
        }

        GeonamesCityEntity c = results.get(0);
        String cName = countryRepo.findByIdOptional(countryCode)
                .map(GeonamesCountryEntity::getCountryName)
                .orElse(countryCode);

        return Optional.of(GeonamesCity.builder()
                .name(c.getName())
                .countryCode(c.getCountryCode())
                .countryName(cName)
                .lat(c.getLat().doubleValue())
                .lng(c.getLng().doubleValue())
                .build());
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> findCountryCodeByName(String countryName) {
        log.atDebug().addArgument(countryName).setMessage("Resolving country code for name='{}'").log();
        return countryRepo
                .find("lower(countryName) like lower(concat('%', :name, '%')) order by length(countryName) asc",
                        Parameters.with("name", countryName))
                .firstResultOptional()
                .map(GeonamesCountryEntity::getCountryCode);
    }

    /**
     * Batch-loads country names for the given set of country codes.
     * Returns a map of {@code countryCode -> countryName}.
     */
    private Map<String, String> resolveCountryNames(Set<String> codes) {
        if (codes.isEmpty()) {
            return Map.of();
        }
        return countryRepo
                .find("countryCode in :codes", Parameters.with("codes", codes))
                .list()
                .stream()
                .collect(Collectors.toMap(
                        GeonamesCountryEntity::getCountryCode,
                        GeonamesCountryEntity::getCountryName));
    }
}

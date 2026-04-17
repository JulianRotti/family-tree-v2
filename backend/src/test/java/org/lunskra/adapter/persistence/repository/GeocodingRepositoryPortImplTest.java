package org.lunskra.adapter.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.adapter.persistence.jpa.GeonamesCityEntity;
import org.lunskra.adapter.persistence.jpa.GeonamesCountryEntity;
import org.lunskra.core.domain.GeonamesCity;
import org.lunskra.core.domain.GeonamesCountry;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeocodingRepositoryPortImplTest {

    @Mock
    GeonamesCountryPanacheRepository countryRepo;

    @Mock
    GeonamesCityPanacheRepository cityRepo;

    @Mock
    PanacheQuery<GeonamesCountryEntity> countryQuery;

    @Mock
    PanacheQuery<GeonamesCityEntity> cityQuery;

    private GeocodingRepositoryPortImpl impl;

    @BeforeEach
    void setUp() {
        impl = new GeocodingRepositoryPortImpl(countryRepo, cityRepo);
    }

    // ──────────────────────────────────────────────
    // searchCountries
    // ──────────────────────────────────────────────

    @DisplayName("Should return mapped domain objects when countries are found")
    @Test
    void testSearchCountries_WhenMatchFound_ThenReturnMappedCountries() {
        GeonamesCountryEntity entity = new GeonamesCountryEntity();
        entity.setCountryCode("DE");
        entity.setCountryName("Germany");

        when(countryRepo.find(anyString(), any(Parameters.class))).thenReturn(countryQuery);
        when(countryQuery.page(any(Page.class))).thenReturn(countryQuery);
        when(countryQuery.list()).thenReturn(List.of(entity));

        List<GeonamesCountry> result = impl.searchCountries("Ger", 20);

        assertEquals(1, result.size());
        assertEquals("DE", result.get(0).getCode());
        assertEquals("Germany", result.get(0).getName());
    }

    @DisplayName("Should return empty list when no countries match")
    @Test
    void testSearchCountries_WhenNoMatch_ThenReturnEmptyList() {
        when(countryRepo.find(anyString(), any(Parameters.class))).thenReturn(countryQuery);
        when(countryQuery.page(any(Page.class))).thenReturn(countryQuery);
        when(countryQuery.list()).thenReturn(List.of());

        List<GeonamesCountry> result = impl.searchCountries("xyz", 20);

        assertTrue(result.isEmpty());
    }

    // ──────────────────────────────────────────────
    // searchCities
    // ──────────────────────────────────────────────

    @DisplayName("Should return mapped city with resolved country name when cities are found")
    @Test
    void testSearchCities_WhenMatchFound_ThenReturnMappedCitiesWithCountryName() {
        GeonamesCityEntity cityEntity = buildCityEntity(1, "Berlin", "Berlin", "DE", 3_500_000, 52.52, 13.405);
        GeonamesCountryEntity countryEntity = buildCountryEntity("DE", "Germany");

        // cities query
        when(cityRepo.find(anyString(), any(Parameters.class))).thenReturn(cityQuery);
        when(cityQuery.page(any(Page.class))).thenReturn(cityQuery);
        when(cityQuery.list()).thenReturn(List.of(cityEntity));

        // country batch lookup
        when(countryRepo.find(anyString(), any(Parameters.class))).thenReturn(countryQuery);
        when(countryQuery.list()).thenReturn(List.of(countryEntity));

        List<GeonamesCity> result = impl.searchCities("Ber", "DE", 8);

        assertEquals(1, result.size());
        GeonamesCity city = result.get(0);
        assertEquals("Berlin", city.getName());
        assertEquals("DE", city.getCountryCode());
        assertEquals("Germany", city.getCountryName());
        assertEquals(52.52, city.getLat(), 0.0001);
        assertEquals(13.405, city.getLng(), 0.0001);
    }

    @DisplayName("Should return empty list when no cities match")
    @Test
    void testSearchCities_WhenNoMatch_ThenReturnEmptyList() {
        when(cityRepo.find(anyString(), any(Parameters.class))).thenReturn(cityQuery);
        when(cityQuery.page(any(Page.class))).thenReturn(cityQuery);
        when(cityQuery.list()).thenReturn(List.of());

        List<GeonamesCity> result = impl.searchCities("zzz", null, 8);

        assertTrue(result.isEmpty());
    }

    // ──────────────────────────────────────────────
    // findCityByNameAndCountry
    // ──────────────────────────────────────────────

    @DisplayName("Should return city with resolved country name when exact match found")
    @Test
    void testFindCityByNameAndCountry_WhenMatchFound_ThenReturnCity() {
        GeonamesCityEntity cityEntity = buildCityEntity(1, "Munich", "Munich", "DE", 1_500_000, 48.13, 11.57);
        GeonamesCountryEntity countryEntity = buildCountryEntity("DE", "Germany");

        when(cityRepo.find(anyString(), any(Parameters.class))).thenReturn(cityQuery);
        when(cityQuery.page(any(Page.class))).thenReturn(cityQuery);
        when(cityQuery.list()).thenReturn(List.of(cityEntity));
        when(countryRepo.findByIdOptional("DE")).thenReturn(Optional.of(countryEntity));

        Optional<GeonamesCity> result = impl.findCityByNameAndCountry("Munich", "DE");

        assertTrue(result.isPresent());
        assertEquals("Munich", result.get().getName());
        assertEquals("Germany", result.get().getCountryName());
        assertEquals(48.13, result.get().getLat(), 0.0001);
        assertEquals(11.57, result.get().getLng(), 0.0001);
    }

    @DisplayName("Should return empty Optional when no city matches")
    @Test
    void testFindCityByNameAndCountry_WhenNoMatch_ThenReturnEmpty() {
        when(cityRepo.find(anyString(), any(Parameters.class))).thenReturn(cityQuery);
        when(cityQuery.page(any(Page.class))).thenReturn(cityQuery);
        when(cityQuery.list()).thenReturn(List.of());

        Optional<GeonamesCity> result = impl.findCityByNameAndCountry("Atlantis", "XX");

        assertTrue(result.isEmpty());
    }

    @DisplayName("Should return empty Optional when multiple cities match (ambiguous input)")
    @Test
    void testFindCityByNameAndCountry_WhenMultipleMatches_ThenReturnEmpty() {
        GeonamesCityEntity regensburg = buildCityEntity(2, "Regensburg", "Regensburg", "DE", 153_000, 49.01, 12.10);
        GeonamesCityEntity dresden = buildCityEntity(3, "Dresden", "Dresden", "DE", 554_000, 51.05, 13.74);

        when(cityRepo.find(anyString(), any(Parameters.class))).thenReturn(cityQuery);
        when(cityQuery.page(any(Page.class))).thenReturn(cityQuery);
        when(cityQuery.list()).thenReturn(List.of(dresden, regensburg));

        Optional<GeonamesCity> result = impl.findCityByNameAndCountry("Re", "DE");

        assertTrue(result.isEmpty());
    }

    // ──────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────

    private GeonamesCityEntity buildCityEntity(int id, String name, String asciiName,
                                               String countryCode, int population,
                                               double lat, double lng) {
        GeonamesCityEntity e = new GeonamesCityEntity();
        e.setId(id);
        e.setName(name);
        e.setAsciiName(asciiName);
        e.setCountryCode(countryCode);
        e.setPopulation(population);
        e.setLat(BigDecimal.valueOf(lat));
        e.setLng(BigDecimal.valueOf(lng));
        return e;
    }

    private GeonamesCountryEntity buildCountryEntity(String code, String name) {
        GeonamesCountryEntity e = new GeonamesCountryEntity();
        e.setCountryCode(code);
        e.setCountryName(name);
        return e;
    }
}

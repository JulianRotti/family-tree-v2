package org.lunskra.adapter.api.service;

import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lunskra.core.domain.GeonamesCity;
import org.lunskra.core.domain.GeonamesCountry;
import org.lunskra.family_tree.api.model.CitySuggestionDto;
import org.lunskra.family_tree.api.model.CountrySuggestionDto;
import org.lunskra.port.out.GeocodingRepositoryPort;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeocodeServiceTest {

    @Mock
    GeocodingRepositoryPort geocodingRepositoryPort;

    private GeocodeService geocodeService;

    @BeforeEach
    void setUp() {
        geocodeService = new GeocodeService(geocodingRepositoryPort);
    }

    @DisplayName("Should return mapped country DTOs when repository returns results")
    @Test
    void testSearchCountries_WhenRepositoryReturnsResults_ThenReturnMappedDtos() {
        when(geocodingRepositoryPort.searchCountries(eq("Ger"), anyInt()))
                .thenReturn(List.of(
                        GeonamesCountry.builder().code("DE").name("Germany").build(),
                        GeonamesCountry.builder().code("GH").name("Ghana").build()
                ));

        List<CountrySuggestionDto> result = geocodeService.searchCountries("Ger");

        assertEquals(2, result.size());
        assertEquals("DE", result.get(0).getCode());
        assertEquals("Germany", result.get(0).getName());
        assertEquals("GH", result.get(1).getCode());
        assertEquals("Ghana", result.get(1).getName());
    }

    @DisplayName("Should throw BadRequestException when q is null")
    @Test
    void testSearchCountries_WhenQIsNull_ThenThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> geocodeService.searchCountries(null));
    }

    @DisplayName("Should throw BadRequestException when q is blank")
    @Test
    void testSearchCountries_WhenQIsBlank_ThenThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> geocodeService.searchCountries("  "));
    }

    @DisplayName("Should return empty list when no countries match")
    @Test
    void testSearchCountries_WhenNoMatch_ThenReturnEmptyList() {
        when(geocodingRepositoryPort.searchCountries(eq("xyz"), anyInt()))
                .thenReturn(List.of());

        List<CountrySuggestionDto> result = geocodeService.searchCountries("xyz");

        assertTrue(result.isEmpty());
    }

    @DisplayName("Should pass COUNTRY_LIMIT to repository")
    @Test
    void testSearchCountries_WhenCalled_ThenUsesCorrectLimit() {
        when(geocodingRepositoryPort.searchCountries(eq("A"), eq(GeocodeService.COUNTRY_LIMIT)))
                .thenReturn(List.of());

        geocodeService.searchCountries("A");

        verify(geocodingRepositoryPort).searchCountries("A", GeocodeService.COUNTRY_LIMIT);
    }

    @DisplayName("Should return mapped city DTOs when repository returns results")
    @Test
    void testSearchCities_WhenRepositoryReturnsResults_ThenReturnMappedDtos() {
        when(geocodingRepositoryPort.searchCities(eq("Ber"), eq("DE"), anyInt()))
                .thenReturn(List.of(
                        GeonamesCity.builder()
                                .name("Berlin").countryCode("DE").countryName("Germany")
                                .lat(52.5200).lng(13.4050).build()
                ));

        List<CitySuggestionDto> result = geocodeService.searchCities("Ber", "DE");

        assertEquals(1, result.size());
        CitySuggestionDto city = result.get(0);
        assertEquals("Berlin", city.getName());
        assertEquals("DE", city.getCountryCode());
        assertEquals("Germany", city.getCountryName());
        assertEquals(52.5200, city.getLat());
        assertEquals(13.4050, city.getLng());
    }

    @DisplayName("Should pass null countryCode to repository when not filtered")
    @Test
    void testSearchCities_WhenNoCountryFilter_ThenPassesNullCountryCode() {
        when(geocodingRepositoryPort.searchCities(eq("Mu"), isNull(), anyInt()))
                .thenReturn(List.of());

        geocodeService.searchCities("Mu", null);

        verify(geocodingRepositoryPort).searchCities("Mu", null, GeocodeService.CITY_LIMIT);
    }

    @DisplayName("Should pass CITY_LIMIT to repository")
    @Test
    void testSearchCities_WhenCalled_ThenUsesCorrectLimit() {
        when(geocodingRepositoryPort.searchCities(eq("Pa"), isNull(), eq(GeocodeService.CITY_LIMIT)))
                .thenReturn(List.of());

        geocodeService.searchCities("Pa", null);

        verify(geocodingRepositoryPort).searchCities("Pa", null, GeocodeService.CITY_LIMIT);
    }

    @DisplayName("Should throw BadRequestException when q is null for cities")
    @Test
    void testSearchCities_WhenQIsNull_ThenThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> geocodeService.searchCities(null, null));
    }

    @DisplayName("Should throw BadRequestException when q is only 1 character for cities")
    @Test
    void testSearchCities_WhenQIsSingleChar_ThenThrowBadRequest() {
        assertThrows(BadRequestException.class, () -> geocodeService.searchCities("B", null));
    }

    @DisplayName("Should return empty list when no cities match")
    @Test
    void testSearchCities_WhenNoMatch_ThenReturnEmptyList() {
        when(geocodingRepositoryPort.searchCities(eq("zzz"), isNull(), anyInt()))
                .thenReturn(List.of());

        List<CitySuggestionDto> result = geocodeService.searchCities("zzz", null);

        assertTrue(result.isEmpty());
    }
}

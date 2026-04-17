package org.lunskra.adapter.api.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lunskra.adapter.api.service.GeocodeService;
import org.lunskra.family_tree.api.model.CitySuggestionDto;
import org.lunskra.family_tree.api.model.CountrySuggestionDto;
import org.mockito.Mockito;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestSecurity(user = "testuser", roles = {"view"})
class GeocodeResourceTest {

    @InjectMock
    GeocodeService geocodeService;

    private static final String API_COUNTRIES = "/api/geocode/countries";
    private static final String API_CITIES    = "/api/geocode/cities";

    @BeforeEach
    void setUp() {
        Mockito.when(geocodeService.searchCountries("Ger")).thenReturn(List.of(
                new CountrySuggestionDto().code("DE").name("Germany"),
                new CountrySuggestionDto().code("GH").name("Ghana")
        ));
        Mockito.when(geocodeService.searchCountries("xyz")).thenReturn(List.of());

        Mockito.when(geocodeService.searchCities("Ber", "DE")).thenReturn(List.of(
                new CitySuggestionDto().name("Berlin").countryCode("DE").countryName("Germany").lat(52.52).lng(13.405)
        ));
        Mockito.when(geocodeService.searchCities("Ber", null)).thenReturn(List.of(
                new CitySuggestionDto().name("Berlin").countryCode("DE").countryName("Germany").lat(52.52).lng(13.405),
                new CitySuggestionDto().name("Bern").countryCode("CH").countryName("Switzerland").lat(46.948).lng(7.447)
        ));
        Mockito.when(geocodeService.searchCities("zzz", null)).thenReturn(List.of());

        // validation failures — the service throws when q is missing/too short
        Mockito.when(geocodeService.searchCountries(null))
                .thenThrow(new BadRequestException(new IllegalArgumentException("Query parameter 'q' is required and must not be blank")));
        Mockito.when(geocodeService.searchCities(null, null))
                .thenThrow(new BadRequestException(new IllegalArgumentException("Query parameter 'q' must be at least 2 characters")));
    }

    @DisplayName("Should return two countries when q=Ger")
    @Test
    void testGetCountries_WhenQueryMatchesTwoCountries_ThenReturnBoth() {
        given()
            .accept(ContentType.JSON)
            .queryParam("q", "Ger")
        .when()
            .get(API_COUNTRIES)
        .then()
            .statusCode(200)
            .body("size()", is(2))
            .body("code", hasItem("DE"))
            .body("name", hasItem("Germany"));
    }

    @DisplayName("Should return empty list when q matches nothing")
    @Test
    void testGetCountries_WhenQueryMatchesNothing_ThenReturnEmptyList() {
        given()
            .accept(ContentType.JSON)
            .queryParam("q", "xyz")
        .when()
            .get(API_COUNTRIES)
        .then()
            .statusCode(200)
            .body("size()", is(0));
    }

    @DisplayName("Should return 400 when q is missing")
    @Test
    void testGetCountries_WhenQueryParamMissing_ThenReturn400() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get(API_COUNTRIES)
        .then()
            .statusCode(400);
    }

    @DisplayName("Should return cities filtered by country when country param provided")
    @Test
    void testGetCities_WhenCountryFilterProvided_ThenReturnFilteredCities() {
        given()
            .accept(ContentType.JSON)
            .queryParam("q", "Ber")
            .queryParam("country", "DE")
        .when()
            .get(API_CITIES)
        .then()
            .statusCode(200)
            .body("size()", is(1))
            .body("name", hasItem("Berlin"))
            .body("countryCode", hasItem("DE"));
    }

    @DisplayName("Should return cities from all countries when no country filter")
    @Test
    void testGetCities_WhenNoCountryFilter_ThenReturnAllMatchingCities() {
        given()
            .accept(ContentType.JSON)
            .queryParam("q", "Ber")
        .when()
            .get(API_CITIES)
        .then()
            .statusCode(200)
            .body("size()", is(2))
            .body("name", hasItem("Berlin"))
            .body("name", hasItem("Bern"));
    }

    @DisplayName("Should return empty list when q matches no cities")
    @Test
    void testGetCities_WhenQueryMatchesNothing_ThenReturnEmptyList() {
        given()
            .accept(ContentType.JSON)
            .queryParam("q", "zzz")
        .when()
            .get(API_CITIES)
        .then()
            .statusCode(200)
            .body("size()", is(0));
    }

    @DisplayName("Should return 400 when q is missing for cities")
    @Test
    void testGetCities_WhenQueryParamMissing_ThenReturn400() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get(API_CITIES)
        .then()
            .statusCode(400);
    }
}

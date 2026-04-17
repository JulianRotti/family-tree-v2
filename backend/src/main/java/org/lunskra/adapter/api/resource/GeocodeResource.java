package org.lunskra.adapter.api.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.api.service.GeocodeService;
import org.lunskra.family_tree.api.GeocodeApi;

/**
 * REST adapter for the GeoNames autocomplete endpoints.
 * Implements the {@code GeocodeApi} interface generated from the OpenAPI specification.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GeocodeResource implements GeocodeApi {

    private final GeocodeService geocodeService;

    @RolesAllowed("view")
    @Override
    public Response getCountries(String q) {
        log.atDebug().addArgument(q).setMessage("Searching countries with q='{}'").log();
        return Response.ok(geocodeService.searchCountries(q)).build();
    }

    @RolesAllowed("view")
    @Override
    public Response getCities(String q, String country) {
        log.atDebug().addArgument(q).addArgument(country)
                .setMessage("Searching cities with q='{}', country='{}'").log();
        return Response.ok(geocodeService.searchCities(q, country)).build();
    }
}

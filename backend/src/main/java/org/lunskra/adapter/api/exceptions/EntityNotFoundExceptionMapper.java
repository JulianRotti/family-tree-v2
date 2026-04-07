package org.lunskra.adapter.api.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.family_tree.api.model.ProblemDetailsDto;

import java.net.URI;

/**
 * JAX-RS exception mapper that translates a JPA {@link EntityNotFoundException} thrown
 * by repository adapters into an HTTP 404 Not Found response using the
 * {@code application/problem+json} format (RFC 9457).
 */
@Slf4j
@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(EntityNotFoundException e) {
        log.atWarn().addArgument(uriInfo.getPath()).addArgument(e.getMessage())
                .setMessage("Entity not found at {}: {}").log();

        ProblemDetailsDto problemDetailsDto = new ProblemDetailsDto()
                .title("Not found")
                .status(Response.Status.NOT_FOUND.getStatusCode())
                .type(URI.create("https://api.lunskra.org/problems/not-found"))
                .detail(e.getMessage())
                .instance(uriInfo.getPath());

        return Response
                .status(Response.Status.NOT_FOUND)
                .type("application/problem+json")
                .entity(problemDetailsDto)
                .build();
    }
}

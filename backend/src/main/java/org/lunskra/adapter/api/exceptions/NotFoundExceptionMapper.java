package org.lunskra.adapter.api.exceptions;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.family_tree.api.model.ProblemDetailsDto;

import java.net.URI;

/**
 * JAX-RS exception mapper that translates a JAX-RS {@link NotFoundException} (e.g. an
 * unknown route) into an HTTP 404 Not Found response using the
 * {@code application/problem+json} format (RFC 9457).
 *
 * @see EntityNotFoundExceptionMapper for JPA-level entity-not-found handling
 */
@Slf4j
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(NotFoundException e) {
        log.atWarn().addArgument(uriInfo.getPath()).addArgument(e.getMessage())
                .setMessage("Resource not found at {}: {}").log();

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

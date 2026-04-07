package org.lunskra.adapter.api.exceptions;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.family_tree.api.model.ProblemDetailsDto;

import java.net.URI;

/**
 * JAX-RS exception mapper that translates a {@link BadRequestException} into an
 * HTTP 400 Bad Request response using the {@code application/problem+json} format
 * (RFC 9457). The cause's message is included as the problem detail.
 */
@Slf4j
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(BadRequestException e) {
        log.atWarn().addArgument(uriInfo.getPath()).addArgument(e.getCause().getMessage())
                .setMessage("Bad request at {}: {}").log();

        ProblemDetailsDto problemDetailsDto = new ProblemDetailsDto()
                .title("Bad Request")
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .type(URI.create("https://api.lunskra.org/problems/bad-request"))
                .detail(e.getCause().getMessage())
                .instance(uriInfo.getPath());

        return Response.status(Response.Status.BAD_REQUEST)
                .type("application/problem+json")
                .entity(problemDetailsDto)
                .build();
    }
}

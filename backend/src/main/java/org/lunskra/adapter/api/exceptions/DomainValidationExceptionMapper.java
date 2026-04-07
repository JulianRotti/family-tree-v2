package org.lunskra.adapter.api.exceptions;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.api.mapper.FieldErrorDtoMapper;
import org.lunskra.core.validation.DomainValidationException;
import org.lunskra.family_tree.api.model.ProblemDetailsDto;

import java.net.URI;

/**
 * JAX-RS exception mapper that translates a {@link DomainValidationException} into an
 * HTTP 409 Conflict response following the {@code application/problem+json} format
 * (RFC 9457). The response body contains a list of field-level errors.
 */
@Slf4j
@Provider
public class DomainValidationExceptionMapper implements ExceptionMapper<DomainValidationException> {

    @Context
    UriInfo uriInfo;

    private final FieldErrorDtoMapper fieldErrorDtoMapper;

    @Inject
    public DomainValidationExceptionMapper(FieldErrorDtoMapper fieldErrorDtoMapper) {
        this.fieldErrorDtoMapper = fieldErrorDtoMapper;
    }

    @Override
    public Response toResponse(DomainValidationException e) {
        log.atWarn().addArgument(uriInfo.getPath()).addArgument(e.getMessage())
                .setMessage("Domain validation conflict at {}: {}").log();

        ProblemDetailsDto problemDetailsDto = new ProblemDetailsDto()
                .title("Domain constraint violation")
                .status(Response.Status.CONFLICT.getStatusCode())
                .type(URI.create("https://api.lunskra.org/problems/domain-constraint-violation"))
                .detail(e.getMessage())
                .instance(uriInfo.getPath())
                .errors(fieldErrorDtoMapper.toDto(e.getErrors()));

        return Response
                .status(Response.Status.CONFLICT)
                .type("application/problem+json")
                .entity(problemDetailsDto)
                .build();
    }
}

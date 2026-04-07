package org.lunskra.adapter.api.exceptions;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.family_tree.api.model.FieldErrorDto;
import org.lunskra.family_tree.api.model.ProblemDetailsDto;

import java.net.URI;
import java.util.List;

/**
 * JAX-RS exception mapper that translates a Bean Validation
 * {@link ConstraintViolationException} — typically triggered by invalid form parameters
 * in {@link org.lunskra.adapter.api.mapper.MemberDtoAssembler} — into an HTTP 400 Bad
 * Request response using the {@code application/problem+json} format (RFC 9457).
 * Each constraint violation is converted to a {@code FieldErrorDto}.
 */
@Slf4j
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException e) {
        log.atWarn().addArgument(uriInfo.getPath()).addArgument(e.getMessage())
                .setMessage("Constraint violation at {}: {}").log();

        List<FieldErrorDto> errors = e.getConstraintViolations().stream()
                .map(v -> new FieldErrorDto()
                        .field(v.getPropertyPath().toString())
                        .message(v.getMessage()))
                .toList();

        ProblemDetailsDto problemDetailsDto = new ProblemDetailsDto()
                .title("Constraint Violation")
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .type(URI.create("https://api.lunskra.org/problems/constraint-violation"))
                .detail(e.getMessage())
                .instance(uriInfo.getPath())
                .errors(errors);

        return Response.status(Response.Status.BAD_REQUEST)
                .type("application/problem+json")
                .entity(problemDetailsDto)
                .build();
    }
}

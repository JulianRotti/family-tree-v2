package org.lunskra.core.validation;

import lombok.Getter;

import java.util.List;

/**
 * Thrown when one or more domain validation rules are violated.
 * <p>
 * The exception carries a list of {@link FieldError field errors} that describe
 * exactly which fields failed and why. It is mapped to an HTTP 409 Conflict
 * response by {@code DomainValidationExceptionMapper}.
 */
@Getter
public class DomainValidationException extends RuntimeException {

    /** The individual field-level validation errors that caused this exception. */
    private final List<FieldError> errors;

    /**
     * @param message a human-readable summary of the validation failure
     * @param errors  the individual field errors; must not be {@code null}
     */
    public DomainValidationException(String message, List<FieldError> errors) {
        super(message);
        this.errors = errors;
    }
}

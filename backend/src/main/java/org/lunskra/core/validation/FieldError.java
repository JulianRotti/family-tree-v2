package org.lunskra.core.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Describes a single validation error on a specific field or combination of fields.
 * <p>
 * Instances are collected by {@link Validator} implementations and bundled into a
 * {@link DomainValidationException} when validation fails.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FieldError {
    /** The name of the field (or comma-separated fields) that failed validation. */
    private String field;
    /** A human-readable description of why the field value is invalid. */
    private String message;
}

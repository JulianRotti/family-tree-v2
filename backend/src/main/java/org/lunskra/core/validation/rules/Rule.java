package org.lunskra.core.validation.rules;

import org.lunskra.core.validation.FieldError;

import java.util.Optional;

/**
 * A single validation rule that can be applied to an object of type {@code T}.
 * <p>
 * Rules are identified by a {@link RuleName} so that callers can select only the
 * rules relevant to a particular validation scenario (e.g. creating vs. updating).
 *
 * @param <T> the type of object this rule validates
 */
public interface Rule<T> {

    /**
     * Returns the unique name that identifies this rule.
     *
     * @return the rule name; never {@code null}
     */
    RuleName getRuleName();

    /**
     * Applies this rule to the given object.
     *
     * @param object the object to validate; never {@code null}
     * @return an empty {@link Optional} if the object passes this rule, or a
     *         {@link FieldError} describing the violation if it fails
     */
    Optional<FieldError> apply(T object);
}

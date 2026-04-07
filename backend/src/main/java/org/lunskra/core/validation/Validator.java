package org.lunskra.core.validation;

import org.lunskra.core.validation.rules.Rule;
import org.lunskra.core.validation.rules.RuleName;

import java.util.List;

/**
 * Validates objects of type {@code T}.
 * <p>
 * A validator executes a set of {@link Rule rules} and returns the
 * {@link FieldError field errors} produced by the selected rules.
 *
 * @param <T> type of object to validate
 */
public interface Validator<T> {

    /**
     * Validates the given object using the provided rule names.
     *
     * @param object the object to validate
     * @param ruleNames the rules that should be applied
     * @return list of validation errors, possibly empty, never {@code null}
     */
    List<FieldError> validate(T object, List<RuleName> ruleNames);
}


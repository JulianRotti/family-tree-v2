package org.lunskra.core.validation;

import org.lunskra.core.validation.rules.Rule;
import org.lunskra.core.validation.rules.RuleName;

import java.util.List;
import java.util.Optional;

/**
 * Base implementation of {@link Validator} that contains the common rule execution logic.
 * <p>
 * Implementations only need to provide the list of {@link Rule rules}
 * applicable for the validated type. CDI supplies these rules via injection.
 *
 * @param <T> type of object to validate
 */
public abstract class AbstractValidator<T> implements Validator<T> {

    /**
     * All available rules for the validated type.
     */
    protected List<Rule<T>> rules;

    /**
     * No-args constructor required for CDI proxying.
     */
    protected AbstractValidator() {
    }

    /**
     * Creates the validator with the given rules.
     *
     * @param rules rules to be applied during validation
     */
    protected AbstractValidator(List<Rule<T>> rules) {
        this.rules = rules;
    }

    /**
     * Executes the selected rules and collects their errors.
     */
    @Override
    public List<FieldError> validate(T object, List<RuleName> ruleNames) {
        return rules.stream()
                .filter(rule -> ruleNames.contains(rule.getRuleName()))
                .map(rule -> rule.apply(object))
                .flatMap(Optional::stream)
                .toList();
    }
}



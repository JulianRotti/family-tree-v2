package org.lunskra.core.validation;

import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.validation.rules.Rule;

import java.util.List;

/**
 * {@link Validator} implementation for {@link Relationship}.
 * <p>
 * All {@link Rule} beans targeting {@link Relationship} are injected automatically.
 */
@ApplicationScoped
public class RelationshipValidator extends AbstractValidator<Relationship> {

    /**
     * No-args constructor required for CDI proxying.
     */
    protected RelationshipValidator() {
    }

    /**
     * Creates the validator with all available relationship rules.
     *
     * @param rules rules that can validate a {@link Relationship}
     */
    @Inject
    public RelationshipValidator(@All List<Rule<Relationship>> rules) {
        super(rules);
    }
}



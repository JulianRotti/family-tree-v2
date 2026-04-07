package org.lunskra.core.validation;

import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.lunskra.core.domain.Member;
import org.lunskra.core.validation.rules.Rule;

import java.util.List;

/**
 * {@link Validator} implementation for {@link Member}.
 * <p>
 * All {@link Rule} beans targeting {@link Member} are injected automatically.
 */
@ApplicationScoped
public class MemberValidator extends AbstractValidator<Member> {

    /**
     * No-args constructor required for CDI proxying.
     */
    protected MemberValidator() {
    }

    /**
     * Creates the validator with all available member rules.
     *
     * @param rules rules that can validate a {@link Member}
     */
    @Inject
    public MemberValidator(@All List<Rule<Member>> rules) {
        super(rules);
    }
}



package org.lunskra.core.validation.rules;

import org.junit.jupiter.api.Test;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;
import org.lunskra.core.validation.FieldError;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelfRelationshipRuleTest {

    private final SelfRelationshipRule rule = new SelfRelationshipRule();

    @Test
    void apply_WhenBothIdsAreEqual_ThenReturnError() {
        Relationship relationship = new Relationship(1, 1, RelationshipType.CURRENT_SPOUSE);

        Optional<FieldError> result = rule.apply(relationship);

        assertTrue(result.isPresent());
        assertEquals("firstMemberId", result.get().getField());
    }

    @Test
    void apply_WhenIdsAreDifferent_ThenReturnEmpty() {
        Relationship relationship = new Relationship(1, 2, RelationshipType.CURRENT_SPOUSE);

        Optional<FieldError> result = rule.apply(relationship);

        assertTrue(result.isEmpty());
    }
}

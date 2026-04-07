package org.lunskra.core.utils;

import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;

public class RelationshipGenerator {

    public static Relationship parentRelationship(int parentId, int childId) {
        return new Relationship(parentId, childId, RelationshipType.PARENT);
    }

    public static Relationship marriedSpouseRelationship(int firstId, int secondId) {
        return new Relationship(firstId, secondId, RelationshipType.CURRENT_MARRIED_SPOUSE);
    }

    public static Relationship currentSpouseRelationship(int firstId, int secondId) {
        return new Relationship(firstId, secondId, RelationshipType.CURRENT_SPOUSE);
    }

    public static Relationship exSpouseRelationship(int firstId, int secondId) {
        return new Relationship(firstId, secondId, RelationshipType.EX_SPOUSE);
    }
}

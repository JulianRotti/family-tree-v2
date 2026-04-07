package org.lunskra.core.domain;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Pre-defined, immutable sets of {@link RelationshipType} values used across the
 * domain to classify relationships without scattering repeated {@code Set.of(...)}
 * literals throughout the codebase.
 */
@UtilityClass
public class RelationshipTypeSets {

    /** All spouse-related relationship types (current and former). */
    public final Set<RelationshipType> TYPE_SPOUSE = Set.of(
            RelationshipType.EX_SPOUSE,
            RelationshipType.CURRENT_SPOUSE,
            RelationshipType.CURRENT_MARRIED_SPOUSE);

    /** Only former spouse relationships. */
    public final Set<RelationshipType> TYPE_EX_SPOUSE = Set.of(
            RelationshipType.EX_SPOUSE
    );

    /** Current spouse relationships, regardless of whether the couple is legally married. */
    public final Set<RelationshipType> TYPE_CURRENT_SPOUSE = Set.of(
            RelationshipType.CURRENT_SPOUSE,
            RelationshipType.CURRENT_MARRIED_SPOUSE
    );
}

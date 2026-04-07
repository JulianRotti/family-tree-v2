package org.lunskra.core.domain;

/**
 * Classifies the nature of a {@link Relationship} between two family members.
 */
public enum RelationshipType {

    /**
     * The first member is the parent of the second member.
     * The direction matters: {@code firstMemberId} → parent, {@code secondMemberId} → child.
     */
    PARENT,

    /** The two members are currently married to each other. */
    CURRENT_MARRIED_SPOUSE,

    /** The two members are in a current partnership but not legally married. */
    CURRENT_SPOUSE,

    /** The two members were formerly in a partnership or marriage. */
    EX_SPOUSE,
}

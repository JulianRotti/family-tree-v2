package org.lunskra.core.validation.rules;

/**
 * Canonical names for all available domain-level validation rules.
 * <p>
 * Use-case implementations reference these names to select only the rules
 * appropriate for a given operation (e.g. creation vs. update).
 */
public enum RuleName {

    /** Ensures no member with the same first name, last name and birth date already exists. */
    CREATE_MEMBER_EXIST,

    /** Ensures a member's death date, when present, is not before their birth date. */
    BIRTH_DEATH_DATE,

    /** Ensures no relationship between the same pair of members already exists. */
    CREATE_RELATIONSHIP_EXIST,

    /** Ensures a member cannot have a relationship with themselves. */
    SELF_RELATIONSHIP,

    /** Ensures both members referenced by a relationship actually exist. */
    RELATIONSHIP_MEMBERS_EXIST,

    /** Ensures each member has at most one current spouse at a time. */
    UNIQUE_CURRENT_SPOUSE,

    /** Ensures the designated parent was born before the child in a PARENT relationship. */
    PARENT_NOT_YOUNGER_THAN_CHILD
}

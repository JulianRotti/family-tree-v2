package org.lunskra.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Domain object representing a directed relationship between two family members.
 * <p>
 * Relationships are stored with a {@code firstMemberId} and a {@code secondMemberId}.
 * For spouse types the order is interchangeable; for {@link RelationshipType#PARENT}
 * {@code firstMemberId} identifies the parent and {@code secondMemberId} the child.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class Relationship {

    /** ID of the first member (parent when {@code relationshipType} is {@link RelationshipType#PARENT}). */
    Integer firstMemberId;
    /** ID of the second member (child when {@code relationshipType} is {@link RelationshipType#PARENT}). */
    Integer secondMemberId;
    RelationshipType relationshipType;

}

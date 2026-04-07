package org.lunskra.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Represents the family unit formed by a member and one spouse, together with
 * the subtrees of all children they share.
 * <p>
 * A single {@link FamilyTreeOfMember} may contain multiple {@code FamilyUnit} entries
 * when the member had children with more than one partner.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class FamilyUnit {
    /** ID of the spouse in this family unit. */
    private Integer spouseId;
    /** The relationship type between the member and the spouse (e.g. current married, ex-spouse). */
    private RelationshipType relationship;
    /** Subtrees of the children shared between the member and this spouse, in no particular order. */
    private List<FamilyTreeOfMember> children;
}

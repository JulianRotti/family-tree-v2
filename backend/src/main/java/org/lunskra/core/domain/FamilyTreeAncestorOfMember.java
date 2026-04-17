package org.lunskra.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Node in the recursive family-tree structure, representing one member and its parents.
 * <p>
 * {@code subtreeLength} is a pre-computed horizontal measurement (in the same unit
 * as the UI spacing parameters) used by the frontend to lay out the node without
 * needing to recurse the tree a second time.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class FamilyTreeAncestorOfMember {
    /** ID of the member this node represents. */
    private Integer memberId;
    /**
     * The generation of that member respective to the trees child member.
     */
    private Integer generation;
    /**
     * Total horizontal width of this member's subtree, derived from node widths and
     * configured spacing values. Used by the frontend for layout calculations.
     */
    private Float subtreeLength;
    /**
     * All known parents of this member.
     */
    private Parents parents;
}

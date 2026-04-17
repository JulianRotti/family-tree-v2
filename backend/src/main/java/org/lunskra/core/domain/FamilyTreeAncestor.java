package org.lunskra.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Top-level result object of the family-tree ancestor generation use case.
 * <p>
 * It bundles the recursive tree structure (rooted at the child member),
 * a flat list of all members reachable from that root, and the id of the root member.
 * The flat {@code members} list lets clients look up member details by id while
 * traversing the tree.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class FamilyTreeAncestor {
    /** ID of the member at the root of the tree. */
    private Integer childId;
    /** Total number of family members in the tree. */
    private Integer numberTotal;
    /** Number of living family members in the tree. */
    private Integer numberLiving;
    /** Number of generations in the tree. */
    private Integer numberGenerations;
    /** Recursive tree structure starting at the starting child member. */
    private FamilyTreeAncestorOfMember tree;
    /** Flat list of all members referenced anywhere in {@link #tree}. */
    private List<Member> members;

}

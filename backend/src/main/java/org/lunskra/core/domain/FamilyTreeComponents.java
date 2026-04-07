package org.lunskra.core.domain;

import java.util.List;

/**
 * Raw data fetched from the database that is needed to build a family tree.
 * <p>
 * Both collections cover all members and relationships reachable from the head-of-family
 * member via the {@code get_family_tree} stored procedure.
 *
 * @param members       all members that appear anywhere in the tree (only id, first name and last name are populated)
 * @param relationships all relationships connecting those members
 */
public record FamilyTreeComponents(
        List<Member> members,
        List<Relationship> relationships
) {}

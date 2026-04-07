package org.lunskra.core.service;

import lombok.experimental.UtilityClass;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;
import org.lunskra.core.domain.RelationshipTypeSets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@UtilityClass
public class RelationshipFinder {

    /**
     * Returns a map with Ids of the Ex-Spouses of memberId as keys, and a List of the Ids of their children as values.
     * @param memberId Member-Id for which spouses are to be found
     * @param relationships All relevant relationships of the family tree to be built
     * @return Map of the like: [{ spouseId1: {childId1, childId2, ...}}, { spouseId2: {childId3, childId4, ...}}, ... ]
     */
    public Map<Spouse, List<Integer>> findSpousesWithChildren(Integer memberId, List<Relationship> relationships) {
        Map<Spouse, List<Integer>> map = new HashMap<>();

        relationships.stream()
                // Filter all spouses and ex-spouses of memberId
                .filter(r ->
                        Set.of(r.getFirstMemberId(), r.getSecondMemberId()).contains(memberId) &&
                                RelationshipTypeSets.TYPE_SPOUSE.contains(r.getRelationshipType())
                )
                // map to Spouse
                .map(r ->
                {
                    Integer spouseId = Objects.equals(r.getFirstMemberId(), memberId) ? r.getSecondMemberId() : r.getFirstMemberId();
                    return new Spouse(spouseId, r.getRelationshipType());
                })
                // add key Spouse with list of ids of children as value
                .forEach( s -> {
                            List<Integer> children = findChildren(memberId, s.memberId(), relationships);
                            if (RelationshipTypeSets.TYPE_EX_SPOUSE.contains(s.type()) && !children.isEmpty()) {
                                map.put(s, children);
                            } else if (RelationshipTypeSets.TYPE_CURRENT_SPOUSE.contains(s.type())) {
                                map.put(s, children);
                            }
                        }
                );

        return map;
    }

    /**
     * Finds all children of memberId and spouseId.
     * @param memberId Member-Id
     * @param spouseId Spouse-Id
     * @param relationships All relevant relationships of the family tree to be built
     * @return List of ids of the children of memberId and spouseId
     */
    public List<Integer> findChildren(Integer memberId, Integer spouseId, List<Relationship> relationships) {
        Set<Integer> seen = new HashSet<>();

        // Set.add() returns false if the element was already present.
        // A child appears twice (once per parent), so only children seen a second time are shared children.
        return relationships.stream()
                .filter(r -> Objects.equals(r.getRelationshipType(), RelationshipType.PARENT))
                .filter(r -> Objects.equals(r.getFirstMemberId(), memberId) ||
                        Objects.equals(r.getFirstMemberId(), spouseId))
                .map(Relationship::getSecondMemberId)
                .filter(child -> !seen.add(child))
                .toList();
    }

    /**
     * Record to hold a spouse and the relationship.
     * @param memberId id of the spouse
     * @param type relationship type
     */
    public record Spouse(Integer memberId, RelationshipType type) {};
}

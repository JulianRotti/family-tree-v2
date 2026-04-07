package org.lunskra.port.out;

import jakarta.persistence.EntityNotFoundException;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;

import java.util.List;

public interface RelationshipRepositoryPort {

    /**
     * Returns the relationship between two members. Order of the ids does not matter.
     *
     * @param firstMemberId id of the first member
     * @param secondMemberId id of the second member
     * @return the relationship
     * @throws EntityNotFoundException if no relationship exists for the pair
     */
    Relationship getRelationshipByMemberPair(Integer firstMemberId, Integer secondMemberId);

    /**
     * Lists all relationships.
     *
     * @return list of relationships
     */
    List<Relationship> listRelationships();

    /**
     * Creates a new relationship. Both members must exist.
     *
     * @param relationship relationship to create
     * @return persisted relationship
     * @throws EntityNotFoundException if one of the members does not exist
     */
    Relationship createRelationship(Relationship relationship);

    /**
     * Deletes the relationship between two members.
     *
     * @param firstMemberId id of the first member
     * @param secondMemberId id of the second member
     * @throws EntityNotFoundException if no relationship exists for the pair
     */
    void deleteRelationshipByMemberPair(Integer firstMemberId, Integer secondMemberId);

    /**
     * Updates the relationship type between two members.
     *
     * @param firstMemberId id of the first member
     * @param secondMemberId id of the second member
     * @param relationshipType new relationship type
     * @return updated relationship
     * @throws EntityNotFoundException if no relationship exists for the pair
     */
    Relationship updateRelationshipByMemberPair(
            Integer firstMemberId,
            Integer secondMemberId,
            RelationshipType relationshipType);
}


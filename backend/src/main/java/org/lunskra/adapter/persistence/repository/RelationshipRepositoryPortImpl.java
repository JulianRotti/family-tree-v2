package org.lunskra.adapter.persistence.repository;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lunskra.adapter.persistence.jpa.RelationshipEntity;
import org.lunskra.adapter.persistence.mapper.RelationshipJpaMapper;
import org.lunskra.core.domain.Relationship;
import org.lunskra.core.domain.RelationshipType;
import org.lunskra.port.out.RelationshipRepositoryPort;

import java.util.List;


/**
 * Persistence adapter that implements {@link RelationshipRepositoryPort} using Quarkus
 * Panache and Hibernate ORM.
 * <p>
 * All lookups by member pair are order-independent: the JPQL predicates check both
 * {@code (first, second)} and {@code (second, first)} so callers do not need to know
 * which ID was stored as first or second.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class RelationshipRepositoryPortImpl implements RelationshipRepositoryPort {

    private final RelationshipPanacheRepository relationshipRepository;
    private final RelationshipJpaMapper mapper;

    /** {@inheritDoc} */
    @Override
    public Relationship getRelationshipByMemberPair(Integer firstMemberId, Integer secondMemberId) {
        log.atInfo().addArgument(firstMemberId).addArgument(secondMemberId).setMessage(
                "Get relationship from database for firstMemberId={} and secondMemberId={}"
        ).log();
        return mapper.toDomain(checkIfRelationshipExists(firstMemberId, secondMemberId));
    }

    /** {@inheritDoc} */
    @Override
    public List<Relationship> listRelationships() {
        log.atDebug().setMessage("Fetching all relationships from DB").log();
        return mapper.toDomain(relationshipRepository.listAll());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Relationship createRelationship(Relationship relationship) {
        log.atInfo().addArgument(relationship.getFirstMemberId()).addArgument(relationship.getSecondMemberId())
                .addArgument(relationship.getRelationshipType())
                .setMessage("Persisting relationship between firstMemberId={} and secondMemberId={} of type={} to DB").log();
        RelationshipEntity entity = mapper.toEntity(relationship);
        relationshipRepository.persist(entity);
        return mapper.toDomain(entity);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteRelationshipByMemberPair(Integer firstMemberId, Integer secondMemberId) {
        log.atInfo().addArgument(firstMemberId).addArgument(secondMemberId)
                .setMessage("Deleting relationship between firstMemberId={} and secondMemberId={} from DB").log();
        RelationshipEntity relationship = checkIfRelationshipExists(firstMemberId, secondMemberId);
        relationshipRepository.delete(relationship);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Relationship updateRelationshipByMemberPair(
            Integer firstMemberId,
            Integer secondMemberId,
            RelationshipType relationshipType) {
        log.atInfo().addArgument(firstMemberId).addArgument(secondMemberId).addArgument(relationshipType)
                .setMessage("Updating relationship between firstMemberId={} and secondMemberId={} to type={} in DB").log();
        RelationshipEntity relationship = checkIfRelationshipExists(firstMemberId, secondMemberId);
        relationship.setRelationshipType(relationshipType);
        relationshipRepository.persist(relationship);
        return mapper.toDomain(relationship);
    }

    /**
     * Retrieves the relationship entity for the given pair of members.
     * The order of the ids is interchangeable.
     *
     * @param firstMemberId id of the first member
     * @param secondMemberId id of the second member
     * @return the relationship entity
     * @throws EntityNotFoundException if no relationship exists
     */
    private RelationshipEntity checkIfRelationshipExists(Integer firstMemberId, Integer secondMemberId) {
        String query = """
                (firstMemberId = :firstMemberId and secondMemberId = :secondMemberId)
                or (secondMemberId = :firstMemberId and firstMemberId = :secondMemberId)
                """;
        Parameters parameters = Parameters
                .with("firstMemberId", firstMemberId)
                .and("secondMemberId", secondMemberId);
        return relationshipRepository.find(query, parameters)
                .singleResultOptional()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No relationship found between member ids %d and %d".formatted(firstMemberId, secondMemberId)
                ));
    }
}



package org.lunskra.adapter.persistence.mapper;

import org.lunskra.adapter.persistence.jpa.RelationshipEntity;
import org.lunskra.core.domain.Relationship;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper that converts between the {@link Relationship} domain object and
 * its JPA counterpart {@link RelationshipEntity}.
 * <p>
 * All field names are identical in both types so no explicit {@code @Mapping}
 * annotations are required. The {@link #updateEntity} method performs an in-place
 * update of a managed entity, allowing Hibernate to detect and persist the changes
 * within the same transaction.
 */
@Mapper(componentModel = "jakarta")
public interface RelationshipJpaMapper {

    Relationship toDomain(RelationshipEntity entity);

    List<Relationship> toDomain(List<RelationshipEntity> entities);

    RelationshipEntity toEntity(Relationship domain);

    List<RelationshipEntity> toEntity(List<Relationship> domain);

    void updateEntity(Relationship source, @MappingTarget RelationshipEntity target);

}

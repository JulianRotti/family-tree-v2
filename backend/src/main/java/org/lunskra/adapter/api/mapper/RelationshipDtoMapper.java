package org.lunskra.adapter.api.mapper;

import org.lunskra.core.domain.Relationship;
import org.lunskra.family_tree.api.model.RelationshipDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper that converts between the {@link Relationship} domain object and
 * the API-layer {@code RelationshipDto}.
 * <p>
 * The only non-trivial mapping is the field rename:
 * {@code relationshipType} (domain) ↔ {@code relationship} (DTO).
 */
@Mapper(componentModel = "jakarta")
public interface RelationshipDtoMapper {

    @Mapping(source = "relationship", target = "relationshipType")
    Relationship toDomain(RelationshipDto dto);

    @Mapping(source = "relationshipType", target = "relationship")
    RelationshipDto toDto(Relationship domain);

    List<Relationship> toDomain(List<RelationshipDto> dto);

    List<RelationshipDto> toDto(List<Relationship> domain);
}

package org.lunskra.adapter.api.mapper;

import org.lunskra.core.domain.FamilyTreeAncestor;
import org.lunskra.core.domain.FamilyTreeAncestorOfMember;
import org.lunskra.core.domain.Parents;
import org.lunskra.core.domain.RelationshipType;
import org.lunskra.family_tree.api.model.FamilyTreeAncestorOfMemberDto;
import org.lunskra.family_tree.api.model.FamilyTreeAncestorResponseDto;
import org.lunskra.family_tree.api.model.ParentsDto;
import org.lunskra.family_tree.api.model.SpouseRelationshipTypeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

@Mapper(componentModel = "jakarta", uses = MemberDtoMapper.class)
public interface FamilyTreeAncestorDtoMapper {

    @Mapping(target = "childMemberId", source = "childId")
    FamilyTreeAncestorResponseDto toDto(FamilyTreeAncestor domain);

    FamilyTreeAncestorOfMemberDto toDto(FamilyTreeAncestorOfMember domain);

    ParentsDto toDto(Parents domain);

    @ValueMapping(source = "PARENT", target = MappingConstants.NULL)
    SpouseRelationshipTypeDto toDto(RelationshipType domain);
}

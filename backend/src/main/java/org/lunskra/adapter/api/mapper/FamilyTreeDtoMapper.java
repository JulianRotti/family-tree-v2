package org.lunskra.adapter.api.mapper;

import org.lunskra.core.domain.FamilyTree;
import org.lunskra.core.domain.FamilyTreeOfMember;
import org.lunskra.core.domain.FamilyUnit;
import org.lunskra.core.domain.RelationshipType;
import org.lunskra.family_tree.api.model.FamilyTreeOfMemberDto;
import org.lunskra.family_tree.api.model.FamilyTreeResponseDto;
import org.lunskra.family_tree.api.model.FamilyUnitDto;
import org.lunskra.family_tree.api.model.SpouseRelationshipTypeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

import java.util.List;

/**
 * MapStruct mapper that converts the family-tree domain objects into their API-layer
 * DTO equivalents.
 * <p>
 * Key mapping notes:
 * <ul>
 *   <li>{@code headOfFamilyId} (domain) → {@code headMemberId} (DTO).</li>
 *   <li>{@link RelationshipType#PARENT} maps to {@code null} in
 *       {@code SpouseRelationshipTypeDto} because parent relationships do not appear
 *       on spouse nodes in the tree response.</li>
 *   <li>Member details inside {@code FamilyTreeResponseDto} are mapped by the
 *       delegated {@link MemberDtoMapper}.</li>
 * </ul>
 */
@Mapper(componentModel = "jakarta", uses = MemberDtoMapper.class)
public interface FamilyTreeDtoMapper {

    @Mapping(target = "headMemberId", source = "headOfFamilyId")
    FamilyTreeResponseDto toDto(FamilyTree domain);

    FamilyTreeOfMemberDto toDto(FamilyTreeOfMember domain);

    List<FamilyTreeOfMemberDto> toDtoFamilyTreeOfMemberList(List<FamilyTreeOfMember> domain);

    FamilyUnitDto toDto(FamilyUnit domain);

    List<FamilyUnitDto> toDtoFamilyUnitList(List<FamilyUnit> domain);

    @ValueMapping(source = "PARENT", target = MappingConstants.NULL)
    SpouseRelationshipTypeDto toDto(RelationshipType domain);
}

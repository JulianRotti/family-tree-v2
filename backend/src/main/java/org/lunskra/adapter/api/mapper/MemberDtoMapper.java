package org.lunskra.adapter.api.mapper;

import org.lunskra.core.domain.Member;
import org.lunskra.family_tree.api.model.MemberDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * MapStruct mapper that converts between the {@link Member} domain object and the
 * API-layer {@code MemberDto}.
 * <p>
 * The field rename ({@code imagePath} ↔ {@code imageFilePath}) is the only
 * non-trivial mapping; all other fields share the same name.
 */
@Mapper(componentModel = "jakarta")
public interface MemberDtoMapper {

    @Mappings({
            @Mapping(source = "imageFilePath", target = "imagePath")
    })
    Member toDomain(MemberDto dto);

    List<Member> toDomain(List<MemberDto> dto);

    @Mappings({
            @Mapping(source = "imagePath", target = "imageFilePath")
    })
    MemberDto toDto(Member member);

    List<MemberDto> toDto(List<Member> members);
}

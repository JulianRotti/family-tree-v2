package org.lunskra.adapter.persistence.mapper;

import org.lunskra.adapter.persistence.jpa.MemberEntity;
import org.lunskra.core.domain.Member;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper that converts between the {@link Member} domain object and its
 * JPA counterpart {@link MemberEntity}.
 * <p>
 * All field names are identical in both types so no explicit {@code @Mapping}
 * annotations are required. The {@link #updateEntity} method performs an in-place
 * update of a managed entity, allowing Hibernate to detect and persist the changes
 * within the same transaction.
 */
@Mapper(componentModel = "jakarta")
public interface MemberJpaMapper {

    Member toDomain(MemberEntity entity);

    List<Member> toDomain(List<MemberEntity> entities);

    MemberEntity toEntity(Member member);

    List<MemberEntity> toEntity(List<Member> members);

    void updateEntity(Member source, @MappingTarget MemberEntity target);
}

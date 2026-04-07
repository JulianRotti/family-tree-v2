package org.lunskra.adapter.utils;

import org.lunskra.family_tree.api.model.RelationshipDto;
import org.lunskra.family_tree.api.model.RelationshipTypeDto;
import org.lunskra.family_tree.api.model.RelationshipUpdateRequestDto;

public class RelationshipDtoGenerator {

    public static RelationshipDto createParentRelationshipDto(int firstMemberId, int secondMemberId) {
        return new RelationshipDto(firstMemberId, secondMemberId, RelationshipTypeDto.PARENT);
    }

    public static RelationshipDto createMarriedSpouseRelationshipDto(int firstMemberId, int secondMemberId) {
        return new RelationshipDto(firstMemberId, secondMemberId, RelationshipTypeDto.CURRENT_MARRIED_SPOUSE);
    }

    public static RelationshipUpdateRequestDto createUpdateRequest(RelationshipTypeDto type) {
        return new RelationshipUpdateRequestDto(type);
    }
}

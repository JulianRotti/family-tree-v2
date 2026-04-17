package org.lunskra.core.domain;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Parents and their relationship.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class Parents {
    @Size(min = 0, max = 2)
    List<FamilyTreeAncestorOfMember> parents;

    private RelationshipType relationship;
}

package org.lunskra.adapter.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.TenantId;
import org.lunskra.core.domain.RelationshipType;

/**
 * JPA entity mapped to the {@code relationships} database table.
 * <p>
 * This class is the persistence representation of the
 * {@link org.lunskra.core.domain.Relationship} domain object. Conversion between the
 * two is handled by
 * {@link org.lunskra.adapter.persistence.mapper.RelationshipJpaMapper}.
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "relationships")
public class RelationshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer id;

    @Column(name = "member_1_id")
    public Integer firstMemberId;

    @Column(name = "member_2_id")
    public Integer secondMemberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship")
    public RelationshipType relationshipType;

    @TenantId
    @Column(name = "tenant_id", length = 36, nullable = false, updatable = false)
    public String tenantId;
}

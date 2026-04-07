package org.lunskra.adapter.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.lunskra.adapter.persistence.jpa.MemberEntity;

/**
 * Quarkus Panache repository for {@link MemberEntity}.
 * <p>
 * Inherits all standard CRUD and query operations from
 * {@link io.quarkus.hibernate.orm.panache.PanacheRepositoryBase}. Custom queries are
 * issued directly via the inherited {@code find} methods in
 * {@link MemberRepositoryPortImpl}.
 */
@ApplicationScoped
public class MemberPanacheRepository implements PanacheRepositoryBase<MemberEntity, Integer> {
}

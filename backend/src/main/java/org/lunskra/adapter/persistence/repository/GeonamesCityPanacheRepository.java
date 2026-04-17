package org.lunskra.adapter.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.lunskra.adapter.persistence.jpa.GeonamesCityEntity;

/**
 * Quarkus Panache repository for {@link GeonamesCityEntity}.
 * The underlying table is read-only reference data; no mutations are performed.
 */
@ApplicationScoped
public class GeonamesCityPanacheRepository implements PanacheRepositoryBase<GeonamesCityEntity, Integer> {
}

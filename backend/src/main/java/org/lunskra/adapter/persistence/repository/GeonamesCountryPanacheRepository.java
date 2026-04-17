package org.lunskra.adapter.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.lunskra.adapter.persistence.jpa.GeonamesCountryEntity;

/**
 * Quarkus Panache repository for {@link GeonamesCountryEntity}.
 * The underlying table is read-only reference data; no mutations are performed.
 */
@ApplicationScoped
public class GeonamesCountryPanacheRepository implements PanacheRepositoryBase<GeonamesCountryEntity, String> {
}

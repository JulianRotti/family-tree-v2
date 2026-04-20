package org.lunskra.adapter.security;

import io.quarkus.arc.Arc;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Quarkus {@link TenantResolver} for discriminator-based multitenancy.
 * <p>
 * Delegates to the request-scoped {@link TenantContext} to obtain the current tenant identifier.
 * When no request context is active (e.g. during startup schema validation), the default tenant
 * identifier is returned.
 */
@Slf4j
@ApplicationScoped
@PersistenceUnitExtension
@RequiredArgsConstructor
public class TenantIdentifierResolver implements TenantResolver {

    private final TenantContext tenantContext;

    @Override
    public String getDefaultTenantId() {
        return "default";
    }

    @Override
    public String resolveTenantId() {
        if (!Arc.container().requestContext().isActive()) {
            log.trace("No active request context — returning default tenant identifier");
            return getDefaultTenantId();
        }
        String tenantId = tenantContext.getTenantId();
        log.trace("Hibernate tenant identifier resolved: tenantId={}", tenantId);
        return tenantId;
    }
}

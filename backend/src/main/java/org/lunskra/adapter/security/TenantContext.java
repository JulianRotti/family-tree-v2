package org.lunskra.adapter.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.MDC;

/**
 * Request-scoped bean that extracts the {@code tenant_id} claim from the current security context.
 * <p>
 * In production with Keycloak OIDC, the principal is an {@link JsonWebToken} and the claim is read
 * directly from the JWT. In tests using {@code @TestSecurity(attributes = {"tenant_id=..."})},
 * the value is read from the {@link SecurityIdentity} attributes map.
 * <p>
 * On first resolution the tenant identifier is written to the SLF4J MDC under the key
 * {@code tenantId} so that all subsequent log statements in the same request automatically
 * carry the tenant context. The MDC entry is removed when the request scope is destroyed.
 */
@Slf4j
@RequestScoped
@RequiredArgsConstructor
public class TenantContext {

    static final String MDC_KEY = "tenantId";

    private final SecurityIdentity identity;

    private String cachedTenantId;

    public String getTenantId() {
        if (cachedTenantId != null) {
            return cachedTenantId;
        }
        cachedTenantId = resolve();
        MDC.put(MDC_KEY, cachedTenantId);
        log.debug("Tenant resolved: tenantId={}, principal={}", cachedTenantId, identity.getPrincipal().getName());
        return cachedTenantId;
    }

    @PreDestroy
    void clearMdc() {
        MDC.remove(MDC_KEY);
    }

    private String resolve() {
        if (identity.getPrincipal() instanceof JsonWebToken jwt) {
            Object raw = jwt.claim("tenant_id").orElse(null);
            if (raw instanceof String s && !s.isBlank()) {
                return s;
            }
            if (raw instanceof java.util.Collection<?> col && !col.isEmpty()) {
                return col.iterator().next().toString();
            }
        }
        Object attr = identity.getAttribute("tenant_id");
        if (attr != null) {
            return attr.toString();
        }
        log.warn("No tenant_id claim found in security context for principal={}", identity.getPrincipal().getName());
        throw new ForbiddenException("Missing tenant_id claim");
    }
}

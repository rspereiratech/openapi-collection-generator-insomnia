package com.github.rspereiratech.openapi.collection.generator.insomnia.url;

import com.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.swagger.v3.oas.models.Operation;

/**
 * Default implementation of {@link InsomniaUrlResolver} that prefixes paths with an
 * Insomnia {@code {{ base_url }}} template variable and converts path parameters
 * to Insomnia colon-prefixed syntax.
 */
public class DefaultInsomniaUrlResolver implements InsomniaUrlResolver {

    /**
     * The security applier used for security-related URL adjustments.
     */
    private final SecurityApplier securityApplier;

    /**
     * Constructs a new resolver.
     *
     * @param sec the security applier used for security-related URL adjustments
     */
    public DefaultInsomniaUrlResolver(SecurityApplier sec) {
        this.securityApplier = sec;
    }

    @Override
    public String resolve(String path, Operation op) {
        return "{{ base_url }}" + path.replace("{", ":").replace("}", "");
    }
}

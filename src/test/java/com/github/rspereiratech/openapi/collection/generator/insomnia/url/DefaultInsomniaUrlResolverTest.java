package com.github.rspereiratech.openapi.collection.generator.insomnia.url;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;

import io.swagger.v3.oas.models.Operation;

class DefaultInsomniaUrlResolverTest {

    private final SecurityApplier securityApplier = mock(SecurityApplier.class);
    private final DefaultInsomniaUrlResolver resolver = new DefaultInsomniaUrlResolver(securityApplier);

    @Test
    void resolve_prefixesBaseUrlTemplateVariable() {
        assertEquals("{{ base_url }}/pets", resolver.resolve("/pets", new Operation()));
    }

    @Test
    void resolve_convertsSinglePathParameterToInsomniaSyntax() {
        assertEquals("{{ base_url }}/pets/:petId", resolver.resolve("/pets/{petId}", new Operation()));
    }

    @Test
    void resolve_convertsMultiplePathParameters() {
        assertEquals(
            "{{ base_url }}/owners/:ownerId/pets/:petId",
            resolver.resolve("/owners/{ownerId}/pets/{petId}", new Operation()));
    }

    @Test
    void resolve_handlesRootPath() {
        assertEquals("{{ base_url }}/", resolver.resolve("/", new Operation()));
    }
}

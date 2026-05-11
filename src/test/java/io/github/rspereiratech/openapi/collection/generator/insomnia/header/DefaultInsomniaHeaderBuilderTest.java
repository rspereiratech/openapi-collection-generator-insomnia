package io.github.rspereiratech.openapi.collection.generator.insomnia.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.github.rspereiratech.openapi.collection.generator.core.security.model.HttpHeader;
import io.github.rspereiratech.openapi.collection.generator.core.security.model.SecurityInjection;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaHeader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

class DefaultInsomniaHeaderBuilderTest {

    private final SecurityApplier sec = mock(SecurityApplier.class);
    private final DefaultInsomniaHeaderBuilder builder = new DefaultInsomniaHeaderBuilder(sec);

    @Test
    void build_returnsEmptyList_whenOperationHasNoHeadersOrBodyOrSecurity() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());

        List<InsomniaHeader> headers = builder.build(new Operation(), new OpenAPI());

        assertTrue(headers.isEmpty());
    }

    @Test
    void build_includesOperationHeaderParametersWithEmptyValue() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());
        Operation op = new Operation()
            .addParametersItem(new HeaderParameter().name("X-Trace-Id"))
            .addParametersItem(new QueryParameter().name("page"));

        List<InsomniaHeader> headers = builder.build(op, new OpenAPI());

        assertEquals(List.of(new InsomniaHeader("X-Trace-Id", "")), headers);
    }

    @Test
    void build_addsContentTypeFromRequestBody() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());
        Operation op = new Operation()
            .requestBody(new RequestBody().content(new Content().addMediaType("application/json", new MediaType())));

        List<InsomniaHeader> headers = builder.build(op, new OpenAPI());

        assertEquals(List.of(new InsomniaHeader("Content-Type", "application/json")), headers);
    }

    @Test
    void build_appendsSecurityHeaders() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection(
            List.of(new HttpHeader("Authorization", "Bearer {{token}}")),
            List.of(),
            List.of()));

        List<InsomniaHeader> headers = builder.build(new Operation(), new OpenAPI());

        assertEquals(List.of(new InsomniaHeader("Authorization", "Bearer {{token}}")), headers);
    }

    @Test
    void build_returnsUnmodifiableList() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());

        List<InsomniaHeader> headers = builder.build(new Operation(), new OpenAPI());

        assertThrows(UnsupportedOperationException.class,
            () -> headers.add(new InsomniaHeader("X", "y")));
    }
}

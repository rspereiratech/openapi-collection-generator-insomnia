package io.github.rspereiratech.openapi.collection.generator.insomnia.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.github.rspereiratech.openapi.collection.generator.core.deprecated.DeprecationMarker;
import io.github.rspereiratech.openapi.collection.generator.core.extension.ExtensionContext;
import io.github.rspereiratech.openapi.collection.generator.core.extension.ExtensionProcessorChain;
import io.github.rspereiratech.openapi.collection.generator.core.extension.ExtensionResult;
import io.github.rspereiratech.openapi.collection.generator.core.id.IdGenerator;
import io.github.rspereiratech.openapi.collection.generator.core.link.LinkDescriptionEnricher;
import io.github.rspereiratech.openapi.collection.generator.insomnia.body.InsomniaBodyBuilder;
import io.github.rspereiratech.openapi.collection.generator.insomnia.header.InsomniaHeaderBuilder;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaBody;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaHeader;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaParameter;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaRequest;
import io.github.rspereiratech.openapi.collection.generator.insomnia.parameter.InsomniaParameterBuilder;
import io.github.rspereiratech.openapi.collection.generator.insomnia.url.InsomniaUrlResolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

class DefaultInsomniaRequestBuilderTest {

    private final IdGenerator id = mock(IdGenerator.class);
    private final InsomniaUrlResolver url = mock(InsomniaUrlResolver.class);
    private final InsomniaHeaderBuilder header = mock(InsomniaHeaderBuilder.class);
    private final InsomniaBodyBuilder body = mock(InsomniaBodyBuilder.class);
    private final InsomniaParameterBuilder params = mock(InsomniaParameterBuilder.class);
    private final DeprecationMarker depr = mock(DeprecationMarker.class);
    private final ExtensionProcessorChain extChain = mock(ExtensionProcessorChain.class);
    private final LinkDescriptionEnricher linkEnricher = mock(LinkDescriptionEnricher.class);

    private final DefaultInsomniaRequestBuilder builder =
        new DefaultInsomniaRequestBuilder(id, url, header, body, params, depr, extChain, linkEnricher);

    private void stubDefaultCollaborators() {
        when(id.generate(anyString(), anyString())).thenReturn("req_123");
        when(url.resolve(anyString(), any())).thenReturn("{{ base_url }}/pets");
        when(header.build(any(), any())).thenReturn(List.<InsomniaHeader>of());
        when(body.build(any(), any())).thenReturn((InsomniaBody) null);
        when(params.build(any(), any())).thenReturn(List.<InsomniaParameter>of());
        when(extChain.process(any())).thenReturn(ExtensionResult.noChange());
        when(linkEnricher.enrich(anyString(), anyMap())).thenAnswer(inv -> inv.getArgument(0));
        when(depr.markName(anyString(), anyBoolean())).thenAnswer(inv -> inv.getArgument(0));
        when(depr.markDescription(anyString(), anyBoolean())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void build_usesSummary_whenPresent() {
        stubDefaultCollaborators();
        Operation op = new Operation().summary("List pets").description("Lists all pets");

        InsomniaRequest req = builder.build("/pets", "GET", op, "fld_1", new OpenAPI());

        assertEquals("List pets", req.name());
        assertEquals("Lists all pets", req.description());
        assertEquals("GET", req.method());
        assertEquals("fld_1", req.parentId());
        assertEquals("{{ base_url }}/pets", req.url());
        assertEquals("req_123", req.id());
    }

    @Test
    void build_fallsBackToMethodAndPath_whenSummaryMissing() {
        stubDefaultCollaborators();

        InsomniaRequest req = builder.build("/pets", "GET", new Operation(), "fld_1", new OpenAPI());

        assertEquals("GET /pets", req.name());
        assertTrue(req.description().isEmpty());
    }

    @Test
    void build_appliesExtensionNameOverrideAndDescriptionAppend() {
        when(id.generate(anyString(), anyString())).thenReturn("req_x");
        when(url.resolve(anyString(), any())).thenReturn("u");
        when(header.build(any(), any())).thenReturn(List.<InsomniaHeader>of());
        when(body.build(any(), any())).thenReturn((InsomniaBody) null);
        when(params.build(any(), any())).thenReturn(List.<InsomniaParameter>of());
        when(extChain.process(any())).thenReturn(new ExtensionResult("Overridden name", "appended note"));
        when(linkEnricher.enrich(anyString(), anyMap())).thenAnswer(inv -> inv.getArgument(0));
        when(depr.markName(anyString(), anyBoolean())).thenAnswer(inv -> inv.getArgument(0));
        when(depr.markDescription(anyString(), anyBoolean())).thenAnswer(inv -> inv.getArgument(0));

        InsomniaRequest req = builder.build("/p", "GET",
            new Operation().summary("Original").description("desc"), "f", new OpenAPI());

        assertEquals("Overridden name", req.name());
        assertEquals("desc\n\nappended note", req.description());
    }

    @Test
    void build_invokesDeprecationMarkerWithDeprecatedFlag() {
        stubDefaultCollaborators();
        Operation op = new Operation().summary("X").deprecated(true);

        builder.build("/p", "GET", op, "f", new OpenAPI());

        verify(depr).markName(anyString(), eq(true));
        verify(depr).markDescription(anyString(), eq(true));
    }

    @Test
    void build_passesCollectedLinksToEnricher() {
        stubDefaultCollaborators();
        Link link = new Link().description("see related");
        Operation op = new Operation().responses(new ApiResponses()
            .addApiResponse("200", new ApiResponse().link("self", link)));

        builder.build("/p", "GET", op, "f", new OpenAPI());

        verify(linkEnricher).enrich(anyString(), eq(Map.of("self", link)));
    }

    @Test
    void build_passesExtensionContextWithPathMethodAndCurrentName() {
        stubDefaultCollaborators();
        Operation op = new Operation().summary("List").description("desc");

        builder.build("/pets", "GET", op, "f", new OpenAPI());

        ArgumentCaptor<ExtensionContext> captor = ArgumentCaptor.forClass(ExtensionContext.class);
        verify(extChain).process(captor.capture());
        ExtensionContext ctx = captor.getValue();
        assertEquals("/pets", ctx.path());
        assertEquals("GET", ctx.httpMethod());
        assertEquals("List", ctx.currentName());
        assertEquals("desc", ctx.currentDescription());
        assertSame(op, ctx.operation());
    }
}

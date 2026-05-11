package io.github.rspereiratech.openapi.collection.generator.insomnia.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.github.rspereiratech.openapi.collection.generator.core.config.GenerationConfig;
import io.github.rspereiratech.openapi.collection.generator.core.generator.CollectionGenerationException;
import io.github.rspereiratech.openapi.collection.generator.core.id.IdGenerator;
import io.github.rspereiratech.openapi.collection.generator.core.model.CollectionFormat;
import io.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.github.rspereiratech.openapi.collection.generator.core.security.model.EnvironmentVariable;
import io.github.rspereiratech.openapi.collection.generator.core.security.model.SecurityInjection;
import io.github.rspereiratech.openapi.collection.generator.core.serializer.CollectionSerializer;
import io.github.rspereiratech.openapi.collection.generator.core.server.ServerEnvironment;
import io.github.rspereiratech.openapi.collection.generator.core.server.ServerEnvironmentGenerator;
import io.github.rspereiratech.openapi.collection.generator.insomnia.builder.InsomniaRequestBuilder;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaEnvironment;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaExport;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaRequest;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaRequestGroup;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaResource;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaWorkspace;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.callbacks.Callback;
import io.swagger.v3.oas.models.info.Info;

class InsomniaCollectionGeneratorTest {

    private final IdGenerator id = mock(IdGenerator.class);
    private final InsomniaRequestBuilder reqBuilder = mock(InsomniaRequestBuilder.class);
    private final CollectionSerializer serializer = mock(CollectionSerializer.class);
    private final SecurityApplier security = mock(SecurityApplier.class);
    private final ServerEnvironmentGenerator serverGen = mock(ServerEnvironmentGenerator.class);

    private final InsomniaCollectionGenerator generator =
        new InsomniaCollectionGenerator(id, reqBuilder, serializer, security, serverGen);

    private OpenAPI minimalApi() {
        return new OpenAPI().info(new Info().title("Pets API").description("the pets api"));
    }

    private GenerationConfig config(String name) {
        return new GenerationConfig(new java.io.File("."), CollectionFormat.INSOMNIA, name);
    }

    @SuppressWarnings("unchecked")
    private List<InsomniaResource> captureResources() throws Exception {
        ArgumentCaptor<InsomniaExport> captor = ArgumentCaptor.forClass(InsomniaExport.class);
        verify(serializer).serialize(captor.capture());
        return (List<InsomniaResource>) captor.getValue().resources();
    }

    private void stubBasics() throws Exception {
        when(id.generate(anyString(), anyString())).thenAnswer(inv ->
            inv.getArgument(0) + "_" + inv.getArgument(1));
        when(security.applyGlobal(any())).thenReturn(new SecurityInjection());
        when(serializer.serialize(any())).thenReturn("{}");
    }

    @Test
    void generate_usesApiTitle_whenConfigCollectionNameIsNull() throws Exception {
        stubBasics();
        when(serverGen.generate(any(), anyString())).thenReturn(List.of());
        OpenAPI api = minimalApi();
        api.setPaths(new Paths());

        generator.generate(api, config(null));

        InsomniaWorkspace ws = (InsomniaWorkspace) captureResources().get(0);
        assertEquals("Pets API", ws.name());
        assertEquals("the pets api", ws.description());
    }

    @Test
    void generate_overridesNameWithConfig_whenProvided() throws Exception {
        stubBasics();
        when(serverGen.generate(any(), anyString())).thenReturn(List.of());
        OpenAPI api = minimalApi();
        api.setPaths(new Paths());

        generator.generate(api, config("Custom Name"));

        InsomniaWorkspace ws = (InsomniaWorkspace) captureResources().get(0);
        assertEquals("Custom Name", ws.name());
    }

    @Test
    void generate_createsOneEnvironmentPerServer_andMergesGlobalSecurityIntoFirst() throws Exception {
        stubBasics();
        when(serverGen.generate(any(), anyString())).thenReturn(List.of(
            new ServerEnvironment("dev", "https://dev.api", "dev"),
            new ServerEnvironment("prod", "https://api", "prod")));
        when(security.applyGlobal(any())).thenReturn(new SecurityInjection(
            List.of(),
            List.of(),
            List.of(new EnvironmentVariable("api_key", "{{api_key}}"))));
        OpenAPI api = minimalApi();
        api.setPaths(new Paths());

        generator.generate(api, config(null));

        List<InsomniaEnvironment> envs = captureResources().stream()
            .filter(InsomniaEnvironment.class::isInstance)
            .map(InsomniaEnvironment.class::cast)
            .toList();
        assertEquals(2, envs.size());
        assertEquals("dev", envs.get(0).name());
        assertEquals("https://dev.api", envs.get(0).data().get("base_url"));
        assertEquals("{{api_key}}", envs.get(0).data().get("api_key"));
        assertEquals("prod", envs.get(1).name());
        assertEquals(Map.of("base_url", "https://api"), envs.get(1).data());
    }

    @Test
    void generate_createsFolderPerTag_andRequestsPerOperation() throws Exception {
        stubBasics();
        when(serverGen.generate(any(), anyString())).thenReturn(List.of());
        Operation listOp = new Operation().addTagsItem("pets").summary("List");
        Operation healthOp = new Operation().addTagsItem("ops").summary("Health");
        OpenAPI api = minimalApi();
        api.setPaths(new Paths()
            .addPathItem("/pets", new PathItem().get(listOp))
            .addPathItem("/health", new PathItem().get(healthOp)));
        when(reqBuilder.build(eq("/pets"), eq("GET"), eq(listOp), anyString(), any()))
            .thenReturn(new InsomniaRequest("r1", "request", "fld_pets", "List", "GET", "u", null, List.of(), List.of(), ""));
        when(reqBuilder.build(eq("/health"), eq("GET"), eq(healthOp), anyString(), any()))
            .thenReturn(new InsomniaRequest("r2", "request", "fld_ops", "Health", "GET", "u", null, List.of(), List.of(), ""));

        generator.generate(api, config(null));

        List<InsomniaResource> resources = captureResources();
        List<String> folderNames = new ArrayList<>();
        for (InsomniaResource r : resources) {
            if (r instanceof InsomniaRequestGroup g) folderNames.add(g.name());
        }
        assertEquals(List.of("pets", "ops"), folderNames);
        long requestCount = resources.stream().filter(InsomniaRequest.class::isInstance).count();
        assertEquals(2, requestCount);
    }

    @Test
    void generate_usesDefaultFolder_whenOperationHasNoTags() throws Exception {
        stubBasics();
        when(serverGen.generate(any(), anyString())).thenReturn(List.of());
        Operation op = new Operation().summary("X");
        OpenAPI api = minimalApi();
        api.setPaths(new Paths().addPathItem("/x", new PathItem().get(op)));
        when(reqBuilder.build(anyString(), anyString(), any(), anyString(), any()))
            .thenReturn(new InsomniaRequest("r", "request", "fld_default", "X", "GET", "u", null, List.of(), List.of(), ""));

        generator.generate(api, config(null));

        List<String> folderNames = new ArrayList<>();
        for (InsomniaResource r : captureResources()) {
            if (r instanceof InsomniaRequestGroup g) folderNames.add(g.name());
        }
        assertEquals(List.of("default"), folderNames);
    }

    @Test
    void generate_createsCallbacksFolder_andRequestsForCallbackOperations() throws Exception {
        stubBasics();
        when(serverGen.generate(any(), anyString())).thenReturn(List.of());

        Operation cbOp = new Operation().summary("Cb");
        Callback callback = new Callback();
        callback.addPathItem("{$request.body#/url}", new PathItem().post(cbOp));

        Operation parentOp = new Operation().summary("Parent");
        parentOp.setCallbacks(Map.of("onEvent", callback));

        OpenAPI api = minimalApi();
        api.setPaths(new Paths().addPathItem("/p", new PathItem().get(parentOp)));

        when(reqBuilder.build(eq("/p"), eq("GET"), eq(parentOp), anyString(), any()))
            .thenReturn(new InsomniaRequest("r1", "request", "fld_default", "Parent", "GET", "u", null, List.of(), List.of(), ""));
        when(reqBuilder.build(eq("/callbacks/onEvent"), eq("POST"), eq(cbOp), anyString(), any()))
            .thenReturn(new InsomniaRequest("r2", "request", "fld_callbacks", "Cb", "POST", "u", null, List.of(), List.of(), ""));

        generator.generate(api, config(null));

        List<String> folderNames = new ArrayList<>();
        for (InsomniaResource r : captureResources()) {
            if (r instanceof InsomniaRequestGroup g) folderNames.add(g.name());
        }
        assertTrue(folderNames.contains("Callbacks"));
    }

    @Test
    void generate_returnsSerializedExport() throws Exception {
        stubBasics();
        when(serverGen.generate(any(), anyString())).thenReturn(List.of());
        OpenAPI api = minimalApi();
        api.setPaths(new Paths());
        when(serializer.serialize(any())).thenReturn("{\"_type\":\"export\"}");

        String json = generator.generate(api, config(null));

        assertNotNull(json);
        assertEquals("{\"_type\":\"export\"}", json);
    }

    @Test
    void generate_wrapsAnyExceptionInCollectionGenerationException() throws Exception {
        when(serverGen.generate(any(), anyString())).thenThrow(new RuntimeException("boom"));
        OpenAPI api = minimalApi();
        api.setPaths(new Paths());

        CollectionGenerationException ex = assertThrows(CollectionGenerationException.class,
            () -> generator.generate(api, config(null)));
        assertTrue(ex.getMessage().contains("Insomnia generation failed"));
    }
}

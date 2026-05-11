package io.github.rspereiratech.openapi.collection.generator.insomnia.generator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.rspereiratech.openapi.collection.generator.core.config.GenerationConfig;
import io.github.rspereiratech.openapi.collection.generator.core.generator.CollectionGenerationException;
import io.github.rspereiratech.openapi.collection.generator.core.generator.CollectionGenerator;
import io.github.rspereiratech.openapi.collection.generator.core.id.IdGenerator;
import io.github.rspereiratech.openapi.collection.generator.insomnia.builder.InsomniaRequestBuilder;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaEnvironment;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaExport;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaRequestGroup;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaResource;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaWorkspace;
import io.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.github.rspereiratech.openapi.collection.generator.core.serializer.CollectionSerializer;
import io.github.rspereiratech.openapi.collection.generator.core.server.ServerEnvironment;
import io.github.rspereiratech.openapi.collection.generator.core.server.ServerEnvironmentGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

/**
 * Generates an Insomnia-format JSON collection from an OpenAPI specification.
 *
 * <p>This generator creates a complete Insomnia export containing a workspace,
 * server-based environments, tag-based request group folders, and individual
 * HTTP requests derived from OpenAPI path operations.</p>
 */
public class InsomniaCollectionGenerator implements CollectionGenerator {

    /**
     * Generator for unique Insomnia resource identifiers.
     */
    private final IdGenerator idGenerator;

    /**
     * Builder that creates Insomnia request resources from OpenAPI operations.
     */
    private final InsomniaRequestBuilder requestBuilder;

    /**
     * Serializer used to convert the export model to JSON.
     */
    private final CollectionSerializer serializer;

    /**
     * Applier used to resolve security schemes and inject variables.
     */
    private final SecurityApplier securityApplier;

    /**
     * Generator that produces server environment definitions from the OpenAPI spec.
     */
    private final ServerEnvironmentGenerator serverEnvGenerator;

    /**
     * Constructs a new Insomnia collection generator.
     *
     * @param id        generator for unique resource identifiers
     * @param rb        builder for individual Insomnia requests
     * @param ser       serializer for converting the export model to JSON
     * @param sec       security applier for global security scheme handling
     * @param serverGen generator for server-based environments
     */
    public InsomniaCollectionGenerator(IdGenerator id, InsomniaRequestBuilder rb, CollectionSerializer ser,
            SecurityApplier sec, ServerEnvironmentGenerator serverGen) {
        this.idGenerator = id;
        this.requestBuilder = rb;
        this.serializer = ser;
        this.securityApplier = sec;
        this.serverEnvGenerator = serverGen;
    }

    @Override
    public String generate(OpenAPI openApi, GenerationConfig config) throws CollectionGenerationException {
        try {
            String name = resolveName(openApi, config);
            String wrkId = idGenerator.generate("wrk", name);
            String description = Optional.ofNullable(openApi.getInfo().getDescription()).orElse("");

            List<InsomniaResource> resources = new ArrayList<>();
            resources.add(InsomniaWorkspace.of(wrkId, name, description));
            buildEnvironments(openApi, name, wrkId, resources);
            buildResourcesFromPaths(openApi, wrkId, resources);

            return serializer.serialize(new InsomniaExport(
                    InsomniaExport.TYPE, InsomniaExport.FORMAT_VERSION,
                    Instant.now().toString(), InsomniaExport.SOURCE, resources));
        } catch (Exception e) {
            throw new CollectionGenerationException("Insomnia generation failed", e);
        }
    }

    /**
     * Creates environment resources from server definitions and adds them to the resource list.
     *
     * @param openApi   the OpenAPI specification
     * @param name      the collection name used for ID generation
     * @param wrkId     the parent workspace identifier
     * @param resources the mutable list to which new resources are added
     */
    private void buildEnvironments(OpenAPI openApi, String name, String wrkId, List<InsomniaResource> resources) {
        List<ServerEnvironment> envs = serverEnvGenerator.generate(openApi, name);
        for (int i = 0; i < envs.size(); i++) {
            Map<String, String> data = new LinkedHashMap<>();
            data.put("base_url", envs.get(i).baseUrl());
            if (i == 0) {
                securityApplier.applyGlobal(openApi).variables()
                        .forEach(v -> data.put(v.name(), v.placeholder()));
            }
            resources.add(InsomniaEnvironment.of(
                    idGenerator.generate("env", name + "_" + i), wrkId, envs.get(i).name(), data));
        }
    }

    /**
     * Creates request groups (folders) per tag and individual request resources from all paths.
     *
     * @param openApi   the OpenAPI specification
     * @param wrkId     the workspace identifier to use as parent
     * @param resources the mutable list to which new resources are added
     */
    private void buildResourcesFromPaths(OpenAPI openApi, String wrkId, List<InsomniaResource> resources) {
        Map<String, String> tagToFolder = new LinkedHashMap<>();
        openApi.getPaths().forEach((path, pi) ->
                pi.readOperationsMap().forEach((method, op) -> {
                    String folderId = getOrCreateFolder(tagToFolder, resolveTag(op), wrkId, resources);
                    resources.add(requestBuilder.build(path, method.name(), op, folderId, openApi));
                    buildCallbackResources(op, tagToFolder, wrkId, resources, openApi);
                }));
    }

    /**
     * Processes callback definitions for an operation, creating requests in a "Callbacks" folder.
     *
     * @param op           the OpenAPI operation
     * @param tagToFolder  the tag-to-folder-ID map
     * @param wrkId        the parent workspace identifier
     * @param resources    the mutable list to which new resources are added
     * @param openApi      the OpenAPI specification
     */
    private void buildCallbackResources(Operation op, Map<String, String> tagToFolder, String wrkId,
                                         List<InsomniaResource> resources, OpenAPI openApi) {
        if (op.getCallbacks() == null) {
            return;
        }
        String cbFolder = getOrCreateFolder(tagToFolder, "Callbacks", wrkId, resources);
        op.getCallbacks().forEach((cbName, cb) -> {
            if (cb == null) {
                return;
            }
            cb.forEach((expr, cbPi) ->
                    cbPi.readOperationsMap().forEach((cbMethod, cbOp) ->
                            resources.add(requestBuilder.build(
                                    "/callbacks/" + cbName, cbMethod.name(), cbOp, cbFolder, openApi))));
        });
    }

    /**
     * Returns the folder ID for the given tag, creating a new request group if needed.
     *
     * @param tagToFolder the tag-to-folder-ID map
     * @param tag         the tag name
     * @param wrkId       the parent workspace identifier
     * @param resources   the mutable list to which the new folder is added
     * @return the folder identifier
     */
    private String getOrCreateFolder(Map<String, String> tagToFolder, String tag, String wrkId,
                                      List<InsomniaResource> resources) {
        return tagToFolder.computeIfAbsent(tag, t -> {
            String id = idGenerator.generate("fld", t);
            resources.add(InsomniaRequestGroup.of(id, wrkId, t));
            return id;
        });
    }

    /**
     * Resolves the primary tag for an operation, defaulting to "default" if none is present.
     *
     * @param op the OpenAPI operation
     * @return the first tag name, or "default"
     */
    private String resolveTag(Operation op) {
        return Optional.ofNullable(op.getTags())
            .filter(t -> !t.isEmpty())
            .map(t -> t.get(0))
            .orElse("default");
    }

    /**
     * Resolves the collection name, preferring the plugin configuration over the API title.
     *
     * @param api the OpenAPI specification
     * @param cfg the plugin configuration
     * @return the resolved collection name
     */
    private String resolveName(OpenAPI api, GenerationConfig cfg) {
        return Optional.ofNullable(cfg.collectionName())
            .orElse(api.getInfo().getTitle());
    }
}

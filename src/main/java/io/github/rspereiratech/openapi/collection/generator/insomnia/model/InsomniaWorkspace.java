package io.github.rspereiratech.openapi.collection.generator.insomnia.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Insomnia workspace resource, which is the root container for a collection.
 *
 * @param id          unique identifier of the workspace
 * @param type        resource type (always {@value #TYPE})
 * @param name        display name of the workspace
 * @param description human-readable description
 * @param scope       workspace scope (e.g. {@value #SCOPE})
 */
public record InsomniaWorkspace(
    @JsonProperty("_id") String id,
    @JsonProperty("_type") String type,
    String name,
    String description,
    String scope) implements InsomniaResource {

    /**
     * Insomnia resource type identifier for workspaces.
     */
    public static final String TYPE = "workspace";

    /**
     * Default scope indicating this workspace represents a collection.
     */
    public static final String SCOPE = "collection";

    /**
     * Creates a new workspace with the default type and scope.
     *
     * @param id   unique identifier
     * @param name display name
     * @param desc description
     * @return a new {@link InsomniaWorkspace} instance
     */
    public static InsomniaWorkspace of(String id, String name, String desc) {
        return new InsomniaWorkspace(id, TYPE, name, desc, SCOPE);
    }
}

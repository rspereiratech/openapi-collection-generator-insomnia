package io.github.rspereiratech.openapi.collection.generator.insomnia.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Insomnia request group (folder) that organizes requests within a workspace.
 *
 * @param id          unique identifier of the request group
 * @param type        resource type (always {@value #TYPE})
 * @param parentId    identifier of the parent workspace or group
 * @param name        display name of the folder
 * @param description human-readable description
 */
public record InsomniaRequestGroup(
    @JsonProperty("_id") String id,
    @JsonProperty("_type") String type,
    @JsonProperty("parentId") String parentId,
    String name,
    String description) implements InsomniaResource {

    /**
     * Insomnia resource type identifier for request groups (folders).
     */
    public static final String TYPE = "request_group";

    /**
     * Creates a new request group with the default type and an empty description.
     *
     * @param id       unique identifier
     * @param parentId parent workspace or group identifier
     * @param name     display name
     * @return a new {@link InsomniaRequestGroup} instance
     */
    public static InsomniaRequestGroup of(String id, String parentId, String name) {
        return new InsomniaRequestGroup(id, TYPE, parentId, name, "");
    }
}

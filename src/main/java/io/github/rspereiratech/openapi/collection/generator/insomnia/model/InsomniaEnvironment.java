package io.github.rspereiratech.openapi.collection.generator.insomnia.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Insomnia environment resource containing key-value variable data.
 *
 * @param id       unique identifier of the environment
 * @param type     resource type (always {@value #TYPE})
 * @param parentId identifier of the parent workspace
 * @param name     display name of the environment
 * @param data     key-value pairs of environment variables
 */
public record InsomniaEnvironment(
    @JsonProperty("_id") String id,
    @JsonProperty("_type") String type,
    @JsonProperty("parentId") String parentId,
    String name,
    Map<String, String> data) implements InsomniaResource {

    /**
     * Insomnia resource type identifier for environments.
     */
    public static final String TYPE = "environment";

    /**
     * Creates a new environment with the default type.
     *
     * @param id       unique identifier
     * @param parentId parent workspace identifier
     * @param name     display name
     * @param data     environment variable key-value pairs
     * @return a new {@link InsomniaEnvironment} instance
     */
    public static InsomniaEnvironment of(String id, String parentId, String name, Map<String, String> data) {
        return new InsomniaEnvironment(id, TYPE, parentId, name, data);
    }
}

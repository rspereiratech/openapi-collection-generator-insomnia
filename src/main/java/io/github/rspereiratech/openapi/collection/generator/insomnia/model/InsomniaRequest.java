package io.github.rspereiratech.openapi.collection.generator.insomnia.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an Insomnia HTTP request resource.
 *
 * @param id          unique identifier of the request
 * @param type        resource type (always {@value #TYPE})
 * @param parentId    identifier of the parent group or workspace
 * @param name        display name of the request
 * @param method      HTTP method (e.g. GET, POST)
 * @param url         the request URL, potentially containing template variables
 * @param body        the request body, or {@code null} if none
 * @param headers     list of HTTP headers
 * @param parameters  list of query parameters
 * @param description human-readable description
 */
public record InsomniaRequest(
    @JsonProperty("_id") String id,
    @JsonProperty("_type") String type,
    @JsonProperty("parentId") String parentId,
    String name,
    String method,
    String url,
    InsomniaBody body,
    List<InsomniaHeader> headers,
    List<InsomniaParameter> parameters,
    String description) implements InsomniaResource {

    /**
     * Insomnia resource type identifier for requests.
     */
    public static final String TYPE = "request";
}

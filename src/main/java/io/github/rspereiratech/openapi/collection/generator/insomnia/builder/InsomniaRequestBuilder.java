package io.github.rspereiratech.openapi.collection.generator.insomnia.builder;

import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaRequest;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

/**
 * Builds a complete {@link InsomniaRequest} from an OpenAPI path and operation.
 */
public interface InsomniaRequestBuilder {

    /**
     * Builds an Insomnia request for the given OpenAPI operation.
     *
     * @param path       the API path template (e.g. "/pets/{petId}")
     * @param httpMethod the HTTP method (e.g. "GET", "POST")
     * @param operation  the OpenAPI operation definition
     * @param parentId   the identifier of the parent folder or workspace
     * @param openApi    the full OpenAPI specification for reference resolution
     * @return the fully constructed {@link InsomniaRequest}
     */
    InsomniaRequest build(String path, String httpMethod, Operation operation, String parentId, OpenAPI openApi);
}

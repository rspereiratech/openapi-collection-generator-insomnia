package com.github.rspereiratech.openapi.collection.generator.insomnia.body;

import com.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaBody;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

/**
 * Builds an {@link InsomniaBody} from an OpenAPI operation's request body definition.
 */
public interface InsomniaBodyBuilder {

    /**
     * Builds the request body for the given operation.
     *
     * @param operation the OpenAPI operation to extract the body from
     * @param openApi   the full OpenAPI specification for schema resolution
     * @return the constructed {@link InsomniaBody}, or {@code null} if the operation has no request body
     */
    InsomniaBody build(Operation operation, OpenAPI openApi);
}

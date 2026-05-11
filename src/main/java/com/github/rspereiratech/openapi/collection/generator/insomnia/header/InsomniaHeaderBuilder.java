package com.github.rspereiratech.openapi.collection.generator.insomnia.header;

import java.util.List;

import com.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaHeader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

/**
 * Builds a list of {@link InsomniaHeader} instances from an OpenAPI operation.
 */
public interface InsomniaHeaderBuilder {

    /**
     * Builds headers for the given operation, including header parameters,
     * content-type, and security-related headers.
     *
     * @param operation the OpenAPI operation to extract headers from
     * @param openApi   the full OpenAPI specification for reference resolution
     * @return an unmodifiable list of Insomnia headers
     */
    List<InsomniaHeader> build(Operation operation, OpenAPI openApi);
}

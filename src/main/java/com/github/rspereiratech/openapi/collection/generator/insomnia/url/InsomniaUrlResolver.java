package com.github.rspereiratech.openapi.collection.generator.insomnia.url;

import io.swagger.v3.oas.models.Operation;

/**
 * Resolves an OpenAPI path into a fully qualified URL suitable for an Insomnia request.
 */
public interface InsomniaUrlResolver {

    /**
     * Resolves the given API path and operation into an Insomnia-compatible URL string.
     *
     * @param path      the OpenAPI path template (e.g. "/pets/{petId}")
     * @param operation the OpenAPI operation associated with the path
     * @return the resolved URL string with Insomnia template variables
     */
    String resolve(String path, Operation operation);
}

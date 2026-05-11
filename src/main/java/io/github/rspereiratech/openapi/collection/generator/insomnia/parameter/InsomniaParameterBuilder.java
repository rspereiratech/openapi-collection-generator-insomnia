package io.github.rspereiratech.openapi.collection.generator.insomnia.parameter;

import java.util.List;

import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaParameter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

/**
 * Builds a list of {@link InsomniaParameter} instances from an OpenAPI operation.
 */
public interface InsomniaParameterBuilder {

    /**
     * Builds query parameters for the given operation, including security-injected parameters.
     *
     * @param operation the OpenAPI operation to extract query parameters from
     * @param openApi   the full OpenAPI specification for reference resolution
     * @return an unmodifiable list of Insomnia parameters
     */
    List<InsomniaParameter> build(Operation operation, OpenAPI openApi);
}

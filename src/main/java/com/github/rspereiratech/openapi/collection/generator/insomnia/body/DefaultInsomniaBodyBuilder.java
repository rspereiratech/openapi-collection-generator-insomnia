package com.github.rspereiratech.openapi.collection.generator.insomnia.body;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rspereiratech.openapi.collection.generator.core.example.SchemaExampleGenerator;
import com.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaBody;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;

/**
 * Default implementation of {@link InsomniaBodyBuilder} that generates request body content
 * from OpenAPI examples or schema-based example generation.
 */
public class DefaultInsomniaBodyBuilder implements InsomniaBodyBuilder {

    /**
     * Generator used to produce example values from OpenAPI schemas.
     */
    private final SchemaExampleGenerator gen;

    /**
     * Jackson mapper used to pretty-print request body examples.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructs a new body builder.
     *
     * @param gen the schema example generator used when no explicit examples are available
     */
    public DefaultInsomniaBodyBuilder(SchemaExampleGenerator gen) {
        this.gen = gen;
    }

    @Override
    public InsomniaBody build(Operation op, OpenAPI openApi) {
        return Optional.ofNullable(op.getRequestBody())
            .map(RequestBody::getContent)
            .filter(c -> !c.isEmpty())
            .map(c -> {
                String mime = c.keySet().iterator().next();
                return buildBody(mime, c.get(mime), openApi);
            })
            .orElse(null);
    }

    /**
     * Builds an {@link InsomniaBody} for a specific media type, preferring explicit examples
     * over generated ones. Falls back to an empty JSON object on error.
     *
     * @param mime    the MIME type
     * @param mt      the OpenAPI media type definition
     * @param openApi the full OpenAPI specification
     * @return the constructed body
     */
    private InsomniaBody buildBody(String mime, MediaType mt, OpenAPI openApi) {
        try {
            if (mt.getExamples() != null && !mt.getExamples().isEmpty()) {
                var v = mt.getExamples().values().iterator().next().getValue();
                if (v != null) {
                    return new InsomniaBody(mime, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(v));
                }
            }

            if (mt.getExample() != null) {
                return new InsomniaBody(mime,
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mt.getExample()));
            }

            return new InsomniaBody(mime, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(gen.generate(mt.getSchema(), openApi)));
        } catch (Exception e) {
            return new InsomniaBody(mime, "{}");
        }
    }
}

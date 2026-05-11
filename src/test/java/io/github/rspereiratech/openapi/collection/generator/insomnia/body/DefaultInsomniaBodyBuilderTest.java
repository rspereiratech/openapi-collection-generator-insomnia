package io.github.rspereiratech.openapi.collection.generator.insomnia.body;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.openapi.collection.generator.core.example.SchemaExampleGenerator;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaBody;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;

class DefaultInsomniaBodyBuilderTest {

    private final SchemaExampleGenerator generator = mock(SchemaExampleGenerator.class);
    private final DefaultInsomniaBodyBuilder builder = new DefaultInsomniaBodyBuilder(generator);

    @Test
    void build_returnsNull_whenOperationHasNoRequestBody() {
        assertNull(builder.build(new Operation(), new OpenAPI()));
    }

    @Test
    void build_returnsNull_whenRequestBodyContentIsEmpty() {
        Operation op = new Operation().requestBody(new RequestBody().content(new Content()));
        assertNull(builder.build(op, new OpenAPI()));
    }

    @Test
    void build_prefersNamedExamples_overSingleExampleAndSchema() {
        Map<String, Example> examples = new LinkedHashMap<>();
        examples.put("default", new Example().value(Map.of("id", 42)));
        MediaType mt = new MediaType().examples(examples).example(Map.of("ignored", true)).schema(new ObjectSchema());
        Operation op = new Operation().requestBody(new RequestBody()
            .content(new Content().addMediaType("application/json", mt)));

        InsomniaBody body = builder.build(op, new OpenAPI());

        assertNotNull(body);
        assertEquals("application/json", body.mimeType());
        assertTrue(body.text().contains("\"id\" : 42"));
    }

    @Test
    void build_usesSingleExample_whenNamedExamplesAbsent() {
        MediaType mt = new MediaType().example(Map.of("name", "Rex"));
        Operation op = new Operation().requestBody(new RequestBody()
            .content(new Content().addMediaType("application/json", mt)));

        InsomniaBody body = builder.build(op, new OpenAPI());

        assertNotNull(body);
        assertTrue(body.text().contains("\"name\" : \"Rex\""));
    }

    @Test
    void build_fallsBackToSchemaExampleGenerator_whenNoExamplesProvided() {
        ObjectSchema schema = new ObjectSchema();
        MediaType mt = new MediaType().schema(schema);
        Operation op = new Operation().requestBody(new RequestBody()
            .content(new Content().addMediaType("application/json", mt)));
        when(generator.generate(any(), any())).thenReturn(Map.of("foo", "bar"));

        InsomniaBody body = builder.build(op, new OpenAPI());

        assertNotNull(body);
        assertTrue(body.text().contains("\"foo\" : \"bar\""));
    }

    @Test
    void build_returnsEmptyJsonObject_whenSerializationThrows() {
        ObjectSchema schema = new ObjectSchema();
        MediaType mt = new MediaType().schema(schema);
        Operation op = new Operation().requestBody(new RequestBody()
            .content(new Content().addMediaType("application/json", mt)));
        when(generator.generate(any(), any())).thenThrow(new RuntimeException("boom"));

        InsomniaBody body = builder.build(op, new OpenAPI());

        assertNotNull(body);
        assertEquals("application/json", body.mimeType());
        assertEquals("{}", body.text());
    }
}

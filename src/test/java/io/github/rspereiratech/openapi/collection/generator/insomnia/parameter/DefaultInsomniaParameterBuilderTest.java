package io.github.rspereiratech.openapi.collection.generator.insomnia.parameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.github.rspereiratech.openapi.collection.generator.core.security.model.HttpQueryParam;
import io.github.rspereiratech.openapi.collection.generator.core.security.model.SecurityInjection;
import io.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaParameter;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

class DefaultInsomniaParameterBuilderTest {

    private final SecurityApplier sec = mock(SecurityApplier.class);
    private final DefaultInsomniaParameterBuilder builder = new DefaultInsomniaParameterBuilder(sec);

    @Test
    void build_returnsEmpty_whenOperationHasNoParametersAndNoSecurity() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());
        assertTrue(builder.build(new Operation(), new OpenAPI()).isEmpty());
    }

    @Test
    void build_includesQueryParametersWithEmptyValueAndDescription() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());
        Operation op = new Operation()
            .addParametersItem(new QueryParameter().name("page").description("the page"))
            .addParametersItem(new HeaderParameter().name("X-Trace-Id"));

        List<InsomniaParameter> params = builder.build(op, new OpenAPI());

        assertEquals(List.of(new InsomniaParameter("page", "", "the page")), params);
    }

    @Test
    void build_handlesQueryParameterWithNullDescription() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection());
        Operation op = new Operation().addParametersItem(new QueryParameter().name("limit"));

        List<InsomniaParameter> params = builder.build(op, new OpenAPI());

        assertEquals(List.of(new InsomniaParameter("limit", "", "")), params);
    }

    @Test
    void build_appendsSecurityQueryParamsTaggedAsSecurity() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection(
            List.of(),
            List.of(new HttpQueryParam("api_key", "{{api_key}}")),
            List.of()));

        List<InsomniaParameter> params = builder.build(new Operation(), new OpenAPI());

        assertEquals(List.of(new InsomniaParameter("api_key", "{{api_key}}", "security")), params);
    }

    @Test
    void build_preservesOrder_operationFirstThenSecurity() {
        when(sec.apply(any(), any())).thenReturn(new SecurityInjection(
            List.of(),
            List.of(new HttpQueryParam("api_key", "{{api_key}}")),
            List.of()));
        Operation op = new Operation().addParametersItem(new QueryParameter().name("page"));

        List<InsomniaParameter> params = builder.build(op, new OpenAPI());

        assertEquals(List.of("page", "api_key"), params.stream().map(InsomniaParameter::name).toList());
    }
}

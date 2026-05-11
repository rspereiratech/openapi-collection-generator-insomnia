package com.github.rspereiratech.openapi.collection.generator.insomnia.parameter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaParameter;
import com.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

/**
 * Default implementation of {@link InsomniaParameterBuilder} that extracts query parameters
 * from the OpenAPI operation and merges them with security-injected query parameters.
 */
public class DefaultInsomniaParameterBuilder implements InsomniaParameterBuilder {

    /**
     * The security applier for injecting authentication query parameters.
     */
    private final SecurityApplier sec;

    /**
     * Constructs a new parameter builder.
     *
     * @param sec the security applier for injecting authentication query parameters
     */
    public DefaultInsomniaParameterBuilder(SecurityApplier sec) {
        this.sec = sec;
    }

    @Override
    public List<InsomniaParameter> build(Operation op, OpenAPI openApi) {
        var inj = sec.apply(op, openApi);
        return Stream.concat(
            Optional.ofNullable(op.getParameters()).orElse(List.of()).stream()
                .filter(p -> "query".equals(p.getIn()))
                .map(p -> new InsomniaParameter(
                    p.getName(), "",
                    Optional.ofNullable(p.getDescription()).orElse(""))),
            inj.queryParams().stream()
                .map(q -> new InsomniaParameter(q.name(), q.value(), "security"))
        ).toList();
    }
}

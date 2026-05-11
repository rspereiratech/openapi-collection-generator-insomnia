package com.github.rspereiratech.openapi.collection.generator.insomnia.header;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.rspereiratech.openapi.collection.generator.insomnia.model.InsomniaHeader;
import com.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

/**
 * Default implementation of {@link InsomniaHeaderBuilder} that extracts header parameters,
 * determines Content-Type from the request body, and appends security-related headers.
 */
public class DefaultInsomniaHeaderBuilder implements InsomniaHeaderBuilder {

    private final SecurityApplier sec;

    /**
     * Constructs a new header builder.
     *
     * @param sec the security applier for injecting authentication headers
     */
    public DefaultInsomniaHeaderBuilder(SecurityApplier sec) {
        this.sec = sec;
    }

    @Override
    public List<InsomniaHeader> build(Operation op, OpenAPI openApi) {
        List<InsomniaHeader> h = new ArrayList<>();
        Optional.ofNullable(op.getParameters()).orElse(List.of()).stream()
            .filter(p -> "header".equals(p.getIn()))
            .map(p -> new InsomniaHeader(p.getName(), ""))
            .forEach(h::add);
        Optional.ofNullable(op.getRequestBody())
            .map(rb -> rb.getContent())
            .filter(c -> !c.isEmpty())
            .map(c -> new InsomniaHeader("Content-Type", c.keySet().iterator().next()))
            .ifPresent(h::add);
        sec.apply(op, openApi).headers().stream()
            .map(s -> new InsomniaHeader(s.name(), s.value()))
            .forEach(h::add);
        return Collections.unmodifiableList(h);
    }
}

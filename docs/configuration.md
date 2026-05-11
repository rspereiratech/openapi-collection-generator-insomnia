# Configuration

## Maven coordinates

```xml
<groupId>com.github.rspereiratech</groupId>
<artifactId>openapi-collection-generator-insomnia</artifactId>
```

The version is inherited from the parent and is currently `1.0.0-SNAPSHOT`.

## Parent POM

This module inherits from `openapi-collection-generator-parent`:

```xml
<parent>
    <groupId>com.github.rspereiratech</groupId>
    <artifactId>openapi-collection-generator-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>
```

Maven resolves the parent from the local or remote repository (no
`<relativePath>` override). The parent owns:

- Java version and compiler plugin configuration
- Managed versions for `swagger-parser`, Jackson, JUnit, and Mockito
- Shared plugin configuration (Surefire, etc.)

If you change Java or dependency versions, do it in the parent — this module
deliberately keeps its POM minimal.

## Dependencies

| Dependency | Scope | Purpose |
|---|---|---|
| `openapi-collection-generator-core` | compile | Shared SPI: `CollectionGenerator`, `IdGenerator`, `SchemaExampleGenerator`, `SecurityApplier`, `ServerEnvironmentGenerator`, extension chain, link enricher |
| `junit-jupiter` | test | Test framework |
| `mockito-core` | test | Mocking collaborators in unit tests |

Note that the OpenAPI parser (`io.swagger.parser.v3:swagger-parser`) and Jackson
arrive transitively through `-core`. This module imports `io.swagger.v3.oas.models.*`
types directly, so the OpenAPI model is part of the API surface.

## Building

```bash
mvn -pl openapi-collection-generator-insomnia -am clean verify
```

`-am` (also-make) ensures the parent and `-core` modules are built first when
working from the multi-module root. From inside this directory, plain
`mvn clean verify` is enough as long as the parent and `-core` are installed
(`mvn install`) in the local Maven repository.

## Java version

Java is configured at the parent. This module uses language features that
require **Java 17 or later** — most notably sealed interfaces
(`InsomniaResource`) and records (every type under `…insomnia.model`).

## Runtime configuration

This module does not read any configuration of its own. It receives a
`GenerationConfig` instance via the `CollectionGenerator.generate(...)` call
and only consults `cfg.collectionName()` to override the API title:

```java
// InsomniaCollectionGenerator.resolveName
return Optional.ofNullable(cfg.collectionName())
    .orElse(api.getInfo().getTitle());
```

Everything else (server environments, security headers, vendor extensions,
deprecation marking) is delegated to collaborators that are themselves
configured by the consumer (typically the Maven plugin module).

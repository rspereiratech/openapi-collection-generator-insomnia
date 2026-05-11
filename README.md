# openapi-collection-generator-insomnia

[![Build](https://github.com/rspereiratech/openapi-collection-generator-insomnia/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/rspereiratech/openapi-collection-generator-insomnia/actions/workflows/build.yml)
[![CodeQL](https://github.com/rspereiratech/openapi-collection-generator-insomnia/actions/workflows/codeql.yml/badge.svg?branch=master)](https://github.com/rspereiratech/openapi-collection-generator-insomnia/actions/workflows/codeql.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)
![Maven 3.9+](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven&logoColor=white)
![OpenAPI 3.0](https://img.shields.io/badge/OpenAPI-3.0-6BA539?logo=openapiinitiative&logoColor=white)
![Swagger v3](https://img.shields.io/badge/Swagger-v3-85EA2D?logo=swagger&logoColor=black)
![Insomnia v4](https://img.shields.io/badge/Insomnia-v4-4000BF?logo=insomnia&logoColor=white)
[![Donate](https://img.shields.io/badge/Donate-NOWPayments-1A1A1A?logo=bitcoin&logoColor=white)](https://nowpayments.io/donation/rspereiratech)

Insomnia collection generator plugin for OpenAPI specifications — converts
OpenAPI specs into importable [Insomnia](https://insomnia.rest/) v4 exports.

This module is the **Insomnia format adapter** in the
`openapi-collection-generator` family. Given an `OpenAPI` model and a
`GenerationConfig`, it returns a JSON string that Insomnia can import as a
workspace with environments, folders, and HTTP requests.

## What it produces

- An Insomnia export document (`__export_format: 4`) containing:
  - **One workspace** named after the OpenAPI title (or the configured name).
  - **One environment per OpenAPI server**, with a `base_url` variable.
    Global security variables are merged into the first environment.
  - **One folder per OpenAPI tag**, plus a `Callbacks` folder when callbacks
    are present, plus a `default` folder for untagged operations.
  - **One request per operation**, with URL, headers, query parameters, and
    a body example derived from `examples`, `example`, or the schema.
- Path parameters are converted from `{petId}` to Insomnia's `:petId`.
- Deprecated operations are prefixed with `⚠` and get a warning in the
  description.
- Vendor extensions can rewrite name/description through the extension chain
  exposed by the core module.

## Maven coordinates

```xml
<dependency>
    <groupId>io.github.rspereiratech</groupId>
    <artifactId>openapi-collection-generator-insomnia</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

The version is inherited from the
[`openapi-collection-generator-parent`](https://github.com/rspereiratech)
parent POM.

## Quick start

This module is normally invoked through
`openapi-collection-generator-maven-plugin`. If you want to use it
programmatically:

```java
InsomniaCollectionGenerator generator = new InsomniaCollectionGenerator(
    idGenerator,           // IdGenerator
    requestBuilder,        // InsomniaRequestBuilder (DefaultInsomniaRequestBuilder)
    serializer,            // CollectionSerializer
    securityApplier,       // SecurityApplier
    serverEnvGenerator);   // ServerEnvironmentGenerator

String insomniaJson = generator.generate(openApi, generationConfig);
// Save insomniaJson to a file and import it from Insomnia → Application → Import.
```

All collaborators are SPIs from `openapi-collection-generator-core`, with
default implementations available in this module under their respective
packages (`url/`, `header/`, `body/`, `parameter/`, `builder/`,
`deprecated/`).

## Building

```bash
mvn clean verify
```

Requires **Java 17+** and Maven 3.9+. The parent POM and the `-core` module
must be installed in your local Maven repository (`mvn install` from the
multi-module root) before building this module standalone.

## Documentation

| Document | What it covers |
|---|---|
| [docs/configuration.md](docs/configuration.md) | Build configuration, Maven coordinates, parent POM, dependencies |
| [docs/architecture.md](docs/architecture.md) | High-level design, collaborator pattern, control flow |
| [docs/components.md](docs/components.md) | Per-package walk-through |
| [docs/publishing.md](docs/publishing.md) | SNAPSHOT publishing to Sonatype Central, required secrets |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Bug reports and pull requests are
welcome — please make sure your change belongs in this module and not in a
sibling one.

All participants are expected to follow the
[Code of Conduct](CODE_OF_CONDUCT.md).

## Security

Found a vulnerability? Please follow the process in [SECURITY.md](SECURITY.md)
and **do not** open public GitHub issues for security-sensitive reports.

## Support

[![Donate](https://img.shields.io/badge/Donate-NOWPayments-1A1A1A?logo=bitcoin&logoColor=white)](https://nowpayments.io/donation/rspereiratech)

If this project saves you time, consider supporting development
via [NOWPayments](https://nowpayments.io/donation/rspereiratech).
Every contribution helps keep it maintained — thank you!

## License

[MIT](LICENSE) © 2026 rspereiratech

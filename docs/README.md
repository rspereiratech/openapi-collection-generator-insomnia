# Documentation

`openapi-collection-generator-insomnia` is the Insomnia-targeted module of the
[`openapi-collection-generator`](https://github.com/rspereiratech) family. It
takes a parsed OpenAPI specification and produces a JSON document that can be
imported into [Insomnia](https://insomnia.rest/) as a workspace with
environments, folders, and HTTP requests.

This module does **not** parse OpenAPI documents itself, nor does it handle
file I/O or Maven plugin wiring. Those concerns live in the sibling modules
(`-core`, `-maven-plugin`, …). This module is only the **format adapter**:
"given an `OpenAPI` model and a `GenerationConfig`, give me a JSON string that
Insomnia understands."

## Index

| Document | What it covers |
|---|---|
| [configuration.md](configuration.md) | Build configuration, Maven coordinates, the parent POM, dependencies |
| [architecture.md](architecture.md) | High-level design, the collaborator pattern, control flow |
| [components.md](components.md) | Per-package walk-through: generator, builders, model, etc. |

## Module at a glance

- **Language / build:** Java, Maven (inherits from `openapi-collection-generator-parent`)
- **Entry point:** `InsomniaCollectionGenerator` — implements the
  `CollectionGenerator` SPI defined in the core module.
- **Output:** Insomnia export format, version 4 (`__export_format: 4`).
- **Public model:** records under `…insomnia.model` describing the four
  Insomnia resource types (workspace, environment, request group, request).

## Where things go

```
src/main/java/com/github/rspereiratech/openapi/collection/generator/insomnia/
├── generator/   InsomniaCollectionGenerator (orchestrator)
├── builder/     InsomniaRequestBuilder + default impl
├── body/        Request-body construction (examples / schema-derived)
├── header/      Header construction (params, content-type, security)
├── parameter/   Query-parameter construction
├── url/         Path → Insomnia URL conversion
├── deprecated/  Deprecation marker (emoji + suffix in name/description)
└── model/       Records that map 1:1 to the Insomnia JSON schema
```

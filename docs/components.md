# Components

This is a per-package walk-through. For the high-level flow, read
[architecture.md](architecture.md) first.

## `generator/`

### `InsomniaCollectionGenerator`

Implements `CollectionGenerator` from the core module. Pure orchestration —
it does not parse, build, or serialize anything itself.

Constructor dependencies:

| Field | Type | Role |
|---|---|---|
| `idGenerator` | `IdGenerator` | Produces stable IDs with prefixes (`wrk_`, `env_`, `fld_`, `req_`) |
| `requestBuilder` | `InsomniaRequestBuilder` | Builds one `InsomniaRequest` per OpenAPI operation |
| `serializer` | `CollectionSerializer` | Converts the `InsomniaExport` record to JSON |
| `securityApplier` | `SecurityApplier` | Resolves global security variables for environment #0 |
| `serverEnvGenerator` | `ServerEnvironmentGenerator` | Produces one environment per OpenAPI server |

Notable methods:

- `buildEnvironments` — only the **first** environment receives the global
  security variables; others are left with just `base_url`. The convention is
  that env #0 is the "default / dev" one Insomnia activates on import.
- `buildResourcesFromPaths` — iterates all paths and operations, lazily
  creates one folder per tag (`getOrCreateFolder` uses `computeIfAbsent`).
- `buildCallbackResources` — operations with `callbacks` get their callback
  operations added under a dedicated `Callbacks` folder, with paths prefixed
  by `/callbacks/<callbackName>`.
- `resolveTag` — returns the first tag, or `"default"` if the operation has
  none.

## `builder/`

### `InsomniaRequestBuilder` / `DefaultInsomniaRequestBuilder`

Eight collaborators come in: `IdGenerator`, `InsomniaUrlResolver`,
`InsomniaHeaderBuilder`, `InsomniaBodyBuilder`,
`InsomniaParameterBuilder`, `DeprecationMarker`, `ExtensionProcessorChain`,
`LinkDescriptionEnricher`.

The default implementation runs the pipeline described in
[architecture.md → Request construction](architecture.md#request-construction):
extension chain → link enrichment → deprecation marking → assemble record.

`collectLinks` flattens links across all responses of an operation into a
single `LinkedHashMap`, keeping insertion order and resolving duplicates
with first-wins (`(a, b) -> a`).

## `body/`

### `InsomniaBodyBuilder` / `DefaultInsomniaBodyBuilder`

Resolves the **first** media type from the operation's request body. The
priority order for body content is:

1. `mediaType.examples` (named examples) — first non-null value wins.
2. `mediaType.example` (single example).
3. `SchemaExampleGenerator.generate(schema, openApi)` — synthesised from the
   schema.

All three are pretty-printed with Jackson (`writerWithDefaultPrettyPrinter`).
On any exception during serialization, the body falls back to
`new InsomniaBody(mime, "{}")` to keep the export importable.

Returns `null` when the operation has no `requestBody` or the content map is
empty — Insomnia treats a null body as "no body."

## `header/`

### `InsomniaHeaderBuilder` / `DefaultInsomniaHeaderBuilder`

Builds headers in this order, then returns them as an unmodifiable list:

1. **Operation header parameters** (`p.in == "header"`), with empty values —
   the user fills them in Insomnia.
2. **Content-Type**, taken from the first key of the request body's content
   map (only added if a request body exists).
3. **Security headers** from `SecurityApplier.apply(op, openApi).headers()`.

## `parameter/`

### `InsomniaParameterBuilder` / `DefaultInsomniaParameterBuilder`

Concatenates two streams:

1. Operation parameters where `in == "query"`. Description comes from the
   parameter spec; value is empty.
2. Security-injected query params, marked with `description = "security"`
   so they are easy to spot in the Insomnia UI.

## `url/`

### `InsomniaUrlResolver` / `DefaultInsomniaUrlResolver`

Translates an OpenAPI path into an Insomnia URL:

```
"/pets/{petId}"  →  "{{ base_url }}/pets/:petId"
```

- Prefixes `{{ base_url }}` so Insomnia resolves it from the active
  environment.
- Replaces `{` → `:` and drops `}` to convert OpenAPI path parameters into
  Insomnia's colon-prefixed syntax.

The `SecurityApplier` is injected but currently unused here; it is kept for
forward compatibility with security schemes that mutate the URL itself
(e.g. signed query strings).

## `deprecated/`

### `InsomniaDeprecationMarker`

Implements `DeprecationMarker` from `-core` with Insomnia-friendly
formatting:

- `markName("Get pet", true)` → `"⚠ Get pet (deprecated)"`
- `markDescription("…", true)` prepends
  `"DEPRECATED: This operation may be removed in a future version."`,
  separated by a blank line. Empty descriptions get only the warning.

When `deprecated == false`, both methods return their inputs unchanged.

## `model/`

Records that map 1:1 to the Insomnia JSON resource schema. Field naming
relies on `@JsonProperty` to bridge Java naming (`id`, `type`) and Insomnia
conventions (`_id`, `_type`).

| Record | `_type` | Notes |
|---|---|---|
| `InsomniaExport` | `export` | Top-level. `__export_format = 4`, `__export_source = "openapi-collection-maven-plugin"`. |
| `InsomniaWorkspace` | `workspace` | `scope = "collection"` |
| `InsomniaEnvironment` | `environment` | `data` is a `Map<String,String>` |
| `InsomniaRequestGroup` | `request_group` | Folder; parented to workspace |
| `InsomniaRequest` | `request` | Holds body / headers / parameters |
| `InsomniaBody` | — | `mimeType`, `text` |
| `InsomniaHeader` | — | `name`, `value` |
| `InsomniaParameter` | — | `name`, `value`, `description` |

`InsomniaResource` is a sealed interface that permits only `InsomniaWorkspace`,
`InsomniaEnvironment`, `InsomniaRequestGroup`, and `InsomniaRequest`. The
exported `resources` list is typed as `List<InsomniaResource>` so the
serializer can polymorphically emit each entry while keeping the closed set
explicit.

Each "leaf" record (workspace, environment, request group) exposes a static
`of(...)` factory that pre-fills the `_type` constant, so the orchestrator
never has to repeat type literals.

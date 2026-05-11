# Architecture

## Design intent

The module is structured around one principle: **one class per concern, all
wired through constructor injection.** The orchestrator
(`InsomniaCollectionGenerator`) does not know how to build a body, resolve a
URL, or apply security — it only knows the *shape* of an Insomnia export and
which collaborators to call to fill it in.

This keeps each piece small and replaceable:

- Switch the URL convention? Provide a different `InsomniaUrlResolver`.
- Render bodies as `multipart/form-data` instead of JSON? Replace
  `InsomniaBodyBuilder`.
- Mark deprecation differently? Swap `InsomniaDeprecationMarker` for another
  `DeprecationMarker` implementation.

The default implementations live next to their interfaces (e.g.
`url/InsomniaUrlResolver.java` and `url/DefaultInsomniaUrlResolver.java`).

## Control flow

```
InsomniaCollectionGenerator.generate(openApi, config)
│
├── resolveName(openApi, config)            → workspace name
├── idGenerator.generate("wrk", name)       → workspace ID
│
├── add InsomniaWorkspace                   → first resource
│
├── buildEnvironments(...)
│   └── for each ServerEnvironment from serverEnvGenerator
│       ├── data["base_url"] = env.baseUrl()
│       ├── (only env #0) merge securityApplier.applyGlobal(...) variables
│       └── add InsomniaEnvironment
│
├── buildResourcesFromPaths(...)
│   └── for each (path, method, operation)
│       ├── folderId = getOrCreateFolder(resolveTag(op))
│       │   resolveTag = first tag, or "default"
│       ├── add requestBuilder.build(path, method, op, folderId, openApi)
│       └── buildCallbackResources(op, …)   → adds requests in "Callbacks" folder
│
└── serializer.serialize(InsomniaExport(...))
    type           = "export"
    exportFormat   = 4
    exportDate     = Instant.now().toString()
    exportSource   = "openapi-collection-maven-plugin"
    resources      = workspace + environments + folders + requests
```

Anything thrown inside `generate` is wrapped in a
`CollectionGenerationException("Insomnia generation failed", e)` so callers
get a single, predictable failure type.

## Request construction

`DefaultInsomniaRequestBuilder.build` is where each individual request is
assembled. It is the most collaborator-heavy class in the module:

```
build(path, method, op, parentId, openApi)
│
├── deprecated   = op.deprecated == TRUE
├── rawName      = op.summary || (method + " " + path)
├── rawDesc      = op.description || ""
│
├── ext          = extChain.process(ExtensionContext(path, method, name, desc, op))
│   ├── name     = ext.nameOverride() ?: rawName
│   └── desc     = rawDesc + "\n\n" + ext.descriptionAppend()  (if present)
│
├── desc         = linkEnricher.enrich(desc, links collected from responses)
├── name         = depr.markName(name, deprecated)              "⚠ … (deprecated)"
├── desc         = depr.markDescription(desc, deprecated)       prepends warning
│
└── new InsomniaRequest(
        id        = id.generate("req", method + path),
        type      = "request",
        parentId  = parentId,
        name, method,
        url       = url.resolve(path, op),
        body      = body.build(op, openApi),
        headers   = header.build(op, openApi),
        params    = params.build(op, openApi),
        desc)
```

## Resource model

```
InsomniaResource (sealed)
├── InsomniaWorkspace        scope = "collection"
├── InsomniaEnvironment      key/value variables (base_url, secrets…)
├── InsomniaRequestGroup     folder, parented to workspace
└── InsomniaRequest          parented to a folder, contains body/headers/params
```

The `sealed` keyword on `InsomniaResource` documents (and enforces) that the
exported resources list contains only those four types — no surprises during
serialization.

## What the module does *not* do

- **No I/O.** The result is returned as a `String`; the Maven-plugin module
  is responsible for writing it to disk.
- **No OpenAPI parsing.** It receives an already-built `OpenAPI` model.
- **No Insomnia upload/import.** The output is a plain JSON file the user
  imports manually (or via Inso CLI).
- **No HTTP execution.** It generates request definitions, not responses.

## Threading

The generator is effectively immutable after construction (all fields are
`final`, all collaborators are expected to be thread-safe), and the only
mutable state during `generate` is the `resources` list and the
`tagToFolder` map, both local to a single invocation. Multiple parallel
calls to `generate` are safe as long as the injected collaborators are.

## Error handling

The module favours **soft fallbacks for optional content** and **fail-fast
for the overall pipeline**:

- Missing summary → uses `METHOD path` as a name.
- Missing description → empty string.
- Missing tags → folder named `default`.
- Missing request body → no body in the request.
- Body serialization throws → falls back to `{}` (see
  `DefaultInsomniaBodyBuilder.buildBody`).
- Anything else throwing → wrapped in `CollectionGenerationException`.

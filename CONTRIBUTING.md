# Contributing

Thanks for your interest in contributing! This project is part of the
`openapi-collection-generator` family — please keep that in mind when
proposing changes here, since some concerns belong in sibling modules
(`-core`, `-maven-plugin`, …) rather than in this one.

## Scope of this module

`openapi-collection-generator-insomnia` is the **Insomnia format adapter**.
A change belongs here if and only if it concerns:

- The shape of the Insomnia export JSON (resources, fields, format version).
- How an OpenAPI element is translated into an Insomnia element
  (URLs, headers, body, query params, deprecation, callbacks, tags-as-folders).
- The Insomnia-specific implementations of core SPIs.

Changes that affect OpenAPI parsing, security scheme resolution,
ID generation, schema example generation, vendor extension processing, or
the Maven plugin wiring belong in the `-core` or `-maven-plugin` modules.

## Development setup

Requirements:

- Java 17 or later
- Maven 3.9+
- The parent POM (`openapi-collection-generator-parent`) and the `-core`
  module installed in your local Maven repository (`mvn install` from the
  multi-module root).

Build and test:

```bash
mvn clean verify
```

## Code style

- Follow the existing patterns: one class per concern, all collaborators
  injected via the constructor, default implementations alongside their
  interfaces.
- Records for plain data, sealed interfaces for closed type hierarchies.
- Prefer small private helpers over large methods.
- Avoid adding fallback/branching logic for cases the OpenAPI spec already
  forbids.
- Don't introduce new dependencies without discussing it first — this module
  is intentionally minimal.

## Tests

- Unit tests use JUnit 5 (`org.junit.jupiter.api.Assertions`) and Mockito (already on the classpath). Do not introduce AssertJ.
- Every new builder/resolver/marker should have at least one test covering
  the happy path and one covering its main fallback branch.
- Tests should not hit the network or read fixture files outside the test
  resources directory.

## Submitting changes

1. Fork the repository and create a topic branch.
2. Keep commits focused and write descriptive commit messages.
3. Run `mvn clean verify` locally before opening a pull request.
4. Open a PR against `master` and describe:
   - What changes and why.
   - Any user-visible impact on the generated Insomnia export.
   - Whether downstream modules (`-core`, `-maven-plugin`) need follow-up.

## Reporting bugs

Open a GitHub issue with:

- The OpenAPI input that triggers the problem (or a minimal reduction).
- The Insomnia export that was produced.
- The Insomnia version you tried to import it into.
- The expected vs. actual behaviour.

## Reporting security issues

Please follow the process described in [SECURITY.md](SECURITY.md) — do **not**
open public issues for security-sensitive reports.

# Publishing

This module publishes SNAPSHOT artifacts to the **Sonatype Central Portal**
on every push to `master`. Release artifacts (signed, tagged versions) are
not yet automated and will be added in a follow-up.

## SNAPSHOT publishing

### Where SNAPSHOTs live

- **Repository URL** —
  <https://central.sonatype.com/repository/maven-snapshots/>
- **Group ID** — `com.github.rspereiratech`
- **Artifact ID** — `openapi-collection-generator-insomnia`
- **Version** — `1.0.0-SNAPSHOT` (current)

### When SNAPSHOTs are published

The [`snapshot.yml`](../.github/workflows/snapshot.yml) workflow runs on:

- Every push to `master`.
- Manual trigger (`workflow_dispatch`).

The workflow refuses to deploy if the project version does not end with
`-SNAPSHOT`, to avoid accidentally pushing a release through this pipeline.

### Required secrets

| Secret | Where to get it |
|---|---|
| `CENTRAL_USERNAME` | Central Portal → "View Account" → "Generate User Token" → username portion |
| `CENTRAL_PASSWORD` | Same place — password portion |

Set them in **Settings → Secrets and variables → Actions** on this
repository. They are also expected by the sibling repositories that
publish SNAPSHOTs of the parent POM and the core module.

### Consuming SNAPSHOTs

This module's `pom.xml` declares only the dependency coordinates; the
`-SNAPSHOT` resolution is handled by [`ci-settings.xml`](../ci-settings.xml),
which adds the Sonatype snapshot repository to Maven's resolution path.

For **local builds**, either:

- Invoke Maven with the project's settings file:
  ```bash
  mvn --settings ci-settings.xml clean verify
  ```
- Or copy the `<profile>` / `<server>` blocks from `ci-settings.xml` into
  your `~/.m2/settings.xml` so resolution works without the `--settings`
  flag.

## Notes and limitations

- **Parent POM resolution.** Maven resolves the `<parent>` element before
  it reads any `<repositories>` block in the consuming `pom.xml`. The
  parent therefore must be resolvable from `settings.xml` repositories
  (which `ci-settings.xml` provides) or from the local repository.
- **Snapshot reads are public.** No authentication is required to download
  SNAPSHOTs from the Central Portal snapshot URL.
- **Snapshot writes are tokenised.** Uploading requires the
  `CENTRAL_USERNAME` / `CENTRAL_PASSWORD` user-token credentials.
- **Cross-repo coordination.** A change in this repository that depends on
  unreleased code in the parent or core module must wait for those repos
  to publish their own SNAPSHOTs before this one's CI passes. Merge the
  upstream change first, wait for its `snapshot.yml` to succeed, then
  rebase this PR.

## Release publishing (planned)

Release artifacts go through a different workflow:

1. GPG-sign the artifacts.
2. Upload to the Central Portal staging area via the
   `central-publishing-maven-plugin`.
3. Promote the staging deployment to the public release repository.

This is not yet automated; a `release.yml` workflow will be added in a
future change.

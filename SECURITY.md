# Security Policy

## Supported versions

This project is in early development (`1.0.0-SNAPSHOT`). Only the latest
`master` branch receives security fixes.

| Version | Supported |
|---|---|
| `master` (latest) | ✅ |
| Older snapshots | ❌ |

## Reporting a vulnerability

Please **do not open a public GitHub issue** for security-sensitive reports.

Instead, use one of the private channels below:

1. **GitHub Private Vulnerability Reporting** — preferred. Open
   `Security` → `Report a vulnerability` on the repository page.
2. **Email** — send the report to `rspereiratech@gmail.com` with the subject
   prefix `[security]`.

Include in your report:

- A description of the issue and its potential impact.
- Steps to reproduce, ideally with a minimal OpenAPI input.
- The version / commit hash you tested against.
- Any suggested mitigation, if known.

## What to expect

- **Acknowledgement** within 7 days of receipt.
- **Initial assessment** within 14 days, including whether the report is
  accepted, declined, or needs more information.
- **Fix and disclosure timeline** agreed with the reporter. The default is
  coordinated disclosure once a fix is available.

## Scope

In-scope for this repository:

- Vulnerabilities in code under `src/main/java/...`.
- Vulnerabilities introduced by direct dependencies declared in this
  module's `pom.xml`.

Out of scope (please report upstream):

- Vulnerabilities in `swagger-parser`, Jackson, or other transitive
  dependencies — report to the respective project.
- Vulnerabilities in Insomnia itself — report to Kong/Insomnia.
- Vulnerabilities in sibling modules (`-core`, `-maven-plugin`) — report on
  their respective repositories.

## Safe-harbour

Good-faith security research that follows this policy will not result in
legal action from the maintainer.

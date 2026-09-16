# Fake Servlet

## What this is

A fake implementation of both `javax.servlet` and `jakarta.servlet` APIs. Requires Java 17+. Written in Kotlin.

## Promises

- **Drop-in fakes, no container.** A test gets working servlet objects from plain constructors, with no server and no mocking library.

## Design docs

| File | Owns | Loaded |
|---|---|---|
| `README.md` | the pitch, install, quickstart | — |
| `CONTRIBUTING.md` | how to contribute, the release steps | — |
| `AGENTS.md` (this) | promises, invariants, the module map, conventions, commands | every turn |
| `design/decisions.md` | why this and not that — `D_` entries, FAQ-shaped | lazy |
| `design/ideas/` | ideas not yet acted on, one file each; deleted once implemented or rejected, nuggets moved to the rows above | lazy |
| doc comments | what one symbol does and why it is shaped so | at the symbol |

Every fact lives in exactly one of these; the others link to it.

## Invariants

- **`fake-servlet` and `fake-servlet5` stay mirror images.** Every change lands in both, differing only in `javax.servlet` ↔ `jakarta.servlet` imports; a fix in one leaves the other artifact buggy.
- **Deprecated `Mock*` typealiases in `kaributesting.mockhttp` stay.** Downstream code still compiles against the old names.

## Module map

- `fake-servlet` — the fakes against `javax.servlet-api` 4.0.1; artifact `fake-servlet`.
- `fake-servlet5` — the same fakes against `jakarta.servlet-api` 5.0.0; artifact `fake-servlet5`.

## Conventions

- **Kotlin, explicit API mode, JVM 17 target.** Every public declaration carries `public` and its type.
- **Fakes are `open` classes with mutable public state**, tuned by tests directly or via `FakeHttpEnvironment`.
- **Tests: JUnit Jupiter with `kotlin.test` asserts**, mirrored per module like the main sources.
- **Diagnostics go through slf4j**, never `println`.
- **Dependency versions live in `gradle/libs.versions.toml`.**

## Commands

- `./gradlew` — `clean build`: tests of both modules, then `design/verify_design_tripwires.sh` (Linux only); CI runs it on Linux/macOS/Windows × JDK 17/21/25 (`.github/workflows/gradle.yml`), plus the tripwire in a job of its own.
- `./gradlew :fake-servlet5:test --tests '*FakeRequestTest*'` — one test class.
- Releasing: the steps in `CONTRIBUTING.md`.

## Maintenance of this file

Loaded every turn; cap 34 KB, a module's own `AGENTS.md` 10 KB. Over it, in this order:
delete what has no home — status, history, class lists, what the code already says; trim
each line to its fact plus one clause and send the explanation home — why →
`design/decisions.md`, how across symbols → `design/architecture.md`, how in one symbol →
its doc comment, what upstream does → `design/research.md`; only then a module's own
`AGENTS.md`, peripheral modules first, never the core. Never paraphrase a lazy entry into a
line here. `design/verify_design_tripwires.sh` checks the caps and the cites.

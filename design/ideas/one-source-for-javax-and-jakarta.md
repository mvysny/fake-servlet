# One source for javax and jakarta

Today `fake-servlet` and `fake-servlet5` are two copies of the same code, and every change
has to go into both. Can we get rid of the copy?

## What the two APIs look like (checked 2026-09-16 with javap)

Compared `javax.servlet-api` 4.0.1 against `jakarta.servlet-api` 5.0.0:

- Both jars contain the same classes. Every signature is identical once the package is renamed,
  with one exception: `javax.servlet.http.HttpSessionBindingEvent` also declares
  `getSession()` (jakarta's inherits it from `HttpSessionEvent`). So Servlet 5.0 is simply
  Servlet 4.0 with the packages renamed.
- Places where one class can't implement both interfaces in Kotlin or Java source:
  - `ServletRequest.getDispatcherType()` returns an enum, and the two `DispatcherType` enums
    have no common subtype.
  - `HttpServletRequest.getCookies()` returns `Cookie[]`, where `Cookie` is a *class* in both
    packages, so no return type fits both.
  - `ServletContext.get{Default,Effective}SessionTrackingModes()` return
    `Set<SessionTrackingMode>`. After generic erasure both are `getDefaultSessionTrackingModes()Ljava/util/Set;`,
    so they are literally the same JVM method, and it can't return two different sets.
- These *would* work through covariant returns: `getSession()`, `getServletContext()` and
  friends, if the return type is a fake that implements both interfaces (`FakeHttpSession :
  javax HttpSession, jakarta HttpSession`), all the way down.

## Options

1. **One class implementing both interfaces.** Blocked in source by the three methods above.
   At bytecode level the JVM does allow two methods that differ only in return type, so a
   post-processor could add them. But the erased `Set` methods collide even there, and the
   classpath would need both API jars. It's a fun experiment, but probably a dead end.
2. **One source tree, generate the other at build time.** Keep `fake-servlet/src`, and in
   `fake-servlet5` use a Gradle `Copy` + `filter` that rewrites `javax.servlet` → `jakarta.servlet`
   into `build/generated/`, then compile that. Since the APIs differ only in the package name,
   a text rewrite is enough. Tests get the same treatment.
3. **Rewrite the bytecode of the compiled jar** with Eclipse Transformer (this is how
   Tomcat/Jetty migrated). Works, but sources and javadoc jars need the same treatment, and it
   adds a tool.
4. **Status quo**: two copies, kept in sync by the invariant in `AGENTS.md`.

## Open questions

- `Q_which_is_source`: which tree is the real source? javax is the legacy one, but jakarta is the
  future. Rewriting jakarta → javax works just as well.
- `Q_ide_experience`: with option 2, the generated module has no sources in the IDE. Is that OK,
  given that nobody edits it?
- `Q_future_divergence`: Servlet 6.0+ removes deprecated methods (`HttpSessionContext`,
  `isRequestedSessionIdFromUrl`, ...). Once `fake-servlet5` moves past 5.0, the text rewrite
  stops being enough. Do we pin 5.0 forever, or are there per-API exceptions?
- `Q_bytecode_spike`: is option 1 worth a one-hour spike just to learn what Kotlin/JVM does?

## If it graduates

Option 2 or 3 implemented → the mirror-image invariant in `AGENTS.md` is replaced by a
convention ("edit `fake-servlet` only; `fake-servlet5` is generated"). The why, including why not
one class for both, becomes a `D_` entry.

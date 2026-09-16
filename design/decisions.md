# Decisions

Why this project is the way it is and not otherwise — FAQ-shaped: each entry is a question and
its current answer. Rewrite the answer when it changes; delete the entry when nobody asks any
more. An entry is earned by what it would cost to reverse — half the code base — or by research
the next person would otherwise redo (cited as its `R_`). Not an entry: windows → panels
"because that's the trend", this red over that red, `get_foo` over `is_foo?`, the testing library,
the CI host, a version bump — a comment at the site of the choice, or nothing; nothing about
`design/` itself. Cite by slug, `D_<slug>`, never by position; `grep '^## D_' design/decisions.md`
is the index. The first entry is the ruler: every later one trims to its length — which is how
long this file gets, so keep it short. When you have written an entry, re-read it against the one
above, check it says nothing the doc comments already say, and cut what is left over.

---

## D_working_fakes — Why working fakes rather than Mockito / JMock mocks?

A test driving a servlet-based framework (Vaadin through Karibu-Testing, where these classes
started) triggers dozens of container calls it never names: session attributes, context init
parameters, the request locale, response headers. Each `Fake*` is a small honest implementation
— `FakeHttpSession` stores attributes, `FakeResponse` records status, headers, cookies and body
— so the framework sees a consistent container and the test asserts on the resulting state.
Why not mocks: every test would stub whichever calls the framework makes today, coupling the
test to the framework's internals and breaking on its next release; the stubs pile up until
nobody knows which behaviour is real; and a mock is inconsistent by default — `setAttribute`
followed by `getAttribute` returns null unless that is stubbed too. This is the **Drop-in fakes,
no container** promise in practice. The cost we carry: every method of every servlet interface is
ours to implement; the rarely used ones throw `UnsupportedOperationException` until someone
needs them.

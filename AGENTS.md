# AGENTS.md

Notes for coding agents (and people in a hurry) working on this repository:
jDynAlloy, which translates JDynAlloy into DynAlloy for TACO. Human-oriented
docs are in [README.md](README.md) and [docs/](docs/).

## Where it sits

    Java + JML ──TACO──▶ JDynAlloy ──jDynAlloy──▶ DynAlloy ──dynalloy4──▶ Alloy ──▶ SAT

- [mffrias/TACO](https://github.com/mffrias/TACO) is the only user, through
  `lib/jdynalloy.jar`; there is no command line and no main class.
- [mffrias/dynalloy4](https://github.com/mffrias/dynalloy4) is a build
  dependency: `lib/dynalloy4.jar`, not in git (never commit it). Build
  dynalloy4 first and copy its `dist/dynalloy4.jar` here.
- TACO's `stryker` branch needs this repository's `stryker` branch
  (`setupTACO.sh` takes the branch named like TACO's).

## Environment

- **JDK 8** and Ant 1.9 or later. javac 21 and 25 reject `source`/`target` 1.7.
- Java 7 syntax: no lambdas or other Java 8 language features.

## Commands

```bash
cp ../dynalloy4/dist/dynalloy4.jar lib/    # once, and after every dynalloy4 change
ant jar                                    # parser, classes, dist/jdynalloy.jar
git checkout build.num                     # ant jar increments it
```

`ant build` (the default) generates the parser and compiles; `ant clean`
deletes `bin/` and the generated parser sources, which `ant build` writes
again, identical. Edit the grammar (`src/ar/edu/jdynalloy/parser/jdynalloy.g`),
not the generated `JDynAlloyLexer.java`, `JDynAlloyParser.java` and
`JDynAlloyParserTokenTypes.*`; commit both.

## Checking a change

There are no tests here. The check is TACO: copy `dist/jdynalloy.jar` into
`../TACO/lib/`, and on TACO's `stryker` branch run its smoke test
(`scripts/smoke-test.sh`; see TACO's `docs/RUNNING.md`), which checks four
repair benchmarks against their known results: fix found, which fix, TACO
calls and pruned mutants. A change that alters the
translation can change those numbers; say so in the commit message. For a
change to one construct, also run a TACO driver that uses it, and compare
`output/output.dals` before and after.

## Things that will bite you

- **The method under analysis is found by name** in several places
  ([docs/NAMING.md](docs/NAMING.md#where-the-method-under-analysis-is-looked-up)).
  TACO's names are `<package>_<Class>_<method>`, with the types in the
  configuration's `methodToCheck`; code that builds
  `<signature>_<program>` adds the class twice and never matches.
- **Overloads share their program name.** Anything keyed by program name alone
  (call graph, modifies table, `retrieveProgram`-style lookups) conflates them.
- **Static state** (`StaticFieldsModuleBuilder`, `JPreludeFactory`, the
  `JavaPrimitive*Value` singletons) survives between TACO runs in one JVM.
- **Fields are relations passed to every program**, not Alloy fields; adding a
  field changes every program's and every call's parameters.
- `JDynAlloyVisitor.visit(JSpecCase)` drops every `requires` when there is a
  fixed input; that is intended
  ([docs/ARCHITECTURE.md](docs/ARCHITECTURE.md#fixed-input)).
- Read [docs/KNOWN-LIMITATIONS.md](docs/KNOWN-LIMITATIONS.md) before "fixing"
  behaviour that looks wrong: several fixes would change TACO's verdicts, and
  so experimental results.

## Code map

- `xlator/JDynAlloyTranslator.java`: `translateAll`, the order of the steps.
- `xlator/JDynAlloyXlatorVisitor.java`: modules, programs and statements to
  DynAlloy; the `check_` assertion.
- `xlator/RecursionUnfolderVisitor.java`, `PruneVisitor`, `CallGraphVisitor`,
  `ModifiesTableBuilder`, `VAlloyVisitor`: the preparation steps.
- `ast/JDynAlloyVisitor.java`: the base visitor, and the fixed input.
- `binding/`, `relevancy/`, `slicer/`, `modifies/`: what TACO's
  `JDynAlloyStage` runs before the translation.
- `factory/JPreludeFactory.java`: the prelude.

## Conventions

- Comments and docs in English; existing Spanish comments stay.
- Open points about intent: [docs/OPEN-QUESTIONS.md](docs/OPEN-QUESTIONS.md).
  Ask rather than guess.

jDynAlloy
=========

JDynAlloy is an object-oriented layer over [DynAlloy](https://github.com/mffrias/dynalloy4):
a module per Java class, with a signature, fields, class and object invariants,
constraints and `represents` clauses, and programs (methods) that have
specification cases (`requires`, `ensures`, `modifies`) and imperative bodies
(assignments, `if`, `while` with loop invariants, calls, object creation,
`assert`, `assume`, `havoc`). This repository translates JDynAlloy into
DynAlloy for TACO. Given the modules of a method under analysis and of the
classes it uses, it resolves the calls, keeps only what the analysis needs,
turns `modifies` clauses into frame conditions, unfolds recursion a bounded
number of times, and produces one DynAlloy module per class, a prelude, and the
assertion `check_<method>`, which says that the method meets its specification.

How it fits with TACO and dynalloy4
-----------------------------------

TACO checks Java methods annotated with JML by translating them, in steps that
live in three repositories, into a SAT problem:

    Java + JML ──TACO──▶ JDynAlloy ──jDynAlloy──▶ DynAlloy ──dynalloy4──▶ Alloy ──Alloy Analyzer──▶ SAT

| Repository | What it does |
|---|---|
| [mffrias/TACO](https://github.com/mffrias/TACO) | Parses Java + JML, translates it into JDynAlloy, runs the whole pipeline and turns counterexamples into JUnit tests. Its `stryker` branch adds Stryker, which repairs faulty methods. |
| [mffrias/jDynAlloy](https://github.com/mffrias/jDynAlloy) (this one) | Translates JDynAlloy into DynAlloy: call bindings, relevancy analysis, frame conditions, recursion unfolding, and the translation itself. Also parses JDynAlloy text (`.djals`). |
| [mffrias/dynalloy4](https://github.com/mffrias/dynalloy4) | Translates DynAlloy into Alloy and runs the Alloy Analyzer. jDynAlloy builds DynAlloy models with its AST classes and prints them with its printer. |

jDynAlloy is a library with one user, TACO, which uses it as `lib/jdynalloy.jar`;
it has no command line, no main class and no tests. TACO builds the JDynAlloy
modules of the classes under analysis from their Java + JML, and reads its
models of JDK classes, written as JDynAlloy text (`.djals`), with this
repository's parser. Its `JDynAlloyStage` then runs the call bindings, the
relevancy analysis, the slicing and the frame conditions, and
`JDynAlloyToDynAlloyManager` runs `JDynAlloyTranslator.translateAll` and
`DynAlloyLinker.link`. TACO writes the result to `output/output.dals`, which
dynalloy4 translates into Alloy. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

Building
--------

Requirements: a JDK 8 and Ant (checked with Zulu 8u504 and Ant 1.9.16), and
dynalloy4's jar, `lib/dynalloy4.jar`, which is not in git: build dynalloy4
first (its README), then

    git clone https://github.com/mffrias/jDynAlloy.git
    cd jDynAlloy
    cp ../dynalloy4/dist/dynalloy4.jar lib/
    ant jar
    git checkout build.num

`ant jar` generates the ANTLR 2.7.6 parser from the grammar
(`src/ar/edu/jdynalloy/parser/jdynalloy.g`), compiles `src/` into `bin/`, and
writes `dist/jdynalloy.jar`. That jar also contains every jar in `lib/`
(dynalloy4's, which itself bundles Alloy, Kodkod and the SAT solvers; ANTLR;
log4j) and `dist/jdynalloy_src.jar`, the Java sources. Other targets: `ant build`
(parser and classes only, the default) and `ant clean` (deletes `bin/` and the
generated parser sources, which are in git; the next `ant build` writes them
again, byte for byte). `ant jar` increments the build number in `build.num`,
which is in git, hence the `git checkout`. javac 21 and 25 reject the build's
`source`/`target` 1.7.

TACO's `setupTACO.sh` does all of this for you: it clones the three
repositories side by side, builds dynalloy4 and then this one, and copies
`dist/jdynalloy.jar` into `TACO/lib`. After changing this repository, rebuild
the jar and copy it into TACO's `lib/`.

Branches
--------

- `master`: Marcelo Frias's line of development.
- `stryker`: what TACO's `stryker` branch needs. Two fixes for the new method
  naming (`<package>_<Class>_<method>(<types>)`): the fixed input of
  Stryker's feedback and of TACO's specified-output runs is applied to the
  method under analysis again, and overloads that call each other are no
  longer taken for recursion ([docs/NAMING.md](docs/NAMING.md)). And the
  modifies table counts the fields that callees assign again, so that an
  invariant broken through a callee is detected, as with comitaco's
  jDynAlloy. TACO's `setupTACO.sh` builds this branch when TACO is on its
  `stryker` branch. TACO's [docs/FIXES-FOR-MASTER.md](https://github.com/mffrias/TACO/blob/docs/docs/FIXES-FOR-MASTER.md) describes these
  fixes, with those in TACO and dynalloy4.
- `docs`: this documentation, on top of `stryker`.

Documentation
-------------

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): packages, how TACO calls
  jDynAlloy, and each step of the JDynAlloy → DynAlloy translation.
- [docs/LANGUAGE.md](docs/LANGUAGE.md): the JDynAlloy language, its AST and its
  text syntax.
- [docs/NAMING.md](docs/NAMING.md): how modules, programs and assertions are
  named, and where the method under analysis is looked up.
- [docs/KNOWN-LIMITATIONS.md](docs/KNOWN-LIMITATIONS.md): what does not work.
- [docs/OPEN-QUESTIONS.md](docs/OPEN-QUESTIONS.md): questions for the
  maintainer about intent.
- [AGENTS.md](AGENTS.md): notes for coding agents (and people) changing this
  repository.

References / how to cite
------------------------

DynAlloy:

- M. F. Frias, C. G. López Pombo, G. A. Baum, N. M. Aguirre, T. Maibaum.
  “Taking Alloy to the Movies.” LNCS, 2003, pp. 678–697.
  DOI [10.1007/978-3-540-45236-2_37](https://doi.org/10.1007/978-3-540-45236-2_37).

TACO, which defines and uses JDynAlloy:

- J. P. Galeotti, N. Rosner, C. G. López Pombo, M. F. Frias. “Analysis of
  Invariants for Efficient Bounded Verification.” ISSTA 2010, pp. 25–36.
  DOI [10.1145/1831708.1831712](https://doi.org/10.1145/1831708.1831712).
- J. P. Galeotti, N. Rosner, C. G. López Pombo, M. F. Frias. “TACO: Efficient
  SAT-Based Bounded Verification Using Symmetry Breaking and Tight Bounds.”
  IEEE TSE 39(9):1283–1307, 2013.
  DOI [10.1109/TSE.2013.15](https://doi.org/10.1109/TSE.2013.15).
- J. P. Galeotti. Software Verification using Alloy. PhD thesis, Universidad de
  Buenos Aires, 2010.

TACO's README has the full list, with the work built on TACO.

License
-------

The Java sources that have a license header, 51 of 136 (for example
`src/ar/edu/jdynalloy/binding/BindingManager.java`), say GNU GPL version 2 or
later. The others, and the repository, have no license statement; whether
to add a license file is in [docs/OPEN-QUESTIONS.md](docs/OPEN-QUESTIONS.md).

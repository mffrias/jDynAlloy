# Architecture

How jDynAlloy turns the JDynAlloy modules TACO gives it into DynAlloy, in the
order TACO runs the steps. Read from the code of the `stryker` branch
(2026-10-18); TACO's side is its `stryker` branch. The language itself is in
[LANGUAGE.md](LANGUAGE.md), and the names of things in [NAMING.md](NAMING.md).

## Packages

All under `src/ar/edu/jdynalloy/`:

| Package | Contents |
|---|---|
| (top) | `JDynAlloyConfig` (holds the configuration, see below), `IJDynAlloyConfig`, `JDynAlloyConfigImpl` (unused), the exceptions |
| `ast` | The JDynAlloy AST (`JDynAlloyModule`, `JSignature`, `JField`, `JProgramDeclaration`, `JSpecCase`, the statements); `JDynAlloyVisitor` (which also applies a fixed input, see below), `JDynAlloyMutator` (a visitor that rebuilds), `JDynAlloyPrinter` (AST → `.djals` text) |
| `parser` | The grammar `jdynalloy.g` (ANTLR 2.7.6), the generated lexer and parser (in git), `JDynAlloyParserManager` |
| `buffer` | `DynJMLAlloyModuleBuffer`, which TACO fills to build a module; `StaticFieldsModuleBuilder`, which collects static fields and class invariants into the module `ClassFields` |
| `binding` | `BindingManager`: which program each call invokes (`callbinding/`), fields (`fieldcollector/`), symbol tables (`symboltable/`) |
| `relevancy` | `RelevancyAnalysisManager`: which modules, programs and fields the method under analysis can reach |
| `slicer` | `SceneSlicerManager`: drops what the relevancy analysis did not reach |
| `modifies` | `ModifiesSolverManager`: frame conditions from `modifies` clauses |
| `xlator` | `JDynAlloyTranslator` and its visitors (the translation proper), `DynAlloyLinker`, `Graph` |
| `factory` | Builders for expressions, predicates, types, signatures and the prelude (`JPreludeFactory`) |

One more package, `src/ar/edu/taco/simplejml/builtin/`, holds TACO's models of
Java's primitive values with Java arithmetic (`JavaPrimitiveIntegerValue` and
the long, char and float ones). They load their Alloy text (`Integer32.als`
and so on) as class-path resources that live in TACO.

## How TACO calls it

Configuration: TACO's `TacoMain` calls `JDynAlloyConfig.buildConfig(file,
properties)`, which reads the key `DynJAlloyConfig.Class` from TACO's
properties file and instantiates that class, `ar.edu.taco.TacoConfigurator`.
Everything in jDynAlloy reads its options from `JDynAlloyConfig.getInstance()`,
so in practice from TACO's configuration (see [Configuration](#configuration)).

TACO produces the modules of the classes under analysis itself, by filling
`DynJMLAlloyModuleBuffer`s, and parses its models of JDK classes (`.djals`
files in TACO's `engine/precompiledmodules/`) with
`JDynAlloyParserManager.parseModulesResource`. Then `JDynAlloyStage.execute`:

1. **Bindings.** `BindingManager.execute()` collects the programs and fields,
   types every call (`SemanticCheckVisitor`) and resolves it to a program
   (`CallBindingResolver`): same static-ness, a receiver type that is the
   program's signature or one of its subtypes, the same name, the same number
   of arguments, and then the most specific parameter types. A call that
   matches no program is a `JDynAlloySemanticException` ("Program not found",
   or "Program overload not found" when only the number of arguments differs),
   and so is one that matches two equally specific ones ("Ambiguous call"). The
   result is a `JDynAlloyBinding`. TACO also stores its loop-unrolling bound
   there, as the number of times to unfold recursion.
2. **Relevancy analysis** (if `relevancyAnalisys`):
   `RelevancyAnalysisManager.process` starts from the method under analysis,
   its module and the module `ClassFields` (plus any module pinned for
   Stryker), and follows calls, field accesses and types until nothing new is
   reached. It returns the ids of the relevant modules as one string, joined
   with `, ` in a `HashSet`'s order, which TACO stores as `relevantClasses`
   and reads back as a list with `getRelevantClasses()`, which adds
   `ClassFields`.
3. **Slicing.** `SceneSlicerManager.process` keeps the relevant (or pinned)
   modules, and decides which parts of the prelude are needed.
4. **Frame conditions.** `ModifiesSolverManager.process` checks that the
   module of the class under analysis has the method under analysis (otherwise
   `MethodToCheckNotFoundException`: "The method to check was not found") and
   replaces that module by a new one, built by `ReplaceModifiesModuleVisitor`,
   in which the `modifies` clauses of the programs whose
   `<signature>_<program>_` starts `methodToCheck` become extra `ensures`
   (but see [KNOWN-LIMITATIONS.md](KNOWN-LIMITATIONS.md#analysis)).
5. **Bindings again**, on the sliced modules, and then
   `JDynAlloyToDynAlloyManager.process`: `new JDynAlloyTranslator(binding,
   classToCheck, methodToCheck, inputToFix)`, `translateAll(modules,
   useJavaArithmetic)`, and `DynAlloyLinker.link`, which prints each DynAlloy
   module (dynalloy4's `DynalloyPrinter`) and adds the `prelude` module. TACO
   writes each module to `output/<module path>.dals` and all of them, the
   prelude first, to `output/output.dals`.

`methodToCheck` is the method's name without its parameter types
(`<package>_<Class>_<method>`), and `inputToFix` is null except in Stryker's
feedback checks and TACO's specified-output runs ([Fixed input](#fixed-input)).

## The translation (`JDynAlloyTranslator.translateAll`)

First it prepares the modules, all of them, with one visitor after another:

1. **`VAlloyVisitor`** gives every program a unique name and adds dynamic
   dispatch. In practice the unique name is the program's own name: the index
   suffix that told overloads apart is gone. A program met after another of
   the same name gets `_0` if it is pure and has no `java_lang_Throwable`
   parameter, but no program is: the pure programs TACO makes all have
   `throw: java_lang_Throwable+null`, and `.djals` programs cannot be pure. A `virtual` program gets a body that calls the implementation
   of the object's class (`if classOf(thiz) = <Sig>Class then call ...`).
2. **`JDynAlloyTransformationVisitor`** replaces each `while` that has a loop
   invariant by `assert` the invariant, `havoc` what the loop modifies,
   `assume` the invariant, and one guarded iteration that ends in `assert`
   the invariant and `assume false`. The `assert`s, the `havoc` and `assume`,
   and the `assume false` each run only if `throw = null` and
   `exit_stmt_reached = false`. From TACO no loop gets here: TACO's
   `WhileRemoverSimplifier` unrolls every `while` and drops its invariant
   first.
3. **`ModularMutator`**, only with `modularReasoning`: the programs other than
   the method under analysis are replaced by their specification
   (`modularReasoning.outputModularJDynalloy` writes the result out).
4. **Call graph** (`CallGraphVisitor`): one node per program name, an edge from
   each program to the programs it calls.
5. **`PruneVisitor`** removes the programs the method under analysis cannot
   reach in the call graph.
6. **`RecursionUnfolderVisitor`** removes recursion: for each program on a
   cycle of calls, it makes copies `<program>_unfold_0` … `_unfold_<n>` of
   every program reachable from it, where n is the bound from step 1. Inside
   copy i, a call that leads back to the program goes to copy i + 1 if
   i + 1 < n, and becomes `assume false` otherwise, so executions that recurse
   deeper are dropped (copy n is never called). In the original program, such
   a call goes to copy 0, or, with n = 0, becomes `assume false`. On the `stryker` branch the
   unfolder identifies programs by name and parameter types (its own call
   graph, built from the same modules), so that an overload calling another
   one is not taken for recursion.
7. **Modifies table** (`ModifiesTableBuilder`, on a new call graph): the fields
   each program assigns, itself or through the programs it calls (on master,
   only itself: [KNOWN-LIMITATIONS.md](KNOWN-LIMITATIONS.md#analysis)). The
   translator uses it to decide which
   invariants the postcondition must re-check. The table is keyed by program
   name, and each program visited starts its entry anew, so of programs that
   share a name (overloads, for one) only the last one visited counts.

Then it translates each module with a `JDynAlloyXlatorVisitor`: the relevant
modules first (with `removeQuantifiers`), each followed by a literal module
for each Java-arithmetic literal (`JavaPrimitiveIntegerLiteral<N>` and the
like) in the first `requires` of any specification case of its programs that
is not yet a relevant module, then the other modules. With a fixed input,
that `requires` is the formula that fixes the input
([Fixed input](#fixed-input)).

## JDynAlloyXlatorVisitor: modules, programs, statements

- **A module** becomes a DynAlloy module with the same name, whose Alloy part
  has the signature, the class's singleton signature `<Sig>Class` and, with
  `useClassSingletons` and `typeSafety`, a fact that ties the singleton to the
  class hierarchy, the literal signature, and the predicates for the module's
  invariants, conditions and contracts.
- **Fields are not Alloy fields** of the signature: each is a relation
  (`f: C -> one T`) that every program takes, with `usedObjects`, as extra
  parameters, and that every call passes on. A program's parameters are its
  own, then every field, then `usedObjects`.
- **Statements**: an assignment to a variable is a DynAlloy assignment, and
  `o.f := v` becomes `f := f ++ (o -> v)` (or `updateArray`, `updateMap`,
  `updateList` for arrays, maps and lists); `if` becomes a choice between
  `test C; then` and `test !C; else`; `while` becomes DynAlloy's `while` with
  `nestedLoopUnroll`, and `(test C; body)*; test !C` otherwise (dynalloy4
  unrolls both); `assume C` is a test; `assert C` is a choice between `C; skip`
  and `!C; throw := java_lang_AssertionErrorLit`; `createObject<C>[x]` takes an
  unused object and sets its class; `havoc` has variants for variables, array
  and list contents and fields; `call p[args]` passes the arguments, the fields
  and `usedObjects`. Conditions are wrapped in predicates named
  `<Sig>Condition<N>`.
- **Exceptions**: jDynAlloy only knows the variable `throw` and the literal
  `java_lang_AssertionErrorLit`; null dereferences, divisions and Java's
  exceptions are already encoded by TACO in the JDynAlloy it produces.
- **Java arithmetic** is in the `JavaPrimitive*Value` modules; the translator
  only threads `useJavaArithmetic` through types and visitors. TACO's
  `useJavaArithmetic` also decides how it builds array accesses
  (`ast/JavaPrimitiveIntValueArrayFactory` if true, `ast/AlloyIntArrayFactory`
  if false) and which array modules it loads (`java_lang_IntArray.djals` or
  `java_lang_AlloyIntArray.djals`, and so on), whatever `builtInModules` says.
  Nothing uses `ast/JavaUtilSetFactory`.

### The assertion `check_<method>`

For the program of the method under analysis, if it has specification cases,
the visitor builds `precondition_<program>`, `postcondition_<program>` and the
assertion `check_<program>`: the precondition and the call imply the
postcondition.

- **Specification cases.** One case is used as it is. With several, the
  precondition is the disjunction of their `requires`, and the postcondition
  the conjunction of `requires_i => ensures_i`.
- **Precondition**: for every relevant module, its class invariant and object
  invariant (for all objects of the class; with
  `skolemizeInstanceInvariant`, only for `thiz` of the class under analysis)
  and its abstraction functions (`represents`), except, for a constructor, the
  object invariant of its own class; the method's `requires`, whose predicate
  `<Sig>_requires` also has `usedObjects` = the objects reachable from the
  parameters and the static fields; `throw = null`.
- **Program**: the call, then, for each `represents` whose fields the method
  modifies, `havoc` the abstract field and `assume` its abstraction.
- **Postcondition**: the method's `ensures`, the primed invariants of the
  relevant modules whose fields the method assigns (with a fixed input, every
  object invariant; class invariants still only if the method assigns one of
  their fields), and the object constraints.

With `includeSimulationProgramDeclaration` it also writes a program
`simulate_<program>`.

## Fixed input

TACO passes an input to fix (a map from parameter names, `thiz` included, to
values) to `JDynAlloyTranslator`, which gives it to every
`JDynAlloyXlatorVisitor`. Then `JDynAlloyVisitor.visit(JSpecCase)` translates
no `requires` at all, and gives the program of the method under analysis,
found by comparing its name with `getFullyQualifiedMethodName()`, a formula
that fixes the input instead (`processInputToFix`): each primitive parameter
equal to a literal (whose literal module is created if needed), each object's
fields (those its class declares, not inherited ones) equal to their values,
read by reflection, and which parameters are the same object. Values are
matched with `equals`, not by identity, and every two different values met,
parameters and values reached through fields, are stated to be different
objects. On the `stryker` branch
`getFullyQualifiedMethodName()` is TACO's `methodToCheck`; on master it adds
the class again, so no program matches ([NAMING.md](NAMING.md)). TACO's
`docs/SPEC-OUTPUT.md` describes who passes an input and why.

## Configuration

What jDynAlloy reads from `JDynAlloyConfig.getInstance()` (TACO's keys):

| Getter | Used for |
|---|---|
| `getClassToCheck`, `getMethodToCheck` | The method under analysis ([NAMING.md](NAMING.md)) |
| `getRelevantClasses` | The modules whose invariants go into the pre- and postcondition |
| `getUseClassSingletons`, `getClassExtendsObject`, `getTypeSafety` | The class singletons and the class-hierarchy fact |
| `getNestedLoopUnroll` | `while` as DynAlloy's `while` or as an iteration |
| `getSkolemizeInstanceInvariant`, `getSkolemizeInstanceAbstraction` | Invariants and abstractions only for `thiz` of the class under analysis |
| `getIncludeSimulationProgramDeclaration` | The `simulate_` program |
| `getModularReasoning`, `getOutputModularJDynalloy` | Specification programs, and where to write them |
| `getDynamicJavaLangFields` | Whether `intValue` is primed in postconditions |
| `getUseCustomRelationalOverride` | The operator for field updates |
| `getBuiltInModules` | Which Map and List parts the prelude has |
| `getUseQualifiedNamesForJTypes` | `java_lang_` prefixes in type names |
| `getJMLObjectSequenceToAlloySequence` | How sequences are typed |

`removeQuantifiers` is not read from the configuration: TACO passes it with
`JDynAlloyTranslator.setRemoveQuantifiers`. Many keys of `IJDynAlloyConfig`
are not read inside jDynAlloy (`checkNullDereference`, `loopResolution`,
`assertionArguments`, …); TACO reads some of them itself.

## State that outlives a run

Static state that persists when TACO runs several analyses in one JVM (as
Stryker does): `JDynAlloyConfig`'s instance (TACO resets it), the
`StaticFieldsModuleBuilder` singleton, `JPreludeFactory`'s registrations, the
`JavaPrimitive*Value` singletons and their literals, the static
`RelevancyAnalysisManager.javaArithmetic` and `RelevancyAnalysisUtils.bitWidth`
(neither has an effect), and the static `quantifiersCount` of
`FieldModifiesCase` and `FieldArraysModifiesCase` (never reset, but those cases
never run).

# Known limitations

What does not work, or works in surprising ways. Each item says whether it was
seen (in a run, through TACO's `stryker` branch, macOS on Apple Silicon, Zulu
8u504) or read from the code. Checked on 2026-10-18, on the `stryker` branch.

## Analysis

- **On master, an invariant broken through a callee is not detected.** The
  postcondition re-checks a class's object and class invariants only if the
  method under analysis may modify one of the fields they mention
  (`JDynAlloyXlatorVisitor.isModifiable_object_invariants` and
  `isModifiable_class_invariants`), and on master the modifies table has only
  the method's own assignments: the transitive part of
  `ModifiesTableBuilder.buildTable` is commented out there ("This has to be
  added back once the naming convention is fixed"). With `int a`,
  `invariant a >= 0` and `setNeg() { a = -1; }`, master finds the violation
  in `direct() { a = -1; }` but not in `viaCall() { setNeg(); }` (seen). The
  `stryker` branch restores it (0f920f1), as comitaco's jDynAlloy has it, and
  finds both.
- **Two overloads with the same number of parameters cannot both be used in
  one analysis.** They have the same name and arity in DynAlloy, and the Alloy
  translation fails (`Name cannot be resolved; possible incorrect
  function/predicate call`). Either alone works (seen). A regression: on
  comitaco, whose jDynAlloy named programs `<signature>_<program>_<index>`,
  it works; the index went with this repository's first commit, 5b33641
  (`VAlloyVisitor.java:78`, `100-101`).
- **`f(null)` is an "Ambiguous call" with Java arithmetic** when `f` has an
  `int` and an object overload: `int` values are objects
  (`JavaPrimitiveIntegerValue`) then, and `null` fits both (seen).
- **`modifies` clauses add no frame conditions.** `ReplaceModifiesModuleVisitor`
  picks the program with `methodToCheck.startsWith(<signature>_<program>_)`,
  which never matches TACO's names; with a class of two fields and a method
  `assignable a`, the postcondition has nothing about the other field, before
  and after the new method naming (seen). A regression: comitaco's
  postcondition has `thiz.b = thiz.b'`. Its `methodToCheck` was
  `<Class>_<method>_<index>`, which the test matches; TACO 3ae43be3 (2022)
  made it `<method>(<types>)`. JML's own checker, in TACO, rejects
  assignments and calls the clause does not allow, by field name.
- **Dynamic dispatch does not work.** `SearchImplementors.searchImplementors`
  compares each binding with itself (it reads `entry` where it means
  `inspectedEntry`) and never puts anything in the map it returns, so no
  `virtual` program has implementors, and `VAlloyVisitor` would then fail to
  build its dispatcher (read from the code; not run).
- **Parts of the prelude are always left out.** `SceneSlicerManager.process`
  asks its list of modules whether it contains the strings `java_util_Map`,
  `java_util_List` and `java_lang_String`, which is always false, so it always
  unregisters those parts of the prelude (read from the code; the effect was
  not checked).
- **Literal modules** take the number from the literal's name
  with fixed offsets (27, 24, 25 and 24 characters, `JDynAlloyTranslator`),
  which assumes the length of the prefix (read from the code).

## Parser and printer

- **`JDynAlloyParserManager.parseModulesFile(file, ctx)` calls itself**, so it
  ends in a `StackOverflowError`. TACO calls it for each file of its key
  `jdynalloy.parser.inputFiles` (commented out in its configurations); the
  resources TACO uses go through `parseModulesResource`, which works (read from
  the code).
- `createReaderFromResource` builds a slash-separated resource name and then
  throws it away (`String.replace` without using the result) (read from the
  code).
- Every parsed `if` is labelled `if_1`, and every `while` `while_10000`:
  `LabelUtils` never increments its counters (read from the code).
- The grammar has `program pure`, but the lexer has no rule for the `PURE`
  token, so pure programs cannot be written in text; and `JDynAlloyPrinter`'s
  output cannot always be parsed back ([LANGUAGE.md](LANGUAGE.md#the-parser))
  (read from the code).

## Building

- **JDK 8 only**: from a clean tree, `ant jar` on JDK 21 or 25 fails with
  `Source option 7 is no longer supported` (and the same for `Target`). After
  a JDK 8 build it "succeeds", but only because the classes are up to date and
  nothing is compiled (seen).
- `lib/dynalloy4.jar` is not in git and must be copied in; without it nothing
  compiles (`package ar.uba.dc.rfm.alloy.ast.expressions does not exist`). The class path also names `lib/antlr-3.2.jar` and
  `lib/alloy4.jar`, which do not exist and are not needed (seen).
- `ant jar` increments `build.num`, which is in git (seen).
- `dist/jdynalloy_src.jar` has the Java sources but not the grammar: the
  patterns `src/**/.g` and `src/**/.xml` match only files named `.g` and `.xml`
  (seen).
- `build.xml.txt` is a copy of `build.xml`, and the project is named `taco`
  (read).
- There are no tests: TACO's drivers are the only check
  ([AGENTS.md](../AGENTS.md)).

## Other

- **Static state outlives a run** in the same JVM (TACO runs many in one JVM
  for Stryker): the `StaticFieldsModuleBuilder` singleton, `JPreludeFactory`'s
  registrations, the `JavaPrimitive*Value` singletons and their literals, the
  static `RelevancyAnalysisManager.javaArithmetic` and
  `RelevancyAnalysisUtils.bitWidth` (neither has an effect), and the static
  `quantifiersCount` of `FieldModifiesCase` and `FieldArraysModifiesCase`
  (never reset, but those cases never run). TACO
  resets only `JDynAlloyConfig` (read from the code).
- `JDynAlloyConfigImpl` is not used: TACO's `TacoConfigurator` implements
  `IJDynAlloyConfig` itself (read from the code).
- `RelevancyAnalysisManager` analyses the body of every program in the scene,
  although its comment says only the method under analysis (read from the
  code).

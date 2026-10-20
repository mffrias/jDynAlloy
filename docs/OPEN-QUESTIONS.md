# Open questions

Points where the code does not say what was intended, for the maintainer
(Marcelo Frias). TACO's and dynalloy4's own questions are in their
`docs/OPEN-QUESTIONS.md`. Line numbers are those of the `stryker` branch,
2026-10-18.

1. **Overloads.** Programs are named without their parameter types, and
   DynAlloy tells them apart by number of parameters only, so two overloads
   with the same number of parameters cannot both be used in one analysis;
   with comitaco's `<signature>_<program>_<index>` names they could. The
   `stryker` branch identifies programs by name and parameter types in the
   recursion unfolder only (d2b09e9). Should program names include the types,
   or the index again?
2. **Frame conditions.** `modifies/ReplaceModifiesModuleVisitor.java:431`
   (and the unused copy in `slicer/`) looks for the method under analysis with
   `methodToCheck.startsWith(<signature>_<program>_)`, which has not matched
   since TACO named the method `<method>(<types>)` (3ae43be3, 2022), so
   `modifies` clauses add nothing; comitaco's add frame conditions. Should it
   match `<program>(`, given that JML's checker already enforces `assignable`
   by field name?
3. **Typed names that never match.** `ModularMutator` (line 76) compares the
   program with the typed `methodToCheck`, so with `modularReasoning` the
   method under analysis would be replaced by its specification too (read
   from the code, not run); and `CallGraphVisitor` (lines 84-87) builds a
   typed name that TACO's untyped `methodToCheck` never matches. Intended?
4. **Dynamic dispatch.** `SearchImplementors.searchImplementors` never finds an
   implementor (it compares each binding with itself and returns an empty
   map). Is `virtual` meant to work?
5. **The prelude.** `SceneSlicerManager.process` tests
   `result.contains("java_util_Map")` (and `List`, `String`) on a list of
   modules, which is always false. Was `relevantClassesList` meant?
6. **`src/ar/edu/taco/simplejml/builtin/`**: TACO's models of primitive values
   live here, under TACO's package name, and load Alloy files that are in
   TACO. Should they move to TACO, or get a jDynAlloy package?
7. **`JDynAlloyConfigImpl`** is unused (TACO implements `IJDynAlloyConfig`), and
   so is `slicer/ReplaceModifiesModuleVisitor`. Keep them?
8. **A license file.** The repository has none. 51 of its 136 Java sources
   say, in their header, GNU GPL version 2 or later (for example
   `binding/BindingManager.java`); the others say nothing. Should a `LICENSE`
   file say GPL version 2 or later for the whole repository? (TACO's headers
   say the same, and its `COPYING` has the text of version 3.)

## From the Javadoc pass

9. **The `_0` fallback for fields** (`binding/symboltable/SymbolTable.java:133-155`,
   `lookupField`): a field that is not found, and whose name starts with
   `SK_jml_pred_java_primitive` or `SK_pred_java_primitive`, is looked up again with `_0`
   appended, in any module. Nothing in jDynAlloy or TACO creates such names. Which fields get
   the suffix, and is the fallback still needed? (When the receiver's own module has the `_0`
   field, the code returns the lookup of the original name, i.e. null.)
10. **What the relevancy analysis skips** (`relevancy/RelevancyAnalysisManager.java`,
    `relevancyAnalysisIterationModule`, lines 263-344; `RelevancyAnalysisStatementVisitor`):
    object and class constraints are not analysed, and loop invariants are not visited (no
    `visit(JLoopInvariant)`), although the translation uses both. Intended?
11. **The static `javaArithmetic` and `bitWidth`** (`RelevancyAnalysisManager.java:66`,
    `setJavaArithmetic`; `RelevancyAnalysisUtils.java:51`, `setBitWidth`): `javaArithmetic` only
    reaches `FieldCollectorVisitor.setJavaArithmetic` (line 465), whose flag nothing reads (the
    analysis uses `process`'s `isJavaArith` argument), and `bitWidth` is only read in
    `processCollectedFunctionNames` (531) behind a test that is always false (it asks a list of
    `FunctionCallAlloyFormulaDescriptor`s whether it contains a `String`). Why are they static,
    are they still needed, and should the `JavaPrimitiveIntegerLiteral` modules for
    `fun_java_primitive_integer_value_size_of` be added?
12. **Functions in specifications** (`RelevancyAnalysisUtils.java`, `analyzeFormula`, and
    `findCalledProgramAndToScene`, 270-275): `BindingManager` binds no function calls (the
    `ExprFunction` variant of `processIProgramCallSupport` in `SemanticCheckVisitor` is commented
    out), so a function in a specification is resolved only if some `call` statement has the same
    name and arguments. Should functions in specifications be bound?
13. **Invariants that reach programs through functions** (`RelevancyAnalysisUtils.java:459`,
    `analyzeObjectInvariant`): a copy of `analyzeFormula` without the function-call lookup, so
    such programs are not added. Intended?
14. **Fields in the scene** (`relevancy/Scene.java:113`, `addField`): nothing calls it, so the
    manager's step for field elements (`RelevancyAnalysisManager.java:168-170`) never runs. Were
    fields meant to be elements of the analysis?
15. **The `modifies` cases** (`modifies/AbstractModifiesCase.java`, `FieldModifiesCase`,
    `FieldArraysModifiesCase`): `ReplaceModifiesModuleVisitor.visit(JSpecCase)` creates both
    cases (147-149) but uses them only in `processJModifies` (315), whose one call (213) is
    commented out. Are they still meant to be used? (See KNOWN-LIMITATIONS, "`modifies` clauses
    add no frame conditions".)
16. **`setLiteralSingleton` with the class singleton**
    (`modifies/ReplaceModifiesModuleVisitor.java:408-410`): a module with a class singleton gets
    it as its literal singleton. Was `setClassSingleton` meant? (No effect today: TACO's modules
    have no class singleton, `useClassSingletons` being false.)
17. **Two-dimensional int arrays** (`ast/JavaPrimitiveIntValueArrayFactory.java:60-61`,
    `array_access`): a `java_lang_IntArray2d+null` array is read through
    `java_lang_IntArray_contents`, the relation of one-dimensional int arrays. TACO's
    `java_lang_IntArray2d.djals` declares `java_lang_IntArray2d_contents` instead, and TACO's
    `PrecompiledModules` never loads that file. Are two-dimensional arrays meant to work?
18. **`JavaUtilSetFactory`** (`ast/JavaUtilSetFactory.java`): nothing in jDynAlloy or TACO calls
    it, and the function it names, `javaUtilSetSize`, is defined neither in the prelude nor in
    TACO's built-in modules (TACO uses `JExpressionFactory.javaUtilSetSizeReturnsAlloyInt` and
    `javaUtilSetSizeReturnsJavaPrimitiveIntegerValue`). Can it go?
19. **Does the `.djals` text parse back?** (`ast/JDynAlloyPrinter.java`, `visit(JDynAlloyModule)`):
    nothing in TACO or jDynAlloy reads the printed modules back, and much of the output does not
    parse with jDynAlloy's grammar (besides the three cases in LANGUAGE.md, "The parser": several
    signature facts, interfaces printed as `in` facts, fields and Alloy predicates and functions
    of the signature itself, the class and literal singletons, empty blocks, bodies that are not
    blocks), while `super`, `pure` and the arithmetic predicates and variables are dropped. Is it
    meant to be read back, or only for people?

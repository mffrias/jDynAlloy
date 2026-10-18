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

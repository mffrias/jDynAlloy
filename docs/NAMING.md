# Names

How jDynAlloy names what it produces, which names it gets from TACO, and where
it looks for the method under analysis by name. Read from the code of the
`stryker` branch (2026-10-18), and checked by running TACO's drivers where
said.

## What TACO gives it

- **Modules and signatures**: Java class names with underscores for dots
  (`java_util_Date`, `ximena_MutRecursive`). A module's name is its
  signature's. Static fields go, as `<module>_<field>`, to the module
  `ClassFields`.
- **Fields**: `<class>_<field>` (`scratchframe_Frame_b` for field `b` of
  `scratchframe.Frame`).
- **Programs**: since TACO 955795ac (2026-10-16), `<declaring class>_<method>`
  (`ximena_MutRecursive_isEven`), constructors `<class>_<class simple name>`
  (`ximena_MutRecursive_MutRecursive`). Overloads therefore share their name.
  In `.djals` files the programs keep their bare names (`toString`), and
  constructors are called `Constructor`.
- **Calls**: on TACO's `stryker` branch, `<receiver type>_<method>` when the
  call is static or its receiver is one of the classes TACO translates, and
  the bare method name for instance calls into the built-in modules. TACO
  gives every method it translates a `thiz` parameter, static ones too, so
  for jDynAlloy those are all instance programs; only the built-in modules'
  static programs (first parameter `throw`) are static, and `JBindingKey` keys
  them as `<signature>_<program>`, which is what their calls are named.
- **`classToCheck`**: the class under analysis with underscores
  (`ximena_MutRecursive`). **`methodToCheck`**: in the configuration, the
  method with its parameter types, `<package>_<Class>_<method>(<types>)`; the
  `methodToCheck` passed to `JDynAlloyTranslator` is the same without the
  types.

## What it names

| Name | What |
|---|---|
| `<program>` | The DynAlloy program; `VAlloyVisitor` keeps the name. A program met after another of the same name would get `<program>_0` if it were pure with no `java_lang_Throwable` parameter, but every pure program TACO makes has `throw: java_lang_Throwable+null`, and `.djals` programs cannot be pure |
| `<program>_unfold_<i>` | The copies the recursion unfolder makes |
| `check_<program>` | The assertion: the method under analysis meets its specification |
| `precondition_<program>`, `postcondition_<program>` | Its two sides |
| `simulate_<program>` | With `includeSimulationProgramDeclaration` |
| `<Sig>_requires`, `<Sig>_ensures` | The method's contract as predicates |
| `<Sig>_object_invariant`, `<Sig>_class_invariant`, `<Sig>_object_constraint` | The class's invariants and constraints |
| `<Sig>_<field>_abstraction` | A `represents` clause |
| `<Sig>Condition<N>` | The condition of an `if` or `while` |
| `<Sig>Class`, `<Sig>Lit` | The class's singleton and literal signatures |
| `JavaPrimitiveIntegerLiteral<N>`, `JavaPrimitiveIntegerLiteralMinus<N>` | Integer literals with Java arithmetic (and the same for long, char and float) |
| `prelude` | The module with the shared declarations (`JPreludeFactory`) |

## Where the method under analysis is looked up

| Where | Compares | For |
|---|---|---|
| `relevancy/RelevancyAnalysisUtils.java:174` | the configuration's `methodToCheck` with `<program>(<parameter types>)`, in the module `classToCheck` | the starting point of the relevancy analysis; "The method to check was not found" if none |
| `modifies/ModifiesSolverManager.java:134` | the same | checking that the method exists |
| `xlator/JDynAlloyTranslator.java:208-209` | `methodToCheck` up to `(` | the root of `PruneVisitor` |
| `xlator/CallGraphVisitor.java:112-115` | `classToCheck + "_" + methodToCheck` with `<program>(<types>)` | a typed node name for the method under analysis; TACO passes the name without types, so it never matches and every node is a plain program name |
| `xlator/JDynAlloyXlatorVisitor.java:830` | the signature with `classToCheck`, and the program with `methodToCheck` up to `(` | which program gets `check_<program>` |
| `xlator/JDynAlloyXlatorVisitor.java:2057` (`getFullyQualifiedMethodName`), used at `ast/JDynAlloyVisitor.java:469` | the program with `methodToCheck` | which program gets the fixed input |
| `xlator/ModularMutator.java:114` | the program with the configuration's `methodToCheck`, with its types | which program keeps its body with `modularReasoning`; the program has no types in its name, so it never matches |
| `modifies/ReplaceModifiesModuleVisitor.java:517` | `methodToCheck.startsWith(<signature>_<program>_)` | which program's `modifies` become frame conditions; never matches the new names ([KNOWN-LIMITATIONS.md](KNOWN-LIMITATIONS.md#analysis)) |

## The new naming

Before 2026-10-16, TACO called the method under analysis `<method>_<index>`
(or `<method>(<types>)`) and added the class itself, and programs were named
after the method alone. Overloads have shared their name since jDynAlloy's
first commit (5b33641, 2022), which dropped `VAlloyVisitor`'s index suffix
except for pure overloads. TACO 955795ac
and jDynAlloy cad278a changed that to the names above: cad278a removed the
`<signature>_` that `RelevancyAnalysisUtils`, `ModifiesSolverManager` and
`VAlloyVisitor` added. Then:

| Commit | What |
|---|---|
| e8c6b06 (master, 2026-10-17) | A program that is not recursive is no longer unfolded as if it were (`Graph.strictDescendentsOf`) |
| 9bb5b49 (`stryker`, 2026-10-18) | `getFullyQualifiedMethodName()` returns `methodToCheck`. It added the class again, so no program matched, and with a fixed input every `requires` was dropped and the input never applied; Stryker's feedback then pruned nothing |
| d2b09e9 (`stryker`, 2026-10-18) | The recursion unfolder identifies programs by name and parameter types. With the names alone, `MutRecursive()` calling `new MutRecursive(0)` looked recursive, its call became `..._MutRecursive_unfold_0` with the other constructor's arguments, and DynAlloy failed with a `NullPointerException`. TACO's scope inference needed the same change (TACO `stryker` 7b560282) |
| 0f920f1 (`stryker`, 2026-10-18) | Not about names, but waiting for them: `ModifiesTableBuilder` counts the fields the callees assign again. Its comment asked for that "once the naming convention is fixed"; without it, an invariant broken through a callee was not detected |

Checked with TACO's `stryker` branch: its smoke test gives the same results as
before the new naming, on JDK 8, 21 and 25 (TACO's `docs/METHOD-NAMING.md` has
the whole status). Still open: two overloads with the same number of
parameters cannot both be used in one analysis, since DynAlloy tells programs
apart by name and number of parameters only
([KNOWN-LIMITATIONS.md](KNOWN-LIMITATIONS.md)).

# The JDynAlloy language

JDynAlloy is what TACO translates Java + JML into, and what jDynAlloy
translates into DynAlloy. It exists as an AST (`src/ar/edu/jdynalloy/ast/`),
which is how TACO builds the modules of the classes under analysis, and as a
text syntax (`.djals` files), which is how TACO writes its models of JDK
classes (`src/ar/edu/taco/engine/precompiledmodules/` in TACO, 30 files) and
how it prints every module it analyses (`output/**/*.djals`). Read from the
code of the `stryker` branch (2026-10-18).

## An example

TACO's model of `java.util.Date`, without its license header:

```
module java_util_Date
sig java_util_Date extends java_lang_Object {} {}

field dateHashCode:(java_util_Date)->one(Int) {}

program java_util_Date::toString[var thiz:java_util_Date,var throw:java_lang_Throwable+null, var return:java_lang_String]
Specification
{
	SpecCase #0 {
		ensures {
			(throw'=null) and (equ[return'.stringHashCode',thiz'.dateHashCode'])
		}
	}
}
Implementation
{
	throw:=null;
	havoc return;
	assume return.stringHashCode = thiz.dateHashCode;
}

program java_util_Date::Constructor[var thiz:java_util_Date, var throw:java_lang_Throwable+null, var i:Int]
...
```

A module is a class: a signature, its fields, and its methods as programs.
Every program has the receiver `thiz` (static methods too, see below), the
variable `throw` (the exception thrown, `null` if none) and, if it returns
something, `return`. In `ensures`, a primed name is the value after the
program (`throw'`, `thiz'.dateHashCode'`).

## Modules

| Part | AST | Syntax |
|---|---|---|
| Module | `JDynAlloyModule`: its name, signature, class singleton and literal singleton, fields, invariants, constraints, `represents`, programs, and the variables and predicates that encode arithmetic in invariants | `module M`, then the signature; the module name must equal the signature's |
| Signature | `JSignature`, a subclass of dynalloy4's `AlloySig`: abstract or not, its parent (`extends`) or the set it is in (`in`), interfaces, facts, type parameters | `[abstract] sig M [extends S \| in S] {} { [fact] }` |
| Field | `JField`: a relation from the class to the field's type | `field f: (C)->one(T) {}` |
| Invariants and constraints | `JObjectInvariant`, `JClassInvariant`, `JObjectConstraint`, `JClassConstraint`, each one formula; object invariants talk about `thiz`; the translation ignores class constraints | `object_invariant F`, `class_invariant F`, `object_constraint F`, `class_constraint F` |
| Abstraction | `JRepresents`: an abstract field of `thiz`, its type, and the formula that defines it | `represents thiz.f such that F` (only `thiz.f` or `f`) |

Static fields are not fields of their class: `StaticFieldsModuleBuilder` puts
them, as `<module>_<field>`, in a module `ClassFields`, together with the
class invariants.

## Programs

`JProgramDeclaration`: the signature it belongs to, its name, its parameters
(`JVariableDeclaration`s: a variable and a type), its specification cases
(`JSpecCase`: lists of `requires`, `ensures` and `modifies`), its body, and
whether it is virtual (dispatched on the receiver's class), a constructor, or
pure.

    [virtual] program M::p[var x: T, ...]
    [Specification { SpecCase #0 { requires {F} ensures {F} modifies {E} ... } ... }]
    Implementation { statements }

A program named `Constructor` is a constructor. A program is static if its
first parameter is `throw` (or `arg_throw`) rather than `thiz`. A `modifies`
with no location means "modifies everything".

## Statements

| Statement | Syntax |
|---|---|
| `JAssert` | `assert F;` |
| `JAssume` | `assume F;` |
| `JVariableDeclaration` | `var x: T;` |
| `JSkip` | `skip;` |
| `JIfThenElse` | `if F { ... } else { ... };` |
| `JCreateObject` | `createObject<C>[x];` |
| `JAssignment` | `x := E;`, `x.f := E;` |
| `JProgramCall` | `[super] call p[args];`: the receiver is the first argument; a call is static if its first argument is `throw` and it has no other, or its second is a variable other than `throw` |
| `JWhile` | `while F [loop_invariant F] { ... };` |
| `JBlock` | `{ ... }` |
| `JHavoc` | `havoc E;` |

`super` has no effect: binding resolves the call as any other,
`VAlloyVisitor` rebuilds every call without it, and `JDynAlloyPrinter` does
not print it.

Formulas are Alloy's (`implies`, `or`, `and`, `not`, the quantifiers `some`,
`all`, `lone`, `no`, `one` with `x: T | {F}`, `=`, predicate calls
`p[...]` and `alias/p[...]`), plus `callSpec p[...]` and
`staticCallSpec p[...]`, the specification of a program as a formula.
Expressions have `+`, `.`, `->`, `++`, `&`, `f[...]`, numbers, `true` and
`false`. Comments are `--`, `//` and `/* */`.

Types are read by `JType.parse`, which first drops every parenthesis: a
signature or a union (`java_lang_Throwable+null`), `set T`, `seq T`, a
function `A->one B` (as in `field`), `A set->lone B`, a relation `A->B`, a
field of sequences `A->seq B`, and a few exact texts for the contents of
arrays, sets, maps and lists (such as `java_lang_IntArray->(Int set->lone Int)`).

## The parser

`src/ar/edu/jdynalloy/parser/jdynalloy.g` is an ANTLR 2.7.6 grammar (parser
k = 7, lexer k = 8). The build generates `JDynAlloyLexer.java`,
`JDynAlloyParser.java` and `JDynAlloyParserTokenTypes.java` (and `.txt`,
`.smap`) from it, in the same folder; they are in git. Entry points, in
`JDynAlloyParserManager`: `parseModulesResource(name, ctx)` (what TACO uses,
for class-path resources), `parseModulesFile`, `parse` and `parseFile`.

`JDynAlloyPrinter` prints an AST as text; `JDynAlloyXlatorVisitor` also uses
its `visit(JSignature)` to write the signatures in the Alloy part of each
DynAlloy module. Not all of its output can be parsed
back: it prints branch labels (`lblpos`) and `one sig`, which the grammar does
not have, and "modifies everything" as `modifies {EVERYTHING}`, for which it
has no keyword. Nor does the grammar take a module's class and literal
singletons (more signatures), more than one fact, interfaces (printed as
facts `M in I`; formulas have no `in`), both `extends` and `in`, the
signature's own fields, the Alloy predicates and functions printed after it,
an empty block, or a program or loop body that is not a block
(`JAlloyProgramBuffer` makes a one-statement body a bare statement). The
printer also leaves out `super`, `pure`, and the variables and predicates
that encode arithmetic. The grammar has a `PURE` token for `program pure`, but
the lexer has no rule for it, so pure programs cannot be written in text. See
[KNOWN-LIMITATIONS.md](KNOWN-LIMITATIONS.md) for the parser's other quirks.

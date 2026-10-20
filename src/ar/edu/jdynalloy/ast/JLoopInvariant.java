package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * {@code loop_invariant F}: the invariant of a {@link JWhile}, an annotation and not a statement.
 * {@code JDynAlloyTransformationVisitor} replaces each {@code while} that has one by
 * {@code assert} the invariant, {@code havoc} what the loop modifies, {@code assume} the
 * invariant and one guarded iteration (docs/ARCHITECTURE.md, step 2 of the translation), so the
 * translation proper does not meet one (its {@code visit(JLoopInvariant)} would translate it as
 * an {@code assert}). The bindings bind the {@code callSpec} formulas in it; the relevancy
 * analysis does not look into it (docs/OPEN-QUESTIONS.md, item 10). No {@code equals} or
 * {@code hashCode}, and {@link JWhile#equals} ignores it.
 */
public final class JLoopInvariant implements IJDynAlloyAnnotation {

	public AlloyFormula getFormula() {
		return formula;
	}

	private final AlloyFormula formula;

	public JLoopInvariant(AlloyFormula formula) {
		super();
		this.formula = formula;
	}

	public final Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}
}

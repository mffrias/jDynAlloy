package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

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

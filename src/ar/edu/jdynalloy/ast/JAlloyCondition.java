package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;

public final class JAlloyCondition {
	private final PredicateFormula f;

	public JAlloyCondition(PredicateFormula _f) {
		f = _f;
	}

	public PredicateFormula getPredicateFormula() {
		return f;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(JAlloyCondition.class)) {
			JAlloyCondition that = (JAlloyCondition) arg0;
			return this.getPredicateFormula()
					.equals(that.getPredicateFormula());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return this.getPredicateFormula().hashCode();
	}

	@Override
	public String toString() {
		return this.getPredicateFormula().toString();
	}
	
	public Object accept(IJDynAlloyVisitor visitor) {
		return visitor.visit(this);
	}
	
}
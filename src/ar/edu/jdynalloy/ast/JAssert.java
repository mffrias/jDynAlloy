package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public final class JAssert extends JStatement {

	private final AlloyFormula assertion;

	public JAssert(AlloyFormula assertion) {
		super();
		this.assertion = assertion;
	}

	public final Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	public AlloyFormula getCondition() {
		return assertion;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(JAssert.class)) {
			JAssert a = (JAssert) arg0;
			return getCondition().equals(a.getCondition());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return getCondition().hashCode();
	}

	@Override
	public String toString() {
		return "assertion " + getCondition().toString();
	}
}

package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

public final class JAssignment extends JStatement {

	public JAssignment(AlloyExpression lvalue, AlloyExpression rvalue) {
		super();
		this.lvalue = lvalue;
		this.rvalue = rvalue;
	}

	public final Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	private final AlloyExpression lvalue;

	private final AlloyExpression rvalue;

	public AlloyExpression getLvalue() {
		return lvalue;
	}

	public AlloyExpression getRvalue() {
		return rvalue;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(JAssignment.class)) {
			JAssignment a = (JAssignment) arg0;
			return getLvalue().equals(a.getLvalue())
					&& getRvalue().equals(a.getRvalue());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return lvalue.hashCode() + rvalue.hashCode();
	}

	@Override
	public String toString() {
		return lvalue.toString() + "=" + rvalue.toString();
	}

}

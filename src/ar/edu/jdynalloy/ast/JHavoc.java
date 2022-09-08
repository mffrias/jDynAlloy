package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

public final class JHavoc extends JStatement {

	@Override
	public String toString() {
		return "havoc " + expression.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JHavoc other = (JHavoc) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
	}

	public AlloyExpression getExpression() {
		return expression;
	}

	public JHavoc(AlloyExpression expression) {
		super();
		this.expression = expression;
	}

	private final AlloyExpression expression;

	@Override
	public Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

}

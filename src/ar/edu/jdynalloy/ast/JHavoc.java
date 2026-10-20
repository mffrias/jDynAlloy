package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

/**
 * {@code havoc E;}: gives {@code E} an arbitrary value. {@code JDynAlloyXlatorVisitor} accepts a
 * variable (a field as a whole, or a local variable or parameter), an array access (the array's
 * contents), {@code l.java_util_List_seq} (the contents of a list, {@link ListAccess}) and a join
 * {@code o.f} (the field of one object). It throws {@code UnsupportedOperationException} for a
 * {@link MapAccess} or a {@code List_contains} access, and {@code IllegalArgumentException} for
 * any other expression. TACO's models of JDK classes use it ({@code havoc return}), and so does
 * the translation, for loop invariants (what the loop modifies), {@code modularReasoning} (what a
 * callee may modify) and {@code represents} (the abstract fields that the method under analysis
 * modifies). Equal when the expressions are equal.
 */
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

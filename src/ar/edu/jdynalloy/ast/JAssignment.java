package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

/**
 * {@code x := E;} or {@code o.f := E;}: an assignment. {@code JDynAlloyXlatorVisitor} translates
 * it by the form of the left side: a variable, into a DynAlloy assignment; an array access
 * ({@code AlloyIntArrayFactory.isArrayAccess}), into {@code updateArray}; a {@link MapAccess},
 * into {@code updateMap}; a {@link ListAccess}, into {@code updateList}; any other join
 * {@code o.f}, with {@code f} a variable, into {@code f := f ++ (o -> E)}, or
 * {@code f := rel_override[f, o, E]} with {@code useCustomRelationalOverride}. Any other left
 * side is an {@code IllegalArgumentException} there. Equal when both sides are equal.
 */
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

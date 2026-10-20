package ar.edu.jdynalloy.ast;

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * {@code represents thiz.f such that F}: an abstraction function, which TACO makes from JML
 * {@code represents} clauses: {@code F} defines the abstract (model) field {@code f} of
 * {@code thiz}. {@code JDynAlloyXlatorVisitor} wraps {@code F} into the predicate
 * {@code <Sig>_<field>_abstraction}, which goes into the precondition of the method under
 * analysis; after the call, for each {@code represents} whose fields the method modifies, it adds
 * {@code havoc} of the abstract field and {@code assume} of its abstraction
 * (docs/ARCHITECTURE.md, "The assertion {@code check_<method>}"). Equal when the three parts are
 * equal.
 */
public class JRepresents {

	/**
	 * Creates an abstraction function.
	 *
	 * @param expression the abstract field: {@code thiz.f} or {@code f}, the only forms the
	 *        parser accepts
	 * @param expressionType the type of the field {@code f} as its module declares it, not that
	 *        of the expression
	 * @param formula the formula that defines the field
	 */
	public JRepresents(AlloyExpression expression, JType expressionType, AlloyFormula formula) {
		super();
		this.expression = expression;
		this.expressionType = expressionType;
		this.formula = formula;
	}

	private final AlloyExpression expression;
	
	private final JType expressionType;

	private final AlloyFormula formula;

	public Object accept(IJDynAlloyVisitor visitor) {
		return visitor.visit(this);
	}

	public AlloyExpression getExpression() {
		return expression;
	}

	public AlloyFormula getFormula() {
		return formula;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
		result = prime * result + ((expressionType == null) ? 0 : expressionType.hashCode());
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
		JRepresents other = (JRepresents) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (formula == null) {
			if (other.formula != null)
				return false;
		} else if (!formula.equals(other.formula))
			return false;
		if (expressionType == null) {
			if (other.expressionType != null)
				return false;
		} else if (!expressionType.equals(other.expressionType))
			return false;
		return true;
	}

	public JType getExpressionType() {
		return expressionType;
	}

}

package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;

/**
 * A precondition or postcondition of a {@link JAssertionDeclaration}: a predicate call
 * ({@code PredicateFormula}). Equal when the formulas are equal.
 *
 * <p>Unused, like {@link JAssertionDeclaration}: nothing in jDynAlloy or TACO creates one, and
 * {@code JDynAlloyVisitor.visit(JAlloyCondition)} throws {@code IllegalArgumentException} ("class
 * JAlloyCondition not supported"). Unclear: whether the two classes are still needed.
 */
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
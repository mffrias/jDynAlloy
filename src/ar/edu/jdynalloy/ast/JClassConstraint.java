package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * {@code class_constraint F}: a constraint on the static state of a class, which TACO makes from
 * static JML {@code constraint} clauses. The modules built with {@code DynJMLAlloyModuleBuffer}
 * (TACO's classes and the parsed {@code .djals} modules) have none of their own: the buffer hands
 * them to {@code StaticFieldsModuleBuilder}, so they all end up in the module {@code ClassFields}
 * (docs/LANGUAGE.md, "Modules"). Equal when the formulas are equal.
 *
 * <p>The translation ignores them: {@code JDynAlloyXlatorVisitor} declares a map for
 * class-constraint predicates but never fills it, and builds nothing from them (docs/NAMING.md
 * lists no {@code <Sig>_class_constraint}); the relevancy analysis does not look into them
 * either. Unclear: whether class constraints are meant to be checked, as object constraints are
 * in the postcondition.
 */
public class JClassConstraint {

	public JClassConstraint(AlloyFormula formula) {
		super();
		this.formula = formula;
	}

	private final AlloyFormula formula;

	public AlloyFormula getFormula() {
		return formula;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
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
		JClassConstraint other = (JClassConstraint) obj;
		if (formula == null) {
			if (other.formula != null)
				return false;
		} else if (!formula.equals(other.formula))
			return false;
		return true;
	}

	public Object accept(IJDynAlloyVisitor visitor) {
		return visitor.visit(this);
	}
	
	
}

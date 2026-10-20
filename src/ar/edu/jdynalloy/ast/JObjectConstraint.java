package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * {@code object_constraint F}: a constraint on the objects of a class, which TACO makes from JML
 * {@code constraint} clauses (history constraints). {@code JDynAlloyXlatorVisitor} conjoins the
 * object constraints of a module into the predicate {@code <Sig>_object_constraint}, which the
 * postcondition of the method under analysis states for every object of the class, for every
 * relevant module. The relevancy analysis does not look into them (docs/OPEN-QUESTIONS.md, item
 * 10). Equal when the formulas are equal.
 */
public class JObjectConstraint {

	public JObjectConstraint(AlloyFormula formula) {
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
		JObjectConstraint other = (JObjectConstraint) obj;
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

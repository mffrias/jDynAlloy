package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * {@code object_invariant F}: an invariant of every object of a class, which TACO makes from JML
 * {@code invariant} clauses; {@code F} talks about {@code thiz}. {@code JDynAlloyXlatorVisitor}
 * conjoins the object invariants of a module into the predicate {@code <Sig>_object_invariant}.
 * The precondition of the method under analysis states it for every object of the class (only for
 * {@code thiz} of the class under analysis with {@code skolemizeInstanceInvariant}), and the
 * postcondition states it again, primed, when the method may assign a field that it mentions
 * (docs/ARCHITECTURE.md, "The assertion {@code check_<method>}"). The variables and predicates
 * that encode the arithmetic in the object invariants belong to the module
 * ({@link JDynAlloyModule}). Equal when the formulas are equal.
 */
public class JObjectInvariant {

	public JObjectInvariant(AlloyFormula formula) {
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
		JObjectInvariant other = (JObjectInvariant) obj;
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

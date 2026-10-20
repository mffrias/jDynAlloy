package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * {@code class_invariant F}: an invariant of the static state of a class, which TACO makes from
 * static JML {@code invariant} clauses. As with {@link JClassConstraint}, the modules built with
 * {@code DynJMLAlloyModuleBuffer} hand them to {@code StaticFieldsModuleBuilder}, so they all end
 * up in the module {@code ClassFields}. {@code JDynAlloyXlatorVisitor} conjoins the class
 * invariants of a module into the predicate {@code <Sig>_class_invariant}, which goes into the
 * precondition of the method under analysis and, primed, into its postcondition
 * (docs/ARCHITECTURE.md, "The assertion {@code check_<method>}", says when). Equal when the
 * formulas are equal.
 */
public class JClassInvariant {

	public JClassInvariant(AlloyFormula formula) {
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
		JClassInvariant other = (JClassInvariant) obj;
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

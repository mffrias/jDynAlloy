package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * {@code assume F;}: keeps only the executions in which {@code F} holds;
 * {@code JDynAlloyXlatorVisitor} translates it into a DynAlloy test of {@code F}. TACO emits it
 * for JML {@code assume} statements and in its models of JDK classes, and
 * {@code JDynAlloyTransformationVisitor} for loop invariants (docs/ARCHITECTURE.md, step 2 of the
 * translation). {@code BindingManager} binds the {@code callSpec} formulas in it. Equal when the
 * formulas are equal.
 */
public final class JAssume extends JStatement {

	public JAssume(AlloyFormula assume) {
		super();
		this.assume = assume;
	}

	private final AlloyFormula assume;
	
	@Override
	public Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	public AlloyFormula getCondition() {
		return assume;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assume == null) ? 0 : assume.hashCode());
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
		JAssume other = (JAssume) obj;
		if (assume == null) {
			if (other.assume != null)
				return false;
		} else if (!assume.equals(other.assume))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "assume " + getCondition().toString();
	}

}

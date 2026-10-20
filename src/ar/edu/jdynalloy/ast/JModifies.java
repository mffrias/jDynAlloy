package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

/**
 * A location in the {@code modifies} of a {@link JSpecCase} ({@code modifies {E}}), or "modifies
 * everything" when the location is null ({@link #buildModifiesEverything()}, or the constructor
 * with null), which {@code JDynAlloyPrinter} prints as {@code EVERYTHING}. TACO makes them from
 * JML {@code assignable} clauses. They reach the analysis only through
 * {@code ReplaceModifiesModuleVisitor}, whose frame conditions never apply with TACO's names
 * (docs/KNOWN-LIMITATIONS.md, "{@code modifies} clauses add no frame conditions"), and through
 * {@code ModularMutator} with {@code modularReasoning}. Equal when the locations are equal.
 */
public final class JModifies {

	public AlloyExpression getLocation() {
		return location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
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
		JModifies other = (JModifies) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

	private JModifies() {
		this(null);
	}

	/**
	 * Returns a clause that modifies everything, as for JML {@code assignable \everything}.
	 *
	 * @return a new clause with a null location; equal to every other such clause
	 */
	public static JModifies buildModifiesEverything() {
		return new JModifies();
	}
	
	public JModifies(AlloyExpression location) {
		super();
		this.location = location;
	}

	public boolean isModifiesEverything() {
		return this.location == null;
	}

	private final AlloyExpression location;

	public final Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

}

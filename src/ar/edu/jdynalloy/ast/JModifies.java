package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

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

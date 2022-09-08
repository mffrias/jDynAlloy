package ar.edu.jdynalloy.ast;

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.AlloyVariable;

public final class JVariableDeclaration extends JStatement {

	private final JType type;

	private final AlloyVariable variable;

	public JVariableDeclaration(AlloyVariable v, JType type) {
		super();
		this.variable = v;
		this.type = type;
	}

	@Override
	public Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null
				&& arg0.getClass().equals(JVariableDeclaration.class)) {
			JVariableDeclaration that = (JVariableDeclaration) arg0;
			return this.getVariable().equals(that.getVariable())
					&& this.getType().equals(that.getType());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return this.getVariable().hashCode() + this.getType().hashCode();
	}

	@Override
	public String toString() {
		return this.getType() + " " + this.getVariable();
	}

	public JType getType() {
		return type;
	}

	public AlloyVariable getVariable() {
		return variable;
	}

}

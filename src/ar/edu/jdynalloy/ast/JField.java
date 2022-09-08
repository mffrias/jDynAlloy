package ar.edu.jdynalloy.ast;

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.AlloyVariable;

public final class JField {

	public JField(AlloyVariable fieldVariable, JType fieldType) {
		super();
		this.fieldVariable = fieldVariable;
		this.fieldType = fieldType;
	}

	private final JType fieldType;

	private final AlloyVariable fieldVariable;

	public JType getFieldType() {
		return fieldType;
	}

	public AlloyVariable getFieldVariable() {
		return fieldVariable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fieldVariable == null) ? 0 : fieldVariable.hashCode());
		result = prime * result
				+ ((fieldType == null) ? 0 : fieldType.hashCode());
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
		JField other = (JField) obj;
		if (fieldVariable == null) {
			if (other.fieldVariable != null)
				return false;
		} else if (!fieldVariable.equals(other.fieldVariable))
			return false;
		if (fieldType == null) {
			if (other.fieldType != null)
				return false;
		} else if (!fieldType.equals(other.fieldType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.fieldVariable + ":" + this.fieldType.toString();
	}

	public final Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

}

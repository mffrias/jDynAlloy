package ar.edu.jdynalloy.ast;

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.AlloyVariable;

/**
 * {@code field f: (C)->one(T) {}}: an instance field of a class, as a relation from the class to
 * the field's type: its variable, named {@code <class>_<field>} by TACO (docs/NAMING.md; the
 * {@code .djals} models choose their own names, such as {@code dateHashCode}), and its type.
 * {@code DynJMLAlloyModuleBuffer} types a field {@code C->one(T)}, a set field
 * {@code C->(T1+...)}, a sequence with {@code JTypeFactory.buildFieldSeq}, and the
 * {@code SK_jml_pred_java_primitive_*} fields with their own type; a static field becomes
 * {@code <module>_<field>}, of type {@code ClassFields->one(T)}, in the module
 * {@code ClassFields}.
 *
 * <p>Fields are not Alloy fields of the signature: the translation passes each one, as a
 * variable, to every program and every call (docs/ARCHITECTURE.md, "JDynAlloyXlatorVisitor:
 * modules, programs, statements"). Equal when the variable and the type are equal;
 * {@code SceneSlicerManager} removes fields from a module by {@code equals}, and TACO's symmetry
 * breaking collects them in a {@code HashSet}.
 */
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

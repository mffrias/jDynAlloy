package ar.edu.jdynalloy.ast;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ar.edu.jdynalloy.factory.JTypeFactory;
import ar.edu.jdynalloy.xlator.JDynAlloyTyping;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.AlloySig;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public class JSignature extends AlloySig {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((inSignatureId == null) ? 0 : inSignatureId.hashCode());
		result = prime * result + (isPrimitive ? 1231 : 1237);
		result = prime * result
				+ ((superInterfaces == null) ? 0 : superInterfaces.hashCode());
		result = prime * result + Arrays.hashCode(typeParameters);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		JSignature other = (JSignature) obj;
		if (inSignatureId == null) {
			if (other.inSignatureId != null)
				return false;
		} else if (!inSignatureId.equals(other.inSignatureId))
			return false;
		if (isPrimitive != other.isPrimitive)
			return false;
		if (superInterfaces == null) {
			if (other.superInterfaces != null)
				return false;
		} else if (!superInterfaces.equals(other.superInterfaces))
			return false;
		if (!Arrays.equals(typeParameters, other.typeParameters))
			return false;
		return true;
	}

	private final boolean isPrimitive;

	private final Set<String> superInterfaces;

	private final Set<AlloyFormula> facts;

	private final String inSignatureId;

	private final String[] typeParameters;

	private final List<String> alloy_predicates;

	private final List<String> alloy_functions;

	public JSignature(boolean isOne, boolean isAbstract, String signatureId,
			JDynAlloyTyping fields, boolean isPrimitive,
			String superSignatureId, String inSignatureId,
			Set<String> superInterfaces, Set<AlloyFormula> facts,
			List<String> alloy_predicates, List<String> alloy_functions,
			String... typeParameters) {

		super(isAbstract, isOne, signatureId, fields, superSignatureId);
		this.isPrimitive = isPrimitive;
		this.typeParameters = typeParameters;
		this.superInterfaces = superInterfaces;
		this.facts = facts;
		this.inSignatureId = inSignatureId;
		this.alloy_predicates = alloy_predicates;
		this.alloy_functions = alloy_functions;
	}

	public JType getType(JType... params) {
		if (this.isPrimitive) {
			return new JType(this.getSignatureId());
		} else {
			if (params.length > 0) {
				if (params.length != typeParameters.length)
					throw new IllegalArgumentException(
							"Incorrect number of type parameters");
				// List[String+null]+null
				// Map[String+null,Character+null]+null
				StringBuffer buffer = new StringBuffer();
				buffer.append(this.getSignatureId() + "<");
				for (JType type : params) {
					if (buffer.charAt(buffer.length() - 1) != '<')
						buffer.append(",");
					buffer.append(type.toString());
				}
				buffer.append(">");
				return JTypeFactory.buildReference(buffer.toString());
			} else
				return JTypeFactory.buildReference(this.getSignatureId());
		}
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(super.toString());
		buff.append("{");
		for (String inSingature : superInterfaces) {
			buff.append(this.getSignatureId() + " in " + inSingature + "\n");
		}
		if (this.facts.size() > 0) {
			buff.append("\n");
			for (AlloyFormula aFact : this.facts) {
				buff.append(aFact.toString());
				buff.append("\n");
			}
		}

		buff.append("}");
		return buff.toString();
	}

	public Set<String> superInterfaces() {
		return this.superInterfaces;
	}

	public Object accept(IJDynAlloyVisitor visitor) {
		return visitor.visit(this);
	}

	public String inSignatureId() {
		return inSignatureId;
	}

	public boolean isPrimitive() {
		return isPrimitive;
	}

	public Set<String> getSuperInterfaces() {
		return superInterfaces;
	}

	public String getInSignatureId() {
		return inSignatureId;
	}

	public String[] getTypeParameters() {
		return typeParameters;
	}

	public Set<AlloyFormula> getFacts() {
		return facts;
	}


	public List<String> getAlloyPredicates() {
		return alloy_predicates;
	}

	public List<String> getAlloyFunctions() {
		return alloy_functions;
	}

}

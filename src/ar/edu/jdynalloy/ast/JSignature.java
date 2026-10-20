package ar.edu.jdynalloy.ast;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ar.edu.jdynalloy.factory.JTypeFactory;
import ar.edu.jdynalloy.xlator.JDynAlloyTyping;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.AlloySig;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * The signature of a module: dynalloy4's {@code AlloySig} (abstract or not, {@code one} or not,
 * its id, its Alloy fields, the signature it extends) plus whether it is primitive, the set it is
 * {@code in} (for interfaces), the interfaces it implements, its facts, Alloy predicates and
 * functions as text, and its type parameters (docs/LANGUAGE.md, "Modules").
 * {@code JSignatureFactory} builds most of them: a class ({@code extends} its superclass), an
 * interface ({@code in} {@code java_lang_Object} or a given set), the class singleton
 * {@code <Sig>Class} and the literal {@code <Sig>Lit} ({@code one sig}), {@code ClassFields},
 * primitives and primitive values. {@code JDynAlloyXlatorVisitor} translates a signature by
 * printing it with {@link JDynAlloyPrinter}: the {@code sig} declaration, then a fact with
 * {@code <Sig> in <I>} for each interface and the facts (sorted), then the predicates and
 * functions.
 *
 * <p>The Alloy fields are empty but for the {@code JavaPrimitive*Value} signatures (their bits):
 * the fields of a class are the {@link JField}s of its module. The sets and lists are the
 * caller's, not copies, and {@link #getAlloyPredicates()} returns the list itself: the
 * {@code JavaPrimitiveIntegerValue} and {@code JavaPrimitiveCharValue} singletons add a predicate
 * to theirs for each new literal, so those lists grow for the life of the JVM.
 *
 * <p>{@code superInterfaces()} and {@code getSuperInterfaces()} return the same set, and
 * {@code inSignatureId()} and {@code getInSignatureId()} the same id. {@code toString()} is not
 * the syntax: it is {@code AlloySig}'s text followed by the interfaces, facts and predicates. For
 * equality see {@link #equals}.
 */
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

	/**
	 * Returns whether {@code obj} is this very signature. The code compares {@code AlloySig}'s
	 * parts and then its own, but {@code AlloySig.equals} accepts only objects whose class is
	 * exactly {@code AlloySig}, so the comparison fails for every other {@code JSignature}: in
	 * effect this is identity. {@code hashCode()} is by value, which is consistent with that. Two
	 * {@link JDynAlloyModule}s are therefore equal only if they share their signature object.
	 *
	 * @param obj the object to compare with
	 * @return true only if {@code obj} is this object
	 */
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

	/**
	 * Creates a signature; the sets and lists are kept, not copied.
	 *
	 * @param isOne true for a {@code one sig} (class singletons, literals, {@code ClassFields})
	 * @param isAbstract true for an {@code abstract sig}
	 * @param signatureId the id: the class name with underscores (docs/NAMING.md)
	 * @param fields the Alloy fields of the signature; empty but for the primitive values
	 * @param isPrimitive true for primitive types and values: {@link #getType} then has no
	 *        {@code null}
	 * @param superSignatureId the signature it extends, or null
	 * @param inSignatureId the set it is {@code in} (interfaces), or null
	 * @param superInterfaces the interfaces it implements; must not be null for
	 *        {@code JDynAlloyPrinter} and {@code toString()}, which iterate over it
	 * @param facts its facts; must not be null for {@code JDynAlloyPrinter}, {@code toString()}
	 *        and the relevancy analysis, which iterate over it. Read from the code, not run:
	 *        {@code DynJMLAlloyModuleBuffer} passes null for an interface without a fact, which
	 *        is every interface TACO translates
	 * @param alloy_predicates Alloy predicates as text, printed after the signature
	 * @param alloy_functions Alloy functions as text, printed after the predicates
	 * @param typeParameters the names of its type parameters, for {@link #getType}
	 */
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

	/**
	 * Returns the type of the values of this signature: for a primitive signature, the signature
	 * id; otherwise a reference type, the signature (followed by the type arguments as
	 * {@code <T1,T2>} if there are any) or {@code null}.
	 *
	 * @param params the type arguments: none, or one for each type parameter
	 * @return the type
	 * @throws IllegalArgumentException if {@code params} is not empty and its length is not the
	 *        number of type parameters ("Incorrect number of type parameters")
	 */
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
		if (this.alloy_predicates.size() > 0){
			buff.append("\n");
			for (String aFact : this.alloy_predicates) {
				buff.append(aFact);
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

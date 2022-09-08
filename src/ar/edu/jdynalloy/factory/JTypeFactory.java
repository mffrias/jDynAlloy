/**
 * 
 */
package ar.edu.jdynalloy.factory;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ar.edu.jdynalloy.xlator.JType;
import ar.edu.jdynalloy.xlator.JType.SpecialType;

/**
 * @author Gabriel Gasser Noblia
 * 
 */
public abstract class JTypeFactory {
	private static final String NULL_VALUE_SIGNATURE_ID = JSignatureFactory.NULL
			.getSignatureId();
	private static final String INT_SIGNATURE_ID = JSignatureFactory.INT
			.getSignatureId();

	/**
	 * 
	 * @param signatureId
	 * @return
	 */
	public static JType buildReferenceSeq(String signatureId) {
		HashSet<String> to = new HashSet<String>();
		to.add(signatureId);
		to.add(NULL_VALUE_SIGNATURE_ID);
		Set<String> from = Collections.<String> singleton(INT_SIGNATURE_ID);
		return new JType(from, to);
	}

	public static JType buildReferenceSet(String signatureId) {
		HashSet<String> to = new HashSet<String>();
		to.add(signatureId);
		to.add(NULL_VALUE_SIGNATURE_ID);
		JType result = new JType(true, to);
		return result;
	}

	/**
	 * 
	 * @param signatureId
	 * @return
	 */
	public static JType buildReference(String signatureId) {
		return buildReferenceVariable(Collections
				.<String> singleton(signatureId));
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static JType buildReferenceVariable(Set<String> s) {
		HashSet<String> sigs = new HashSet<String>(s);
		sigs.add(NULL_VALUE_SIGNATURE_ID);
		return new JType(sigs);
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static JType buildReferenceVariable(String... s) {
		HashSet<String> sigs = new HashSet<String>();
		for (String string : s) {
			sigs.add(string);
		}
		sigs.add(NULL_VALUE_SIGNATURE_ID);
		return new JType(sigs);
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static JType buildReferenceField(String from, String to) {
		return buildReferenceField(Collections.<String> singleton(from),
				Collections.<String> singleton(to));
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static JType buildReferenceField(Set<String> from, Set<String> to) {
		HashSet<String> toSigs = new HashSet<String>(to);
		toSigs.add(NULL_VALUE_SIGNATURE_ID);
		return new JType(from, toSigs);
	}

	/**
	 * 
	 * @param fromSignatureId
	 * @param sequenceType
	 * @return
	 */
	public static JType buildFieldSeq(String fromSignatureId, JType sequenceType) {
		List<String> fromSignatureIdList = Collections
				.singletonList(fromSignatureId);
		List<String> toSignatureIdList = new LinkedList<String>(
				sequenceType.from());

		JType fieldSeq = new JType(fromSignatureIdList, toSignatureIdList,
				true, false, false, false, (SpecialType) null);

		return fieldSeq;
	}
}

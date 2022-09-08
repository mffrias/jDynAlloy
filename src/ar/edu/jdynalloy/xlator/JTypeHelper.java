/**
 * 
 */
package ar.edu.jdynalloy.xlator;

import java.util.HashSet;
import java.util.Set;

import ar.edu.jdynalloy.factory.JSignatureFactory;

/**
 * @author Gabriel Gasser Noblia
 *
 */
public abstract class JTypeHelper {
	
	private static final String NULL_VALUE_SIGNATURE_ID = JSignatureFactory.NULL.getSignatureId();

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static boolean fromIncludesNull(JType type) {
		return type.from().contains(NULL_VALUE_SIGNATURE_ID);
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static boolean toIncludesNull(JType type) {
		return type.to().contains(NULL_VALUE_SIGNATURE_ID);
	}
	
	/**
	 * 
	 * @param alloyType
	 * @return
	 */
	public static String getBaseType(JType alloyType) {
		Set<String> fromWithOutAssertionFailure = new HashSet<String>();
		fromWithOutAssertionFailure.addAll(alloyType.from());
		fromWithOutAssertionFailure.remove("AssertionFailure");

		if (fromWithOutAssertionFailure.size() != 2)
			throw new IllegalStateException();
		if (!fromWithOutAssertionFailure.contains(NULL_VALUE_SIGNATURE_ID))
			throw new IllegalStateException();
		for (String signature : fromWithOutAssertionFailure) {
			if (!signature.equals(NULL_VALUE_SIGNATURE_ID))
				return signature;
		}
		throw new IllegalStateException();
	}
	




}

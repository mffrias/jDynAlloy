package ar.edu.jdynalloy.ast;

import static ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction.buildExprFunction;

import java.util.ArrayList;
import java.util.List;

import ar.edu.jdynalloy.factory.DynalloyFactory;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.factory.JSignatureFactory;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.edu.jdynalloy.xlator.JType;


/**
 * Builds the size of a {@code java.util.Set} as
 * {@code javaUtilSetSize[set, java_util_Set_elems]}. Nothing in jDynAlloy or TACO calls it, and
 * no function {@code javaUtilSetSize} is defined, in the prelude or in TACO's built-in modules;
 * TACO builds the size of a set with
 * {@code JExpressionFactory.javaUtilSetSizeReturnsAlloyInt} and
 * {@code javaUtilSetSizeReturnsJavaPrimitiveIntegerValue}, whose functions the prelude defines.
 * No configuration option selects this class. Unclear: whether it is still needed. No state.
 */
public final class JavaUtilSetFactory {

	/**
	 * Returns {@code javaUtilSetSize[set, java_util_Set_elems]}. No callers (see the class
	 * comment).
	 *
	 * @param javaUtilSet_type the set's type; only {@code java_util_Set} is supported
	 * @param set the set
	 * @return the expression
	 * @throws RuntimeException for another type ("Unsupported set type size")
	 */
	public static AlloyExpression javaUtilSetSize(JType javaUtilSet_type,
			AlloyExpression set) {

		if (javaUtilSet_type.equals(JSignatureFactory.JAVA_UTIL_SET_TYPE)) {
			return buildExprFunction("javaUtilSetSize", set, DynalloyFactory.JAVA_UTIL_SET_ELEMS_FIELD_EXPRESSION);
		} else
			throw new RuntimeException("Unsupported set type size");
	}


	/**
	 * Returns {@code javaUtilSetSize[set, java_util_Set_elems']}, the size after the program. No
	 * callers (see the class comment).
	 *
	 * @param javaUtilSet_type the set's type; only {@code java_util_Set} is supported
	 * @param set the set
	 * @return the expression
	 * @throws RuntimeException for another type ("Unsupported set type size")
	 */
	public static AlloyExpression primmedJavaUtilSetSize(JType javaUtilSet_type,
			AlloyExpression set) {

		if (javaUtilSet_type.equals(JSignatureFactory.JAVA_UTIL_SET_TYPE)) {
			return buildExprFunction("javaUtilSetSize", set, DynalloyFactory.PRIMED_JAVA_UTIL_SET_ELEMS_FIELD_EXPRESSION);
		} else
			throw new RuntimeException("Unsupported set type size");
	}


}

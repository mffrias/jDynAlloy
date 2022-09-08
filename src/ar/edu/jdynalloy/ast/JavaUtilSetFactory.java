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


public final class JavaUtilSetFactory {

	public static AlloyExpression javaUtilSetSize(JType javaUtilSet_type,
			AlloyExpression set) {

		if (javaUtilSet_type.equals(JSignatureFactory.JAVA_UTIL_SET_TYPE)) {
			return buildExprFunction("javaUtilSetSize", set, DynalloyFactory.JAVA_UTIL_SET_ELEMS_FIELD_EXPRESSION);
		} else
			throw new RuntimeException("Unsupported set type size");
	}


	public static AlloyExpression primmedJavaUtilSetSize(JType javaUtilSet_type,
			AlloyExpression set) {

		if (javaUtilSet_type.equals(JSignatureFactory.JAVA_UTIL_SET_TYPE)) {
			return buildExprFunction("javaUtilSetSize", set, DynalloyFactory.PRIMED_JAVA_UTIL_SET_ELEMS_FIELD_EXPRESSION);
		} else
			throw new RuntimeException("Unsupported set type size");
	}


}

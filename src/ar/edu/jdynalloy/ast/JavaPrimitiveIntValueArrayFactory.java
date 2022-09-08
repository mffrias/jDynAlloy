package ar.edu.jdynalloy.ast;

import static ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction.buildExprFunction;

import java.util.ArrayList;
import java.util.List;

import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.factory.JSignatureFactory;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.edu.jdynalloy.factory.DynalloyFactory;


public class JavaPrimitiveIntValueArrayFactory {

	public static AlloyExpression array_length(JType array_type,
			AlloyExpression array) {
		if (array_type.equals(JSignatureFactory.INT_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.INT_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else if (array_type.equals(JSignatureFactory.CHAR_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.CHAR_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else if (array_type.equals(JSignatureFactory.LONG_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.LONG_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else if (array_type.equals(JSignatureFactory.OBJECT_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.OBJECT_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else
			throw new RuntimeException("Unsupported array type length");
	}


	public static AlloyExpression primed_array_length(JType array_type,
			AlloyExpression array) {
		if (array_type.equals(JSignatureFactory.INT_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.PRIMED_INT_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else if (array_type.equals(JSignatureFactory.CHAR_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.PRIMED_CHAR_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else if (array_type.equals(JSignatureFactory.LONG_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.PRIMED_LONG_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else if (array_type.equals(JSignatureFactory.OBJECT_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.PRIMED_OBJECT_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else
			throw new RuntimeException("Unsupported array type length");
	}


	public static AlloyExpression array_access(JType array_type,
			AlloyExpression array, AlloyExpression index) {
		if (array_type.equals(JSignatureFactory.INT_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.INT_ARRAY_CONTENTS_FIELD_EXPRESSION, index);
		} else if (array_type.equals(JSignatureFactory.CHAR_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.CHAR_ARRAY_CONTENTS_FIELD_EXPRESSION, index);
		} else if (array_type.equals(JSignatureFactory.LONG_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.LONG_ARRAY_CONTENTS_FIELD_EXPRESSION, index);
		} else if (array_type.equals(JSignatureFactory.OBJECT_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.OBJECT_ARRAY_CONTENTS_FIELD_EXPRESSION, index);
		} else
			throw new RuntimeException("Unsupported read array type :"
					+ array_type);
	}


	public static AlloyExpression primed_array_access(JType array_type,
			AlloyExpression array, AlloyExpression index) {
		if (array_type.equals(JSignatureFactory.INT_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.PRIMED_INT_ARRAY_CONTENTS_FIELD_EXPRESSION, index);
		} else if (array_type.equals(JSignatureFactory.CHAR_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.PRIMED_CHAR_ARRAY_CONTENTS_FIELD_EXPRESSION, index);
		} else if (array_type.equals(JSignatureFactory.LONG_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.PRIMED_LONG_ARRAY_CONTENTS_FIELD_EXPRESSION, index);
		} else if (array_type.equals(JSignatureFactory.OBJECT_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.PRIMED_OBJECT_ARRAY_CONTENTS_FIELD_EXPRESSION, index);
		} else
			throw new RuntimeException("Unsupported read array type :"
					+ array_type);
	}

	public static JStatement array_write_stmt(AlloyExpression array_access,
			AlloyExpression new_value) {
		// array_index.(array_expr.array_contents) := expr

		AlloyExpression array_index = getArrayIndex(array_access);
		AlloyExpression array_expr = getArrayExpression(array_access);
		ExprVariable array_contents = getArrayContents(array_access);

		if (array_contents
				.equals(JExpressionFactory.INT_ARRAY_CONTENTS_EXPRESSION)) {

			List<AlloyExpression> argumentsList = new ArrayList<AlloyExpression>();
			argumentsList.add(array_expr);
			argumentsList.add(JExpressionFactory.THROW_EXPRESSION);
			argumentsList.add(new_value);
			argumentsList.add(array_index);

			String programId = "int_array_write";
			JProgramCall programCall = new JProgramCall(false, programId,
					argumentsList);

			return programCall;
		} else if (array_contents
				.equals(JExpressionFactory.CHAR_ARRAY_CONTENTS_EXPRESSION)) {

			List<AlloyExpression> argumentsList = new ArrayList<AlloyExpression>();
			argumentsList.add(array_expr);
			argumentsList.add(JExpressionFactory.THROW_EXPRESSION);
			argumentsList.add(new_value);
			argumentsList.add(array_index);

			String programId = "char_array_write";
			JProgramCall programCall = new JProgramCall(false, programId,
					argumentsList);

			return programCall;
		} else if (array_contents
				.equals(JExpressionFactory.LONG_ARRAY_CONTENTS_EXPRESSION)) {

			List<AlloyExpression> argumentsList = new ArrayList<AlloyExpression>();
			argumentsList.add(array_expr);
			argumentsList.add(JExpressionFactory.THROW_EXPRESSION);
			argumentsList.add(new_value);
			argumentsList.add(array_index);

			String programId = "long_array_write";
			JProgramCall programCall = new JProgramCall(false, programId,
					argumentsList);

			return programCall;
		} else if (array_contents
				.equals(JExpressionFactory.OBJECT_ARRAY_CONTENTS_EXPRESSION)) {

			List<AlloyExpression> argumentsList = new ArrayList<AlloyExpression>();
			argumentsList.add(array_expr);
			argumentsList.add(JExpressionFactory.THROW_EXPRESSION);
			argumentsList.add(new_value);
			argumentsList.add(array_index);

			String programId = "object_array_write";
			JProgramCall programCall = new JProgramCall(false, programId,
					argumentsList);

			return programCall;
		} else
			throw new RuntimeException("not supported array write: "
					+ array_contents);

	}

	public static JStatement array_read_stmt(AlloyExpression lvalue,
			AlloyExpression array_access) {

		AlloyExpression array_index = getArrayIndex(array_access);
		AlloyExpression array_expr = getArrayExpression(array_access);
		ExprVariable array_contents = getArrayContents(array_access);

		if (array_contents
				.equals(JExpressionFactory.INT_ARRAY_CONTENTS_EXPRESSION)) {

			List<AlloyExpression> argumentsList = new ArrayList<AlloyExpression>();
			argumentsList.add(array_expr);
			argumentsList.add(JExpressionFactory.THROW_EXPRESSION);
			argumentsList.add(lvalue);
			argumentsList.add(array_index);

			String programId = "int_array_read";
			JProgramCall programCall = new JProgramCall(false, programId,
					argumentsList);

			return programCall;

		} else if (array_contents
				.equals(JExpressionFactory.CHAR_ARRAY_CONTENTS_EXPRESSION)) {

			List<AlloyExpression> argumentsList = new ArrayList<AlloyExpression>();
			argumentsList.add(array_expr);
			argumentsList.add(JExpressionFactory.THROW_EXPRESSION);
			argumentsList.add(lvalue);
			argumentsList.add(array_index);

			String programId = "char_array_read";
			JProgramCall programCall = new JProgramCall(false, programId,
					argumentsList);

			return programCall;
		} else if (array_contents
				.equals(JExpressionFactory.LONG_ARRAY_CONTENTS_EXPRESSION)) {

			List<AlloyExpression> argumentsList = new ArrayList<AlloyExpression>();
			argumentsList.add(array_expr);
			argumentsList.add(JExpressionFactory.THROW_EXPRESSION);
			argumentsList.add(lvalue);
			argumentsList.add(array_index);

			String programId = "long_array_read";
			JProgramCall programCall = new JProgramCall(false, programId,
					argumentsList);

			return programCall;

		} else if (array_contents
				.equals(JExpressionFactory.OBJECT_ARRAY_CONTENTS_EXPRESSION)) {

			List<AlloyExpression> argumentsList = new ArrayList<AlloyExpression>();
			argumentsList.add(array_expr);
			argumentsList.add(JExpressionFactory.THROW_EXPRESSION);
			argumentsList.add(lvalue);
			argumentsList.add(array_index);

			String programId = "object_array_read";
			JProgramCall programCall = new JProgramCall(false, programId,
					argumentsList);

			return programCall;
		} else

			throw new RuntimeException("not supported array write: "
					+ array_contents);

	}


	private static AlloyExpression getArrayExpression(
			AlloyExpression array_access) {
		ExprFunction access_function = (ExprFunction) array_access;
		AlloyExpression array_object = access_function.getParameters().get(0);
		return array_object;
	}


	private static ExprVariable getArrayContents(AlloyExpression array_access) {
		ExprFunction access_function = (ExprFunction) array_access;
		ExprVariable array_contents = (ExprVariable) access_function.getParameters().get(1);
		return array_contents;
	}


	private static AlloyExpression getArrayIndex(AlloyExpression array_access) {
		ExprFunction access_function = (ExprFunction) array_access;
		AlloyExpression array_index = access_function.getParameters().get(2); 
		return array_index;
	}

}

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


/**
 * Builds the JDynAlloy of Java array expressions and statements with Java arithmetic, where
 * {@code int} values and indices are {@code JavaPrimitiveIntegerValue} objects. TACO's
 * translators ({@code ExpressionVisitor}, {@code JmlExpressionVisitor},
 * {@code BlockStatementsVisitor}) use it when {@code useJavaArithmetic} is true, and
 * {@link AlloyIntArrayFactory} when it is false; recognizing and taking apart an array access
 * is left to {@code AlloyIntArrayFactory}'s helpers in both cases.
 *
 * <p>It builds the same expressions and calls as {@code AlloyIntArrayFactory}
 * ({@code arrayAccess[array, contents, index]}, {@code arrayLength[array, length]},
 * {@code call <kind>_array_read} and {@code _write}), for more array types: {@code int},
 * {@code char}, {@code long} and object arrays. What changes with the arithmetic is what defines
 * them: the prelude ({@code JPreludeFactory}) defines the functions with
 * {@code JavaPrimitiveIntegerValue} indices when that module is in the run, and TACO loads the
 * built-in modules {@code java_lang_IntArray.djals}, {@code java_lang_CharArray.djals},
 * {@code java_lang_LongArray.djals} and {@code java_lang_ObjectArray.djals}, whatever
 * {@code builtInModules} says. The array types each method supports are listed with it; the
 * others throw a {@code RuntimeException}. No state.
 */
public class JavaPrimitiveIntValueArrayFactory {

	/**
	 * Returns the length of an array: {@code arrayLength[array, <length relation>]}.
	 *
	 * @param array_type the array's type: {@code java_lang_IntArray+null},
	 *        {@code java_lang_CharArray+null}, {@code java_lang_LongArray+null} or
	 *        {@code java_lang_ObjectArray+null}
	 * @param array the array
	 * @return the expression
	 * @throws RuntimeException for another type ("Unsupported array type length")
	 */
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


	/**
	 * Returns the length of an array after the program, for postconditions: as
	 * {@link #array_length}, with the primed length relation.
	 *
	 * @param array_type the array's type, as for {@link #array_length}
	 * @param array the array
	 * @return the expression
	 * @throws RuntimeException for another type ("Unsupported array type length")
	 */
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


	/**
	 * Returns an element of an array: {@code arrayAccess[array, <contents relation>, index]}.
	 * Unclear: a {@code java_lang_IntArray2d+null} array gets {@code java_lang_IntArray_contents},
	 * the relation of one-dimensional int arrays, while TACO's {@code java_lang_IntArray2d.djals}
	 * (which {@code PrecompiledModules} does not load) declares
	 * {@code java_lang_IntArray2d_contents}; whether two-dimensional arrays are meant to work is an
	 * open question.
	 *
	 * @param array_type the array's type: {@code java_lang_IntArray+null},
	 *        {@code java_lang_CharArray+null}, {@code java_lang_LongArray+null},
	 *        {@code java_lang_ObjectArray+null} or {@code java_lang_IntArray2d+null}
	 * @param array the array
	 * @param index the index
	 * @return the expression
	 * @throws RuntimeException for another type ("Unsupported read array type :...")
	 */
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
		} else if (array_type.equals(JSignatureFactory.TWODIM_INT_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.INT_ARRAY_CONTENTS_FIELD_EXPRESSION, index);
		} else
			throw new RuntimeException("Unsupported read array type :"
					+ array_type);
	}


	/**
	 * Returns an element of an array after the program, for postconditions: as
	 * {@link #array_access}, with the primed contents relation, without the two-dimensional case.
	 *
	 * @param array_type the array's type: {@code java_lang_IntArray+null},
	 *        {@code java_lang_CharArray+null}, {@code java_lang_LongArray+null} or
	 *        {@code java_lang_ObjectArray+null}
	 * @param array the array
	 * @param index the index
	 * @return the expression
	 * @throws RuntimeException for another type ("Unsupported read array type :...")
	 */
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

	/**
	 * Returns the statement for {@code a[i] = v}: {@code call <kind>_array_write[a, throw, v, i]},
	 * where the kind ({@code int}, {@code char}, {@code long}, {@code object}) comes from the
	 * contents relation of the access. The program sets {@code throw} when the index is out of
	 * bounds (TACO's array modules).
	 *
	 * @param array_access the left side, an {@link #array_access} call
	 * @param new_value the value written
	 * @return the call
	 * @throws ClassCastException if {@code array_access} is not a function call whose second
	 *         argument is a variable
	 * @throws RuntimeException for another contents relation ("not supported array write: ...")
	 */
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

	/**
	 * Returns the statement for {@code x = a[i]}: {@code call <kind>_array_read[a, throw, x, i]},
	 * where the kind ({@code int}, {@code char}, {@code long}, {@code object}) comes from the
	 * contents relation of the access.
	 *
	 * @param lvalue the location assigned
	 * @param array_access the right side, an {@link #array_access} call
	 * @return the call
	 * @throws ClassCastException if {@code array_access} is not a function call whose second
	 *         argument is a variable
	 * @throws RuntimeException for another contents relation ("not supported array write: ...",
	 *         sic)
	 */
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

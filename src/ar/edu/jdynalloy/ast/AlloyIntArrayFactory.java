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
 * Builds the JDynAlloy of Java array expressions and statements when integers are Alloy's
 * {@code Int}. TACO's translators ({@code ExpressionVisitor}, {@code JmlExpressionVisitor},
 * {@code BlockStatementsVisitor}) use it when {@code useJavaArithmetic} is false, and
 * {@link JavaPrimitiveIntValueArrayFactory} when it is true. jDynAlloy's
 * {@code JDynAlloyXlatorVisitor} and {@code ModifiesTableBuilder} use its helpers
 * ({@link #isArrayAccess}, {@link #getArrayContents}, {@link #getArrayIndex}) to recognize and
 * take apart an array access, with either arithmetic.
 *
 * <p>An array access is the function call {@code arrayAccess[array, contents, index]}, where
 * {@code contents} is the relation of the array's type ({@code java_lang_IntArray_contents},
 * ...), and a length is {@code arrayLength[array, length]}; the prelude
 * ({@code JPreludeFactory}) defines these functions for each array module of the run, with
 * {@code Int} indices when {@code JavaPrimitiveIntegerValue} is not among the modules. A
 * statement that reads or writes an element is a call to the program
 * {@code <kind>_array_read} or {@code <kind>_array_write} of TACO's built-in array modules;
 * without Java arithmetic, TACO loads {@code java_lang_AlloyIntArray.djals} and
 * {@code java_lang_AlloyIntObjectArray.djals} (the modules {@code java_lang_IntArray} and
 * {@code java_lang_ObjectArray}), whatever {@code builtInModules} says. The array types each
 * method supports are listed with it; the others throw a {@code RuntimeException}. No state.
 */
public final class AlloyIntArrayFactory {

	/**
	 * Returns the length of an array: {@code arrayLength[array, <length relation>]}.
	 *
	 * @param array_type the array's type: {@code java_lang_IntArray+null} or
	 *        {@code java_lang_ObjectArray+null}
	 * @param array the array
	 * @return the expression
	 * @throws RuntimeException for another type ("Unsupported array type length")
	 */
	public static AlloyExpression arrayLength(JType array_type,
			AlloyExpression array) {

		if (array_type.equals(JSignatureFactory.INT_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.INT_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else if (array_type.equals(JSignatureFactory.OBJECT_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.OBJECT_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else
			throw new RuntimeException("Unsupported array type length");
	}


	/**
	 * Returns the length of an array after the program, for postconditions: as
	 * {@link #arrayLength}, with the primed length relation.
	 *
	 * @param array_type the array's type: {@code java_lang_IntArray+null} or
	 *        {@code java_lang_ObjectArray+null}
	 * @param array the array
	 * @return the expression
	 * @throws RuntimeException for another type ("Unsupported array type length")
	 */
	public static AlloyExpression primedArrayLength(JType array_type,
			AlloyExpression array) {

		if (array_type.equals(JSignatureFactory.INT_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.PRIMED_INT_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else if (array_type.equals(JSignatureFactory.OBJECT_ARRAY_TYPE)) {
			return buildExprFunction("arrayLength", array, DynalloyFactory.PRIMED_OBJECT_ARRAY_LENGTH_FIELD_EXPRESSION);
		} else
			throw new RuntimeException("Unsupported array type length");
	}

	
	/**
	 * Returns an element of an array: {@code arrayAccess[array, <contents relation>, index]}.
	 *
	 * @param type the array's type: {@code java_lang_IntArray+null},
	 *        {@code java_lang_LongArray+null} or {@code java_lang_ObjectArray+null}
	 * @param array the array
	 * @param index the index
	 * @return the expression
	 * @throws RuntimeException for another type ("Unsupported content array type in array with
	 *         Alloy int index. ...")
	 */
	public static AlloyExpression arrayAccess(JType type, AlloyExpression array,
			AlloyExpression index) {
		if (type.equals(JSignatureFactory.INT_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.INT_ARRAY_CONTENTS_FIELD_EXPRESSION, index); //mfrias
		} else if (type.equals(JSignatureFactory.LONG_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.LONG_ARRAY_CONTENTS_FIELD_EXPRESSION, index); //mfrias
		} else if (type.equals(JSignatureFactory.OBJECT_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.OBJECT_ARRAY_CONTENTS_FIELD_EXPRESSION, index); //mfrias
		} else {
			throw new RuntimeException("Unsupported content array type in array with Alloy int index. Unsupported type: "
					+ type);
		}
	}

	/**
	 * Returns an element of an array after the program, for postconditions: as
	 * {@link #arrayAccess}, with the primed contents relation, for fewer types.
	 *
	 * @param type the array's type: {@code java_lang_IntArray+null} or
	 *        {@code java_lang_ObjectArray+null}
	 * @param array the array
	 * @param index the index
	 * @return the expression
	 * @throws RuntimeException for another type ("Unsupported content array type in primed array
	 *         with Alloy int index. ...")
	 */
	public static AlloyExpression primedArrayAccess(JType type, AlloyExpression array,
			AlloyExpression index) {
		if (type.equals(JSignatureFactory.INT_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.PRIMED_INT_ARRAY_CONTENTS_FIELD_EXPRESSION, index); //mfrias
		} else if (type.equals(JSignatureFactory.OBJECT_ARRAY_TYPE)) {
			return buildExprFunction("arrayAccess", array, DynalloyFactory.PRIMED_OBJECT_ARRAY_CONTENTS_FIELD_EXPRESSION, index); //mfrias
		} else 
			throw new RuntimeException("Unsupported content array type in primed array with Alloy int index. Unsupported type: "
					+ type);
	}

	
	
	
	/**
	 * Returns the statement for {@code a[i] = v}: {@code call <kind>_array_write[a, throw, v, i]},
	 * where the kind ({@code int}, {@code char}, {@code long}, {@code object}) comes from the
	 * contents relation of the access. The program sets {@code throw} when the index is out of
	 * bounds (TACO's array modules).
	 *
	 * @param array_access the left side, an {@link #arrayAccess} call
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
	 * where the kind ({@code int} or {@code object}) comes from the contents relation of the
	 * access.
	 *
	 * @param lvalue the location assigned
	 * @param array_access the right side, an {@link #arrayAccess} call
	 * @return the call
	 * @throws ClassCastException if {@code array_access} is not a function call whose second
	 *         argument is a variable
	 * @throws RuntimeException for another contents relation, a {@code long} or {@code char} one
	 *         included ("not supported array write: ...", sic)
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


	/**
	 * Returns the contents relation of an array access, its second argument.
	 *
	 * @param array_access an {@code arrayAccess} call ({@link #isArrayAccess})
	 * @return the relation, such as {@code java_lang_IntArray_contents}
	 * @throws ClassCastException if it is not a function call whose second argument is a variable
	 */
	public static ExprVariable getArrayContents(AlloyExpression array_access) {
		ExprFunction access_function = (ExprFunction) array_access;
		ExprVariable array_contents = (ExprVariable) access_function.getParameters().get(1);
		return array_contents;
	}

//	public static AlloyExpression getArray(AlloyExpression e) {
//	ExprFunction arrayAccess = toExprFunction(e);
//	return arrayAccess.getParameters().get(1);
//  }
 
	
	/**
	 * Returns the set of elements of an int array:
	 * {@code arrayElements[java_lang_IntArray_contents, array]} (the contents first). TACO's
	 * {@code ExpressionVisitor} uses it, without Java arithmetic, to assume that a new array of a
	 * numeric type holds only 0; it passes the int relation for every numeric type.
	 *
	 * @param array the array
	 * @return the expression
	 */
	public static AlloyExpression arrayElements(AlloyExpression array) {
		return buildExprFunction("arrayElements",
				DynalloyFactory.INT_ARRAY_CONTENTS_FIELD_EXPRESSION, array);
	}

//	public static AlloyExpression alloyIntArrayElements(AlloyExpression array) {
//		return buildExprFunction("arrayElements",
//				DynalloyFactory.ARRAY_EXPRESSION, array);
//	}


	/**
	 * Tells whether an expression is an array access: an {@code ExprFunction} (not a subclass)
	 * named {@code arrayAccess}, as both array factories build it.
	 *
	 * @param e the expression
	 * @return true for an array access
	 */
	public static boolean isArrayAccess(AlloyExpression e) {
		return (e.getClass().equals(ExprFunction.class) && (((ExprFunction) e)
				.getFunctionId().equals("arrayAccess")));
	}


	

	/**
	 * Returns the index of an array access, its third argument.
	 *
	 * @param array_access an {@code arrayAccess} call ({@link #isArrayAccess})
	 * @return the index
	 * @throws ClassCastException if it is not a function call
	 */
	public static AlloyExpression getArrayIndex(AlloyExpression array_access) {
		ExprFunction access_function = (ExprFunction) array_access;
		AlloyExpression array_index = access_function.getParameters().get(2); 
		return array_index;
	}

//	private static ExprFunction toExprFunction(AlloyExpression e) {
//		if (!isArrayAccess(e))
//			throw new IllegalArgumentException(e.toString()
//					+ " is not an array access");
//		ExprFunction arrayAccess = (ExprFunction) e;
//		return arrayAccess;
//	}

}

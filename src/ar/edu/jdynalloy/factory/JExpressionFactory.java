package ar.edu.jdynalloy.factory;

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import static ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction.buildExprFunction;
import static ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant.buildExprConstant;
import static ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable.buildExprVariable;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;


/**
 * Built-In JAlloy Expressions
 * 
 * @author jgaleotti
 * 
 */
public abstract class JExpressionFactory {

	private static final ExprVariable JML_OBJECT_SET_CONTAINS = ExprVariable
			.buildExprVariable("JMLObjectSet_contains");

	public static final String FUN_REACH_JMLOBJECT_SET = "fun_reach_JMLObjectSet";

	public static final String MUL = "mul";

	public static final String DIV = "div";

	public static final String NEGATE = "negate";

	public static final String COMPLEMENT = "complement";

	public static final String REM = "rem";

	public static final String ADD = "add";

	public static final String SUB = "sub";

	public static final String EQ = "fun_univ_equals";

	public static final String SHL = "shl";

	public static final String USHR = "ushr";

	public static final String SSHR = "sshr";

	public static final String FUN_REACH = "fun_reach";

	public static final String FUN_JAVA_PRIMITIVE_INTEGER_VALUE_ADD = "fun_java_primitive_integer_value_add";

	public static final String FUN_JAVA_PRIMITIVE_INTEGER_VALUE_SUB = "fun_java_primitive_integer_value_sub";
	
//	public static final String FUN_JAVA_PRIMITIVE_INTEGER_VALUE_EQ = "fun_java_primitive_integer_value_eq";
	
	private static final String FUN_JAVA_PRIMITIVE_INTEGER_VALUE_NEGATE = "fun_java_primitive_integer_value_negate";

	private static final String FUN_JAVA_PRIMITIVE_INTEGER_VALUE_MUL = "fun_java_primitive_integer_value_mul";

	private static final String FUN_JAVA_PRIMITIVE_INTEGER_VALUE_DIV = "fun_java_primitive_integer_value_div";

	private static final String FUN_JAVA_PRIMITIVE_INTEGER_VALUE_REM = "fun_java_primitive_integer_value_rem";

	private static final String FUN_JAVA_PRIMITIVE_INTEGER_VALUE_SIZE_OF = "fun_java_primitive_integer_value_size_of";

	private static final String FUN_JAVA_PRIMITIVE_INTEGER_VALUE_SSHR = "fun_java_primitive_integer_value_sshr";

	private static final String FUN_JAVA_PRIMITIVE_LONG_VALUE_ADD = "fun_java_primitive_long_value_add";

	private static final String FUN_JAVA_PRIMITIVE_LONG_VALUE_SUB = "fun_java_primitive_long_value_sub";
	
	private static final String FUN_JAVA_PRIMITIVE_LONG_VALUE_NEGATE = "fun_java_primitive_long_value_negate";
	
	private static final String FUN_JAVA_PRIMITIVE_LONG_VALUE_EQ = "fun_java_primitive_long_value_eq";
	
	private static final String FUN_JAVA_PRIMITIVE_CHARCHARtoINT_VALUE_ADD = "fun_java_primitive_char_value_addCharCharToJavaPrimitiveIntegerValue";

	private static final String FUN_JAVA_PRIMITIVE_CHARCHARtoINT_VALUE_SUB = "fun_java_primitive_char_value_subCharCharToJavaPrimitiveIntegerValue";

	private static final String FUN_JAVA_PRIMITIVE_CHARINTtoINT_VALUE_ADD = "fun_java_primitive_char_value_addCharIntToJavaPrimitiveIntegerValue";

	private static final String FUN_JAVA_PRIMITIVE_CHARINTtoINT_VALUE_SUB = "fun_java_primitive_char_value_subCharIntToJavaPrimitiveIntegerValue";

	private static final String FUN_JAVA_PRIMITIVE_INTCHARtoINT_VALUE_ADD = "fun_java_primitive_char_value_addIntCharToJavaPrimitiveIntegerValue";

	private static final String FUN_JAVA_PRIMITIVE_INTCHARtoINT_VALUE_SUB = "fun_java_primitive_char_value_subIntCharToJavaPrimitiveIntegerValue";

	private static final String FUN_JAVA_PRIMITIVE_LONGCHARtoLONG_VALUE_ADD = "fun_java_primitive_char_value_addLongCharToJavaPrimitiveLongValue";
	
	private static final String FUN_JAVA_PRIMITIVE_LONGCHARtoLONG_VALUE_SUB = "fun_java_primitive_char_value_subLongCharToJavaPrimitiveLongValue";


	private static final String NOT = "Not";

	public static final AlloyVariable THIS_VARIABLE = new AlloyVariable("thiz");

	public static final AlloyVariable PRIMED_THIS_VARIABLE = new AlloyVariable(
			"thiz", true);

	public static final AlloyVariable USED_OBJECTS_VARIABLE = new AlloyVariable("usedObjects");

	public static final AlloyVariable RETURN_VARIABLE = new AlloyVariable(
			"return");

	public static final AlloyVariable PRIMED_RETURN_VARIABLE = new AlloyVariable(
			"return", true);

	public static final AlloyVariable THROW_VARIABLE = new AlloyVariable(
			"throw");

	public static final AlloyVariable BREAK_REACHED_VARIABLE = new AlloyVariable(
			"break_reached");


	public static final AlloyVariable ARG_THROW_VARIABLE = new AlloyVariable(
			"arg_throw");

	public static final AlloyVariable PRIMED_THROW_VARIABLE = new AlloyVariable(
			"throw", true);

	public static final AlloyVariable EXIT_REACHED_VARIABLE = new AlloyVariable(
			"exit_stmt_reached");

	public static final AlloyVariable INT_ARRAY_LENGTH = new AlloyVariable(
			"java_lang_IntArray_length");

	public static final AlloyVariable INT_ARRAY_CONTENTS = new AlloyVariable(
			"java_lang_IntArray_contents");

	public static final AlloyVariable CHAR_ARRAY_LENGTH = new AlloyVariable(
			"java_lang_CharArray_length");

	public static final AlloyVariable CHAR_ARRAY_CONTENTS = new AlloyVariable(
			"java_lang_CharArray_contents");

	public static final AlloyVariable LONG_ARRAY_LENGTH = new AlloyVariable(
			"java_lang_LongArray_length");

	public static final AlloyVariable LONG_ARRAY_CONTENTS = new AlloyVariable(
			"java_lang_LongArray_contents");

	public static final AlloyVariable OBJECT_ARRAY_LENGTH = new AlloyVariable(
			"java_lang_ObjectArray_length");

	public static final AlloyVariable OBJECT_ARRAY_CONTENTS = new AlloyVariable(
			"java_lang_ObjectArray_contents");

	public static final ExprVariable OBJECT_ARRAY_LENGTH_EXPRESSION = buildExprVariable(OBJECT_ARRAY_LENGTH);

	public static final ExprVariable OBJECT_ARRAY_CONTENTS_EXPRESSION = buildExprVariable(OBJECT_ARRAY_CONTENTS);

	public static final AlloyVariable JAVA_UTIL_SET_ELEMS_VARIABLE = new AlloyVariable("java_util_Set_elems", false);
	public static final AlloyVariable PRIMED_JAVA_UTIL_SET_ELEMS_VARIABLE = new AlloyVariable("java_util_Set_elems", true);

	public static final ExprVariable JAVA_UTIL_SET_ELEMS_EXPRESSION = buildExprVariable(JAVA_UTIL_SET_ELEMS_VARIABLE);
	public static final ExprVariable PRIMED_JAVA_UTIL_SET_ELEMS_EXPRESSION = buildExprVariable(PRIMED_JAVA_UTIL_SET_ELEMS_VARIABLE);


	public static final ExprConstant NULL_EXPRESSION = buildExprConstant(JSignatureFactory.NULL
			.getSignatureId());

	public static final ExprConstant FALSE_EXPRESSION = buildExprConstant(JSignatureFactory.FALSE
			.getSignatureId());

	public static final ExprConstant TRUE_EXPRESSION = buildExprConstant(JSignatureFactory.TRUE
			.getSignatureId());

	public static final ExprConstant ASSERTION_FAILURE_EXPRESSION = buildExprConstant("java_lang_AssertionErrorLit");

	public static final ExprVariable THROW_EXPRESSION = buildExprVariable(THROW_VARIABLE);

	public static final ExprVariable BREAK_REACHED_EXPRESSION = buildExprVariable(BREAK_REACHED_VARIABLE);


	public static final ExprVariable PRIMED_THROW_EXPRESSION = buildExprVariable(PRIMED_THROW_VARIABLE);

	public static final ExprVariable RETURN_EXPRESSION = buildExprVariable(RETURN_VARIABLE);

	public static final ExprVariable THIS_EXPRESSION = buildExprVariable(THIS_VARIABLE);

	public static final ExprVariable PRIMED_THIS_EXPRESSION = buildExprVariable(PRIMED_THIS_VARIABLE);

	public static final ExprVariable EXIT_REACHED_EXPRESSION = buildExprVariable(EXIT_REACHED_VARIABLE);

	public static final ExprVariable INT_ARRAY_LENGTH_EXPRESSION = buildExprVariable(INT_ARRAY_LENGTH);

	public static final ExprVariable INT_ARRAY_CONTENTS_EXPRESSION = buildExprVariable(INT_ARRAY_CONTENTS);

	public static final ExprVariable CHAR_ARRAY_CONTENTS_EXPRESSION = buildExprVariable(CHAR_ARRAY_CONTENTS);

	public static final ExprVariable LONG_ARRAY_CONTENTS_EXPRESSION = buildExprVariable(LONG_ARRAY_CONTENTS);



	public static ExprConstant buildCharConstant(char c) {
		return buildExprConstant("Char" + new Character(c).hashCode());
	}

	public static ExprConstant buildStringConstant(String c) {
		return buildExprConstant("String" + c.hashCode());
	}

	public static ExprFunction alloy_int_mul(AlloyExpression... es) {
		return unroll(MUL, es);
	}

	private static ExprFunction unroll(String functionId, AlloyExpression[] es) {
		if (es.length < 2)
			throw new IllegalArgumentException("cannot invoke " + functionId
					+ " with " + es.length + " operands.");
		ExprFunction function = ExprFunction.buildExprFunction(functionId,
				es[0], es[1]);
		for (int i = 2; i < es.length; i++) {
			AlloyExpression e = es[i];
			function = ExprFunction.buildExprFunction(functionId, function, e);
		}

		return function;
	}

	public static ExprFunction alloy_int_div(AlloyExpression... es) {
		return unroll(DIV, es);
	}

	public static ExprFunction alloy_int_negate(AlloyExpression e) {
		return buildExprFunction(NEGATE, e);
	}

	public static ExprFunction complement(AlloyExpression e) {
		JPreludeFactory.register_integer_complement();
		return buildExprFunction(COMPLEMENT, e);
	}

	public static ExprFunction alloy_int_rem(AlloyExpression... es) {
		return unroll(REM, es);
	}

	public static ExprFunction alloy_int_add(AlloyExpression... es) {
		return unroll(ADD, es);
	}

	public static ExprFunction fun_java_primitive_integer_value_add(
			AlloyExpression... es) {
		return unroll(FUN_JAVA_PRIMITIVE_INTEGER_VALUE_ADD, es);
	}

	public static ExprFunction fun_java_primitive_integer_value_sub(
			AlloyExpression... es) {
		return unroll(FUN_JAVA_PRIMITIVE_INTEGER_VALUE_SUB, es);
	}

//	public static ExprFunction fun_java_primitive_integer_value_eq(
//			AlloyExpression... es) {
//		return unroll(FUN_JAVA_PRIMITIVE_INTEGER_VALUE_EQ, es);
//	}

	public static ExprFunction fun_java_primitive_integer_value_negate(
			AlloyExpression e) {
		return buildExprFunction(FUN_JAVA_PRIMITIVE_INTEGER_VALUE_NEGATE, e);
	}

	public static ExprFunction fun_java_primitive_long_value_negate(
			AlloyExpression e) {
		return buildExprFunction(FUN_JAVA_PRIMITIVE_LONG_VALUE_NEGATE, e);
	}

	public static Object fun_java_primitive_charCharToInt_value_add(AlloyExpression... es) {
		// TODO Auto-generated method stub
		return unroll(FUN_JAVA_PRIMITIVE_CHARCHARtoINT_VALUE_ADD, es);
	}
	
	public static Object fun_java_primitive_charCharToInt_value_sub(AlloyExpression... es) {
		// TODO Auto-generated method stub
		return unroll(FUN_JAVA_PRIMITIVE_CHARCHARtoINT_VALUE_SUB, es);
	}

	
	public static Object fun_java_primitive_charIntToInt_value_add(AlloyExpression... es) {
		// TODO Auto-generated method stub
		return unroll(FUN_JAVA_PRIMITIVE_CHARINTtoINT_VALUE_ADD, es);
	}
	
	public static Object fun_java_primitive_charIntToInt_value_sub(AlloyExpression... es) {
		// TODO Auto-generated method stub
		return unroll(FUN_JAVA_PRIMITIVE_CHARINTtoINT_VALUE_SUB, es);
	}

	public static Object fun_java_primitive_intCharToInt_value_add(AlloyExpression... es) {
		// TODO Auto-generated method stub
		return unroll(FUN_JAVA_PRIMITIVE_INTCHARtoINT_VALUE_ADD, es);
	}
	
	public static Object fun_java_primitive_intCharToInt_value_sub(AlloyExpression... es) {
		// TODO Auto-generated method stub
		return unroll(FUN_JAVA_PRIMITIVE_INTCHARtoINT_VALUE_SUB, es);
	}

	
	public static ExprFunction alloy_int_sub(AlloyExpression... es) {
		return unroll(SUB, es);
	}

	public static ExprFunction alloy_int_eq(AlloyExpression... es) {
		return unroll(EQ, es);
	}

	public static ExprFunction alloy_int_shl(AlloyExpression... es) {
		return unroll(SHL, es);
	}

	public static ExprFunction alloy_iny_ushr(AlloyExpression... es) {
		return unroll(USHR, es);
	}

	public static ExprFunction alloy_int_sshr(AlloyExpression... es) {
		return unroll(SSHR, es);
	}

	public static AlloyExpression classOf(AlloyExpression expr) {
		return new ExprJoin(expr, new ExprConstant(null, "class"));
	}

	public static ExprConstant classSingleton(String signatureId) {
		return new ExprConstant(null, signatureId + "Class");
	}

	public static AlloyExpression setAdd(AlloyExpression set,
			AlloyExpression elem) {
		return ExprFunction.buildExprFunction("fun_set_add",
				new AlloyExpression[] { set, elem });
	}

	public static AlloyExpression setRemove(AlloyExpression set,
			AlloyExpression elem) {
		return ExprFunction.buildExprFunction("fun_set_remove",
				new AlloyExpression[] { set, elem });
	}

	public static AlloyExpression setContains(AlloyExpression set,
			AlloyExpression elem) {
		return ExprFunction.buildExprFunction("fun_set_contains",
				new AlloyExpression[] { set, elem });
	}

	public static AlloyExpression setIsEmpty(AlloyExpression set) {
		return ExprFunction.buildExprFunction("fun_set_is_empty",
				new AlloyExpression[] { set });
	}

	public static AlloyExpression mapPut(AlloyExpression map,
			AlloyExpression key, AlloyExpression value) {
		return ExprFunction.buildExprFunction("fun_map_put",
				new AlloyExpression[] { map, key, value });
	}

	public static AlloyExpression mapContainsKey(AlloyExpression map,
			ExprVariable key) {
		return ExprFunction.buildExprFunction("fun_map_contains_key",
				new AlloyExpression[] { map, key });
	}

	public static AlloyExpression mapRemove(AlloyExpression map,
			AlloyExpression key) {
		return ExprFunction.buildExprFunction("fun_map_remove",
				new AlloyExpression[] { map, key });
	}

	public static AlloyExpression mapGet(AlloyExpression map,
			AlloyExpression key) {
		return ExprFunction.buildExprFunction("fun_map_get",
				new AlloyExpression[] { map, key });
	}

	public static AlloyExpression listAdd(AlloyExpression list,
			AlloyExpression elem) {
		return ExprFunction.buildExprFunction("fun_list_add",
				new AlloyExpression[] { list, elem });
	}

	public static AlloyExpression listGet(AlloyExpression list,
			AlloyExpression index) {
		return ExprFunction.buildExprFunction("fun_list_get",
				new AlloyExpression[] { list, index });
	}

	public static AlloyExpression listEmpty(AlloyExpression list) {
		return ExprFunction.buildExprFunction("fun_list_empty",
				new AlloyExpression[] { list, });
	}

	public static AlloyExpression listContains(AlloyExpression list,
			AlloyExpression elem) {
		return ExprFunction.buildExprFunction("fun_list_contains",
				new AlloyExpression[] { list, elem });
	}

	public static AlloyExpression listRemove(AlloyExpression list,
			AlloyExpression index) {
		return ExprFunction.buildExprFunction("fun_list_remove",
				new AlloyExpression[] { list, index });
	}

	public static AlloyExpression reach(AlloyExpression head,
			ExprConstant typeConstant, ExprVariable[] fieldVariables) {
		if (fieldVariables.length>1){
			return ExprFunction.buildExprFunction("fun_reach", head, typeConstant,
					ExprUnion.buildExprUnion(fieldVariables)); //mfrias-mffrias-25-09-2012-Cambiado para poder pasar suma de campos
		} else {
			return ExprFunction.buildExprFunction("fun_reach", head, typeConstant,
					fieldVariables[0]); 
		}

	}

	public static AlloyExpression reach_JMLObjectSet(AlloyExpression head,
			ExprConstant typeConstant, ExprVariable[] fieldVariables) {
		JPreludeFactory.register_reach_JMLObjectSet();
		return ExprFunction.buildExprFunction(FUN_REACH_JMLOBJECT_SET, head,
				typeConstant, ExprUnion.buildExprUnion(fieldVariables), JML_OBJECT_SET_CONTAINS);
	}

	public static final AlloyVariable NULL_DEREF_VAR = new AlloyVariable(
			"nullDerefBool");

	public static final ExprVariable NULL_DEREF_EXPR = ExprVariable
			.buildExprVariable(NULL_DEREF_VAR);

	public static final AlloyVariable ASSERTION_FAILURE_VAR = new AlloyVariable(
			"assertionFailure");

	public static final ExprVariable ASSERTION_FAILURE_EXPR = ExprVariable
			.buildExprVariable(ASSERTION_FAILURE_VAR);

	public static final AlloyVariable NULL_DEREF_THROW = new AlloyVariable(
			"nullDerefThrow");

	public static final AlloyExpression PRIMED_RETURN_EXPRESSION = buildExprVariable(PRIMED_RETURN_VARIABLE);

	public static AlloyExpression setSize(AlloyExpression expr) {
		return ExprFunction.buildExprFunction("fun_set_size", expr);
	}

	/*mfrias: functions added for set_size returning a JavaPrimitiveIntegerValue*/
	public static AlloyExpression setSizeReturnsJavaPrimitiveIntegerValue(AlloyExpression expr) {
		return ExprFunction.buildExprFunction("fun_java_primitive_integer_value_size_of", expr);
	}


	/*mfrias functions added for size of java_util_set*/	
	public static AlloyExpression javaUtilSetSizeReturnsJavaPrimitiveIntegerValue(AlloyExpression expr, AlloyExpression fieldExpr) {
		return ExprFunction.buildExprFunction("fun_java_primitive_integer_value_java_util_set_size", 
				new AlloyExpression[]{expr,fieldExpr});
	}

	public static AlloyExpression javaUtilSetSizeReturnsAlloyInt(AlloyExpression expr, AlloyExpression fieldExpr) {
		return ExprFunction.buildExprFunction("fun_alloy_int_java_util_set_size", 
				new AlloyExpression[]{expr,fieldExpr});
	}

	/*mfrias functions added for contains of java_util_set*/	

	public static AlloyExpression javaUtilSetContains(AlloyExpression expr, AlloyExpression arg, AlloyExpression fieldExpr) {
		return ExprFunction.buildExprFunction("fun_java_util_set_contains", 
				new AlloyExpression[]{expr,arg,fieldExpr});
	}



	public static AlloyExpression fun_java_primitive_float_value_add(AlloyExpression arg1, AlloyExpression arg2) {
		return ExprFunction.buildExprFunction("fun_java_primitive_float_value_add", 
				new AlloyExpression[]{arg1,arg2});
	}
	
	public static AlloyExpression fun_java_primitive_float_value_sub(AlloyExpression arg1, AlloyExpression arg2) {
		return ExprFunction.buildExprFunction("fun_java_primitive_float_value_sub", 
				new AlloyExpression[]{arg1,arg2});
	}

	public static AlloyExpression fun_java_primitive_float_value_mul(AlloyExpression arg1, AlloyExpression arg2) {
		return ExprFunction.buildExprFunction("fun_java_primitive_float_value_mul", 
				new AlloyExpression[]{arg1,arg2});
	}
	
	public static AlloyExpression fun_java_primitive_float_value_div(AlloyExpression arg1, AlloyExpression arg2) {
		return ExprFunction.buildExprFunction("fun_java_primitive_float_value_div", 
				new AlloyExpression[]{arg1,arg2});
	}


	public static AlloyExpression floatIsNaN(AlloyExpression arg) {
		return ExprFunction.buildExprFunction("fun_java_lang_float_isNaN", 
				new AlloyExpression[]{arg});
	}


	public static AlloyExpression setSum(AlloyExpression expr) {
		return ExprFunction.buildExprFunction("fun_set_sum", expr);
	}

	public static AlloyExpression listSize(AlloyExpression expr) {
		return ExprFunction.buildExprFunction("fun_list_size", expr);
	}

	public static AlloyExpression listGet(AlloyExpression expr) {
		return ExprFunction.buildExprFunction("fun_list_get", expr);
	}

	public static AlloyExpression listEquals(AlloyExpression list1,
			AlloyExpression list2) {
		return ExprFunction.buildExprFunction("fun_list_equals",
				new AlloyExpression[] { list1, list2 });
	}

	public static AlloyExpression listEquals(AlloyExpression list1) {
		return ExprFunction.buildExprFunction("fun_list_empty",
				new AlloyExpression[] { list1 });
	}

	public static AlloyExpression objectEquals(AlloyExpression object1,
			AlloyExpression object2) {
		return ExprFunction.buildExprFunction("fun_univ_equals",
				new AlloyExpression[] { object1, object2 });
	}

	public static AlloyExpression customRelationOverride(AlloyExpression rel,
			AlloyExpression key, AlloyExpression value) {
		return ExprFunction.buildExprFunction("rel_override", rel, key, value);
	}

	public static AlloyExpression closure(AlloyExpression rel) {
		return ExprFunction.buildExprFunction("fun_closure", rel);
	}

	public static AlloyExpression reflexiveClosure(AlloyExpression rel) {
		return ExprFunction.buildExprFunction("fun_reflexive_closure", rel);
	}

	public static AlloyExpression transpose(AlloyExpression rel) {
		return ExprFunction.buildExprFunction("fun_transpose", rel);
	}

	public static AlloyExpression intersection(AlloyExpression left,
			AlloyExpression right) {
		return ExprFunction.buildExprFunction("fun_set_intersection", left,
				right);
	}

	public static AlloyExpression difference(AlloyExpression left,
			AlloyExpression right) {
		return ExprFunction
				.buildExprFunction("fun_set_difference", left, right);
	}

	public static ExprConstant buildLiteralSingleton(String exceptionClassname) {
		String signatureId = String.format("%sLit", exceptionClassname);
		return ExprConstant.buildExprConstant(signatureId);
	}

	public static final ExprConstant CLASS_FIELDS = ExprConstant
			.buildExprConstant("ClassFields");

	public static final ExprConstant UNIV_EXPRESSION = ExprConstant
			.buildExprConstant("univ");

	private static final String BOOLEAN_OR = "Or";

	private static final String BOOLEAN_AND = "And";

	private static final String FUN_PRIMITIVE_EQ = "fun_univ_equals";

	private static final String CAST_CHAR_TO_INT = "fun_cast_char_to_int";

	private static final String CAST_CHAR_TO_LONG = "fun_cast_char_to_long";
	
	private static final String CAST_INT_TO_LONG = "fun_cast_int_to_long";

	private static final String NARROW_CAST_INT_TO_CHAR = "fun_narrowing_cast_int_to_char";
	
	private static final String NARROW_CAST_LONG_TO_CHAR = "fun_narrowing_cast_long_to_char";

	private static final String NARROW_CAST_LONG_TO_INT = "fun_narrowing_cast_long_to_int";

	private static final String FUN_JAVA_PRIMITIVE_LONGINTtoLONG_VALUE_ADD = "fun_long_int_to_long_add";
	
	private static final String FUN_JAVA_PRIMITIVE_INTLONGtoLONG_VALUE_ADD = "fun_int_long_to_long_add";

	private static final String FUN_JAVA_PRIMITIVE_LONGINTtoLONG_VALUE_SUB = "fun_long_int_to_long_sub";

	private static final String FUN_JAVA_PRIMITIVE_INTLONGtoLONG_VALUE_SUB = "fun_int_long_to_long_sub";


	public static AlloyExpression fun_java_primitive_integer_value_mul(
			AlloyExpression... es) {
		return unroll(FUN_JAVA_PRIMITIVE_INTEGER_VALUE_MUL, es);
	}

	public static AlloyExpression fun_java_primitive_integer_value_div(
			AlloyExpression... es) {
		return unroll(FUN_JAVA_PRIMITIVE_INTEGER_VALUE_DIV, es);
	}

	public static AlloyExpression fun_java_primitive_integer_value_rem(
			AlloyExpression... es) {
		return unroll(FUN_JAVA_PRIMITIVE_INTEGER_VALUE_REM, es);

	}

	public static AlloyExpression fun_java_primitive_integer_value_size_of(
			AlloyExpression expression) {
		return buildExprFunction(FUN_JAVA_PRIMITIVE_INTEGER_VALUE_SIZE_OF,
				expression);
	}

	public static AlloyExpression fun_java_primitive_integer_value_sshr(
			AlloyExpression... es) {
		if (es.length > 2) {
			throw new IllegalArgumentException("cannot invoke sshr" + " with "
					+ es.length + " operands.");
		}

		if ((!(es[1] instanceof ExprConstant))
				|| (!((ExprConstant) es[1]).toString().equals(
						"JavaPrimitiveIntegerLiteral1"))) {
			throw new IllegalArgumentException("cannot invoke sshr"
					+ " with argument " + es[1].toString());
		}

		AlloyExpression expression = es[0];
		// TODO Auto-generated method stub
		return buildExprFunction(FUN_JAVA_PRIMITIVE_INTEGER_VALUE_SSHR,
				expression);
	}

	public static ExprFunction boolean_or(AlloyExpression... es) {
		return unroll(BOOLEAN_OR, es);
	}

	public static ExprFunction boolean_and(AlloyExpression... es) {
		return unroll(BOOLEAN_AND, es);
	}

	public static Object fun_java_primitive_long_value_add(
			AlloyExpression... es) {
		return unroll(FUN_JAVA_PRIMITIVE_LONG_VALUE_ADD, es);
	}

	public static Object fun_java_primitive_long_value_sub(
			AlloyExpression... es) {
		return unroll(FUN_JAVA_PRIMITIVE_LONG_VALUE_SUB, es);
	}

	public static Object fun_java_primitive_long_value_eq(
			AlloyExpression... es) {
		return unroll(FUN_JAVA_PRIMITIVE_LONG_VALUE_EQ, es);
	}

	public static ExprFunction not(AlloyExpression es) {
		return ExprFunction.buildExprFunction(NOT, es);
	}

	public static AlloyExpression buildEquals(String qualified_name, AlloyExpression prefix_expression, AlloyExpression alloyExpression) {
		return ExprFunction.buildExprFunction(qualified_name, prefix_expression, alloyExpression);
	}

	public static AlloyExpression buildMethodCall(String name, AlloyExpression prefix_expression,
			Vector<AlloyExpression> args_expression, String cType) {
		AlloyExpression[] params = new AlloyExpression[args_expression.size() + 1];
		params[0] = prefix_expression;
		for (int i = 0; i<args_expression.size(); i++){
			params[i+1] = args_expression.get(i);
		}
		return ExprFunction.buildExprFunction(name, params, cType);	
	}

	public static Object fun_boolean_eq(AlloyExpression... es) {
		return unroll(FUN_PRIMITIVE_EQ, es);
	}

	public static Object fun_java_primitive_char_value_to_int_value(AlloyExpression... es) {
		return buildExprFunction(CAST_CHAR_TO_INT, es);
		
	}
	
	public static Object fun_java_primitive_char_value_to_long_value(AlloyExpression... es) {
		return buildExprFunction(CAST_CHAR_TO_LONG, es);
		
	}
	
	public static Object fun_java_primitive_int_value_to_long_value(AlloyExpression... es) {
		return buildExprFunction(CAST_INT_TO_LONG, es);
		
	}

	public static Object fun_java_primitive_int_value_to_char_value(AlloyExpression... es){
		return buildExprFunction(NARROW_CAST_INT_TO_CHAR, es);
	}
	
	public static Object fun_java_primitive_long_value_to_char_value(AlloyExpression... es){
		return buildExprFunction(NARROW_CAST_LONG_TO_CHAR, es);
	}

	
	public static Object fun_java_primitive_long_value_to_int_value(AlloyExpression... es){
		return buildExprFunction(NARROW_CAST_LONG_TO_INT, es);
	}

	
	public static Object fun_java_primitive_longCharToLong_value_add(AlloyExpression... es) {
			// TODO Auto-generated method stub
			return unroll(FUN_JAVA_PRIMITIVE_LONGCHARtoLONG_VALUE_ADD, es);
	}

	public static Object fun_java_primitive_longCharToLong_value_sub(AlloyExpression... es) {
		// TODO Auto-generated method stub
		return unroll(FUN_JAVA_PRIMITIVE_LONGCHARtoLONG_VALUE_SUB, es);
	}

	public static Object fun_java_primitive_longIntToLong_value_add(AlloyExpression... es) {
		// TODO Auto-generated method stub
		return unroll(FUN_JAVA_PRIMITIVE_LONGINTtoLONG_VALUE_ADD, es);
	}
	
	public static Object fun_java_primitive_intLongToLong_value_add(AlloyExpression... es) {
		return unroll(FUN_JAVA_PRIMITIVE_INTLONGtoLONG_VALUE_ADD, es);
	}


	public static Object fun_java_primitive_longIntToLong_value_sub(AlloyExpression... es) {
		// TODO Auto-generated method stub
		return unroll(FUN_JAVA_PRIMITIVE_LONGINTtoLONG_VALUE_SUB, es);
	}

	public static Object fun_java_primitive_intLongToLong_value_sub(AlloyExpression... es) {
		// TODO Auto-generated method stub
		return unroll(FUN_JAVA_PRIMITIVE_INTLONGtoLONG_VALUE_SUB, es);
	}






}

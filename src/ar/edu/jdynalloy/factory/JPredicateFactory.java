package ar.edu.jdynalloy.factory;

import static ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula.buildPredicate;

import java.util.ArrayList;

import ar.edu.taco.simplejml.builtin.JavaPrimitiveCharValue;
import ar.edu.taco.simplejml.builtin.JavaPrimitiveFloatValue;
import ar.edu.taco.simplejml.builtin.JavaPrimitiveIntegerValue;
import ar.edu.taco.simplejml.builtin.JavaPrimitiveLongValue;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntLiteral;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;


public abstract class JPredicateFactory {

	public static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_LT = "pred_java_primitive_integer_value_lt";

	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_INT_LT = "pred_java_primitive_char_value_CharIntlt";
	
	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_INT_CHAR_LT = "pred_java_primitive_char_value_IntCharlt";

	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_CHAR_LT = "pred_java_primitive_char_value_CharCharlt";

	private static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_LTE = "pred_java_primitive_integer_value_lte";

	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_INT_LTE = "pred_java_primitive_char_value_CharIntlte";
	
	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_INT_CHAR_LTE = "pred_java_primitive_char_value_IntCharlte";

	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_CHAR_LTE = "pred_java_primitive_char_value_CharCharlte";

	private static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_GT = "pred_java_primitive_integer_value_gt";
		
	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_INT_GT = "pred_java_primitive_char_value_CharIntgt";
	
	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_INT_CHAR_GT = "pred_java_primitive_char_value_IntChargt";

	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_CHAR_GT = "pred_java_primitive_char_value_CharChargt";
	
	private static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_LONG_CHAR_GT = "pred_java_primitive_char_value_LongChargt";

	private static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_GTE = "pred_java_primitive_integer_value_gte";
	
	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_INT_GTE = "pred_java_primitive_char_value_CharIntgte";
	
	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_INT_CHAR_GTE = "pred_java_primitive_char_value_IntChargte";

	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_CHAR_GTE = "pred_java_primitive_char_value_CharChargte";

	private static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_EQ = "pred_java_primitive_integer_value_eq";
	
	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_INT_EQ = "pred_java_primitive_char_value_CharInteq";
	
	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_INT_CHAR_EQ = "pred_java_primitive_char_value_IntChareq";

	public static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_CHAR_EQ = "pred_java_primitive_char_value_CharChareq";

	public static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_MUL = "pred_java_primitive_integer_value_mul";
	
	public static String PRED_JAVA_PRIMITIVE_CHAR_VALUE_INT_MUL = "pred_java_primitive_char_value_int_mul";
	
	public static String PRED_JAVA_PRIMITIVE_CHAR_VALUE_LONG_MUL = "pred_java_primitive_char_value_long_mul";
	
	public static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_ADD_MARKER = "pred_java_primitive_integer_value_add_marker";
	
	public static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_ADD = "pred_java_primitive_integer_value_add";
	
	public static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_SUB = "pred_java_primitive_integer_value_sub";

	public static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_MUL_MARKER = "pred_java_primitive_integer_value_mul_marker";

	public static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_MUL = "pred_java_primitive_long_value_mul";
	
	public static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_ADD = "pred_java_primitive_long_value_add";
	
	public static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_SUB = "pred_java_primitive_long_value_sub";

	public static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_DIV_REM = "pred_java_primitive_integer_value_div_rem";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_GT = "pred_java_primitive_long_value_gt";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_GTE = "pred_java_primitive_long_value_gte";

	public static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_LT = "pred_java_primitive_long_value_lt";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_LTE = "pred_java_primitive_long_value_lte";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_EQ = "pred_java_primitive_long_value_eq";
	
	 static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_EQ = "pred_java_primitive_char_value_eq";
	
	private static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_LONG_CHAR_GTE = "pred_java_primitive_char_value_LongChargte";
	
	private static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_LONG_CHAR_LT = "pred_java_primitive_char_value_LongCharlt";

	private static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_LONG_CHAR_LTE = "pred_java_primitive_char_value_LongCharlte";
	
	private static final String PRED_JAVA_PRIMITIVE_CHAR_VALUE_LONG_CHAR_EQ = "pred_java_primitive_char_value_LongChareq";

	public static final String INT_LT_PRED_ID = "lt";

	public static final String INT_LT_PRED = "pred lt[o1,o2:univ]{o1 in Int o2 in Int o1<o2}";

	public static final String INT_LTE_PRED_ID = "lte";

	public static final String INT_LTE_PRED = "pred lte[o1,o2:univ]{o1 in Int o2 in Int o1<o2 or o1=o2}";

	public static final String INT_GT_PRED_ID = "gt";

	public static final String INT_GT_PRED = "pred gt[o1,o2:univ]{o1 in Int o2 in Int o1>o2}";

	public static final String INT_GTE_PRED_ID = "gte";

	public static final String INT_GTE_PRED = "pred gte[o1,o2:univ]{o1 in Int o2 in Int o1>o2 or o1=o2}";
	
	public static final String EQU_PRED_ID = "equ";

	public static final String EQU_PRED_SPEC = "pred equ[o1,o2:univ]{o1=o2}";

	public static final String NEQ_PRED_ID = "neq";

	public static final String NEQ_PRED_SPEC = "pred neq[o1,o2:univ]{o1!=o2}";

	public static final String OVERRIDE_RELATION_POST_ID = "overrideRelPost";

	public static final String OVERRIDE_RELATION_POST_SPEC = "pred "
			+ OVERRIDE_RELATION_POST_ID
			+ "[r0,r1:univ->univ,l,r: univ]{r1=r0++(l->r)}";

	public static final String ALLOC_ATOM_POST_ID = "allocAtomPost";

	public static final String ALLOC_ATOM_POST_SPEC = "pred "
			+ ALLOC_ATOM_POST_ID
			+ "[newObject:univ, entryObjects: set univ, fields: univ->univ]{ newObject !in entryObjects.^fields newObject !in entryObjects }";

	public static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_DIV_REM = "pred_java_primitive_long_value_div_rem";
	
	public static String PRED_JAVA_PRIMITIVE_CHAR_INT_VALUE_DIV_REM = "pred_java_primitive_char_int_value_div_rem";

	public static String PRED_JAVA_PRIMITIVE_CHAR_LONG_VALUE_DIV_REM = "pred_java_primitive_char_long_value_div_rem";
	
	private static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_EQ = "pred_java_primitive_float_value_eq";

	private static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_NEQ = "pred_java_primitive_float_value_neq";

	private static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_LT = "pred_java_primitive_float_value_lt";
	private static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_LTE = "pred_java_primitive_float_value_lte";
	private static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_GT = "pred_java_primitive_float_value_gt";
	private static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_GTE = "pred_java_primitive_float_value_gte";

	public static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_MUL = "pred_java_primitive_float_value_mul";

	public static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_MUL_MARKER = "pred_java_primitive_float_value_mul_marker";

	public static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_DIV = "pred_java_primitive_float_value_div";

	public static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_ADD = "pred_java_primitive_float_value_add";

	public static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_SUB = "pred_java_primitive_float_value_sub";

	public static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_MUL_MARKER = "pred_java_primitive_long_value_mul_marker";

	public static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_DIV_REM_MARKER = "pred_java_primitive_long_value_div_rem_marker";
	
	public static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_DIV_REM_MARKER = "pred_java_primitive_integer_value_div_rem_marker";
	
	public static String PRED_JAVA_PRIMITIVE_CHAR_VALUE_DIV_REM_INT_MARKER = "pred_java_primitive_char_value_div_rem_int_marker";

	public static String PRED_JAVA_PRIMITIVE_CHAR_VALUE_DIV_REM_LONG_MARKER = "pred_java_primitive_char_value_div_rem_int_marker";

	public static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_DIV_MARKER = "pred_java_primitive_float_value_div_marker";

	public static final String PRED_JAVA_PRIMITIVE_FLOAT_VALUE_ADD_MARKER = "pred_java_primitive_float_value_add_marker";

	public static final String PRED_JAVA_PRIMITIVE_INTEGER_VALUE_SUB_MARKER = "pred_java_primitive_float_value_sub_marker";

	public static final String PRED_JAVA_PRIMITIVE_charIntToInt_VALUE_ADD = "pred_java_primitive_char_value_addCharIntToJavaPrimitiveIntegerValue";
	
	public static final String PRED_JAVA_PRIMITIVE_CHARINTtoINT_VALUE_ADD_MARKER = "pred_java_primitive_char_value_addCharIntToJavaPrimitiveIntegerValue_marker";

	public static final String PRED_JAVA_PRIMITIVE_intCharToInt_VALUE_ADD = "pred_java_primitive_char_value_addIntCharToJavaPrimitiveIntegerValue";
	
	public static final String PRED_JAVA_PRIMITIVE_INTCHARtoINT_VALUE_ADD_MARKER = "pred_java_primitive_char_value_addIntCharToJavaPrimitiveIntegerValue_marker";

	public static final String PRED_JAVA_PRIMITIVE_charCharToInt_VALUE_ADD = "pred_java_primitive_char_value_addCharCharToJavaPrimitiveIntegerValue";
	
	public static final String PRED_JAVA_PRIMITIVE_CHARCHARtoINT_VALUE_ADD_MARKER = "pred_java_primitive_char_value_addCharCharToJavaPrimitiveIntegerValue_marker";

	public static final String PRED_JAVA_PRIMITIVE_charCharToInt_VALUE_SUB = "pred_java_primitive_char_value_subCharCharToJavaPrimitiveIntegerValue";

	public static final String PRED_JAVA_PRIMITIVE_CHARCHARtoINT_VALUE_SUB_MARKER = "pred_java_primitive_char_value_subCharCharToJavaPrimitiveIntegerValue_marker";

	public static final String PRED_JAVA_PRIMITIVE_intCharToInt_VALUE_SUB = "pred_java_primitive_char_value_subIntCharToJavaPrimitiveIntegerValue";

	public static final String PRED_JAVA_PRIMITIVE_INTCHARtoINT_VALUE_SUB_MARKER = "pred_java_primitive_char_value_subIntCharToJavaPrimitiveIntegerValue_marker";

	public static final String PRED_JAVA_PRIMITIVE_charIntToInt_VALUE_SUB = "pred_java_primitive_char_value_subCharIntToJavaPrimitiveIntegerValue";

	public static final String PRED_JAVA_PRIMITIVE_CHARINTtoINT_VALUE_SUB_MARKER = "pred_java_primitive_char_value_subCharIntToJavaPrimitiveIntegerValue_marker";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_LONG_INT_GT = "pred_java_primitive_long_value_long_int_gt";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_LONG_INT_GTE = "pred_java_primitive_long_value_long_int_gte";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_LONG_INT_LT = "pred_java_primitive_long_value_long_int_lt";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_LONG_INT_LTE = "pred_java_primitive_long_value_long_int_lte";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_LONG_INT_EQ = "pred_java_primitive_long_value_long_int_eq";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_INT_LONG_GT = "pred_java_primitive_long_value_int_long_gt";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_INT_LONG_GTE = "pred_java_primitive_long_value_int_long_gte";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_INT_LONG_LT = "pred_java_primitive_long_value_int_long_lt";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_INT_LONG_LTE = "pred_java_primitive_long_value_int_long_lte";

	private static final String PRED_JAVA_PRIMITIVE_LONG_VALUE_INT_LONG_EQ = "pred_java_primitive_long_value_int_long_eq";

	


	







	

	
	public static PredicateFormula eq(AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create eq with "
					+ es.length + " operands");
		return buildPredicate(EQU_PRED_ID, es);
	}

	public static PredicateFormula isTrue(AlloyExpression expr) {
		return eq(expr, JExpressionFactory.TRUE_EXPRESSION);
	}

	public static PredicateFormula neq(AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create neq with "
					+ es.length + " operands");
		return buildPredicate(NEQ_PRED_ID, es);
	}

	public static PredicateFormula alloy_int_lt(AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(INT_LT_PRED_ID, es);
	}

	public static PredicateFormula pred_java_primitive_integer_value_lt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_INTEGER_VALUE_LT, es);
	}

	public static PredicateFormula pred_java_primitive_char_value_java_primitive_integer_value_lt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_INT_LT, es);
	}
	
	public static PredicateFormula pred_java_primitive_integer_value_java_primitive_char_value_lt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_INT_CHAR_LT, es);
	}

	public static PredicateFormula pred_java_primitive_char_value_java_primitive_char_value_lt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_CHAR_LT, es);
	}


	public static PredicateFormula alloy_int_gt(AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create gt with "
					+ es.length + " operands");
		return buildPredicate(INT_GT_PRED_ID, es);
	}
	
	public static PredicateFormula pred_java_primitive_char_value_java_primitive_integer_value_gt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_INT_GT, es);
	}
	
	public static PredicateFormula pred_java_primitive_integer_value_java_primitive_char_value_gt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_INT_CHAR_GT, es);
	}

	public static PredicateFormula pred_java_primitive_char_value_java_primitive_char_value_gt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_CHAR_GT, es);
	}


	public static PredicateFormula pred_java_primitive_long_value_java_primitive_char_value_gt(AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_LONG_CHAR_GT, es);
	}

	public static PredicateFormula pred_java_primitive_long_value_java_primitive_char_value_lt(AlloyExpression... es) {
	if (es.length != 2)
		throw new IllegalArgumentException("cannot create lt with "
				+ es.length + " operands");
	return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_LONG_CHAR_LT, es);
	}

	public static PredicateFormula pred_java_primitive_long_value_java_primitive_char_value_lte(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_LONG_CHAR_LTE, es);
	}

	
	public static PredicateFormula alloy_int_lte(AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lte with "
					+ es.length + " operands");
		return buildPredicate(INT_LTE_PRED_ID, es);
	}
	
	public static PredicateFormula pred_java_primitive_char_value_java_primitive_integer_value_lte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_INT_LTE, es);
	}
	
	public static PredicateFormula pred_java_primitive_char_value_java_primitive_integer_value_eq(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_INT_EQ, es);
	}
	
	

	public static PredicateFormula pred_java_primitive_integer_value_java_primitive_char_value_lte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_INT_CHAR_LTE, es);
	}

	public static PredicateFormula pred_java_primitive_integer_value_java_primitive_char_value_eq(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_INT_CHAR_EQ, es);
	}

	public static PredicateFormula pred_java_primitive_char_value_java_primitive_char_value_lte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_CHAR_LTE, es);
	}
	

	public static PredicateFormula pred_java_primitive_char_value_java_primitive_char_value_eq(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_CHAR_EQ, es);
	}

	
	public static PredicateFormula alloy_int_gte(AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create gte with "
					+ es.length + " operands");
		return buildPredicate(INT_GTE_PRED_ID, es);
	}
	
	
	public static PredicateFormula pred_java_primitive_char_value_java_primitive_integer_value_gte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_INT_GTE, es);
	}
	
	public static PredicateFormula pred_java_primitive_integer_value_java_primitive_char_value_gte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_INT_CHAR_GTE, es);
	}

	public static PredicateFormula pred_java_primitive_char_value_java_primitive_char_value_gte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_CHAR_CHAR_GTE, es);
	}



	public static PredicateFormula instanceOf(AlloyExpression l,
			String signatureId) {
		return buildPredicate("instanceOf", l, new ExprConstant(null,
				signatureId));
	}

	public static PredicateFormula isCasteableTo(AlloyExpression l,
			String signatureId) {
		return buildPredicate("isCasteableTo", l, new ExprConstant(null,
				signatureId));
	}

	public static AlloyFormula emptySet(AlloyExpression set) {
		return buildPredicate("pred_empty_set", set);
	}

	public static AlloyFormula someSet(AlloyExpression set) {
		return buildPredicate("pred_set_some", set);
	}

	public static AlloyFormula loneSet(AlloyExpression set) {
		return buildPredicate("pred_set_lone", set);
	}

	public static AlloyFormula oneSet(AlloyExpression set) {
		return buildPredicate("pred_set_one", set);
	}

	public static AlloyFormula objectSubset(AlloyExpression set) {
		return buildPredicate("pred_Object_subset", set);
	}

	public static AlloyFormula emptyList(AlloyExpression seq) {
		return buildPredicate("pred_empty_list", seq);
	}

	public static AlloyFormula emptyMap(AlloyExpression set) {
		return buildPredicate("pred_empty_map", set);
	}

	public static AlloyFormula isEmptyOrNull(AlloyExpression expr) {
		return buildPredicate("isEmptyOrNull", expr);
	}

	public static AlloyFormula isSubset(AlloyExpression left,
			AlloyExpression right) {
		return buildPredicate("isSubset", left, right);
	}

	public static AlloyFormula isNotSubset(AlloyExpression left,
			AlloyExpression right) {
		return buildPredicate("isNotSubset", left, right);
	}

	public static AlloyFormula liftExpression(AlloyExpression expression) {
		return buildPredicate("liftExpression", expression);
	}

	public static AlloyFormula xor(AlloyFormula left, AlloyFormula right) {
		AlloyFormula not_left = new NotFormula(left);
		AlloyFormula not_right = new NotFormula(right);
		AlloyFormula left_and_not_right = new AndFormula(left, not_right);
		AlloyFormula not_left_and_right = new AndFormula(not_left, right);
		AlloyFormula xor = new OrFormula(left_and_not_right, not_left_and_right);
		return xor;
	}

	public static AlloyFormula pred_java_primitive_integer_value_lte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lte with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_INTEGER_VALUE_LTE, es);

	}
	
	public static AlloyFormula pred_java_primitive_integer_value_eq(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create eq with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_INTEGER_VALUE_EQ, es);

	}

	public static AlloyFormula pred_java_primitive_integer_value_gt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create gt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_INTEGER_VALUE_GT, es);
	}

	public static AlloyFormula pred_java_primitive_integer_value_gte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create gte with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_INTEGER_VALUE_GTE, es);
	}

	public static AlloyFormula pred_java_primitive_integer_value_mul(
			AlloyExpression... es) {
		if (es.length != 4)
			throw new IllegalArgumentException(
					"cannot create pred_java_primitive_integer_value_mul with "
							+ es.length + " operands");

		return buildPredicate(PRED_JAVA_PRIMITIVE_INTEGER_VALUE_MUL, es);
	}

	public static AlloyFormula pred_java_primitive_integer_value_div_rem(
			AlloyExpression... es) {
		if (es.length != 4)
			throw new IllegalArgumentException(
					"cannot create pred_java_primitive_integer_value_mul with "
							+ es.length + " operands");

		return buildPredicate(PRED_JAVA_PRIMITIVE_INTEGER_VALUE_DIV_REM, es);
	}

	public static AlloyFormula pred_java_primitive_long_value_gt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create long gt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_GT, es);
	}

	public static AlloyFormula pred_java_primitive_long_value_gte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create long gte with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_GTE, es);
	}

	public static PredicateFormula pred_java_primitive_long_value_lt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create long lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_LT, es);
	}

	public static PredicateFormula pred_java_primitive_long_value_lte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create long lte with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_LTE, es);
	}
	
	
	public static AlloyFormula pred_java_primitive_long_value_eq(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create eq with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_EQ, es);

	}
	



	
	public static Object pred_java_primitive_long_value_java_primitive_char_value_gte(AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_LONG_CHAR_GTE, es);
	}

	
	public static Object pred_java_primitive_long_value_java_primitive_char_value_eq(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_LONG_CHAR_EQ, es);
	}

	
	
	public static PredicateFormula pred_java_primitive_float_value_eq(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create float eq with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_FLOAT_VALUE_EQ, es);
	}

	public static PredicateFormula pred_java_primitive_float_value_neq(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create float neq with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_FLOAT_VALUE_NEQ, es);
	}

	public static PredicateFormula pred_java_primitive_float_value_lt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create float lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_FLOAT_VALUE_LT, es);
	}


	public static PredicateFormula pred_java_primitive_float_value_gt(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create float gt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_FLOAT_VALUE_GT, es);
	}

	public static PredicateFormula pred_java_primitive_float_value_lte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create float lte with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_FLOAT_VALUE_LTE, es);
	}
	
	public static PredicateFormula pred_java_primitive_float_value_gte(
			AlloyExpression... es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create float gte with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_FLOAT_VALUE_GTE, es);
	}
	
	public static PredicateFormula equZero(AlloyExpression e, String typeName){
		
		if (typeName.equals("Int")){
			AlloyExpression[] pars = new AlloyExpression[]{e,new ExprIntLiteral(0)};
			return buildPredicate(EQU_PRED_ID, pars);
		}
		if (typeName.equals("JavaPrimitiveIntegerValue")) {
			String varName = ((ExprVariable)e).getVariable().getVariableId().getString();
			int index = Integer.valueOf(varName.substring(varName.lastIndexOf("_")+1, varName.length()));
			AlloyExpression new_e = new ExprVariable(new AlloyVariable(varName));
			AlloyExpression[] pars = new AlloyExpression[]{new_e, JavaPrimitiveIntegerValue.getInstance().toJavaPrimitiveIntegerLiteral(0, false)};
			return buildPredicate(PRED_JAVA_PRIMITIVE_INTEGER_VALUE_EQ, pars);
		}
		if (typeName.equals("JavaPrimitiveLongValue")) {
			AlloyExpression[] pars = new AlloyExpression[]{e, JavaPrimitiveLongValue.getInstance().toJavaPrimitiveLongLiteral(0, false)};
			return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_EQ, pars);
		}
		if (typeName.equals("JavaPrimitiveCharValue")) {
			AlloyExpression[] pars = new AlloyExpression[]{e, JavaPrimitiveCharValue.getInstance().toJavaPrimitiveCharLiteral('\u0000', false)};
			return buildPredicate(PRED_JAVA_PRIMITIVE_CHAR_VALUE_EQ, pars);
		}
		if (typeName.equals("JavaPrimitiveFloatValue")) {
			AlloyExpression[] pars = new AlloyExpression[]{e, JavaPrimitiveFloatValue.getInstance().toJavaPrimitiveFloatLiteral(0, false)};
			return buildPredicate(PRED_JAVA_PRIMITIVE_FLOAT_VALUE_EQ, pars);
		}
		throw new RuntimeException("Method PredicateFormula.equZero called with expression of type " + typeName);
	}

	public static Object pred_java_primitive_long_value_java_primitive_int_value_gt(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_LONG_INT_GT, es);
	}

	public static Object pred_java_primitive_long_value_java_primitive_int_value_gte(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_LONG_INT_GTE, es);
	}

	public static Object pred_java_primitive_long_value_java_primitive_int_value_lt(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_LONG_INT_LT, es);
	}

	public static Object pred_java_primitive_long_value_java_primitive_int_value_lte(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_LONG_INT_LTE, es);
	}

	public static Object pred_java_primitive_long_value_java_primitive_int_value_eq(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_LONG_INT_EQ, es);
	}

	public static Object pred_java_primitive_long_value_java_primitive_integer_value_gt(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_LONG_INT_GT, es);
	}

	public static Object pred_java_primitive_long_value_java_primitive_integer_long_value_gt(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_INT_LONG_GT, es);
	}

	public static Object pred_java_primitive_long_value_java_primitive_integer_long_value_gte(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_INT_LONG_GTE, es);
	}

	public static Object pred_java_primitive_long_value_java_primitive_integer_long_value_lt(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_INT_LONG_LT, es);
	}

	public static Object pred_java_primitive_long_value_java_primitive_integer_long_value_lte(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_INT_LONG_LTE, es);
	}

	public static Object pred_java_primitive_long_value_java_primitive_integer_long_value_eq(AlloyExpression...es) {
		if (es.length != 2)
			throw new IllegalArgumentException("cannot create lt with "
					+ es.length + " operands");
		return buildPredicate(PRED_JAVA_PRIMITIVE_LONG_VALUE_INT_LONG_EQ, es);
	}





}

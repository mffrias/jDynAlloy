package ar.edu.jdynalloy.factory;

import static ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable.buildExprVariable;
import static ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction.buildInvokeAction;

import java.util.Set;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprOverride;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprProduct;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;

public abstract class DynalloyFactory {

	public static String declareFactClassHierarchy(String signatureId,
			String classSignatureId, Set<String> descendantsOfSignatureId) {
		StringBuffer sb = new StringBuffer();
		sb.append("fact {");
		sb.append("(" + signatureId);
		if (!descendantsOfSignatureId.isEmpty()) {
			sb.append("-(");
			for (String descendant : descendantsOfSignatureId) {
				if (!sb.toString().endsWith("("))
					sb.append("+");
				sb.append(descendant);
			}
			sb.append(")");
		}
		sb.append(")=class." + classSignatureId);
		sb.append("}");
		sb.append("\n");
		return sb.toString();
	}

	public static final ExprVariable USED_OBJECTS = buildExprVariable("usedObjects");

	

	public static final AlloyVariable ALLOY_INT_OBJECT_ARRAY_VARIABLE = new AlloyVariable("AlloyInt_Object_Array");
	public static final AlloyVariable PRIMED_ALLOY_INT_OBJECT_ARRAY_VARIABLE = new AlloyVariable("Alloy_Int_Object_Array", true);
	
	public static final AlloyVariable OBJECT_ARRAY_CONTENTS_FIELD_VARIABLE = new AlloyVariable("java_lang_ObjectArray_contents", false);
	public static final AlloyVariable PRIMED_OBJECT_ARRAY_CONTENTS_FIELD_VARIABLE = new AlloyVariable("java_lang_ObjectArray_contents", true);
	
	public static final ExprVariable OBJECT_ARRAY_CONTENTS_FIELD_EXPRESSION = buildExprVariable(OBJECT_ARRAY_CONTENTS_FIELD_VARIABLE);
	public static final ExprVariable PRIMED_OBJECT_ARRAY_CONTENTS_FIELD_EXPRESSION = buildExprVariable(PRIMED_OBJECT_ARRAY_CONTENTS_FIELD_VARIABLE);

	public static final AlloyVariable INT_ARRAY_CONTENTS_FIELD_VARIABLE = new AlloyVariable("java_lang_IntArray_contents", false);
	public static final AlloyVariable PRIMED_INT_ARRAY_CONTENTS_FIELD_VARIABLE = new AlloyVariable("java_lang_IntArray_contents", true);

	public static final ExprVariable INT_ARRAY_CONTENTS_FIELD_EXPRESSION = buildExprVariable(INT_ARRAY_CONTENTS_FIELD_VARIABLE);
	public static final ExprVariable PRIMED_INT_ARRAY_CONTENTS_FIELD_EXPRESSION = buildExprVariable(PRIMED_INT_ARRAY_CONTENTS_FIELD_VARIABLE);

	public static final AlloyVariable CHAR_ARRAY_CONTENTS_FIELD_VARIABLE = new AlloyVariable("java_lang_CharArray_contents", false);
	public static final AlloyVariable PRIMED_CHAR_ARRAY_CONTENTS_FIELD_VARIABLE = new AlloyVariable("java_lang_CharArray_contents", true);

	public static final ExprVariable CHAR_ARRAY_CONTENTS_FIELD_EXPRESSION = buildExprVariable(CHAR_ARRAY_CONTENTS_FIELD_VARIABLE);
	public static final ExprVariable PRIMED_CHAR_ARRAY_CONTENTS_FIELD_EXPRESSION = buildExprVariable(PRIMED_CHAR_ARRAY_CONTENTS_FIELD_VARIABLE);

	public static final AlloyVariable LONG_ARRAY_CONTENTS_FIELD_VARIABLE = new AlloyVariable("java_lang_LongArray_contents", false);
	public static final AlloyVariable PRIMED_LONG_ARRAY_CONTENTS_FIELD_VARIABLE = new AlloyVariable("java_lang_LongArray_contents", true);

	public static final ExprVariable LONG_ARRAY_CONTENTS_FIELD_EXPRESSION = buildExprVariable(LONG_ARRAY_CONTENTS_FIELD_VARIABLE);
	public static final ExprVariable PRIMED_LONG_ARRAY_CONTENTS_FIELD_EXPRESSION = buildExprVariable(PRIMED_LONG_ARRAY_CONTENTS_FIELD_VARIABLE);

//mfrias 16-03-2013: adding java_util_set support in specs
	public static final AlloyVariable JAVA_UTIL_SET_ELEMS_FIELD_VARIABLE = new AlloyVariable("java_util_Set_elems", false);
	public static final AlloyVariable PRIMED_JAVA_UTIL_SET_ELEMS_FIELD_VARIABLE = new AlloyVariable("java_util_Set_elems", true);
	
	public static final ExprVariable JAVA_UTIL_SET_ELEMS_FIELD_EXPRESSION = buildExprVariable(JAVA_UTIL_SET_ELEMS_FIELD_VARIABLE);
	public static final ExprVariable PRIMED_JAVA_UTIL_SET_ELEMS_FIELD_EXPRESSION = buildExprVariable(PRIMED_JAVA_UTIL_SET_ELEMS_FIELD_VARIABLE);

	
	public static final AlloyVariable OBJECT_ARRAY_LENGTH_FIELD_VARIABLE = new AlloyVariable("java_lang_ObjectArray_length", false);
	public static final AlloyVariable PRIMED_OBJECT_ARRAY_LENGTH_FIELD_VARIABLE = new AlloyVariable("java_lang_ObjectArray_length", true);
	
	public static final ExprVariable OBJECT_ARRAY_LENGTH_FIELD_EXPRESSION = buildExprVariable(OBJECT_ARRAY_LENGTH_FIELD_VARIABLE);
	public static final ExprVariable PRIMED_OBJECT_ARRAY_LENGTH_FIELD_EXPRESSION = buildExprVariable(PRIMED_OBJECT_ARRAY_LENGTH_FIELD_VARIABLE);
	
	
	public static final AlloyVariable INT_ARRAY_LENGTH_FIELD_VARIABLE = new AlloyVariable("java_lang_IntArray_length", false);
	public static final AlloyVariable PRIMED_INT_ARRAY_LENGTH_FIELD_VARIABLE = new AlloyVariable("java_lang_IntArray_length", true);
	
	public static final ExprVariable INT_ARRAY_LENGTH_FIELD_EXPRESSION = buildExprVariable(INT_ARRAY_LENGTH_FIELD_VARIABLE);
	public static final ExprVariable PRIMED_INT_ARRAY_LENGTH_FIELD_EXPRESSION = buildExprVariable(PRIMED_INT_ARRAY_LENGTH_FIELD_VARIABLE);
	
	public static final AlloyVariable CHAR_ARRAY_LENGTH_FIELD_VARIABLE = new AlloyVariable("java_lang_CharArray_length", false);
	public static final AlloyVariable PRIMED_CHAR_ARRAY_LENGTH_FIELD_VARIABLE = new AlloyVariable("java_lang_CharArray_length", true);
	
	public static final ExprVariable CHAR_ARRAY_LENGTH_FIELD_EXPRESSION = buildExprVariable(CHAR_ARRAY_LENGTH_FIELD_VARIABLE);
	public static final ExprVariable PRIMED_CHAR_ARRAY_LENGTH_FIELD_EXPRESSION = buildExprVariable(PRIMED_CHAR_ARRAY_LENGTH_FIELD_VARIABLE);

	public static final AlloyVariable LONG_ARRAY_LENGTH_FIELD_VARIABLE = new AlloyVariable("java_lang_LongArray_length", false);
	public static final AlloyVariable PRIMED_LONG_ARRAY_LENGTH_FIELD_VARIABLE = new AlloyVariable("java_lang_LongArray_length", true);
	
	public static final ExprVariable LONG_ARRAY_LENGTH_FIELD_EXPRESSION = buildExprVariable(LONG_ARRAY_LENGTH_FIELD_VARIABLE);
	public static final ExprVariable PRIMED_LONG_ARRAY_LENGTH_FIELD_EXPRESSION = buildExprVariable(PRIMED_LONG_ARRAY_LENGTH_FIELD_VARIABLE);
	
	public static final ExprVariable ALLOY_INT_OBJECT_ARRAY_EXPRESSION = buildExprVariable(ALLOY_INT_OBJECT_ARRAY_VARIABLE);
	public static final ExprVariable PRIMED_ALLOY_INT_OBJECT_ARRAY_EXPRESSION = buildExprVariable(PRIMED_ALLOY_INT_OBJECT_ARRAY_VARIABLE);

	public static final ExprVariable MAP_ENTRIES = buildExprVariable("Map_entries");

	public static final ExprVariable LIST_CONTAINS = buildExprVariable("List_contains");

	static final String GET_UNUSED_OBJECT_ID = "getUnusedObject";

	static final String UPDATE_VARIABLE_ID = "updateVariable";

	private static final String HAVOC_VARIABLE_ID = "havocVariable";

	private static final String HAVOC_FIELD_ID = "havocField";
	
	private static final String HAVOC_LIST_SEQ_ID = "havocListSeq";
	
	private static final String HAVOC_ARRAY_CONTENTS_ID = "havocArrayContents";

	private static final String UPDATE_FIELD_ID = "updateField";

	private static final String UPDATE_ARRAY_ID = "updateArray";

	private static final String UPDATE_MAP_ID = "updateMap";

	private static final String UPDATE_LIST_ID = "updateList";

	private static final String HAVOC_VARIABLE_2_ID = "havocVariable2";

	private static final String HAVOC_VARIABLE_3_ID = "havocVariable3";

	private static final String HAVOC_FIELD_CONTENTS_ID = "havocFieldContents";
	
	
	public static InvokeAction getUnusedObject(AlloyVariable v) {
		return buildInvokeAction(null, GET_UNUSED_OBJECT_ID,
				buildExprVariable(v), USED_OBJECTS);
	}

	public static Assigment updateVariable(ExprVariable lvalue,
			AlloyExpression rvalue) {
		return new Assigment(lvalue, rvalue);
	}

	public static InvokeAction havocVariable(ExprVariable expression) {
		return buildInvokeAction(null, HAVOC_VARIABLE_ID, expression);
	}

	public static InvokeAction havocField(ExprJoin lvalue) {
		if (lvalue.getRight().getClass().equals(ExprVariable.class)) {
			ExprVariable field = (ExprVariable) lvalue.getRight();
			AlloyExpression from = lvalue.getLeft();

			return buildInvokeAction(null, HAVOC_FIELD_ID, field, from);
		} else
			throw new IllegalArgumentException();

	}
	

	public static InvokeAction havocListSeq(ExprJoin lvalue) {
		if (lvalue.getRight().getClass().equals(ExprVariable.class)) {
			ExprVariable field = (ExprVariable) lvalue.getRight();
			AlloyExpression from = lvalue.getLeft();

			return buildInvokeAction(null, HAVOC_LIST_SEQ_ID, from, field);
		} else
			throw new IllegalArgumentException();

	}

	
	public static InvokeAction havocArray(ExprVariable arrayVariable) {
		return buildInvokeAction(null, HAVOC_ARRAY_CONTENTS_ID, null);
	}

	public static Assigment updateField(ExprJoin lvalue, AlloyExpression rvalue) {
		if (lvalue.getRight().getClass().equals(ExprVariable.class)) {
			ExprVariable field = (ExprVariable) lvalue.getRight();
			AlloyExpression from = lvalue.getLeft();
			AlloyExpression to = rvalue;

			ExprVariable left = field;
			AlloyExpression right;
			if (JDynAlloyConfig.getInstance().getUseCustomRelationalOverride() == true) {
				right = JExpressionFactory.customRelationOverride(field, from,
						to);
			} else {
				right = new ExprOverride(field, new ExprProduct(from, to));
			}

			return new Assigment(left, right);
		} else
			throw new IllegalArgumentException();

	}

	
	public static InvokeAction updateArray(AlloyExpression array,
			AlloyExpression index, AlloyExpression elem) {
		return buildInvokeAction(null, UPDATE_ARRAY_ID, INT_ARRAY_CONTENTS_FIELD_EXPRESSION, array,
				index, elem);
	}

	
//	public static InvokeAction updateAlloyIntObjectArray(AlloyExpression array,
//			AlloyExpression index, AlloyExpression elem) {
//		return buildInvokeAction(null, UPDATE_ARRAY_ID, INT_ARRAY_CONTENTS_FIELD_EXPRESSION, array,
//				index, elem);
//	}
//	
//	public static InvokeAction updateAlloyIntArray(AlloyExpression array,
//			AlloyExpression index, AlloyExpression elem) {
//		return buildInvokeAction(null, UPDATE_ARRAY_ID, INT_ARRAY_CONTENTS_FIELD_EXPRESSION, array,
//				index, elem);
//	}

	public static InvokeAction updateMap(AlloyExpression oldMap,
			AlloyExpression newMap) {
		return buildInvokeAction(null, UPDATE_MAP_ID, MAP_ENTRIES, oldMap,
				newMap);
	}

	public static InvokeAction updateList(AlloyExpression oldList,
			AlloyExpression newList) {
		return buildInvokeAction(null, UPDATE_LIST_ID, LIST_CONTAINS, oldList,
				newList);
	}

	public static Object havocVariable2(ExprVariable exprVariable) {
		return buildInvokeAction(null, HAVOC_VARIABLE_2_ID, exprVariable);
	}
	
	public static Object havocVariable3(ExprVariable exprVariable) {
		return buildInvokeAction(null, HAVOC_VARIABLE_3_ID, exprVariable);
	}
	
//	public static Object havocAlloyIntObjectArrayContents(AlloyExpression expr) {
//		return buildInvokeAction(null, HAVOC_ARRAY_CONTENTS_ID, expr, new ExprVariable(new AlloyVariable("Int")), ALLOY_INT_OBJECT_ARRAY_EXPRESSION);
//	}

	public static Object havocArrayContents(AlloyExpression expr) {
		return buildInvokeAction(null, HAVOC_ARRAY_CONTENTS_ID, expr, new ExprVariable(new AlloyVariable("Int")), INT_ARRAY_CONTENTS_FIELD_EXPRESSION);
	}

	public static Object havocFieldContents(AlloyExpression left,
			ExprVariable right) {
		return buildInvokeAction(null, HAVOC_FIELD_CONTENTS_ID, left, right);
	}

}

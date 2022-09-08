/*
 * TACO: Translation of Annotated COde
 * Copyright (c) 2010 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
package ar.edu.jdynalloy.binding.callbinding;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import ar.edu.jdynalloy.JDynAlloyNotImplementedYetException;
import ar.edu.jdynalloy.JDynAlloySemanticException;
import ar.edu.jdynalloy.binding.symboltable.SymbolCell;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.factory.JSignatureFactory;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprComprehension;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIfCondition;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntLiteral;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntersection;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprOverride;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprProduct;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprSum;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;

public class ExpressionTypeResolver extends ExpressionVisitor {

	SymbolTable symbolTable;


	public ExpressionTypeResolver(SymbolTable symbolTable) {
		super();
		this.symbolTable = symbolTable;
	}

	public ExpressionTypeResolver(FormulaVisitor formulaVisitor,
			SymbolTable symbolTable) {
		super();
		this.formulaVisitor = formulaVisitor;
		this.symbolTable = symbolTable;
	}

	@Override
	public Object visit(ExprConstant exprConstant) {
		JType type;
		if (exprConstant.getConstantId().equals("null")) {
			type = new JType(JSignatureFactory.NULL.getSignatureId());
		} else if (exprConstant.getConstantId().startsWith("Char")) {
			type = new JType(JSignatureFactory.CHAR.getSignatureId());
		} else if (exprConstant.getConstantId().startsWith("String")) {
			type = new JType("java_lang_String");
		} else if (exprConstant.getConstantId().startsWith("false")) {
			type = new JType(JSignatureFactory.BOOLEAN.getSignatureId());
		} else if (exprConstant.getConstantId().startsWith("true")) {
			type = new JType(JSignatureFactory.BOOLEAN.getSignatureId());

		} else {
			// DOB::debe ser la constante que representa el nombre de la clase
			type = JType.parse(exprConstant.getConstantId());
			// throw new DynJML4AlloySemanticException("Constant not supported"
			// + exprConstant.toString());
		}
		return type;
	}

	@Override
	public Object visit(ExprFunction n) {

		Object ret_val = super.visit(n);

		// Functions defined in alloy built in predicate file utils/integer.als
		Set<String> integerAlloyFunctionsSet = new HashSet<String>();
		Collections.addAll(integerAlloyFunctionsSet, "add", "sub", "mul",
				"div", "rem", "negate", "fun_list_size", "arrayLength",
				"fun_map_size", "fun_set_size","sshr", 
				"fun_alloy_int_java_util_set_size");

		Set<String> booleanAlloyFunctionsSet = new HashSet<String>();
		Collections.addAll(booleanAlloyFunctionsSet, "fun_list_empty",
				"fun_set_contains", "fun_map_is_empty", "fun_map_contains_key",
				"fun_not_empty_set", "fun_set_is_empty", "fun_map_clear", 
				"fun_java_util_set_contains");

		Set<String> univAlloyFunctionsSet = new HashSet<String>();
		Collections.addAll(univAlloyFunctionsSet, "fun_list_get",
				"fun_set_get", "fun_map_remove", "fun_map_put", "fun_map_get"
				);

		Set<String> seqFunctionsSet = new HashSet<String>();
		Collections.addAll(seqFunctionsSet,
				"fun_list_add", "fun_list_remove");

		
		Set<String> javaPrimitiveCharValueFunctionsSet = new HashSet<String>();
		Collections.addAll(javaPrimitiveCharValueFunctionsSet,"fun_narrowing_cast_long_to_char");
		
		Set<String> javaPrimitiveIntegerValueFunctionsSet = new HashSet<String>();
		Collections.addAll(javaPrimitiveIntegerValueFunctionsSet,
				"fun_java_primitive_integer_value_add",
				"fun_java_primitive_integer_value_sub",
				"fun_java_primitive_integer_value_negate",
				"fun_java_primitive_integer_value_div",
				"fun_java_primitive_integer_value_mul",
				"fun_java_primitive_integer_value_rem",
				"fun_java_primitive_integer_value_size_of",
				"fun_java_primitive_integer_value_sshr",
				"fun_java_primitive_integer_value_java_util_set_size",
				"fun_java_primitive_char_value_addCharCharToJavaPrimitiveIntegerValue",
				"fun_java_primitive_char_value_subCharCharToJavaPrimitiveIntegerValue",
				"fun_java_primitive_char_value_addCharIntToJavaPrimitiveIntegerValue",
				"fun_java_primitive_char_value_subCharIntToJavaPrimitiveIntegerValue",
				"fun_java_primitive_char_value_addIntCharToJavaPrimitiveIntegerValue",
				"fun_java_primitive_char_value_subIntCharToJavaPrimitiveIntegerValue",
				"fun_cast_char_to_int",
				"fun_narrowing_cast_int_to_char",
				"fun_narrowing_cast_long_to_int");

		Set<String> javaPrimitiveLongValueFunctionsSet = new HashSet<String>();
		Collections.addAll(javaPrimitiveLongValueFunctionsSet,
				"fun_java_primitive_long_value_add",
				"fun_java_primitive_long_value_sub",
				"fun_java_primitive_long_value_negate",
				"fun_java_primitive_long_value_div",
				"fun_java_primitive_long_value_mul",
				"fun_java_primitive_long_value_rem",
				"fun_java_primitive_long_value_size_of",
				"fun_java_primitive_long_value_sshr",
				"fun_cast_char_to_long",
				"fun_cast_int_to_long",
				"fun_java_primitive_char_value_addCharLongToJavaPrimitiveLongValue",
				"fun_java_primitive_char_value_addLongCharToJavaPrimitiveLongValue",
				"fun_java_primitive_char_value_subCharLongToJavaPrimitiveLongValue",
				"fun_java_primitive_char_value_subLongCharToJavaPrimitiveLongValue",
				"fun_long_int_to_long_add",
				"fun_int_long_to_long_add",
				"fun_long_int_to_long_sub");

		if (javaPrimitiveLongValueFunctionsSet.contains(n.getFunctionId())) {
			return JSignatureFactory.JAVA_PRIMITIVE_LONG_VALUE;
		} else if (javaPrimitiveIntegerValueFunctionsSet.contains(n.getFunctionId())) {
			return JSignatureFactory.JAVA_PRIMITIVE_INTEGER_VALUE;
		} else if (javaPrimitiveCharValueFunctionsSet.contains(n.getFunctionId())) {
			return JSignatureFactory.JAVA_PRIMITIVE_INTEGER_VALUE;
		} else if (integerAlloyFunctionsSet.contains(n.getFunctionId())) {
			return JSignatureFactory.INT.getType();
		} else if (booleanAlloyFunctionsSet.contains(n.getFunctionId())) {
			return JSignatureFactory.BOOLEAN.getType();
		} else if (seqFunctionsSet.contains(n.getFunctionId())){	
			return JSignatureFactory.ALLOY_SEQ.getType();
		} else if (univAlloyFunctionsSet.contains(n.getFunctionId())) {
			return new JType("java_lang_Object");
		} else if (n.getFunctionId().equalsIgnoreCase(
				JExpressionFactory.FUN_REACH)) {
			// return new JType("set univ");
			return new JType("org_jmlspecs_models_JMLObjectSet");
		} else if (n.getFunctionId().equalsIgnoreCase(
				JExpressionFactory.FUN_REACH_JMLOBJECT_SET)) {
			return new JType("org_jmlspecs_models_JMLObjectSet");
		} else if (n.getFunctionId().equalsIgnoreCase("arrayAccess")) {
			return new JType("java_lang_Object");
		} else if (n.getFunctionId().equalsIgnoreCase("arrayElements")) {
			return JType.parse("univ");
		} else if (n.getFunctionId().equalsIgnoreCase("fun_set_difference")) {
			return new JType("java_lang_Set");
		} else if (n.getFunctionId().equalsIgnoreCase("fun_set_intersection")) {
			return new JType("set univ");
		} else if (n.getFunctionId().equalsIgnoreCase("fun_reflexive_closure")) {
			return JType.parse("univ->univ");
		} else if (n.getFunctionId().equalsIgnoreCase("fun_closure")) {
			return JType.parse("univ->univ");
		} else if (n.getFunctionId().equalsIgnoreCase("fun_set_add")) {
			return JType.parse("set univ");
		} else if (n.getFunctionId().equalsIgnoreCase("fun_set_remove")) {
			return JType.parse("set univ");
		} else if (n.getFunctionId().equalsIgnoreCase("fun_rel_difference")) {
			return JType.parse("univ->univ");
		} else if (n.getFunctionId().equalsIgnoreCase("fun_rel_add")) {
			return JType.parse("univ->univ");
		} else if (n.getFunctionId().equalsIgnoreCase("Not")) {
			return JSignatureFactory.BOOLEAN.getType();			
		} else if (n.getFunctionId().equalsIgnoreCase("Or")) {
			return JSignatureFactory.BOOLEAN.getType();			
		} else if (n.getFunctionId().equalsIgnoreCase("And")) {
			return JSignatureFactory.BOOLEAN.getType();			
		} else if (n.getFunctionId().equalsIgnoreCase("fun_java_lang_float_isNaN")) {
			return JSignatureFactory.BOOLEAN.getType();
		} else if (n.getFunctionId().contains("_equals")) {
			return JSignatureFactory.BOOLEAN.getType();
		} else if (n.getReturnType() != null){
			return new JType(n.getReturnType());
		}	else {
			// other functions aren't implemented
			throw new JDynAlloyNotImplementedYetException(
					"Function not implemented for Type Resolution: "
							+ n.getFunctionId());
		}

	}

	@Override
	public Object visit(ExprIntLiteral n) {
		return new JType(JSignatureFactory.INT.getSignatureId());
	}

	@Override
	public Object visit(ExprJoin n) {

		Object ret_val = super.visit(n);

		// things like "object1.field1.otherField" are not supported yet
		JType leftExpression = (JType) n.getLeft().accept(this);

		if (n.getRight() instanceof ExprVariable) {
			ExprVariable rightExpression = (ExprVariable) n.getRight();
			String moduleName = leftExpression.singletonFrom();
			String fieldName = rightExpression.getVariable().getVariableId()
					.getString();
			JType fieldType = symbolTable.lookupField(moduleName, fieldName);
			return fieldType;
		} else if (n.getRight() instanceof ExprFunction) {

			JType rightExpression = (JType) n.getRight().accept(this);
			if (rightExpression.isBinaryRelation()) {

				StringBuffer buff = new StringBuffer();
				for (String string : rightExpression.to()) {
					if (!buff.toString().isEmpty())
						buff.append("+");
					buff.append(string);
				}
				JType image = JType.parse(buff.toString());
				return image;
			} else
				throw new JDynAlloySemanticException(
						"Invalid 'Join' expression. Only variable expression are supported at right size in join expression");
		} else if (n.getRight() instanceof ExprJoin) {

			JType type = (JType) n.getRight().accept(this);

			return type;
		} else if (n.getRight() instanceof ExprUnion) {

			ExprUnion union = (ExprUnion) n.getRight();
			AlloyExpression left = union.getLeft();
			AlloyExpression right = union.getRight();

			JType leftType = (JType) left.accept(this);
			JType rightType = (JType) right.accept(this);

			if (!leftType.isBinaryRelation() && !rightType.isBinaryRelation()) {

				StringBuffer buff = new StringBuffer();
				Set<String> imageList = new HashSet<String>();
				imageList.addAll(leftType.from());
				imageList.addAll(rightType.from());
				for (String string : imageList) {
					if (!buff.toString().isEmpty())
						buff.append("+");
					buff.append(string);
				}
				JType image = JType.parse(buff.toString());
				return image;

			}
		} else if (n.getRight() instanceof ExprConstant) {

			if (n.getRight().equals(JExpressionFactory.UNIV_EXPRESSION)) {
				JType leftType = (JType) n.getLeft().accept(this);
				Set<String> from =leftType.from();
				return new JType(from);
			}
		} else if (n.getRight() instanceof ExprProduct) {
			//this should only happen when a static field is not accessed statically. This is allowed by the Java accesibility rules.
			//the RHS expression should be of the form (univ) -> ((Classfields).(staticField)). The resulting type should be that 
			//returned by the static field.
			return ((ExprProduct)n.getRight()).getRight().accept(this);
		}

		throw new JDynAlloySemanticException(
				"Invalid 'Join' expression. Only variable expression are supported at right side in join expression");
	}

	@Override
	public Object visit(ExprOverride n) {

		Object ret_val = super.visit(n);

		Object result = n.getLeft().accept(this);
		return result;
	}

	@Override
	public Object visit(ExprProduct n) {

		Object ret_val = super.visit(n);

		if (n.getLeft().equals(new ExprConstant(null, "none"))
				&& n.getRight().equals(new ExprConstant(null, "none"))) {
			JType type = JType.parse(n.toString());
			return type;
		} else {
			Vector<JType> vec = (Vector<JType>)ret_val;
			JType left = (JType)vec.get(0);
			JType right = (JType)vec.get(1);

			JType type = JType.parse(left.toString() + "->" + right.toString());

			return type;
		}
	}

	@Override
	public Object visit(ExprUnion n) {
		Object ret_val = super.visit(n);

		JType type = JType.parse(n.toString());
		return type;
	}

	@Override
	public Object visit(ExprVariable n) {
		SymbolCell symbolCell = symbolTable.lookup(n.getVariable()
				.getVariableId());
		if (symbolCell == null) {

			JType fieldType = symbolTable.lookupField(null, n.getVariable()
					.getVariableId().getString());

			if (fieldType == null)

				throw new JDynAlloySemanticException("Variable '"
						+ n.getVariable().getVariableId() + "' wasn't declared");

			return fieldType;
		}
		return symbolCell.getType();
	}

	@Override
	public Object visit(ExprSum e) {
		Object ret_val = super.visit(e);
		return new JType(JSignatureFactory.INT.getSignatureId());
	}

	@Override
	public Object visit(ExprIfCondition e) {
		super.visit(e);
		JType leftType = (JType) e.getLeft().accept(this);
		JType rightType = (JType) e.getLeft().accept(this);

		if (!leftType.equals(rightType)) {
			throw new JDynAlloySemanticException(
					"In ExprIfCondition node left expression and right expression type must be equals. Left="
							+ leftType + " Right=" + rightType);
		}

		return leftType;
	}

	@Override
	public Object visit(ExprComprehension e) {
		this.symbolTable.beginScope();
		for (String name : e.getNames()) {
			JType type = (JType) e.getSetOf(name).accept(this);
			this.symbolTable.insertLocal(new VariableId(name), type);
		}
		super.visit(e);
		this.symbolTable.endScope();
		return new JType(JSignatureFactory.BOOLEAN.getSignatureId());
	}

	@Override
	public Object visit(ExprIntersection e) {
		super.visit(e);
		return e.getLeft().accept(this);
	}

}

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

/**
 * Computes the {@link JType} of an Alloy expression in JDynAlloy: each {@code visit} returns the
 * type of its expression. Variables are looked up in the {@link SymbolTable} (local variables,
 * then fields by name), the field of a join by the type of its left side, constants by their
 * name, functions by fixed tables of the prelude's and the Java-arithmetic modules' functions or
 * by their declared return type. Used to type call arguments for the bindings
 * ({@link SemanticCheckVisitor} and the call collectors), in the relevancy analysis
 * ({@code RelevantAnalysisExpressionTypeResolver} extends it) and in {@code FieldModifiesCase}.
 *
 * <p>Several expressions take the type of their left operand alone: override, intersection and
 * the {@code if}-expression; a product is the relation between its operands' types; a sum and an
 * integer literal are {@code Int}; a set comprehension is typed {@code boolean}, not as a set.
 */
public class ExpressionTypeResolver extends ExpressionVisitor {

	SymbolTable symbolTable;


	/**
	 * Creates a resolver without formula visitor: the formulas inside expressions (the condition of
	 * an {@code if}-expression, the body of a comprehension) are not visited.
	 *
	 * @param symbolTable the variables in scope and the fields
	 */
	public ExpressionTypeResolver(SymbolTable symbolTable) {
		super();
		this.symbolTable = symbolTable;
	}

	/**
	 * Creates a resolver that visits the formulas inside expressions with {@code formulaVisitor}.
	 *
	 * @param formulaVisitor the visitor for nested formulas, or null
	 * @param symbolTable the variables in scope and the fields
	 */
	public ExpressionTypeResolver(FormulaVisitor formulaVisitor,
			SymbolTable symbolTable) {
		super();
		this.formulaVisitor = formulaVisitor;
		this.symbolTable = symbolTable;
	}

	/**
	 * Types a constant by its name: {@code null}; a name starting with {@code Char} is
	 * {@code char}, with {@code String} {@code java_lang_String}, with {@code true} or
	 * {@code false} {@code boolean}; any other name is parsed as a type (a signature, such as a
	 * literal or a class).
	 *
	 * @param exprConstant the constant
	 * @return its type
	 */
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

	/**
	 * Types a function call by its name, after visiting its arguments: the prelude's integer
	 * functions ({@code add}, {@code sub}, ..., the sizes, {@code arrayLength}) are {@code Int}; the
	 * Java-arithmetic ones are {@code JavaPrimitiveLongValue} or {@code JavaPrimitiveIntegerValue}
	 * (and so is {@code fun_narrowing_cast_long_to_char}, although
	 * {@code JSignatureFactory.JAVA_PRIMITIVE_CHAR_VALUE} exists); the boolean ones, and any name
	 * containing {@code _equals}, are {@code boolean}; the collection accessors and
	 * {@code arrayAccess} are {@code java_lang_Object}; {@code fun_reach} is
	 * {@code org_jmlspecs_models_JMLObjectSet}; closures and relation operations are
	 * {@code univ->univ}; {@code fun_set_difference} is {@code java_lang_Set} (not
	 * {@code java_util_Set}); any other function has its declared return type.
	 *
	 * @param n the function call
	 * @return its type
	 * @throws JDynAlloyNotImplementedYetException for a function that is in no table and has no
	 *         return type ("Function not implemented for Type Resolution")
	 */
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

	/**
	 * Types a join {@code l.r} from the type of {@code l}: if {@code r} is a variable, the type of
	 * field {@code r} of {@code l}'s signature ({@code singletonFrom()}); if a function, the image of
	 * its type, which must be a relation; if a join, the type of {@code r} alone; if a union of two
	 * types that are not relations, the union of their domains; if {@code univ}, the domain of
	 * {@code l}; if a product (a static field read through an instance), the type of its right side.
	 *
	 * @param n the join
	 * @return its type
	 * @throws JDynAlloySemanticException for any other right side ("Invalid 'Join' expression"),
	 *         or if the field is unknown
	 */
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

	/**
	 * Types a union by parsing its text as a type, which works for unions of signature names such
	 * as {@code A+null}.
	 *
	 * @param n the union
	 * @return the parsed type
	 */
	@Override
	public Object visit(ExprUnion n) {
		Object ret_val = super.visit(n);

		JType type = JType.parse(n.toString());
		return type;
	}

	/**
	 * Types a variable: from the innermost scope that declares it, otherwise as the field of that
	 * name in any module ({@code lookupField(null, name)}).
	 *
	 * @param n the variable
	 * @return its type
	 * @throws JDynAlloySemanticException if it is neither ("Field doesn't exist", from the symbol
	 *         table)
	 */
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

	/**
	 * Types an {@code if}-expression as its left branch. It is meant to check that both branches
	 * have the same type ("left expression and right expression type must be equals"), but it types
	 * the left branch twice, so the check never fails.
	 *
	 * @param e the expression
	 * @return the left branch's type
	 */
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

	/**
	 * Declares the comprehension's variables, typed by their bounds, in a new scope of the symbol
	 * table, visits the comprehension, and closes the scope.
	 *
	 * @param e the comprehension
	 * @return {@code boolean}, not the type of the set
	 */
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

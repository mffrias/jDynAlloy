package ar.edu.jdynalloy.factory;

import static ar.edu.jdynalloy.factory.JExpressionFactory.RETURN_EXPRESSION;
import static ar.edu.jdynalloy.factory.JExpressionFactory.THIS_EXPRESSION;
import static ar.edu.jdynalloy.factory.JExpressionFactory.THIS_VARIABLE;
import static ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable.buildExprVariable;
import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.ast.JAssignment;
import ar.edu.jdynalloy.ast.JBlock;
import ar.edu.jdynalloy.ast.JIfThenElse;
import ar.edu.jdynalloy.ast.JSkip;
import ar.edu.jdynalloy.ast.JStatement;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.ast.JWhile;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntLiteral;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public abstract class JDynAlloyFactory {

	public static JAssignment assign(AlloyExpression lvalue,
			AlloyExpression rvalue) {
		return new JAssignment(lvalue, rvalue);
	}

	public static JAssignment assign(AlloyExpression lvalue,
			AlloyVariable rvalue) {
		return new JAssignment(lvalue, buildExprVariable(rvalue));
	}

	public static JAssignment assign(AlloyVariable lvalue,
			AlloyExpression rvalue) {
		return new JAssignment(buildExprVariable(lvalue), rvalue);
	}

	public static JAssignment assign(AlloyVariable lvalue, AlloyVariable rvalue) {
		return new JAssignment(buildExprVariable(lvalue),
				buildExprVariable(rvalue));
	}

	public static JBlock block(JStatement... ps) {
		return new JBlock(ps);
	}

	public static JIfThenElse ifThenElse(AlloyFormula condition,
			JStatement trueStmt, JStatement falseStmt) {
		return new JIfThenElse(condition, trueStmt, falseStmt, null);
	}

	public static JIfThenElse ifThenElse(AlloyFormula condition,
			JStatement trueStmt) {
		return ifThenElse(condition, trueStmt, new JSkip());
	}

	public static JVariableDeclaration declare(AlloyVariable v, JType t) {
		return new JVariableDeclaration(v, t);
	}

	public static JWhile doWhile(AlloyFormula condition, JStatement body,
			String branchId) {
		return new JWhile(condition, body, null, branchId);
	}

	public static JStatement buildConstructor(JType type, JStatement body) {
		JBlock block = block(declare(THIS_VARIABLE, type), body, assign(
				RETURN_EXPRESSION, THIS_EXPRESSION));
		return block;
	}

	public static JStatement initializeThrow() {
		JAssignment initThrow = assign(JExpressionFactory.THROW_EXPRESSION,
				JExpressionFactory.NULL_EXPRESSION);
		return initThrow;
	}

	public static JStatement initializeObjectReturn() {
		JAssignment initReturn = assign(JExpressionFactory.RETURN_EXPRESSION,
				JExpressionFactory.NULL_EXPRESSION);
		return initReturn;
	}

	public static JStatement initializeNumericReturn() {
		AlloyExpression zeroExpression = new ExprIntLiteral(0);
		JAssignment initReturn = assign(JExpressionFactory.RETURN_EXPRESSION,
				zeroExpression);
		return initReturn;
	}

	public static JStatement initializeBooleanReturn() {
		JAssignment initReturn = assign(JExpressionFactory.RETURN_EXPRESSION,
				JExpressionFactory.FALSE_EXPRESSION);
		return initReturn;
	}

	public static JStatement initializeExitStatementReached() {
		JAssignment initReturnReached = assign(
				JExpressionFactory.EXIT_REACHED_VARIABLE,
				JExpressionFactory.FALSE_EXPRESSION);
		return initReturnReached;
	}

	public static final JVariableDeclaration THROW_DECLARATION = new JVariableDeclaration(
			JExpressionFactory.THROW_VARIABLE, JType
					.parse((JDynAlloyConfig.getInstance()
							.getUseQualifiedNamesForJTypes() ? "java_lang_"
							: "")
							+ "Throwable+null"));

	public static JAssignment decrement(AlloyExpression operandExpr) {
		return assign(operandExpr, JExpressionFactory.alloy_int_sub(operandExpr,
				new ExprIntLiteral(1)));
	}
	
	public static JAssignment alloy_int_inc(AlloyExpression operandExpr) {
		return assign(operandExpr, JExpressionFactory.alloy_int_add(operandExpr,
				new ExprIntLiteral(1)));
	}
	
}

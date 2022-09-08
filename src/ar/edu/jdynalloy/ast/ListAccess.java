package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

public final class ListAccess {

	public static boolean isListAccess(AlloyExpression expr) {
		if (expr instanceof ExprJoin) {
			ExprJoin exprJoin = (ExprJoin) expr;
			if (exprJoin.getRight() instanceof ExprVariable) {
				ExprVariable field = (ExprVariable) exprJoin.getRight();
				return field.getVariable().equals(
						new AlloyVariable("List_contains"));
			}
		}
		return false;
	} 

	public static boolean isListAsSeqAccess(AlloyExpression expr) {
		if (expr instanceof ExprJoin) {
			ExprJoin exprJoin = (ExprJoin) expr;
			if (exprJoin.getRight() instanceof ExprVariable) {
				ExprVariable field = (ExprVariable) exprJoin.getRight();
				return field.getVariable().equals(
						new AlloyVariable("java_util_List_seq"));
			}
		}
		return false;
	}

	
	public static AlloyExpression getList(AlloyExpression e) {
		if (!isListAccess(e))
			throw new IllegalArgumentException(e.toString()
					+ " is not a list access");

		ExprJoin j = (ExprJoin) e;
		return j.getLeft();
	}

}

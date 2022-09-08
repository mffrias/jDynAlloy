package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

public final class MapAccess {

	public static boolean isMapAccess(AlloyExpression expr) {
		if (expr instanceof ExprJoin) {
			ExprJoin exprJoin = (ExprJoin) expr;
			if (exprJoin.getRight() instanceof ExprVariable) {
				ExprVariable field = (ExprVariable) exprJoin.getRight();
				return field.getVariable().equals(
						new AlloyVariable("Map_entries"));
			}
		}
		return false;
	}

	public static AlloyExpression getMap(AlloyExpression e) {
		if (!isMapAccess(e))
			throw new IllegalArgumentException(e.toString()
					+ " is not a map access");

		ExprJoin j = (ExprJoin) e;
		return j.getLeft();
	}

}

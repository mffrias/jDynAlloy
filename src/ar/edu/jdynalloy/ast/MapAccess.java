package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

/**
 * Recognizes the expressions that access the entries of a map, for {@code JDynAlloyXlatorVisitor}
 * (assignments, translated into the prelude's {@code updateMap}, and {@code havoc}, which does
 * not support them) and {@code ModifiesTableBuilder}: a join whose right side is the variable
 * {@code Map_entries}. Only static methods; no state.
 *
 * <p>Unclear: TACO's model of maps uses the field {@code java_util_Map_entries}, and its fields
 * are named {@code <class>_<field>} with qualified class names, so nothing appears to produce
 * {@code Map_entries}. Whether this class is still needed is an open question.
 */
public final class MapAccess {

	/**
	 * Returns whether {@code expr} is {@code m.Map_entries}: a join whose right side is the
	 * variable {@code Map_entries}. The left side is not checked.
	 *
	 * @param expr an expression
	 * @return true for such a join
	 */
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

	/**
	 * Returns the map of a {@code Map_entries} access: the left side of the join.
	 *
	 * @param e an expression for which {@link #isMapAccess} is true
	 * @return the left side of {@code e}
	 * @throws IllegalArgumentException if {@link #isMapAccess} is false for {@code e} ("... is
	 *        not a map access")
	 */
	public static AlloyExpression getMap(AlloyExpression e) {
		if (!isMapAccess(e))
			throw new IllegalArgumentException(e.toString()
					+ " is not a map access");

		ExprJoin j = (ExprJoin) e;
		return j.getLeft();
	}

}

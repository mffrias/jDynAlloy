package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

/**
 * Recognizes the expressions that access the contents of a list, for
 * {@code JDynAlloyXlatorVisitor} (assignments and {@code havoc}) and
 * {@code ModifiesTableBuilder}: a join whose right side is the variable {@code List_contains}
 * ({@link #isListAccess}), or {@code java_util_List_seq} ({@link #isListAsSeqAccess}). Only
 * static methods; no state.
 *
 * <p>Unclear: TACO's model of lists uses the field {@code java_util_List_seq}, and its fields are
 * named {@code <class>_<field>} with qualified class names, so nothing appears to produce
 * {@code List_contains}; and the prelude's declaration of {@code updateList}, into which an
 * assignment to it would be translated, is commented out in {@code JPreludeFactory}. Whether
 * {@link #isListAccess} is still needed is an open question.
 */
public final class ListAccess {

	/**
	 * Returns whether {@code expr} is {@code l.List_contains}: a join whose right side is the
	 * variable {@code List_contains}. The left side is not checked.
	 *
	 * @param expr an expression
	 * @return true for such a join
	 */
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

	/**
	 * Returns whether {@code expr} is {@code l.java_util_List_seq}: a join whose right side is
	 * the variable {@code java_util_List_seq}, the field of TACO's model of lists. The left side
	 * is not checked.
	 *
	 * @param expr an expression
	 * @return true for such a join
	 */
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

	
	/**
	 * Returns the list of a {@code List_contains} access: the left side of the join.
	 *
	 * @param e an expression for which {@link #isListAccess} is true
	 * @return the left side of {@code e}
	 * @throws IllegalArgumentException if {@link #isListAccess} is false for {@code e} ("... is
	 *        not a list access")
	 */
	public static AlloyExpression getList(AlloyExpression e) {
		if (!isListAccess(e))
			throw new IllegalArgumentException(e.toString()
					+ " is not a list access");

		ExprJoin j = (ExprJoin) e;
		return j.getLeft();
	}

}

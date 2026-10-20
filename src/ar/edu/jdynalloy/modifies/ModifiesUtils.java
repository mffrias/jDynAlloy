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
package ar.edu.jdynalloy.modifies;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;

/**
 * Builds the frame condition {@code r.f = r.f'}: the field {@code f} of {@code r} keeps its value
 * (the primed variable is the value after the program). Used by {@link FieldModifiesCase} and by
 * an unused private method of {@link ReplaceModifiesModuleVisitor}; neither runs (see
 * {@link AbstractModifiesCase}). No state.
 */
public class ModifiesUtils {
	
	/**
	 * Utilitiy static class!
	 * @param reciberName
	 * @param variableName
	 * @return
	 */
	private ModifiesUtils() {
		
	}
	
	/**
	 * Returns {@code reciberName.variableName = reciberName.variableName'}: only the field is primed,
	 * not the receiver.
	 *
	 * @param reciberName the receiver variable
	 * @param variableName the field
	 * @return the equality
	 */
	public static AlloyFormula createFormulaFieldValueEqualsToOldFieldValue(String reciberName, String variableName) {
		AlloyFormula formula;
		AlloyExpression left = fieldReferenceExpression(reciberName, variableName, false);

		AlloyExpression right = fieldReferenceExpression(reciberName, variableName, true);

		formula = new EqualsFormula(left, right);
		return formula;
	}

	/**
	 * Returns the join {@code reciberName.variableName}, with the field primed if asked; the receiver
	 * is never primed (the line that primed it too is commented out).
	 *
	 * @param reciberName the receiver variable
	 * @param variableName the field
	 * @param primed whether to prime the field
	 * @return the join
	 */
	public static AlloyExpression fieldReferenceExpression(String reciberName, String variableName, boolean primed) {
		ExprVariable exprVariable = ExprVariable.buildExprVariable(new AlloyVariable(variableName, primed));
		// ExprVariable reciber = ExprVariable.buildExprVariable(new
		// AlloyVariable(reciberName, primed));

		ExprVariable reciber = ExprVariable.buildExprVariable(new AlloyVariable(reciberName, false));
		AlloyExpression fieldReference = new ExprJoin(reciber, exprVariable);
		return fieldReference;
	}

}

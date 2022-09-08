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
package ar.edu.jdynalloy.buffer;

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public class Represents {

	public Represents(AlloyExpression expr, JType exprType,
			AlloyFormula formula) {
		super();
		this.expr = expr;
		this.exprType = exprType;
		this.formula = formula;
	}

	public AlloyExpression expr;

	public JType exprType;

	public AlloyFormula formula;

	public AlloyExpression getExpr() {
		return expr;
	}

	public void setExpr(AlloyExpression expr) {
		this.expr = expr;
	}

	public JType getExprType() {
		return exprType;
	}

	public void setExprType(JType exprType) {
		this.exprType = exprType;
	}

	public AlloyFormula getFormula() {
		return formula;
	}

	public void setFormula(AlloyFormula formula) {
		this.formula = formula;
	}
}

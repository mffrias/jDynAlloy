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

import java.util.HashSet;
import java.util.Set;

import ar.edu.jdynalloy.JDynAlloyNotImplementedYetException;
import ar.edu.jdynalloy.ast.JModifies;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public abstract class AbstractModifiesCase {

	public abstract boolean accepts(JModifies modifies);
	public abstract AlloyFormula generateFormula(JModifies modifies);
	private Set<String> modifiableFieldSet;
	private SymbolTable symbolTable;
	
	public AbstractModifiesCase(SymbolTable symbolTable) {
		setUnmodificableFieldSet(new HashSet<String>());
		this.setSymbolTable(symbolTable);
	}
	
	public void setSymbolTable(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}
	
	public SymbolTable getSymbolTable() {
		return symbolTable;
	}
	
	public void setUnmodificableFieldSet(Set<String> modifiableFieldSet) {
		this.modifiableFieldSet = modifiableFieldSet;
	}
	
	public Set<String> getModifiableFieldSet() {
		return modifiableFieldSet;
	}
	
	protected AlloyExpression primeExpression(AlloyExpression expression) {

		if (expression instanceof ExprVariable) {
			ExprVariable exprVariable = (ExprVariable) expression;
			return new ExprVariable(new AlloyVariable(exprVariable.getVariable().getVariableId(),true));
		} else if (expression instanceof ExprJoin) {
			ExprJoin exprJoin = (ExprJoin) expression;
			AlloyExpression left = primeExpression(exprJoin.getLeft());
			AlloyExpression right = primeExpression(exprJoin.getRight());
			return ExprJoin.join(left, right);
		} else if (expression instanceof ExprConstant) {
			return expression;
		} else {
			throw new JDynAlloyNotImplementedYetException();
		}
		
	}

}

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

import java.util.Collections;
import java.util.List;

import ar.edu.jdynalloy.ast.JModifies;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula.Operator;

public class FieldArraysModifiesCase extends AbstractModifiesCase {
	
	private static final String MODIFIES_QUANTIFIER_VARIABLE = "mod_array_q";
	private static int quantifiersCount = 0;

	
	public FieldArraysModifiesCase(SymbolTable symbolTable) {
		super(symbolTable);
	}

	@Override
	public boolean accepts(JModifies modifies) {
		if (modifies.isModifiesEverything()) {
			return false;
		}

		return acceptLocation(modifies.getLocation());
	}

	@Override
	public AlloyFormula generateFormula(JModifies modifies) {
		// This ExprFormula should be: arrayAccess(Object_Array, variableName, index)
		
		ExprFunction exprFunction = (ExprFunction) modifies.getLocation();
		
		// Gets the elems of the array which is represented on position '0' by 'Object_Array'
		AlloyExpression arrayElems = exprFunction.getParameters().get(0);
		// Gets the array variable name which is represented on position '1'
		AlloyExpression arrayVariable = exprFunction.getParameters().get(1);
		
		String modifiesVarName = MODIFIES_QUANTIFIER_VARIABLE + "_" + quantifiersCount;
		quantifiersCount++;
		List<String> names = Collections.<String> singletonList(modifiesVarName);
		
		AlloyExpression type;
		if (arrayVariable.toString().contains("java_lang_IntArray")) {
			type = ExprConstant.buildExprConstant("java_lang_IntArray");
		} else if (arrayVariable.toString().contains("java_lang_LongArray")) {
			type = ExprConstant.buildExprConstant("java_lang_LongArray");
		} else if (arrayVariable.toString().contains("java_lang_CharArray")) {
			type = ExprConstant.buildExprConstant("java_lang_CharArray");
		} else {
			type = ExprConstant.buildExprConstant("java_lang_ObjectArray");
		}

		List<AlloyExpression> typeSets = Collections.<AlloyExpression> singletonList(type);
		
		AlloyExpression quantifiedVariable = ExprVariable.buildExprVariable(modifiesVarName);
		
		ExprJoin exprJoin = new ExprJoin(quantifiedVariable, arrayElems);
		ExprJoin primedExprJoin = new ExprJoin(quantifiedVariable, primeExpression(arrayElems));
		
		AlloyFormula neqExpr = new NotFormula(new EqualsFormula(primedExprJoin, exprJoin));
		
		AlloyFormula eqExpr = new EqualsFormula(quantifiedVariable, arrayVariable);
		
		AlloyFormula impliesFormula = new ImpliesFormula(neqExpr, eqExpr);
		
		AlloyFormula modifiesFormula = new QuantifiedFormula(Operator.FOR_ALL, names, typeSets, impliesFormula);
		
		return modifiesFormula;
	}
	
	private boolean acceptLocation(AlloyExpression location) {
		if (location instanceof ExprFunction) {
			ExprFunction exprFunction = (ExprFunction) location;
			return (exprFunction.getFunctionId().equals("arrayAccess"));
		}

		return false;
	}
}

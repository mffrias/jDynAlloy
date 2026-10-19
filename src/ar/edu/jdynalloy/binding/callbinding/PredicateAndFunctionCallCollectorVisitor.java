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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ar.edu.jdynalloy.JDynAlloyException;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateCallAlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula;

/**
 * The formula visitor that {@link SemanticCheckVisitor} runs on each specification formula: it
 * collects the {@code callSpec}/{@code staticCallSpec} formulas, with their argument types, in
 * {@link #getPredicatesCollected()}, and, through its
 * {@link FunctionCallCollectorExpressionVisitor}, the function calls in
 * {@link #getCalledFunctions()}, which {@code SemanticCheckVisitor} does not read (that use is
 * commented out). One instance per formula.
 */
public class PredicateAndFunctionCallCollectorVisitor extends JFormulaVisitor {

	private SymbolTable symbolTable;

	private List<PredicateCallAlloyFormulaDescriptor> predicatesCollected;
	private List<FunctionCallAlloyFormulaDescriptor> functionsCollected;

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	/**
	 * Returns the call specs found so far, in the order visited.
	 *
	 * @return the list itself
	 */
	public List<PredicateCallAlloyFormulaDescriptor> getPredicatesCollected() {
		return predicatesCollected;
	}


	/**
	 * Returns the function calls collected so far. They are added at each predicate formula
	 * ({@code PredicateFormula}): every call the expression visitor has recorded until then, again,
	 * so a call can appear more than once, and a call after the last predicate formula not at all.
	 *
	 * @return the list itself
	 */
	public List<FunctionCallAlloyFormulaDescriptor>  getCalledFunctions(){
		return this.functionsCollected;
	}


	/**
	 * Creates a visitor with a new {@link FunctionCallCollectorExpressionVisitor} as expression
	 * visitor.
	 *
	 * @param symbolTable the table to type arguments with: the fields and the variables in scope;
	 *        quantified variables are added and removed while their formula is visited
	 */
	public PredicateAndFunctionCallCollectorVisitor(SymbolTable symbolTable) {
		// DOB::I need put some ExpressionVisitor there, but I will does not
		// use it.
		// I chose ExpressionVisitor, but any instance it's ok.
		super(new FunctionCallCollectorExpressionVisitor());
		ExpressionVisitor expressionVisitor = this.getDfsExprVisitor();
		expressionVisitor.setFormulaVisitor(this);
		this.predicatesCollected = new ArrayList<PredicateCallAlloyFormulaDescriptor>();
		this.functionsCollected = new ArrayList<FunctionCallAlloyFormulaDescriptor>();
		this.symbolTable = symbolTable;
	}

	//	private static ExpressionVisitor createExpressionVisitor(JFormulaVisitor instance) {
	//		ExpressionVisitor expressionVisitor = new ExpressionVisitor();
	//		FormulaVisitor formulaVisitor = new FormulaVisitor(expressionVisitor);
	//		formulaVisitor.
	//		
	//		return varCollector.setFormulaVisitor(instance);
	//	}

	@Override
	public Object visit(PredicateFormula n) {
		Vector<Object> result = new Vector<Object>();
		result.add(super.visit(n));
		this.functionsCollected.addAll(((FunctionCallCollectorExpressionVisitor)getDfsExprVisitor()).getFunctionsCollected());
		return result;
	}



	@Override
	public Object visit(PredicateCallAlloyFormula predicateCallAlloyFormula) {
		List<JType> argumentsTypeList = new ArrayList<JType>();

		for (AlloyExpression alloyExpression : predicateCallAlloyFormula.getArguments()) {
			ExpressionTypeResolver expressionTypeResolver = new ExpressionTypeResolver(this,this.symbolTable);

			JType jType = (JType) alloyExpression.accept(expressionTypeResolver);
			argumentsTypeList.add(jType);

		}
		PredicateCallAlloyFormulaDescriptor predicateCallAlloyFormulaDescriptor = new PredicateCallAlloyFormulaDescriptor(predicateCallAlloyFormula,
				argumentsTypeList);
		predicatesCollected.add(predicateCallAlloyFormulaDescriptor);
		return super.visit(predicateCallAlloyFormula);
	}

	/**
	 * Declares the quantified variables in a new scope of the symbol table, each typed by its bound,
	 * visits the quantified formula, and closes the scope.
	 *
	 * @param node the quantified formula
	 * @return the base visitor's results
	 * @throws JDynAlloyException if a bound is not a signature name or the union of two
	 */
	@Override
	public Object visit(QuantifiedFormula node) {
		this.symbolTable.beginScope();
		for (String aName : node.getNames()) {
			AlloyExpression alloyExpression = node.getSetOf(aName);
			JType jType ;
			if (alloyExpression instanceof ExprConstant) {
				ExprConstant exprConstant = (ExprConstant) alloyExpression;
				jType = JType.parse(exprConstant.getConstantId());

			} else if (alloyExpression instanceof ExprUnion) {
				ExprUnion exprUnion = (ExprUnion) alloyExpression;
				//Set<String> dom = new HashSet<String>();
				String left;
				String right;
				if (exprUnion.getLeft() instanceof ExprConstant) {
					ExprConstant leftExprConstant = (ExprConstant) exprUnion.getLeft();
					//dom.add( leftExprConstant.getConstantId() );
					left = leftExprConstant.getConstantId();
				} else {
					throw new JDynAlloyException("Only ExprConstant in ExprUnion");
				}

				if (exprUnion.getRight() instanceof ExprConstant) {
					ExprConstant rightExprConstant = (ExprConstant) exprUnion.getRight();
					//dom.add( rightExprConstant.getConstantId() );
					right = rightExprConstant.getConstantId();
				} else {
					throw new JDynAlloyException("Only ExprConstant in ExprUnion");
				}
				jType = JType.parse(left + "+" + right);

			} else {
				throw new JDynAlloyException("Only supported quantifiers with ExprConstant or ExprUnion as defined types");
			}

			this.symbolTable.insertLocal(new VariableId(aName), jType);
		}
		Vector<Object> resultValue = (Vector<Object>) super.visit(node);

		//Bug: scope closed, but pending arithmetic predicate calls exist that will fall outside the scope.
		this.symbolTable.endScope();
		return resultValue;
	}

}

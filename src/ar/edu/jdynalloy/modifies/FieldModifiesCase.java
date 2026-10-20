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

import ar.edu.jdynalloy.JDynAlloyException;
import ar.edu.jdynalloy.JDynAlloyNotImplementedYetException;
import ar.edu.jdynalloy.ast.JModifies;
import ar.edu.jdynalloy.binding.callbinding.ExpressionTypeResolver;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula.Operator;

/**
 * The {@link AbstractModifiesCase} for a field: a clause whose location is a variable {@code f},
 * read as {@code thiz.f}, or a join that ends in one, {@code o.f}. For {@code o.f} it builds
 * {@code all mod_q_<n>: T | !(mod_q_<n>.f = mod_q_<n>.f') implies mod_q_<n> = o'}, where
 * {@code T} is the type of {@code o} and {@code o'} is {@code o} with every variable primed: an
 * object of that type whose {@code f} changes is {@code o}. It never runs (see
 * {@link AbstractModifiesCase}).
 *
 * <p>Static state: the counter that numbers the quantified variables {@code mod_q_<n>} is shared
 * by all instances and never reset, so the names would differ from one run to the next in one
 * JVM.
 */
public class FieldModifiesCase extends AbstractModifiesCase {

	public FieldModifiesCase(SymbolTable symbolTable) {
		super(symbolTable);
	}

	/**
	 * Returns true for a location that is a variable, or a join whose right side is a variable (the
	 * left side is not checked); false for a clause that modifies everything.
	 *
	 * @param modifies the clause
	 * @return whether this case handles it
	 */
	@Override
	public boolean accepts(JModifies modifies) {
		if (modifies.isModifiesEverything()) {
			return false;
		}

		return acceptLocation(modifies.getLocation());
	}

	private boolean acceptLocation(AlloyExpression location) {
		if (location instanceof ExprVariable) {
			return true;
		}

		if (location instanceof ExprJoin) {
			ExprJoin exprUnion = (ExprJoin) location;
			if (exprUnion.getRight() instanceof ExprVariable) {
				return acceptLocation(exprUnion.getRight());
			} else {
				return false;
			}
		}

		return false;
	}

	/**
	 * Builds the formula of the class comment; a location without a join gets {@code thiz} as
	 * receiver. Adds the field to {@link #getModifiableFieldSet()} and advances the static counter.
	 *
	 * @param modifies a clause this case accepts
	 * @return the formula
	 */
	@Override
	public AlloyFormula generateFormula(JModifies modifies) {
		ExprJoin location;
		if (!(modifies.getLocation() instanceof ExprJoin)) {
			try {
				location = (ExprJoin) addThizToStart(modifies.getLocation());
			} catch (Exception e){
				throw new JDynAlloyNotImplementedYetException(e.getMessage());
			}
		} else {
			location = (ExprJoin) modifies.getLocation();
		}

		String fieldName = extractFieldName(location);

		getModifiableFieldSet().add(fieldName);

		ExpressionTypeResolver expressionTypeResolver = new ExpressionTypeResolver(null, this.getSymbolTable());
		AlloyExpression leftPart = location.getLeft();
		JType leftPartType = (JType) leftPart.accept(expressionTypeResolver);
		ExprVariable rightPart = (ExprVariable) location.getRight();

		AlloyFormula returnValue = modifiableFieldSupport(leftPart, leftPartType, rightPart.getVariable().getVariableId().getString());
		return returnValue;
	}

	private AlloyExpression addThizToStart(AlloyExpression location) throws Exception {
		AlloyExpression returnValue;
		if (location instanceof ExprVariable) {
			ExprVariable exprVariable = (ExprVariable) location;
			if (exprVariable.getVariable().getVariableId().getString().equals(JExpressionFactory.THIS_EXPRESSION.getVariable().getVariableId())) {
				returnValue = location;
			} else {
				returnValue = new ExprJoin(JExpressionFactory.THIS_EXPRESSION, exprVariable);
			}
		} else if (location instanceof ExprJoin) {
			ExprJoin exprJoin =(ExprJoin) location;
			AlloyExpression left = addThizToStart(exprJoin.getLeft());
			returnValue = ExprJoin.join(left, exprJoin.getRight()); 
		} else {
			throw new Exception("Not supported!");
		}
		return returnValue;
	}

	private String extractFieldName(AlloyExpression alloyExpression) {
		if (alloyExpression instanceof ExprVariable) {
			ExprVariable exprVariable = (ExprVariable) alloyExpression;
			return exprVariable.getVariable().getVariableId().getString();
		} else if (alloyExpression instanceof ExprJoin) {
			ExprJoin exprJoin = (ExprJoin) alloyExpression;
			if (exprJoin.getRight() instanceof ExprVariable) {
				ExprVariable right = (ExprVariable) exprJoin.getRight();
				return right.getVariable().getVariableId().getString();
			} else if (exprJoin.getLeft() instanceof ExprJoin) {
				return extractFieldName((ExprJoin) exprJoin.getLeft());
			} else {
				throw new JDynAlloyException("Not supported!");
			}
		} else {
			throw new JDynAlloyException("Not supported!");
		}
	}

	private static final String MODIFIES_QUANTIFIER_VARIABLE = "mod_q";
	private static int quantifiersCount = 0;

	/**
	 * Builds the formula of the class comment for {@code leftPart.fieldName}, with a new quantified
	 * variable; advances the static counter.
	 *
	 * @param leftPart the receiver {@code o}
	 * @param leftPartType its type; {@code dpdTypeNameExtract()} gives the signature the variable
	 *        ranges over
	 * @param fieldName the field
	 * @return the formula
	 */
	protected AlloyFormula modifiableFieldSupport(AlloyExpression leftPart, JType leftPartType,String fieldName) {
		String modifiesVarName = MODIFIES_QUANTIFIER_VARIABLE + "_" + quantifiersCount;
		quantifiersCount++;

		AlloyFormula leftFormula = new NotFormula(ModifiesUtils.createFormulaFieldValueEqualsToOldFieldValue(modifiesVarName, fieldName));

		//		ExprVariable.buildExprVariable("thiz")
		AlloyExpression primedLeftPartExpression = primeExpression(leftPart);

		AlloyFormula rightFormula = new EqualsFormula(ExprVariable.buildExprVariable(modifiesVarName), primedLeftPartExpression);
		AlloyFormula alloyFormula = new ImpliesFormula(leftFormula, rightFormula);
		List<AlloyExpression> typesSet = Collections.<AlloyExpression> singletonList(ExprConstant.buildExprConstant(leftPartType.dpdTypeNameExtract()));
		return new QuantifiedFormula(Operator.FOR_ALL, Collections.<String> singletonList(modifiesVarName), typesSet, alloyFormula);
	}
}

package ar.edu.jdynalloy.relevancy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.edu.jdynalloy.binding.callbinding.ExpressionTypeResolver;
import ar.edu.jdynalloy.binding.callbinding.FunctionCallAlloyFormulaDescriptor;
import ar.edu.jdynalloy.binding.callbinding.PredicateAndFunctionCallRelevancyVisitor;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;

/**
 * The expression visitor of the relevancy analysis: an {@code ExpressionTypeResolver} that, while
 * it types the expressions of a formula, records the types of the constants other than
 * {@code null}, {@code univ}, {@code Int}, {@code boolean} and {@code none}
 * ({@link #getRelevantTypes()}), and every function call, with the types of its arguments and of
 * its result ({@link #getCalledFunctions()}). {@link RelevancyAnalysisUtils#analyzeFormula}
 * creates one per formula and installs it in a {@code PredicateAndFunctionCallRelevancyVisitor},
 * which copies the function calls recorded so far at each predicate formula; then it adds the
 * modules of the constants' types to the scene. No static state.
 */
public class RelevantAnalysisExpressionTypeResolver extends
		ExpressionTypeResolver {

	private final Set<JType> relevantTypes;
	
	private Set<FunctionCallAlloyFormulaDescriptor> functionsCollected = new HashSet<FunctionCallAlloyFormulaDescriptor>();

	/**
	 * Creates a resolver for one formula.
	 *
	 * @param formulaVisitor the formula visitor that uses it; it must be a
	 *        {@code PredicateAndFunctionCallRelevancyVisitor}, to which {@code visit(ExprFunction)}
	 *        casts it to get its symbol table
	 * @param symbolTable the analysis's table
	 */
	public RelevantAnalysisExpressionTypeResolver(
			FormulaVisitor formulaVisitor, SymbolTable symbolTable) {
		super(formulaVisitor, symbolTable);
		relevantTypes = new HashSet<JType>();
	}

	
	public Set<FunctionCallAlloyFormulaDescriptor> getCalledFunctions(){
		return this.functionsCollected;
	}
	
	public Object visit(ExprConstant exprConstant) {
		JType type = (JType) super.visit(exprConstant);
		if (!type.equals(new JType("null")) && !type.equals(new JType("univ"))
				&& !type.equals(new JType("Int")) && !type.equals(new JType("boolean"))
				&& !type.equals(new JType("none"))) {
			
			relevantTypes.add(type);
		}
		return type;
	}
	
	
	/**
	 * Records the call, with the types of its arguments and of its result (each computed by a new
	 * {@code ExpressionTypeResolver} on the formula visitor's symbol table), visits each argument
	 * with this resolver, and returns the type of the call. {@code ExpressionTypeResolver}'s visit
	 * then visits the arguments again, so a call nested in another is recorded twice, and one nested
	 * d levels deep {@code 2^d} times.
	 *
	 * @param fc the function call
	 * @return its type, as {@code ExpressionTypeResolver} computes it
	 * @throws ClassCastException if the formula visitor is not a
	 *         {@code PredicateAndFunctionCallRelevancyVisitor}
	 */
	public Object visit(ExprFunction fc){
		List<JType> argumentsTypeList = new ArrayList<JType>();
		ExpressionTypeResolver expressionTypeResolver;
		for (AlloyExpression alloyExpression : fc.getParameters()) {
			expressionTypeResolver = 
					new ExpressionTypeResolver(((PredicateAndFunctionCallRelevancyVisitor)this.formulaVisitor).getSymbolTable());

			JType jType = (JType) alloyExpression.accept(expressionTypeResolver);
			argumentsTypeList.add(jType);
			alloyExpression.accept(this);
		}
		expressionTypeResolver = new ExpressionTypeResolver(((PredicateAndFunctionCallRelevancyVisitor)this.formulaVisitor).getSymbolTable());
		JType returnType = (JType) fc.accept(expressionTypeResolver);
		FunctionCallAlloyFormulaDescriptor functionCallAlloyFormulaDescriptor = new FunctionCallAlloyFormulaDescriptor(fc,returnType,argumentsTypeList);
		functionsCollected.add(functionCallAlloyFormulaDescriptor);
		return super.visit(fc);
	}
	
	public Object visit(ExprVariable exprVariable) {
		return super.visit(exprVariable);
	}

	public Set<JType> getRelevantTypes() {
		return relevantTypes;
	}

}

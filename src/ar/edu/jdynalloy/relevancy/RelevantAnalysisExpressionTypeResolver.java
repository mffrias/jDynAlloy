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

public class RelevantAnalysisExpressionTypeResolver extends
		ExpressionTypeResolver {

	private final Set<JType> relevantTypes;
	
	private Set<FunctionCallAlloyFormulaDescriptor> functionsCollected = new HashSet<FunctionCallAlloyFormulaDescriptor>();

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

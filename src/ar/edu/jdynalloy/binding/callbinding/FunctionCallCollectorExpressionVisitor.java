package ar.edu.jdynalloy.binding.callbinding;

import java.util.ArrayList;
import java.util.List;

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;

public class FunctionCallCollectorExpressionVisitor extends ExpressionVisitor {
	
	
	private List<FunctionCallAlloyFormulaDescriptor> functionsCollected = new ArrayList<FunctionCallAlloyFormulaDescriptor>();

	public List<FunctionCallAlloyFormulaDescriptor> getFunctionsCollected(){
		return this.functionsCollected;
	}
	
	@Override
	public Object visit(ExprFunction fc) {
		List<JType> argumentsTypeList = new ArrayList<JType>();

		ExpressionTypeResolver expressionTypeResolver;
		for (AlloyExpression alloyExpression : fc.getParameters()) {
			expressionTypeResolver = 
					new ExpressionTypeResolver(((PredicateAndFunctionCallCollectorVisitor)this.formulaVisitor).getSymbolTable());

			JType jType = (JType) alloyExpression.accept(expressionTypeResolver);
			argumentsTypeList.add(jType);
			alloyExpression.accept(this);
		}
		expressionTypeResolver = new ExpressionTypeResolver(((PredicateAndFunctionCallCollectorVisitor)this.formulaVisitor).getSymbolTable());
		JType returnType = (JType) fc.accept(expressionTypeResolver);
		FunctionCallAlloyFormulaDescriptor functionCallAlloyFormulaDescriptor = new FunctionCallAlloyFormulaDescriptor(fc,returnType,argumentsTypeList);
		functionsCollected.add(functionCallAlloyFormulaDescriptor);
		return super.visit(fc);
	}
}

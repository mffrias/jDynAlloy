package ar.edu.jdynalloy.binding.callbinding;

import java.util.ArrayList;
import java.util.List;

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;

/**
 * The expression visitor of {@link PredicateAndFunctionCallCollectorVisitor}: records every
 * function call ({@code ExprFunction}) it reaches, with its argument and return types, in
 * {@link #getFunctionsCollected()}. A call nested in another's arguments is recorded twice (once
 * while the outer call's arguments are typed, once by the traversal). Its {@code formulaVisitor}
 * must be a {@code PredicateAndFunctionCallCollectorVisitor}, whose symbol table it uses to type
 * the arguments; that visitor's constructor sets it. Typing a call can throw
 * {@code JDynAlloyNotImplementedYetException} (see {@link ExpressionTypeResolver}).
 */
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

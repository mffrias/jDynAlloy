/**
 * 
 */
package ar.edu.jdynalloy.xlator;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaMutator;
import ar.uba.dc.rfm.alloy.util.VarSubstitutor;

public class ThizToObjXExprMutator extends VarSubstitutor  {

	private JFormulaMutator formulaMutator;

	@Override
	protected AlloyExpression getExpr(AlloyVariable v) {
		if (v.getVariableId().toString().equals("thiz"))
			return ExprVariable.buildExprVariable("objx");
		else
			return ExprVariable.buildExprVariable(v);
	}
}
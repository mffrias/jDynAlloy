/**
 * 
 */
package ar.edu.jdynalloy.xlator;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaMutator;
import ar.uba.dc.rfm.alloy.util.VarSubstitutor;

public class PostconditionExprMutator extends VarSubstitutor  {

	@Override
	protected AlloyExpression getExpr(AlloyVariable v) {
		JFormulaMutator jFormulaMutator = (JFormulaMutator) formulaVisitor;
		if (!jFormulaMutator.isBoundedVariable(v) && !v.isPrimed()) {
			if (JDynAlloyConfig.getInstance().getDynamicJavaLangFields() == false) {
				if (v.toString().equals("intValue")) {
					return ExprVariable.buildExprVariable(v);
				}
			}
			return ExprVariable.buildExprVariable(new AlloyVariable(v
					.getVariableId(), true));
		} else
			return ExprVariable.buildExprVariable(v);
	}
}
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

/**
 * Moves a formula to the post-state: every variable that is not bound by a quantifier of the
 * formula and is not primed becomes primed ({@code x} becomes {@code x'}), except
 * {@code intValue} when {@code dynamicJavaLangFields} is false (docs/ARCHITECTURE.md,
 * "Configuration"). {@code JDynAlloyXlatorVisitor} applies it to the object and class
 * invariants it re-checks in {@code postcondition_<program>}. Reads
 * {@code JDynAlloyConfig.getInstance()} for each such variable.
 *
 * <p>It must run inside a {@code JFormulaMutator}, which tells it which variables are bound: it
 * casts its formula visitor to one, so with another formula visitor it fails with a
 * {@link ClassCastException}. {@code JFormulaMutator}'s constructor installs itself as that
 * visitor. The visits return new expressions; the input is not changed. No state.
 */
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
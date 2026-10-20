/**
 * 
 */
package ar.edu.jdynalloy.xlator;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaMutator;
import ar.uba.dc.rfm.alloy.util.VarSubstitutor;

/**
 * Replaces the receiver {@code thiz} with the variable {@code objx}, so that a formula about one
 * object can be stated for every object of a class. {@code JDynAlloyXlatorVisitor} applies it,
 * through a {@code JFormulaMutator}, to the object invariants, object constraints and
 * abstraction predicates of the modules it does not skolemize, and then wraps the result in
 * {@code all objx: <Sig> | ...} (docs/ARCHITECTURE.md, "The assertion check_&lt;method&gt;").
 * Every {@code thiz}, primed or not, becomes the unprimed {@code objx}; the other variables are
 * kept. The visits return new expressions; the input is not changed. No state.
 */
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
package ar.edu.jdynalloy.ast;

import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.util.ExpressionMutator;


/**
 * The expression mutator of {@link VariableNameChangerFormulaMutator}: rebuilds an expression
 * with each variable of the given list replaced by a new variable named
 * {@code do_body_rename_<name>}, not primed. Other variables are kept. Used, through
 * {@link VariableNameChangerVisitor}, for the copy of the body of a {@code do} loop that
 * {@link JAlloyProgramBuffer#closeDo()} builds. No static state.
 */
public class VariableNameChangerExpressionMutator extends ExpressionMutator {

	
	List<AlloyVariable> vars = new LinkedList<AlloyVariable>();
	
	public VariableNameChangerExpressionMutator(List<AlloyVariable> vars) {
		super();
		this.vars = vars;
	}

	/* (non-Javadoc)
	 * @see ar.uba.dc.rfm.alloy.util.ExpressionMutator#visit(ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable)
	 */
	@Override
	public Object visit(ExprVariable n) {
		// TODO Auto-generated method stub
		if (vars.contains(n.getVariable())){
			return new ExprVariable(new AlloyVariable("do_body_rename_" + n.getVariable().getVariableId().getString()));
		} else
			return n;
	}

}

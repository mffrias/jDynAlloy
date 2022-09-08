package ar.edu.jdynalloy.ast;

import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.util.ExpressionMutator;


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

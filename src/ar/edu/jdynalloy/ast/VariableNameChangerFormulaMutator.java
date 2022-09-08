package ar.edu.jdynalloy.ast;

import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaMutator;

public class VariableNameChangerFormulaMutator extends JFormulaMutator {
	
	

	public VariableNameChangerFormulaMutator(List<AlloyVariable> vars) {
		super(new VariableNameChangerExpressionMutator(vars));
		// TODO Auto-generated constructor stub
	}

	
}

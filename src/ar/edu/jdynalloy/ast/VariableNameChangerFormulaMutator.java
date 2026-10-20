package ar.edu.jdynalloy.ast;

import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaMutator;

/**
 * A {@code JFormulaMutator} whose expression mutator is a
 * {@link VariableNameChangerExpressionMutator}: it rebuilds a formula with the variables of the
 * given list renamed to {@code do_body_rename_<name>}. {@link VariableNameChangerVisitor} uses it
 * for the formulas and expressions of the statements, and
 * {@link JAlloyProgramBuffer#closeDo()} for the loop's condition. No static state.
 */
public class VariableNameChangerFormulaMutator extends JFormulaMutator {
	
	

	public VariableNameChangerFormulaMutator(List<AlloyVariable> vars) {
		super(new VariableNameChangerExpressionMutator(vars));
		// TODO Auto-generated constructor stub
	}

	
}

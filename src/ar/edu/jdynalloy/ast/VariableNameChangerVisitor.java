package ar.edu.jdynalloy.ast;

import java.util.List;

import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaMutator;

/**
 * This class is used to modify names of variables declared inside Do statements. The unfolding
 * performed "do body wjile cond ==> body;while cond do body" may generate repeated variable definitions.
 * @author mfrias
 *
 */


public class VariableNameChangerVisitor extends JDynAlloyMutator{


	List<AlloyVariable> vars = null;


	/**
	 * Creates the visitor for one {@code do} loop. It returns a copy of the statement it visits
	 * in which each variable of {@code vars} is renamed to {@code do_body_rename_<name>}: in the
	 * declarations, and, through a {@link VariableNameChangerFormulaMutator}, in the formulas and
	 * expressions of the statements. Being a {@link JDynAlloyMutator}, it does not rename the
	 * variable of a {@code createObject}. {@link JAlloyProgramBuffer#closeDo()} creates one, with
	 * the variables that the loop's body declares.
	 *
	 * @param vars the variables to rename
	 * @param isJavaArithmetic true with Java arithmetic; passed to {@link JDynAlloyMutator}
	 */
	public VariableNameChangerVisitor(List<AlloyVariable> vars, boolean isJavaArithmetic) {
		super(isJavaArithmetic);
		this.vars = vars;
		VariableNameChangerFormulaMutator fm = new VariableNameChangerFormulaMutator(vars);
		this.setFormulaMutator(fm);

	}


	@Override
	public Object visit(JVariableDeclaration n) {
		if (vars.contains(n.getVariable()))
			return new JVariableDeclaration(new AlloyVariable("do_body_rename_" + n.getVariable().getVariableId().getString()), n.getType());
		else 
			return n;
	}



}

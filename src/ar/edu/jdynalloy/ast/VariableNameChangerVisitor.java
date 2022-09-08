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

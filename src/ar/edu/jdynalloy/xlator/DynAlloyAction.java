package ar.edu.jdynalloy.xlator;

import java.util.HashSet;
import java.util.Set;

import ar.edu.jdynalloy.factory.DynalloyFactory;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;

class DynAlloyAction extends DynalloyFactory {

	private static int index = 0;

	private static Set<ActionDeclaration> actions = new HashSet<ActionDeclaration>();
	private static StringBuffer alloy = new StringBuffer();

	public static String registerNewActionWhile() {
		// new action declaration
		
		// create action invokation
		index++;
		return "while_action_" + index;
	}
}

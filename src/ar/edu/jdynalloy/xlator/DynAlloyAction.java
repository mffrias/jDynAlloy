package ar.edu.jdynalloy.xlator;

import java.util.HashSet;
import java.util.Set;

import ar.edu.jdynalloy.factory.DynalloyFactory;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;

/**
 * Names for DynAlloy actions of loops, {@code while_action_<n>}. Nothing in jDynAlloy or TACO uses
 * this class: {@code JDynAlloyXlatorVisitor.visit(JWhile)} translates a loop without actions
 * (docs/ARCHITECTURE.md, "JDynAlloyXlatorVisitor: modules, programs, statements"). It extends
 * {@code DynalloyFactory} but uses none of its members.
 *
 * <p>Static state: the counter {@code index}, which nothing resets, so it would keep growing across
 * runs in one JVM; and the static {@code actions} and {@code alloy}, which nothing reads or writes.
 * Unclear: whether the class is still needed.
 */
class DynAlloyAction extends DynalloyFactory {

	private static int index = 0;

	private static Set<ActionDeclaration> actions = new HashSet<ActionDeclaration>();
	private static StringBuffer alloy = new StringBuffer();

	/**
	 * Increments the static counter and returns {@code while_action_<n>} with its new value (1 the
	 * first time in a JVM). It declares no action, although the comments in its body name that step
	 * and the creation of an invocation. Nothing calls it.
	 *
	 * @return the name
	 */
	public static String registerNewActionWhile() {
		// new action declaration
		
		// create action invokation
		index++;
		return "while_action_" + index;
	}
}

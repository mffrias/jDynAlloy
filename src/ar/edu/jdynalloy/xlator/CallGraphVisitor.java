package ar.edu.jdynalloy.xlator;

import java.util.List;

import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JVariableDeclaration;

/**
 * Builds the call graph of a set of modules ({@link #getCallGraph()}): a node for each program
 * and each program called, and an edge from each program to each program it calls. Nodes are
 * names: a program's node is its program id, and a call's node is the call's program id, which
 * {@code VAlloyVisitor} has made the id of the program it is bound to. So overloads, and
 * programs of the same name in different modules, share a node. Only {@code call} statements
 * make edges; programs used in specifications ({@code callSpec}) do not. One instance visits all
 * the modules and holds the graph; the visits return {@code JDynAlloyVisitor}'s results, which
 * are not used, and the modules are not changed. No static state.
 *
 * <p>{@code JDynAlloyTranslator.translateAll} builds two (docs/ARCHITECTURE.md, "The
 * translation"): one after {@code ModularMutator}, for {@link PruneVisitor} and, as the
 * carrier of the modules, {@link RecursionUnfolderVisitor}; and one after the recursion is
 * unfolded, for {@link ModifiesTableBuilder}.
 */
class CallGraphVisitor extends JDynAlloyVisitor {

	String classToCheck = "";
	String methodToCheck = "";

	public CallGraphVisitor(boolean isJavaArith, String classToCheck, String methodToCheck) {
		super(isJavaArith);
		this.classToCheck = classToCheck.replace('.', '_');
		this.methodToCheck = methodToCheck;
	}


	public Graph<String> getCallGraph() {
		return callGraph;
	}

	private String currentProgramDeclaration;

	private final Graph<String> callGraph = new Graph<String>(true);

	@Override
	public Object visit(JProgramCall n) {
		String caller = currentProgramDeclaration;
		String callee = n.getProgramId();

		if (!callGraph.containsNode(caller))
			callGraph.addNode(caller);

		if (!callGraph.containsNode(callee))
			callGraph.addNode(callee);

		callGraph.addEdge(caller, callee);

		return super.visit(n);
	}

	private String filterParameters(List<JVariableDeclaration> parameters) {
		String params = "";
		boolean hasParams = false;
		for (JVariableDeclaration v : parameters){
			if (!v.getVariable().getVariableId().getString().equals("thiz") && !v.getVariable().getVariableId().getString().equals("throw") && !v.getVariable().getVariableId().getString().equals("return")){
				String param = v.getType().toString();
				if (param.equals("Int"))
					param = "int";
				if (param.equals("Long"))
					param = "long";
				if (param.equals("Float"))
					param = "float";
				if (param.equals("java_lang_IntArray+null"))
					param = "int[]";
				if (param.equals("java_lang_LongArray+null"))
					param = "long[]";
				if (param.equals("java_lang_CharArray+null"))
					param = "char[]";
				if (param.equals("JavaPrimitiveIntegerValue"))
					param = "int";
				if (param.equals("JavaPrimitiveLongValue"))
					param = "long";
				if (param.equals("JavaPrimitiveCharValue"))
					param = "char";
				if (param.equals("JavaPrimitiveFloatValue"))
					param = "float";

				params += param + ",";
				hasParams = true;
			}
		}
		if (hasParams)
			params = params.substring(0, params.length()-1);
		return params;
	}

	/**
	 * Adds the program's node and makes it the caller of the calls in its body. The node is the
	 * program id, except for a program whose {@code <id>(<parameter types>)} (without
	 * {@code thiz}, {@code throw} and {@code return}, with Java's names for primitive values and
	 * their arrays) equals {@code classToCheck + "_" + methodToCheck}: that one gets the typed
	 * name. TACO passes {@code methodToCheck} without types, and it already starts with the
	 * class, so no program matches and every node is a plain program id (docs/NAMING.md, "Where
	 * the method under analysis is looked up"). If one matched, calls to it would still go to the
	 * untyped node.
	 *
	 * @param node the program
	 * @return {@code JDynAlloyVisitor}'s result
	 */
	@Override
	public Object visit(JProgramDeclaration node) {
		String qualifiedCheckedMethodName = this.classToCheck + "_" + this.methodToCheck.replaceAll(" ", "");
		String qualifiedThisMethodName = node.getProgramId() + "(" + filterParameters(node.getParameters()) + ")";
		String qualifiedMethodName = "";
		if (qualifiedCheckedMethodName.equals(qualifiedThisMethodName)){
			qualifiedMethodName = qualifiedThisMethodName;
		} else {
			qualifiedMethodName = node.getProgramId();
		}

		currentProgramDeclaration = qualifiedMethodName;

		if (!callGraph.containsNode(currentProgramDeclaration))
			callGraph.addNode(currentProgramDeclaration);

		return super.visit(node);
	}

}

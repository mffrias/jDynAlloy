package ar.edu.jdynalloy.xlator;

import java.util.List;

import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JVariableDeclaration;

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

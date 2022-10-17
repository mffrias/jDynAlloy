package ar.edu.jdynalloy.xlator;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyMutator;
import ar.edu.jdynalloy.ast.JAssume;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JSpecCase;
import ar.edu.jdynalloy.ast.JStatement;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.factory.JPredicateFactory;


import java.util.List;
import java.util.HashSet;
import java.util.LinkedList;

class RecursionUnfolderVisitor extends JDynAlloyMutator {

	private final Graph<String> callGraph;

	private final List<JProgramDeclaration> unfolded_programs;

	private final int unroll;

	public List<JProgramDeclaration> getUnfoldedPrograms() {
		return unfolded_programs;
	}

	public RecursionUnfolderVisitor(Graph<String> callGraph, int unroll, boolean isJavaArith) {
		super(isJavaArith);
		this.callGraph = callGraph;
		this.unfolded_programs = new LinkedList<JProgramDeclaration>();
		this.unroll = unroll;
	}

	@Override
	public Object visit(JDynAlloyModule node) {
		this.unfolded_programs.clear();
		JDynAlloyModule module = (JDynAlloyModule)super.visit(node);
		module.getPrograms().addAll(this.unfolded_programs);
		return module;
	}

	private String unfold_programId = null;
	private int unfold_index = -1;

	@Override
	public Object visit(JProgramDeclaration n) {
		String programId = n.getProgramId();
//		String typedProgramId = programId + "(" + filterParameters(n.getParameters()) + ")";
		assert (!programId.contains("("));

		if (callGraph.containsNode(programId) && callGraph.childrenOf(programId).contains(programId)) {
			for (int i=0; i<=unroll; i++) {
				// create n-program declarations

				boolean isAbstract = n.isAbstract();
				String signatureId = n.getSignatureId();
				String unfolded_programId = n.getProgramId() + "_unfold_" + i;
				List<JVariableDeclaration> parameters = new LinkedList<JVariableDeclaration>(n.getParameters());
				List<JSpecCase> specCases = new LinkedList<JSpecCase>(n.getSpecCases());

				unfold_programId = programId;
				unfold_index = i+1;
				JStatement body = (JStatement) n.getBody().accept(this);

				JProgramDeclaration unfolded_program = new JProgramDeclaration(
						isAbstract, 
						n.isConstructor(),
						n.isPure(),
						signatureId,
						unfolded_programId,
						parameters,
						specCases, 
						body, null, null);

				unfolded_programs.add(unfolded_program);
			}
			unfold_index = 0;

			// update original program declaration
			JStatement updated_body = (JStatement) n.getBody().accept(this);

			JProgramDeclaration updated_n = new JProgramDeclaration(n.isAbstract(),
					n.isConstructor(),
					n.isPure(),
					n.getSignatureId(),
					n.getProgramId(),
					n.getParameters(),
					n.getSpecCases(), 
					updated_body, 
					n.getVarsResultOfArithmeticOperationsInRequiresAndEnsures(),
					n.getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures());

			unfold_index = -1;
			unfold_programId = null;

			return updated_n;

		} else { 
			JProgramDeclaration ret = (JProgramDeclaration)super.visit(n);
			return ret;
		}
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
	public Object visit(JProgramCall n) {
		if (unfold_index!=-1 && unfold_programId!=null) {
			if (n.getProgramId().equals(unfold_programId)) {
				if (unfold_index < unroll) {
					boolean isSuperCall = n.isSuperCall();
					String programId = n.getProgramId() + "_unfold_" + unfold_index;
					List<AlloyExpression> arguments = n.getArguments();
					return new JProgramCall(isSuperCall, programId, arguments);
				} else {
					return new JAssume(JPredicateFactory.eq(JExpressionFactory.TRUE_EXPRESSION, 
							JExpressionFactory.FALSE_EXPRESSION));
				}
			}
		}
		JProgramCall ret = (JProgramCall)super.visit(n);
		return ret;
	}


}

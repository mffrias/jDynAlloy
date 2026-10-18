package ar.edu.jdynalloy.xlator;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyMutator;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JAssume;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JSpecCase;
import ar.edu.jdynalloy.ast.JStatement;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.factory.JPredicateFactory;


import java.util.*;

class RecursionUnfolderVisitor extends JDynAlloyMutator {

	// Programs are identified by name and parameter types, as JBindingKey does, so that overloads
	// (two constructors, for example) are not taken for one recursive program. The call graph
	// handed in, keyed by name alone, only provides the modules.
	private final Graph<String> callGraph;

	private final Map<String, List<JProgramDeclaration>> programsById = new HashMap<String, List<JProgramDeclaration>>();

	private final List<JProgramDeclaration> unfolded_programs;

	private final int unroll;

	public List<JProgramDeclaration> getUnfoldedPrograms() {
		return unfolded_programs;
	}

	public RecursionUnfolderVisitor(Graph<String> callGraph, int unroll, boolean isJavaArith) {
		super(isJavaArith);
		Vector<JDynAlloyModule> modules = (Vector<JDynAlloyModule>) callGraph.getExtraData();
		for (JDynAlloyModule module : modules) {
			for (JProgramDeclaration program : module.getPrograms()) {
				if (!programsById.containsKey(program.getProgramId()))
					programsById.put(program.getProgramId(), new LinkedList<JProgramDeclaration>());
				programsById.get(program.getProgramId()).add(program);
			}
		}
		TypedCallGraphBuilder builder = new TypedCallGraphBuilder(isJavaArith);
		for (JDynAlloyModule module : modules) {
			module.accept(builder);
		}
		this.callGraph = builder.graph;
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

		String programKey = programKey(n);

		if (callGraph.containsNode(programKey) && (callGraph.childrenOf(programKey).contains(programKey) || callGraph.strictDescendentsOf(programKey).contains(programKey)))  {// descendentsOf(programId).contains(programId)) {

			for (String prgKey : callGraph.descendentsOf(programKey)) {
				n = retrieveProgram(prgKey);
				for (int i = 0; i <= unroll; i++) {
					// create n-program declarations

					boolean isAbstract = n.isAbstract();
					String signatureId = n.getSignatureId();
					String unfolded_programId = n.getProgramId() + "_unfold_" + i;
					List<JVariableDeclaration> parameters = new LinkedList<JVariableDeclaration>(n.getParameters());
					List<JSpecCase> specCases = new LinkedList<JSpecCase>(n.getSpecCases());

					unfold_programId = programKey;
					unfold_index = i + 1;
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
			}

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

	private JProgramDeclaration retrieveProgram(String prgKey) {
		for (List<JProgramDeclaration> programs : programsById.values()) {
			for (JProgramDeclaration jpd : programs) {
				if (programKey(jpd).equals(prgKey)) {
					return jpd;
				}
			}
		}
		return null;
	}

	// The program's name and parameter types, e.g. ximena_MutRecursive_MutRecursive(int).
	private String programKey(JProgramDeclaration program) {
		return program.getProgramId() + "(" + filterParameters(program.getParameters()) + ")";
	}

	// The programs a call may invoke: those with its name and number of arguments, which is how
	// DynAlloy tells programs apart. Two overloads with the same number of parameters both qualify.
	private List<String> calleeKeys(JProgramCall call) {
		List<String> keys = new LinkedList<String>();
		if (programsById.containsKey(call.getProgramId())) {
			for (JProgramDeclaration program : programsById.get(call.getProgramId())) {
				if (program.getParameters().size() == call.getArguments().size())
					keys.add(programKey(program));
			}
		}
		if (keys.isEmpty())
			keys.add(call.getProgramId() + "(" + call.getArguments().size() + " arguments)");
		return keys;
	}

	private final class TypedCallGraphBuilder extends JDynAlloyVisitor {

		private final Graph<String> graph = new Graph<String>(true);

		private String currentProgram;

		TypedCallGraphBuilder(boolean isJavaArith) {
			super(isJavaArith);
		}

		@Override
		public Object visit(JProgramDeclaration node) {
			currentProgram = programKey(node);
			if (!graph.containsNode(currentProgram))
				graph.addNode(currentProgram);
			return super.visit(node);
		}

		@Override
		public Object visit(JProgramCall n) {
			for (String callee : calleeKeys(n)) {
				if (!graph.containsNode(callee))
					graph.addNode(callee);
				graph.addEdge(currentProgram, callee);
			}
			return super.visit(n);
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
		if (unfold_index!=-1 && unfold_programId != null) {
			if (reaches(n, unfold_programId)) {//if (n.getProgramId().equals(unfold_programId)) {
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

	// Whether the call may lead back to the program with the given key.
	private boolean reaches(JProgramCall call, String programKey) {
		for (String callee : calleeKeys(call)) {
			if (callGraph.containsNode(callee) && callGraph.descendentsOf(callee).contains(programKey))
				return true;
		}
		return false;
	}


}

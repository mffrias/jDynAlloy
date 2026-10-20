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

/**
 * Removes recursion by unfolding it a bounded number of times, so that the programs DynAlloy
 * receives do not recurse: the sixth preparation step of {@code JDynAlloyTranslator.translateAll},
 * run on every module after {@link PruneVisitor}, with the bound
 * {@code JDynAlloyBinding.unfoldScopeForRecursiveCode}, which TACO sets to its loop-unrolling
 * bound (docs/ARCHITECTURE.md, "How TACO calls it", step 1, and "The translation", step 6).
 *
 * <p>Programs are identified by name and parameter types ({@code <id>(<types>)}, without
 * {@code thiz}, {@code throw} and {@code return}, with Java's names for primitive values and
 * their arrays), on a call graph of its own, built from the modules the given graph carries: a
 * call has an edge to every program with its name and number of arguments, which is how DynAlloy
 * tells programs apart (d2b09e9, docs/NAMING.md). A program is recursive if it can reach itself
 * in that graph. For each recursive program p, with bound n:
 * <ul>
 * <li>every program reachable from p, p included, gets copies {@code <id>_unfold_0} to
 *     {@code <id>_unfold_<n>}, with the same parameters and specification cases;</li>
 * <li>in copy i, a call that can lead back to p goes to {@code <callee>_unfold_<i+1>} if
 *     i + 1 &lt; n, and becomes {@code assume true = false} otherwise; other calls are kept;</li>
 * <li>in p, such a call goes to {@code <callee>_unfold_0} ({@code assume true = false} if n is
 *     0).</li>
 * </ul>
 * So an execution that nests more than n calls on its way back to p is dropped. Copy
 * {@code _unfold_<n>} is never called, and neither are the copies of the programs that cannot
 * reach p. Unclear: whether only the programs on a cycle through p were meant to be copied.
 * The copies are added to the module of p, whichever module the copied program comes from, and
 * have no arithmetic encodings (null).
 *
 * <p>When p also calls other programs, the program rebuilt as p is the last one that the loop
 * over the reachable programs fetched, since the loop reassigns the variable that holds p: p
 * can be replaced by a second copy of another program and disappear from its module, and TACO
 * then fails with a {@code NullPointerException} (checked by running TACO; TACO's
 * docs/FIXES-FOR-MASTER.md, F14).
 *
 * <p>Returns new modules, to whose program sets the copies are then added. The programs by id,
 * the typed graph and the copies of the module being visited are per instance, and the
 * translator creates one per translation; no static state.
 */
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

	/**
	 * Creates the unfolder. The modules come from {@code callGraph.getExtraData()}, which must be
	 * the {@code Vector<JDynAlloyModule>} the graph was built from ({@link Graph#setExtraData});
	 * the translator passes its first call graph, built before pruning, so the typed graph and the
	 * programs copied are those of the modules before {@link PruneVisitor}. The graph itself is not
	 * used.
	 *
	 * @param callGraph a call graph that carries its modules
	 * @param unroll the bound n (see the class comment)
	 * @param isJavaArith true with Java arithmetic; passed to {@code JDynAlloyVisitor}
	 * @throws ClassCastException if the extra data is not a {@code Vector}
	 * @throws NullPointerException if the graph carries no extra data
	 */
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

	/**
	 * Rebuilds the module, and adds to its programs the copies made while visiting them.
	 *
	 * @param node the module
	 * @return the new module
	 */
	@Override
	public Object visit(JDynAlloyModule node) {
		this.unfolded_programs.clear();
		JDynAlloyModule module = (JDynAlloyModule)super.visit(node);
		module.getPrograms().addAll(this.unfolded_programs);
		return module;
	}

	private String unfold_programId = null;
	private int unfold_index = -1;

	/**
	 * Rebuilds a program that is not recursive as {@code JDynAlloyMutator} does. For a recursive
	 * one, makes the copies of the class comment and returns the program with its calls
	 * redirected.
	 *
	 * <p>As written, the variable that holds the program is reused for each program copied, so the
	 * program returned is built from the last program of the reachable set (a {@code HashSet}, in
	 * its iteration order). That is the visited program only if it comes last or reaches no other
	 * program; otherwise the visited program is replaced by another one, rebuilt. A program
	 * reachable from two recursive programs is copied once for each, with different redirections:
	 * in one module the copies have the same header, so they are {@code equals} and the module's
	 * set keeps the first one added; in two modules, both get copies with the same names.
	 *
	 * @param n the program
	 * @return the rebuilt program
	 * @throws NullPointerException if a node reachable from a recursive program has no program (a
	 *         call whose name and number of arguments match no program)
	 */
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

	/**
	 * Redirects a call that can lead back to the recursive program, inside a copy or inside the
	 * recursive program being rebuilt (see the class comment); rebuilds the other calls. A
	 * redirected call keeps its arguments and its {@code super} flag.
	 *
	 * @param n the call
	 * @return a new {@code JProgramCall}, or a {@code JAssume} of {@code true = false}
	 */
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

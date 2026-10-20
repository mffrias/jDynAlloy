package ar.edu.jdynalloy.xlator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyMutator;
import ar.edu.jdynalloy.ast.JClassConstraint;
import ar.edu.jdynalloy.ast.JClassInvariant;
import ar.edu.jdynalloy.ast.JField;
import ar.edu.jdynalloy.ast.JObjectConstraint;
import ar.edu.jdynalloy.ast.JObjectInvariant;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JRepresents;
import ar.edu.jdynalloy.ast.JSignature;

/**
 * Prunes unused methods from DynJAlloy
 * 
 * @author jgaleotti
 *
 */
class PruneVisitor extends JDynAlloyMutator {

	/**
	 * Returns a new module without the programs that {@link #visit(JProgramDeclaration)}
	 * dropped; the rest is rebuilt as {@code JDynAlloyMutator} does.
	 *
	 * @param node the module
	 * @return the new module
	 */
	@Override
	public Object visit(JDynAlloyModule node) {
		JDynAlloyModule module = (JDynAlloyModule) super.visit(node);

		String moduleId = module.getModuleId();
		JSignature signature = module.getSignature();
		JSignature class_singleton = module.getClassSingleton();
		JSignature literal_singleton = module.getLiteralSingleton();
		List<JField> fields = module.getFields();
		Set<JClassInvariant> class_invariants = module.getClassInvariants();
		Set<JClassConstraint> class_constraints = module.getClassConstraints();
		Set<JObjectInvariant> object_invariants = module.getObjectInvariants();
		Set<JObjectConstraint> object_constraints = module
				.getObjectConstraints();
		Set<JRepresents> represents = module.getRepresents();
		

		/*
		 * prune programs
		 */
		Set<JProgramDeclaration> programs = new HashSet<JProgramDeclaration>();
		for (JProgramDeclaration decl : module.getPrograms()) {
			if (decl != null)
				programs.add(decl);
		}
		
		/*
		 * build fresh module
		 */
		JDynAlloyModule pruned_module = new JDynAlloyModule(moduleId, signature,
				class_singleton, literal_singleton, fields, class_invariants,
				class_constraints, object_invariants, object_constraints,
				represents, programs, node.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants(), 
				node.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants(), node.pinnedForNonRelevancyAnalysisForStryker);

		return pruned_module;
	}

	/**
	 * Creates the pruning pass: {@code JDynAlloyTranslator.translateAll} runs one instance on
	 * every module, after the call graph is built and before the recursion is unfolded
	 * (docs/ARCHITECTURE.md, "The translation", step 5). The programs kept are those whose name
	 * is reached from {@code programToCheck} in the call graph, itself included; the rest are
	 * dropped. Programs are compared by name only (any {@code (...)} in a node of the graph is
	 * ignored), so every program with a reached name is kept, overloads included, in whichever
	 * module. The graph's edges come from {@code call} statements only. No static state.
	 *
	 * @param callGraph the call graph of these modules ({@link CallGraphVisitor})
	 * @param programToCheck the name of the method under analysis: the translator passes the
	 *        configuration's {@code methodToCheck} up to its {@code '('}, that is
	 *        {@code <package>_<Class>_<method>} (docs/NAMING.md)
	 * @param isJavaArith true with Java arithmetic; passed to {@code JDynAlloyVisitor}
	 * @throws NullPointerException if {@code programToCheck} is not a node of the graph, that is,
	 *         if no module has a program of that name ({@link Graph#childrenOf} prints
	 *         "Unknown node : ..." first)
	 */
	public PruneVisitor(Graph<String> callGraph, String programToCheck, boolean isJavaArith) {
		super(isJavaArith);
		this.reachedPrograms = callGraph.descendentsOf(programToCheck);
		this.reachedPrograms.add(programToCheck);
	}

	private final Set<String> reachedPrograms;

	/**
	 * Rebuilds a reached program, and drops the others.
	 *
	 * @param node the program
	 * @return the new program, or null if its name is not reached
	 */
	@Override
	public Object visit(JProgramDeclaration node) {
		HashSet<String> untypedReachedPrograms = new HashSet<String>();
		for (String s : reachedPrograms){
			untypedReachedPrograms.add(removeTyping(s));
		}
		if (untypedReachedPrograms.contains(node.getProgramId())) {
			return super.visit(node);
		} else
			return null;
	}

	
	private String removeTyping(String s) {
		if (s.indexOf("(", 0) >= 0) 
			return s.substring(0, s.indexOf("(", 0));
		else 
			return s;
	}

}

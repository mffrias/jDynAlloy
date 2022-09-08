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

	public PruneVisitor(Graph<String> callGraph, String programToCheck, boolean isJavaArith) {
		super(isJavaArith);
		this.reachedPrograms = callGraph.descendentsOf(programToCheck);
		this.reachedPrograms.add(programToCheck);
	}

	private final Set<String> reachedPrograms;

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

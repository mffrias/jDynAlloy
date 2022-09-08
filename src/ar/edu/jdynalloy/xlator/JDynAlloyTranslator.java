package ar.edu.jdynalloy.xlator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.ast.JClassConstraint;
import ar.edu.jdynalloy.ast.JClassInvariant;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyPrinter;
import ar.edu.jdynalloy.ast.JField;
import ar.edu.jdynalloy.ast.JObjectConstraint;
import ar.edu.jdynalloy.ast.JObjectInvariant;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JRepresents;
import ar.edu.jdynalloy.ast.JSignature;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;

public final class JDynAlloyTranslator {

	private Object inputToFix = null;
	private final JDynAlloyBinding binding;
	private boolean removeQuantifiers;
	private String classToCheck;
	private String methodToCheck;
	
	public boolean getRemoveQuantifiers(){
		return removeQuantifiers;
	}

	public void setRemoveQuantifiers(boolean rq){
		this.removeQuantifiers = rq;
	}
	
	
	public static String headerComment(String fragmentId) {
		return "//-------------- " + fragmentId + "--------------//" + "\n";
	}

	public JDynAlloyTranslator(JDynAlloyBinding binding, String classToCheck, String methodToCheck, Object inputToFix) {
		super();
		this.binding = binding;
		this.inputToFix = inputToFix;
		this.classToCheck = classToCheck;
		this.methodToCheck = methodToCheck;
	}

	public Vector<DynalloyModule> translateAll(JDynAlloyModule[] asts, boolean isJavaArith) {
		Vector<JDynAlloyModule> initial = new Vector<JDynAlloyModule>(Arrays.asList(asts));

		JDynAlloyContext context = createContext(initial);

		Vector<JDynAlloyModule> prepared = prepareAll(context, initial, isJavaArith);

		Vector<DynalloyModule> translated = translateAll(context, prepared, isJavaArith);

		return translated;
	}

	private JDynAlloyContext createContext(Vector<JDynAlloyModule> ms) {
		JDynAlloyContext context = JDynAlloyContext.buildNewContext();
		for (JDynAlloyModule m : ms)
			context.load(m);

		return context;
	}

	private Vector<JDynAlloyModule> prepareAll(JDynAlloyContext context, Vector<JDynAlloyModule> modules, boolean isJavaArith) {

		// create dynamic dispatch
		Vector<JDynAlloyModule> ms = new Vector<JDynAlloyModule>();
		VAlloyVisitor valloyVisitor = new VAlloyVisitor(binding, isJavaArith);
		for (JDynAlloyModule module : modules) {
			JDynAlloyModule resolved_module = (JDynAlloyModule) module.accept(valloyVisitor);
			ms.add(resolved_module);
		}
		
		// transform loop invariants into assert,havoc,assume
		Vector<JDynAlloyModule> transformedModules = new Vector<JDynAlloyModule>();
		JDynAlloyTransformationVisitor transformationVisitor = new JDynAlloyTransformationVisitor(isJavaArith);
		for (JDynAlloyModule module : ms) {
			JDynAlloyModule transformedModule = (JDynAlloyModule) module.accept(transformationVisitor);
			transformedModules.add(transformedModule);
		}
		ms = transformedModules;

		// create spec programs (if wanted)
		if (JDynAlloyConfig.getInstance().getModularReasoning() == true) {
			Vector<JDynAlloyModule> modular_modules = create_spec_programs(ms, isJavaArith);
			ms = modular_modules;

			// print spec programs (if wanted)
			if (JDynAlloyConfig.getInstance().getOutputModularJDynalloy() != null) {

				String filename = JDynAlloyConfig.getInstance().getOutputModularJDynalloy();

				writeDynJAlloyOutput(filename, ms.toArray(new JDynAlloyModule[] {}), isJavaArith);
			}

		}

		// create call graph
		CallGraphVisitor callGraphVisitor = new CallGraphVisitor(isJavaArith, this.classToCheck, this.methodToCheck);
		for (JDynAlloyModule module : ms) {
			module.accept(callGraphVisitor);
		}
		Graph<String> callGraph = callGraphVisitor.getCallGraph();

		// prune unused methods
		String programToCheck = JDynAlloyConfig.getInstance().getMethodToCheck();
		String programToCheclWithoutTyping = programToCheck.substring(0, programToCheck.indexOf('('));

		
		PruneVisitor pruneVisitor = new PruneVisitor(callGraph, programToCheclWithoutTyping, isJavaArith);
		Vector<JDynAlloyModule> prunedModules = new Vector<JDynAlloyModule>();
		for (JDynAlloyModule module : ms) {
			JDynAlloyModule prunedModule = (JDynAlloyModule) module.accept(pruneVisitor);
			prunedModules.add(prunedModule);
		}

		// inline contracts (if needed)

		// Unfold recursion
		Vector<JDynAlloyModule> non_recursive_modules = new Vector<JDynAlloyModule>();
		int recursion_unfold = this.binding.unfoldScopeForRecursiveCode;
		RecursionUnfolderVisitor recursionUnfolderVisitor = new RecursionUnfolderVisitor(callGraph, recursion_unfold, isJavaArith);
		for (JDynAlloyModule module : prunedModules) {
			JDynAlloyModule non_recursive_module = (JDynAlloyModule) module.accept(recursionUnfolderVisitor);
			non_recursive_modules.add(non_recursive_module);
		}

		// create call graph
		CallGraphVisitor non_recursive_callGraphVisitor = new CallGraphVisitor(isJavaArith, this.classToCheck, this.methodToCheck);
		for (JDynAlloyModule module : non_recursive_modules) {
			module.accept(non_recursive_callGraphVisitor);
		}
		Graph<String> non_recursive_callGraph = non_recursive_callGraphVisitor.getCallGraph();

		// fill modifies table
		ModifiesTableBuilder modifiesTableBuilder = new ModifiesTableBuilder();
		Map<String, Set<AlloyVariable>> modifiesTable = modifiesTableBuilder.buildTable(non_recursive_modules, non_recursive_callGraph, isJavaArith);

		context.setModifiesTable(modifiesTable);

		return non_recursive_modules;
	}

	private Vector<JDynAlloyModule> create_spec_programs(Vector<JDynAlloyModule> ms, boolean isJavaArith) {
		Vector<JDynAlloyModule> modular_modules;
		modular_modules = new Vector<JDynAlloyModule>();
		ModularMutator modularMutator = new ModularMutator(isJavaArith);
		for (JDynAlloyModule module : ms) {
			JDynAlloyModule spec_program_module = (JDynAlloyModule) module.accept(modularMutator);
			modular_modules.add(spec_program_module);
		}
		return modular_modules;
	}

	private Vector<DynalloyModule> translateAll(JDynAlloyContext context, Vector<JDynAlloyModule> modules, boolean isJavaArith) {

		Vector<DynalloyModule> ms = new Vector<DynalloyModule>();
		
		// start by translating relevant modules
		for (JDynAlloyModule m : modules) {
			String signatureId = m.getSignature().getSignatureId();

			// Collect vars per module. These vars come from arithmetic constraints and 
			// should be constrained outside the program. That requires to (eventually) prefix them 
			// with a "QF.".
			HashSet<AlloyVariable> sav = new HashSet<AlloyVariable>();
			if (m.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants() != null)
					sav.addAll(m.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants().getVarsInTyping());
			
			for (JProgramDeclaration jpd : m.getPrograms()){
				if (jpd.getVarsResultOfArithmeticOperationsInRequiresAndEnsures() != null)
					sav.addAll(jpd.getVarsResultOfArithmeticOperationsInRequiresAndEnsures().getVarsInTyping());
			}			

			if (containsModule(context.getRelevantModules(), signatureId)) {				
				JDynAlloyXlatorVisitor visitor = new JDynAlloyXlatorVisitor(context, sav, inputToFix, isJavaArith, this.classToCheck, this.methodToCheck);
				visitor.setRemoveQuantifiers(removeQuantifiers);
				DynalloyModule dynalloyModule = (DynalloyModule) m.accept(visitor);
				ms.add(dynalloyModule);
				
				//Will create new modules for those primitive type values that were
				//introduced in the precondition when fixing the input value.
				for (String s : visitor.intsCollected){
					if (!containsModule(context.getRelevantModules(), s)){
						// Create new module and add it to relevant modules
						ExprConstant theParam = new ExprConstant(null, s);
						LinkedList<AlloyExpression> theParams = new LinkedList<>();
						theParams.add(theParam);
						String theNumber = s.substring(27);
						PredicateFormula pred = new PredicateFormula(null, "pred_java_primitive_integer_value_literal_" + theNumber, theParams);
						HashSet<AlloyFormula> theFacts = new HashSet<AlloyFormula>();
						theFacts.add(pred);		
						JSignature sig = new JSignature(true, false, s, new JDynAlloyTyping(), false, "JavaPrimitiveIntegerValue", null, new HashSet<String>(), theFacts, new LinkedList<String>(), new LinkedList<String>(), new String[0]);
						JDynAlloyModule lit = new JDynAlloyModule(s, sig, null, null, new LinkedList<JField>(), new HashSet<JClassInvariant>(), new HashSet<JClassConstraint>(), new HashSet<JObjectInvariant>(), new HashSet<JObjectConstraint>(), new HashSet<JRepresents>(), new HashSet<JProgramDeclaration>(), new AlloyTyping(), new LinkedList<AlloyFormula>(), true);
						context.getRelevantModules().add(lit);
						dynalloyModule = (DynalloyModule) lit.accept(visitor);
						ms.add(dynalloyModule);
					}
				}
				for (String s : visitor.longsCollected){
					if (!containsModule(context.getRelevantModules(), s)){
						// Create new module and add it to relevant modules
						ExprConstant theParam = new ExprConstant(null, s);
						LinkedList<AlloyExpression> theParams = new LinkedList<>();
						theParams.add(theParam);
						String theNumber = s.substring(27);
						PredicateFormula pred = new PredicateFormula(null, "pred_java_primitive_long_value_literal_" + theNumber, theParams);
						HashSet<AlloyFormula> theFacts = new HashSet<AlloyFormula>();
						theFacts.add(pred);		
						JSignature sig = new JSignature(true, false, s, new JDynAlloyTyping(), false, "JavaPrimitiveLongValue", null, new HashSet<String>(), theFacts, new LinkedList<String>(), new LinkedList<String>(), new String[0]);
						JDynAlloyModule lit = new JDynAlloyModule(s, sig, null, null, new LinkedList<JField>(), new HashSet<JClassInvariant>(), new HashSet<JClassConstraint>(), new HashSet<JObjectInvariant>(), new HashSet<JObjectConstraint>(), new HashSet<JRepresents>(), new HashSet<JProgramDeclaration>(), new AlloyTyping(), new LinkedList<AlloyFormula>(), true);
						context.getRelevantModules().add(lit);
						dynalloyModule = (DynalloyModule) lit.accept(visitor);
						ms.add(dynalloyModule);
					}
				}
				for (String s : visitor.floatsCollected){
					if (!containsModule(context.getRelevantModules(), s)){
						// Create new module and add it to relevant modules
						ExprConstant theParam = new ExprConstant(null, s);
						LinkedList<AlloyExpression> theParams = new LinkedList<>();
						theParams.add(theParam);
						String theNumber = s.substring(27);
						PredicateFormula pred = new PredicateFormula(null, "pred_java_primitive_float_value_literal_" + theNumber, theParams);
						HashSet<AlloyFormula> theFacts = new HashSet<AlloyFormula>();
						theFacts.add(pred);		
						JSignature sig = new JSignature(true, false, s, new JDynAlloyTyping(), false, "JavaPrimitiveFloatValue", null, new HashSet<String>(), theFacts, new LinkedList<String>(), new LinkedList<String>(), new String[0]);
						JDynAlloyModule lit = new JDynAlloyModule(s, sig, null, null, new LinkedList<JField>(), new HashSet<JClassInvariant>(), new HashSet<JClassConstraint>(), new HashSet<JObjectInvariant>(), new HashSet<JObjectConstraint>(), new HashSet<JRepresents>(), new HashSet<JProgramDeclaration>(), new AlloyTyping(), new LinkedList<AlloyFormula>(), true);
						context.getRelevantModules().add(lit);
						dynalloyModule = (DynalloyModule) lit.accept(visitor);
						ms.add(dynalloyModule);
					}
				}
				for (String s : visitor.charsCollected){
					if (!containsModule(context.getRelevantModules(), s)){
						// Create new module and add it to relevant modules
						ExprConstant theParam = new ExprConstant(null, s);
						LinkedList<AlloyExpression> theParams = new LinkedList<>();
						theParams.add(theParam);
						String theNumber = s.substring(27);
						PredicateFormula pred = new PredicateFormula(null, "pred_java_primitive_char_value_literal_" + theNumber, theParams);
						HashSet<AlloyFormula> theFacts = new HashSet<AlloyFormula>();
						theFacts.add(pred);		
						JSignature sig = new JSignature(true, false, s, new JDynAlloyTyping(), false, "JavaPrimitiveCharValue", null, new HashSet<String>(), theFacts, new LinkedList<String>(), new LinkedList<String>(), new String[0]);
						JDynAlloyModule lit = new JDynAlloyModule(s, sig, null, null, new LinkedList<JField>(), new HashSet<JClassInvariant>(), new HashSet<JClassConstraint>(), new HashSet<JObjectInvariant>(), new HashSet<JObjectConstraint>(), new HashSet<JRepresents>(), new HashSet<JProgramDeclaration>(), new AlloyTyping(), new LinkedList<AlloyFormula>(), true);
						context.getRelevantModules().add(lit);
						dynalloyModule = (DynalloyModule) lit.accept(visitor);
						ms.add(dynalloyModule);
					}
				}
			}
		}

		// continue with the rest of the modules
		for (JDynAlloyModule m : modules) {
			String signatureId = m.getSignature().getSignatureId();
			HashSet<AlloyVariable> sav = new HashSet<AlloyVariable>();
			if (m.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants() != null)
					sav.addAll(m.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants().getVarsInTyping());
			
			for (JProgramDeclaration jpd : m.getPrograms()){
				if (jpd.getVarsResultOfArithmeticOperationsInRequiresAndEnsures() != null)
					sav.addAll(jpd.getVarsResultOfArithmeticOperationsInRequiresAndEnsures().getVarsInTyping());
			}

			if (!containsModule(context.getRelevantModules(), signatureId)) {
				JDynAlloyXlatorVisitor visitor = new JDynAlloyXlatorVisitor(context, sav, inputToFix, isJavaArith, this.classToCheck, this.methodToCheck);
				DynalloyModule dynalloyModule = (DynalloyModule) m.accept(visitor);
				ms.add(dynalloyModule);
			}
		}
		return ms;
	}

	private boolean containsModule(List<JDynAlloyModule> modules, String signatureId) {
		for (JDynAlloyModule module : modules) {
			if (module.getSignature().getSignatureId().equals(signatureId))
				return true;
		}
		return false;
	}

	public static void writeDynJAlloyOutput(String outputDynJAlloy, JDynAlloyModule[] modules, boolean isJavaArith) {
		StringBuffer sb = new StringBuffer();
		for (JDynAlloyModule m : modules) {
			String modHeader = headerComment(m.getSignature().getSignatureId());
			String modBody = (String) m.accept(new JDynAlloyPrinter(isJavaArith));
			sb.append(modHeader);
			sb.append(modBody);
		}
		try {
			FileWriter writer = new FileWriter(outputDynJAlloy);
			writer.write(sb.toString());
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}

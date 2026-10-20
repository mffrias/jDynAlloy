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

import static ar.edu.taco.simplejml.builtin.JavaPrimitiveFloatValue.create_literal_signature;

/**
 * jDynAlloy's entry point for TACO: translates the JDynAlloy modules of one analysis into dynalloy4
 * {@code DynalloyModule}s. TACO's {@code JDynAlloyToDynAlloyManager.process} creates one per run, in
 * step 5 of {@code JDynAlloyStage.execute}, after the bindings, the relevancy analysis, the slicing
 * and the frame conditions, and hands the result to {@link DynAlloyLinker}
 * (docs/ARCHITECTURE.md, "How TACO calls it").
 *
 * <p>{@link #translateAll(JDynAlloyModule[], boolean)} loads the modules into a new
 * {@link JDynAlloyContext}, prepares them (dynamic dispatch, loop invariants, specification programs
 * with {@code modularReasoning}, call graph, pruning, recursion unfolding, modifies table), and
 * translates each one with a {@link JDynAlloyXlatorVisitor}; docs/ARCHITECTURE.md, "The
 * translation", has the steps in order.
 *
 * <p>Assumes that the binding was built by {@code BindingManager.execute()} on the same modules
 * ({@code VAlloyVisitor} looks every call up in it), that the slicing, if any, already ran (every
 * module given is translated), and that {@code JDynAlloyConfig.getInstance()} is TACO's
 * configuration of this run: its {@code relevantClasses} (set by the relevancy analysis when it
 * runs) are the modules whose invariants go into the pre- and postcondition, and its typed
 * {@code methodToCheck} names the root of the pruning and the program that gets the assertion
 * (docs/NAMING.md, "Where the method under analysis is looked up").
 *
 * <p>No static state of its own, and the configuration is read, not changed. But with a fixed input
 * and Java arithmetic the visitors add literals to the static {@code JavaPrimitive*Value}
 * singletons, which nothing here resets (docs/ARCHITECTURE.md, "State that outlives a run").
 */
public final class JDynAlloyTranslator {

	private Object inputToFix = null;
	private final JDynAlloyBinding binding;
	private boolean removeQuantifiers;
	private String classToCheck;
	private String methodToCheck;
	
	public boolean getRemoveQuantifiers(){
		return removeQuantifiers;
	}

	/**
	 * Sets TACO's {@code dynalloy.toAlloy.removeQuantifiers} (which TACO also gives to dynalloy4),
	 * for the visitors of the relevant modules only: with it, they prefix with {@code QF.} the
	 * variables of the arithmetic encodings ({@link JDynAlloyXlatorVisitor}). The other modules are
	 * translated without it. Call it before {@link #translateAll(JDynAlloyModule[], boolean)}; the
	 * default is false.
	 *
	 * @param rq the flag
	 */
	public void setRemoveQuantifiers(boolean rq){
		this.removeQuantifiers = rq;
	}
	
	
	public static String headerComment(String fragmentId) {
		return "//-------------- " + fragmentId + "--------------//" + "\n";
	}

	/**
	 * Creates a translator for one run.
	 *
	 * @param binding the binding of the modules to translate, with
	 *        {@code unfoldScopeForRecursiveCode} set (TACO sets its loop-unrolling bound)
	 * @param classToCheck the class under analysis as TACO's driver gives it, with dots. It goes to
	 *        the call graphs, where the name it helps build never matches (docs/NAMING.md, "Where
	 *        the method under analysis is looked up"), and to the visitors, which do not read it
	 * @param methodToCheck the method under analysis without its parameter types,
	 *        {@code <package>_<Class>_<method>}: the visitors apply the fixed input to the program
	 *        with this id. The program that gets the assertion is found with the configuration's
	 *        {@code classToCheck} and {@code methodToCheck} instead
	 * @param inputToFix the input to fix, a {@code HashMap<String, Object>} from parameter names
	 *        ({@code thiz} included) to values, or null (docs/ARCHITECTURE.md, "Fixed input")
	 */
	public JDynAlloyTranslator(JDynAlloyBinding binding, String classToCheck, String methodToCheck, Object inputToFix) {
		super();
		this.binding = binding;
		this.inputToFix = inputToFix;
		this.classToCheck = classToCheck;
		this.methodToCheck = methodToCheck;
	}

	/**
	 * Translates the modules of one analysis: loads them into a new {@link JDynAlloyContext},
	 * prepares them and translates each one (see the class comment).
	 *
	 * <p>The result has each relevant module (one whose id is in {@code relevantClasses}), each
	 * followed by the literal modules its visitor asked for, and then the other modules. A literal
	 * module is made for each {@code JavaPrimitive*Literal} constant that the visitor found in the
	 * {@code requires} of a specification case (with a fixed input, in the formula that fixes it;
	 * see {@code JDynAlloyXlatorVisitor.visit(JSpecCase)}) and that is not a relevant module yet: a
	 * {@code one sig} that extends {@code JavaPrimitiveIntegerValue} (or the long, char or float
	 * one), with the literal's predicate as its fact; the number in the predicate's name is read
	 * from the literal's name at a fixed offset (docs/KNOWN-LIMITATIONS.md, "Analysis"). For a
	 * negative value that gives {@code pred_java_primitive_integer_value_literal_Minus<n>}, while
	 * the literal's predicate is {@code ..._literal_minus_<n>}, so Alloy rejects the model: this
	 * happens for negative values reached through the fields of a fixed input (a negative
	 * literal written in a {@code requires} already has its module; TACO's
	 * docs/FIXES-FOR-MASTER.md, F15). The new
	 * module is added to the context's relevant modules, so it is made once, and a module with the
	 * same id among {@code asts} is then not translated. Only the relevant modules' visitors get the
	 * flag of {@link #setRemoveQuantifiers(boolean)}.
	 *
	 * <p>Side effects: with {@code modularReasoning} and {@code outputModularJDynalloy}, writes the
	 * modules with their specification programs to that file ({@link #writeDynJAlloyOutput}).
	 *
	 * <p>Assumes that the configuration's {@code methodToCheck} has its parameter types in
	 * parentheses: the pruning takes it up to the {@code (} (a
	 * {@code StringIndexOutOfBoundsException} without one).
	 *
	 * @param asts the modules, after TACO's slicing and frame conditions
	 * @param isJavaArith true with Java arithmetic (TACO's {@code useJavaArithmetic}), for every
	 *        visitor
	 * @return the DynAlloy modules, in the order above
	 */
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
		callGraph.setExtraData(ms);

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
						int beginIndex = 27;
						String theNumber = s.substring(beginIndex);
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
						int beginIndex = 24;
						String theNumber = s.substring(beginIndex);
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
						int beginIndex = 25;
						String theNumber = s.substring(beginIndex);
						long parsedLong = Long.parseLong(theNumber, 16);
						float actualFloat = Float.intBitsToFloat((int) parsedLong);
						PredicateFormula pred = new PredicateFormula(null, "pred_java_primitive_float_value_literal_" + theNumber, theParams);
						HashSet<AlloyFormula> theFacts = new HashSet<AlloyFormula>();
						theFacts.add(pred);
						JSignature sig = create_literal_signature(actualFloat);
//						JSignature sig = new JSignature(true, false, s, new JDynAlloyTyping(), false, "JavaPrimitiveFloatValue", null, new HashSet<String>(), theFacts, new LinkedList<String>(), new LinkedList<String>(), new String[0]);
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
						int beginIndex = 24;
						String theNumber = s.substring(beginIndex);
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

	/**
	 * Writes modules as JDynAlloy text ({@link JDynAlloyPrinter}) to one file, each after the line
	 * {@code //-------------- <signature id>--------------//}. {@code translateAll} uses it for
	 * {@code outputModularJDynalloy}. An {@code IOException} is printed, not thrown.
	 *
	 * @param outputDynJAlloy the path of the file, replaced if it exists
	 * @param modules the modules
	 * @param isJavaArith passed to the printer
	 */
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

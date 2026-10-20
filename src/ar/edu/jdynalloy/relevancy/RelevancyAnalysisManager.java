/*
 * TACO: Translation of Annotated COde
 * Copyright (c) 2010 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
package ar.edu.jdynalloy.relevancy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.JDynAlloyException;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JClassInvariant;
import ar.edu.jdynalloy.ast.JField;
import ar.edu.jdynalloy.ast.JObjectInvariant;
import ar.edu.jdynalloy.ast.JPostcondition;
import ar.edu.jdynalloy.ast.JPrecondition;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JRepresents;
import ar.edu.jdynalloy.ast.JSpecCase;
import ar.edu.jdynalloy.ast.JStatement;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.binding.fieldcollector.FieldCollectorVisitor;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.factory.JSignatureFactory;
import ar.edu.jdynalloy.factory.JTypeFactory;
import ar.edu.jdynalloy.xlator.JDynAlloyBinding;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;

/**
 * Finds what the method under analysis can reach: the relevancy analysis, step 2 of TACO's
 * {@code JDynAlloyStage}. TACO runs it only with {@code relevancyAnalisys}, after
 * {@code BindingManager.execute()} and before {@code SceneSlicerManager}, which slices the modules
 * to what the {@link Scene} of this analysis holds (docs/ARCHITECTURE.md, "How TACO calls it").
 *
 * <p>{@link #process} starts the scene with the method under analysis, the module of
 * {@code classToCheck}, the modules pinned for Stryker
 * ({@code pinnedForNonRelevancyAnalysisForStryker}; TACO pins the literal modules of a fixed
 * input) and the module {@code ClassFields}. Then it analyses each element of the scene once,
 * programs before modules, until every element has been analysed, and adds to the scene what each
 * analysis reaches. Nothing is ever removed.
 * <ul>
 * <li>A program: the types of its parameters and of the variables for the arithmetic of its
 *     contracts, its {@code requires} and {@code ensures}, its body
 *     ({@link RelevancyAnalysisStatementVisitor}) and the predicates for that arithmetic. The body
 *     of every program in the scene is analysed, not only that of the method under analysis
 *     (docs/KNOWN-LIMITATIONS.md, "Other").</li>
 * <li>A module: the module of its superclass, the facts of its signature, its {@code represents}
 *     clauses, its object invariants (and their arithmetic predicates) and class invariants, the
 *     types of its fields, and the modules that some built-in modules need
 *     ({@code JavaPrimitiveCharValue} for {@code java_lang_CharArray}, for example). Its programs
 *     are not analysed: a program is reached only through a call.</li>
 * </ul>
 * Formulas go through {@link RelevancyAnalysisUtils#analyzeFormula}, which adds the programs they
 * call and the modules of the types they mention. Unclear: object and class constraints are not
 * analysed, and neither are loop invariants ({@link RelevancyAnalysisStatementVisitor}), although
 * the translation uses them; whether that is intended is an open question.
 *
 * <p>Static state: {@code javaArithmetic} (see {@link #setJavaArithmetic(boolean)}) keeps its
 * value between runs in one JVM, and {@link #process} sets the static {@code bitWidth} of
 * {@link RelevancyAnalysisUtils}. TACO sets the first before every run, {@code process} sets the
 * second in every run, and neither changes the result (see the two setters; docs/ARCHITECTURE.md,
 * "State that outlives a run"). The rest is per instance; TACO creates one instance per run.
 */
public class RelevancyAnalysisManager {
	private static Logger log = Logger
			.getLogger(RelevancyAnalysisManager.class);

	JDynAlloyBinding dynJAlloyBinding;
	List<JDynAlloyModule> modules;
	RelevancyAnalysisSymbolTable symbolTable;

	private ar.edu.jdynalloy.relevancy.Scene scene;

	private int bitWidth;

	private static boolean javaArithmetic;

	/**
	 * Sets the bit width that {@link #process} passes to the static
	 * {@link RelevancyAnalysisUtils#setBitWidth(int)} before it analyses each program. TACO passes
	 * its {@code bitwidth}. The default is 0.
	 *
	 * @param bw the bit width
	 */
	public void setBitWidth(int bw){
		this.bitWidth = bw;
	}

	public boolean getJavaArithmetic(){
		return this.javaArithmetic;
	}

	/**
	 * Sets the static {@code javaArithmetic} flag, which all instances share and which keeps its
	 * value for later runs in the same JVM. {@link #process} only passes it to the
	 * {@code FieldCollectorVisitor} that fills its symbol table, whose flag of that name nothing
	 * reads; the analysis itself uses the {@code isJavaArith} argument of {@code process}. TACO sets
	 * both from {@code useJavaArithmetic}. Unclear: why the flag is static, and whether it is still
	 * needed next to that argument.
	 *
	 * @param b true with Java arithmetic
	 */
	public void setJavaArithmetic(boolean b){
		this.javaArithmetic = b;
	}

	/**
	 * Runs the relevancy analysis (see the class comment) and returns the ids of the modules it
	 * reached; the scene stays available through {@link #getScene()}.
	 *
	 * <p>Assumes that {@code JDynAlloyConfig.getInstance()} is set, with {@code classToCheck} and the
	 * typed {@code methodToCheck}: the method under analysis is the program of module
	 * {@code classToCheck} whose {@code <program>(<parameter types>)} equals {@code methodToCheck}
	 * ({@link RelevancyAnalysisUtils#findMethodToCheckDeclaration}; docs/NAMING.md, "Where the method
	 * under analysis is looked up"). Assumes also that {@code dynJAlloyBinding} was built by
	 * {@code BindingManager.execute()} on these modules: calls are looked up in it by equality, and a
	 * call it lacks stops the analysis. Builds its own symbol table with the fields of all the
	 * modules ({@code FieldCollectorVisitor}). The modules are not changed.
	 *
	 * @param modules every module of the run
	 * @param dynJAlloyBinding the bindings of the same modules
	 * @param isJavaArith true with Java arithmetic; passed to the symbol table and the visitors
	 * @return the ids of the modules in the scene, separated by {@code ", "}, in no particular order
	 *         (they go through a {@code HashSet}). TACO stores the string as {@code relevantClasses}
	 * @throws JDynAlloyException if the method under analysis is not found ("The method to check
	 *         was not found"; a plain {@code JDynAlloyException}, where {@code ModifiesSolverManager}
	 *         throws a {@code MethodToCheckNotFoundException}), if a type it meets is not
	 *         {@code boolean}, {@code Int}, {@code univ} or the id of a module or of its literal
	 *         signature ("Module '...' not found."), if a module that a built-in module needs is
	 *         missing ("Module JavaPrimitiveCharValue not found.", ...), if a {@code callSpec} has no
	 *         binding ("Binding not found for: null"), or if a function called in a specification is
	 *         neither bound nor known ("Program ... called in specification cannot be found.")
	 * @throws IllegalArgumentException if a {@code call} statement has no binding: the scene rejects
	 *         the null program ("Argument must not be null")
	 */
	public String process(List<JDynAlloyModule> modules,
			JDynAlloyBinding dynJAlloyBinding, boolean isJavaArith) {		
		this.dynJAlloyBinding = dynJAlloyBinding;
		this.modules = modules;

		log.debug("Making relevancy analysis: ");
		this.symbolTable = createSymbolTable(modules, isJavaArith);

		JProgramDeclaration methodToCheckDeclaration = RelevancyAnalysisUtils
				.findMethodToCheckDeclaration(modules);

		// scene = <{ Program_to_check }, {}, {}>
		scene = new Scene();


		String moduleName = JDynAlloyConfig.getInstance().getClassToCheck();

		/* Keyword "Instrumented" as part of class/method names seems to be obsolete.
		String[] splitModuleName = moduleName.split("_");
		moduleName = "";
		for (int idx = 0; idx < splitModuleName.length - 2; idx++){
			moduleName += splitModuleName[idx] + "_";
		}
		if (splitModuleName.length > 1){
			moduleName += splitModuleName[splitModuleName.length - 2] + "Instrumented_";
		}
		moduleName += splitModuleName[splitModuleName.length - 1];
		 */


		JDynAlloyModule moduleToCheck = RelevancyAnalysisUtils
				.findModuleByName(moduleName, modules);
		scene.addModule(moduleToCheck);
		scene.addProgram(methodToCheckDeclaration);
		for (JDynAlloyModule m : modules){
			if (m.pinnedForNonRelevancyAnalysisForStryker){
				scene.addModule(m);
			}
		}

		// add class fields to scene
		// scene = <{ Program_to_check }, {CLASS_FIELDS}, {}>
		JDynAlloyModule classFields = RelevancyAnalysisUtils.findModuleByName(
				JSignatureFactory.buildClassFieldsSignature().getSignatureId(),
				this.modules);
		scene.addModule(classFields);

		relevancyAnalysis(scene, isJavaArith);

		String relevantClassesAsString = extractRelevantClassesAsString(scene);
		return relevantClassesAsString;

	}

	private String extractRelevantClassesAsString(Scene scene) {
		Set<String> relevantTypesSet = new HashSet<String>();
		for (JDynAlloyModule module : scene.getModules()) {
			relevantTypesSet.add(module.getModuleId());
		}
		StringBuffer buffer = new StringBuffer();
		for (String moduleName : relevantTypesSet) {
			if (buffer.length() != 0) {
				buffer.append(", ");
			}
			buffer.append(moduleName);
		}
		return buffer.toString();
	}

	private void relevancyAnalysis(Scene scene, boolean isJavaArith) {

		// marked = {}
		Set<Object> marked = new HashSet<Object>();

		// While Some X / X in scene and (X in marked)
		while (!scene.isContainedBy(marked)) {
			Object x = findNotMarkedElement(marked, scene);
			marked.add(x);
			relevancyAnalysisIteration(scene, x, isJavaArith);
		}

	}

	private void relevancyAnalysisIteration(Scene scene, Object x, boolean isJavaArith) {
		if (x instanceof JProgramDeclaration) {
			JProgramDeclaration program = (JProgramDeclaration) x;
			relevancyAnalysisIterationProgram(scene, program, isJavaArith);

		} else if (x instanceof JField) {
			JField field = (JField) x;
			relevancyAnalysisIterationField(scene, field);

		} else if (x instanceof JDynAlloyModule) {
			JDynAlloyModule module = (JDynAlloyModule) x;
			relevancyAnalysisIterationModule(scene, module);

		} else {
			throw new JDynAlloyException("Not supported");
		}

	}

	private void relevancyAnalysisIterationProgram(Scene scene,
			JProgramDeclaration program, boolean isJavaArith) {

		this.symbolTable.beginScope();		
		
		for (JVariableDeclaration varDeclaration : program.getParameters()) {
			this.symbolTable.insertLocal(varDeclaration.getVariable()
					.getVariableId(), varDeclaration.getType());
		}

		for (JVariableDeclaration varDeclaration : program.getParameters()) {
			JType type = varDeclaration.getType();
			RelevancyAnalysisUtils.findModuleAndToScene(scene, type,
					this.modules);
		}


		//Add those variables arising from arithmetic operations in contracts.
		for (AlloyVariable av : program.getVarsResultOfArithmeticOperationsInRequiresAndEnsures().varSet()) {
			this.symbolTable.insertLocal(av.getVariableId(), 
					JType.parse(program.getVarsResultOfArithmeticOperationsInRequiresAndEnsures().get(av)));
		}

		//Check the types of variables coming from contracts are indeed considered relevant.
		for (AlloyVariable av : program.getVarsResultOfArithmeticOperationsInRequiresAndEnsures().varSet()) {
			JType type = JType.parse(program.getVarsResultOfArithmeticOperationsInRequiresAndEnsures().get(av));
			RelevancyAnalysisUtils.findModuleAndToScene(scene, type,
					this.modules);
		}


		// For each C in X.SpecCase
		// AnalyzeFormula( scene, C.Precondition )
		// AnalyzeFormula( scene, C.Postcondition )
		// End For each

		RelevancyAnalysisUtils.setBitWidth(this.bitWidth);

		for (JSpecCase specCase : program.getSpecCases()) {
			for (JPrecondition precondition : specCase.getRequires()) {
				RelevancyAnalysisUtils.analyzeFormula(scene, precondition
						.getFormula(), this.symbolTable, this.dynJAlloyBinding,
						this.modules);
			}
			for (JPostcondition posPostcondition : specCase.getEnsures()) {
				RelevancyAnalysisUtils.analyzeFormula(scene, posPostcondition
						.getFormula(), this.symbolTable, this.dynJAlloyBinding,
						this.modules);
			}
		}


		// If X = Program_to_check
		// For each s in x.Body
		// AnalizeStatement( scene, s )
		// End For each
		JProgramDeclaration methodToCheckDeclaration = RelevancyAnalysisUtils
				.findMethodToCheckDeclaration(modules);
		analizeStatement(scene, program.getBody(), isJavaArith);

		// End If
		// End If
		
		for (AlloyFormula af : program.getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures()) {
			RelevancyAnalysisUtils.analyzeFormula(scene, af,
					this.symbolTable, this.dynJAlloyBinding,
					this.modules);
		}


		this.symbolTable.endScope();
	}

	private void relevancyAnalysisIterationField(Scene scene, JField field) {
		// If X is Field
		// scene.add( x.Type )
		// End If
		JType type = field.getFieldType();
		RelevancyAnalysisUtils.findModuleAndToScene(scene, type, this.modules);
	}

	private void relevancyAnalysisIterationModule(Scene scene,
			JDynAlloyModule module) {
		// If X is Module

		// add "thiz" to SymbolTable
		this.symbolTable.beginScope();

		JType jtype = JTypeFactory.buildReference(module.getModuleId());
		this.symbolTable.insertLocal(JExpressionFactory.THIS_VARIABLE
				.getVariableId(), jtype);

		// If (X.Super != null)
		// Scene.add( X.Super.Type )
		// End If
		if (module.getSignature().isExtension()) {
			String superModuleName = module.getSignature().getExtSigId();
			JDynAlloyModule superModule = RelevancyAnalysisUtils
					.findModuleByName(superModuleName, this.modules);
			scene.addModule(superModule);
		}

		// For each F in X.Signature.Axioms
		// AnalyzeFormula(scene, F)
		for (AlloyFormula alloy_fact : module.getSignature().getFacts()) {
			RelevancyAnalysisUtils.analyzeFormula(scene, alloy_fact,
					this.symbolTable, this.dynJAlloyBinding, this.modules);
		}

		// For each J in X.Represents
		// Scene.add( J.ReferencedField.Type )
		// AnalyzeFormula(scene, J.Predicate )
		// End For each
		for (JRepresents represents : module.getRepresents()) {
			RelevancyAnalysisUtils.findModuleAndToScene(scene, represents
					.getExpressionType(), this.modules);
			RelevancyAnalysisUtils.analyzeFormula(scene, represents
					.getFormula(), this.symbolTable, this.dynJAlloyBinding,
					this.modules);
		}

		// For each i in X.Invariant
		// AnalyzeFormula(scene, I.Predicate )
		// End For each
		for (JObjectInvariant invariant : module.getObjectInvariants()) {
			RelevancyAnalysisUtils.analyzeObjectInvariant(scene,
					invariant.getFormula(), this.symbolTable,
					this.dynJAlloyBinding, this.modules);
		}

		if (module.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants() != null){
			for (AlloyFormula af : module.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants()){
				RelevancyAnalysisUtils.analyzeFormula(scene,
						af, this.symbolTable,
						this.dynJAlloyBinding, this.modules);

			}
		}

		for (JClassInvariant invariant : module.getClassInvariants()) {
			RelevancyAnalysisUtils.analyzeFormula(scene,
					invariant.getFormula(), this.symbolTable,
					this.dynJAlloyBinding, this.modules);
		}


		// For each f in X.Fields
		// AnalyzeField(scene, f )
		// End For each
		for (JField field : module.getFields()){
			relevancyAnalysisIterationField(scene, field);
		}


		// For each primitiveTypeUsed t
		// Add(scene, t )
		// End For each
		relevancyAnalysisAddRequiredTypes(scene, module);



		this.symbolTable.endScope();
	}


	//For the time being, this method will encode for some types (notably arrays) the types they require, and that were
	//incorrectly considered irrelevant at this point. This should be generalized.
	private void relevancyAnalysisAddRequiredTypes(Scene scene2, JDynAlloyModule module) {
		if (module.getModuleId().equals("java_lang_CharArray")){
			JDynAlloyModule theCharMod = null;
			for (JDynAlloyModule mod : this.modules){
				if (mod.getModuleId().equals("JavaPrimitiveCharValue")){
					theCharMod = mod;
					break;
				}
			}
			if (theCharMod != null){
				scene.addModule(theCharMod);
			} else {
				throw new JDynAlloyException("Module JavaPrimitiveCharValue not found.");
			}
		}
		
		if (module.getModuleId().equals("java_lang_LongArray")){
			JDynAlloyModule theLongMod = null;
			for (JDynAlloyModule mod : this.modules){
				if (mod.getModuleId().equals("JavaPrimitiveLongValue")){
					theLongMod = mod;
					break;
				}
			}
			if (theLongMod != null){
				scene.addModule(theLongMod);
			} else {
				throw new JDynAlloyException("Module JavaPrimitiveLongValue not found.");
			}
		}


		if (module.getModuleId().equals("JavaPrimitiveLongValue")){
			JDynAlloyModule theCharMod = null;
			JDynAlloyModule theIntMod = null;
			for (JDynAlloyModule mod : this.modules){
				if (mod.getModuleId().equals("JavaPrimitiveCharValue")){
					theCharMod = mod;
				}
				if (mod.getModuleId().equals("JavaPrimitiveIntegerValue")){
					theIntMod = mod;
				}

			}
			if (theCharMod != null){
				scene.addModule(theCharMod);
			} else {
				throw new JDynAlloyException("Module JavaPrimitiveCharValue not found.");
			}
			if (theIntMod != null){
				scene.addModule(theIntMod);
			} else {
				throw new JDynAlloyException("Module JavaPrimitiveIntegerValue not found.");
			}

		}

		if (module.getModuleId().equals("JavaPrimitiveIntegerValue")){
			JDynAlloyModule theCharMod = null;
			for (JDynAlloyModule mod : this.modules){
				if (mod.getModuleId().equals("JavaPrimitiveCharValue")){
					theCharMod = mod;
				}

			}
			if (theCharMod != null){
				scene.addModule(theCharMod);
			} else {
				throw new JDynAlloyException("Module JavaPrimitiveCharValue not found.");
			}
		}
	}

	private void analizeStatement(Scene scene, JStatement body, boolean isJavaArith) {
		RelevancyAnalysisStatementVisitor relevancyAnalysisStatementVisitor = new RelevancyAnalysisStatementVisitor(
				this.dynJAlloyBinding, this.modules, this.symbolTable, scene, isJavaArith);
		body.accept(relevancyAnalysisStatementVisitor);
	}

	/**
	 * Obtain next element to analyze
	 * 
	 * @param marked
	 * @param scene
	 * @return
	 */
	private Object findNotMarkedElement(Set<Object> marked, Scene scene) {
		for (JProgramDeclaration element : scene.getPrograms()) {
			if (!marked.contains(element)) {
				marked.add(element);
				return element;
			}
		}

		for (JDynAlloyModule element : scene.getModules()) {
			if (!marked.contains(element)) {
				marked.add(element);
				return element;
			}
		}

		for (JField element : scene.getFields()) {
			if (!marked.contains(element)) {
				marked.add(element);
				return element;
			}
		}

		return null;
	}

	private static RelevancyAnalysisSymbolTable createSymbolTable(
			List<JDynAlloyModule> modules, boolean isJavaArith) {
		RelevancyAnalysisSymbolTable symbolTable = new RelevancyAnalysisSymbolTable();
		symbolTable.setJavaArithmetic(isJavaArith);
		FieldCollectorVisitor fieldCollectorVisitor = new FieldCollectorVisitor(symbolTable, isJavaArith);
		fieldCollectorVisitor.setJavaArithmetic(javaArithmetic);
		// ProgramDeclarationCollectorVisitor and fieldCollectorVisitor do not have
		// interdependences.
		// They can run together. But semanticCheckVisitor visitor needs the
		// Fields collected by fieldCollectorVisitor.
		// fieldCollectorVisitor must be run before semanticCheckVisitor
		for (JDynAlloyModule dynJAlloyModule : modules) {
			dynJAlloyModule.accept(fieldCollectorVisitor);
			//			if (dynJAlloyModule.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants() != null){
			//				for (AlloyVariable av : dynJAlloyModule.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants().varSet()){
			//					fieldCollectorVisitor.getSymbolTable().insertField(dynJAlloyModule.getModuleId(), av.getVariableId().getString(), new JType(dynJAlloyModule.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants().get(av)));
			//				}
			//			}	
		}
		return symbolTable;
	}

	/**
	 * Returns the scene of the last {@link #process}: the programs and modules it reached. TACO
	 * passes it to {@code SceneSlicerManager.process}, which keeps only the programs in it.
	 *
	 * @return the scene, or null before {@code process} has run
	 */
	public Scene getScene() {
		return scene;
	}

}

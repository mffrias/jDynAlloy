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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.JDynAlloyException;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.binding.callbinding.PredicateCallAlloyFormulaDescriptor;
import ar.edu.jdynalloy.binding.callbinding.FunctionCallAlloyFormulaDescriptor;
import ar.edu.jdynalloy.binding.callbinding.PredicateAndFunctionCallRelevancyVisitor;
import ar.edu.jdynalloy.xlator.JDynAlloyBinding;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.IProgramCall;

/**
 * Static helpers of the relevancy analysis ({@link RelevancyAnalysisManager}): finding the method
 * under analysis and the module of a type, and analysing a formula, which adds to the
 * {@link Scene} what the formula reaches.
 *
 * <p>Static state: {@code bitWidth} ({@link #setBitWidth(int)}), which keeps its value between
 * runs in one JVM; {@code RelevancyAnalysisManager.process} sets it again in every run, and it
 * has no effect.
 */
public class RelevancyAnalysisUtils {


	private static int bitWidth;


	private RelevancyAnalysisUtils() {

	}

	/**
	 * Sets the static bit width. {@code RelevancyAnalysisManager} calls it before it analyses each
	 * program. It is read only to choose the {@code JavaPrimitiveIntegerLiteral} modules (those from
	 * 0 to {@code 2^(bitWidth-1) - 1}) that a formula calling
	 * {@code fun_java_primitive_integer_value_size_of} needs, but the test for that call is always
	 * false: it asks a list of {@code FunctionCallAlloyFormulaDescriptor}s whether it contains a
	 * string. So the value has no effect. Unclear: whether those literal modules are meant to be
	 * added.
	 *
	 * @param bw the bit width (TACO's {@code bitwidth})
	 */
	public static void setBitWidth(int bw){
		bitWidth = bw;
	}

	/**
	 * 
	 * @param moduleId
	 * @return "null" if moduleId is a primitive type or module called
	 *         "moduleId"
	 */
	static JDynAlloyModule findModuleByName(String moduleId,
			List<JDynAlloyModule> modules) {
		if (moduleId.equals("boolean") || moduleId.equals("Int")
				|| moduleId.equals("univ")) {
			return null;
		}
		for (JDynAlloyModule module : modules) {
			if (module.getModuleId().equals(moduleId)) {
				return module;
			} else if (module.getLiteralSingleton() != null
					&& module.getLiteralSingleton().getSignatureId().equals(
							moduleId)) {
				return module;
			}
		}
		throw new JDynAlloyException("Module '" + moduleId + "' not found.");
	}

	/**
	 * Adds to the scene the module of a type: the one whose id, or the id of whose literal signature,
	 * is {@code type.dpdTypeNameExtract()}. Nothing is added for {@code boolean}, {@code Int} and
	 * {@code univ}.
	 *
	 * @param scene the scene to add to
	 * @param type the type
	 * @param modules every module of the run
	 * @throws JDynAlloyException if no module has that id ("Module '...' not found.")
	 */
	static void findModuleAndToScene(Scene scene, JType type,
			List<JDynAlloyModule> modules) {
		String moduleName = type.dpdTypeNameExtract();
		JDynAlloyModule module = RelevancyAnalysisUtils.findModuleByName(
				moduleName, modules);
		// if module isn't primitive type (ie: Int)
		if (module != null) {
			scene.addModule(module);
		}
	}

	/**
	 * Returns the program of the method under analysis: the program of the module
	 * {@code classToCheck} whose {@code <program>(<parameter types>)} equals the configuration's
	 * {@code methodToCheck}. The parameter types leave out {@code thiz}, {@code throw},
	 * {@code return} and the {@code returnVarFromSpecMethodCall} variables, drop {@code null} from
	 * {@code X+null} and {@code null+X}, and use Java's names for primitive values and their arrays
	 * ({@code int}, {@code long[]}, ...), as TACO's {@code methodToCheck} does (docs/NAMING.md,
	 * "Where the method under analysis is looked up"). Reads {@code JDynAlloyConfig.getInstance()}.
	 *
	 * @param modules the modules to search
	 * @return the program
	 * @throws JDynAlloyException if no program matches ("The method to check was not found")
	 */
	public static JProgramDeclaration findMethodToCheckDeclaration(
			List<JDynAlloyModule> modules) {
		JProgramDeclaration methodToCheckDeclaration = null;

		String classToCheck = JDynAlloyConfig.getInstance().getClassToCheck();

		/* Keyword "Instrumented" as part of class/method names seems to be obsolete.
		String[] splitClassToCheck = classToCheck.split("_");
		classToCheck = "";
		for (int idx = 0; idx < splitClassToCheck.length - 2; idx++){
			classToCheck += splitClassToCheck[idx] + "_";
		}
		if (splitClassToCheck.length > 1){
			classToCheck += splitClassToCheck[splitClassToCheck.length - 2] + "Instrumented_";
		}
		classToCheck += splitClassToCheck[splitClassToCheck.length - 1];
		 */

		String methodToCheck = JDynAlloyConfig.getInstance().getMethodToCheck();

		/* Keyword "Instrumented" as part of class/method names seems to be obsolete.
		String[] splitMethodToCheck = methodToCheck.split("_");
		methodToCheck = "";
		for (int idx = 0; idx < splitMethodToCheck.length - 4; idx++){
			methodToCheck += splitMethodToCheck[idx] + "_";
		}
		if (splitMethodToCheck.length >= 4){
			methodToCheck += splitMethodToCheck[splitMethodToCheck.length - 4] + "Instrumented_";
		}
		methodToCheck += splitMethodToCheck[splitMethodToCheck.length - 3] + "_";
		methodToCheck += splitMethodToCheck[splitMethodToCheck.length - 2] + "_";
		methodToCheck += splitMethodToCheck[splitMethodToCheck.length - 1];
		 */


		for (JDynAlloyModule dynJAlloyModule : modules) {
			if (dynJAlloyModule.getModuleId().equals(classToCheck)) {

				for (JProgramDeclaration programDeclaration : dynJAlloyModule
						.getPrograms()) {
					String qualifiedMethodName = programDeclaration.getProgramId() + "(" + filterParameters(programDeclaration.getParameters()) + ")";
					if (methodToCheck.equals(qualifiedMethodName)) {
						methodToCheckDeclaration = programDeclaration;
					}
				}
			}
		}

		if (methodToCheckDeclaration == null) {
			String moreInfo = "classToCheck: "
					+ JDynAlloyConfig.getInstance().getClassToCheck()
					+ ". methodToCheck: "
					+ JDynAlloyConfig.getInstance().getMethodToCheck();
			throw new JDynAlloyException(
					"The method to check was not found. Please source files and check the configurations keys 'classToCheck' and 'methodToCheck'. "
							+ moreInfo);
		}
		return methodToCheckDeclaration;
	}

	private static String filterParameters(List<JVariableDeclaration> parameters) {
		String params = "";
		boolean hasParams = false;
		for (JVariableDeclaration v : parameters){
			if (!v.getVariable().getVariableId().getString().equals("thiz") && !v.getVariable().getVariableId().getString().equals("throw") && !v.getVariable().getVariableId().getString().equals("return") && !v.getVariable().getVariableId().getString().startsWith("returnVarFromSpecMethodCall")){
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
				if (param.contains("+null")) {
					param = param.substring(0,param.indexOf('+'));
				} else {
					if (param.contains("null+")) {
						param = param.substring(5,param.length());
					}
				}


				params += param + ",";
				hasParams = true;
			}
		}
		if (hasParams)
			params = params.substring(0, params.length()-1);
		return params;

	}

	/**
	 * Adds to the scene what a formula reaches. Visits the formula with a
	 * {@code PredicateAndFunctionCallRelevancyVisitor} and a
	 * {@link RelevantAnalysisExpressionTypeResolver}, and adds:
	 * <ul>
	 * <li>the program bound to each {@code callSpec} or {@code staticCallSpec};</li>
	 * <li>the modules of the types the symbol table recorded during the visit (bound variables of
	 *     quantifiers, fields looked up) and of the constants;</li>
	 * <li>the program of each function call the visitor collected (only those before the last
	 *     predicate formula: see the visitor's {@code getCalledFunctions()}), looked up as a
	 *     {@code call} statement with the same name and arguments. A call without a
	 *     binding is skipped if it is one of a fixed list of functions of the prelude and of the
	 *     Java-arithmetic modules ({@code add}, {@code arrayLength},
	 *     {@code fun_java_primitive_integer_value_add}, ...), and is an error otherwise.</li>
	 * </ul>
	 * Unclear: {@code BindingManager} binds no function calls (that code is commented out in
	 * {@code SemanticCheckVisitor}), so a function resolves only if some {@code call} statement has
	 * the same name and arguments; whether functions in specifications are meant to be bound is an
	 * open question.
	 *
	 * <p>Enables the recording of {@code symbolTable} for the visit and disables it after, and
	 * replaces its set of relevant types.
	 *
	 * @param scene the scene to add to
	 * @param formula the formula
	 * @param symbolTable the analysis's table, with the variables in scope
	 * @param dynJAlloyBinding the bindings
	 * @param modules every module of the run
	 * @throws JDynAlloyException as {@link RelevancyAnalysisManager#process} says: "Binding not found
	 *         for: null", "Module '...' not found.", "Program ... called in specification cannot be
	 *         found."
	 */
	public static void analyzeFormula(Scene scene, AlloyFormula formula,
			RelevancyAnalysisSymbolTable symbolTable,
			JDynAlloyBinding dynJAlloyBinding, List<JDynAlloyModule> modules) {

		PredicateAndFunctionCallRelevancyVisitor visitor = new PredicateAndFunctionCallRelevancyVisitor(
				symbolTable);

		RelevantAnalysisExpressionTypeResolver expressionTypeResolver = new RelevantAnalysisExpressionTypeResolver(
				visitor, symbolTable);
		visitor.setExpressionVisitor(expressionTypeResolver);

		symbolTable.setEnableRelevantAnalysis(true);
		symbolTable.setRelevantTypes(new HashSet<JType>());

		formula.accept(visitor);

		//		visitor.getCalledFunctions().addAll(((RelevantAnalysisExpressionTypeResolver)visitor.getDfsExprVisitor()).getCalledFunctionsNames());
		//		for (String funName : visitor.getCalledFunctionsNames()){
		//			if ()
		//			JProgramCall fakePC = new JProgramCall(false, funName, )
		//			if (dynJAlloyBinding.resolve(node))
		//				
		//			JProgramDeclaration programDeclaration = this.dynJAlloyBinding.resolve(node);
		//			scene.addProgram(programDeclaration);
		//			//JPG::workaround to use AnalyzeFormula
		//			PredicateFormula predFormula = new PredicateFormula(null,node.getProgramId(),node.getArguments());
		//			RelevancyAnalysisUtils.analyzeFormula(scene, predFormula, symbolTable, dynJAlloyBinding, modules);
		//
		//		}
		processCollectedFunctionNames(scene, visitor, modules);


		symbolTable.setEnableRelevantAnalysis(false);
		// For each c in MethodCall in P (*)
		// 		scene.Add( c.Method )
		// End For each
		retriveBindingsTypeSupport(scene, visitor, dynJAlloyBinding);

		// For each f in FieldExpression in P (*)
		// 		Scene.Add( f.Prefix.Type )
		// 		scene.Add( f.Field )
		// End For each
		// For each v in Quantified Variable in P (*)
		// 		scene.Add( v.Type )
		// End For each

		for (JType type : symbolTable.getRelevantTypes()) {
			RelevancyAnalysisUtils.findModuleAndToScene(scene, type, modules);
		}

		// For each constant C in P (*)
		//   scene.Add (C.Type)
		// End For
		for (JType type : expressionTypeResolver.getRelevantTypes()) {
			String module_id = type.dpdTypeNameExtract();

			JDynAlloyModule module = RelevancyAnalysisUtils.findModuleByName(
					module_id, modules);

			if (module != null) {
				RelevancyAnalysisUtils.findModuleAndToScene(scene, type,
						modules);
			}
		}


		for (FunctionCallAlloyFormulaDescriptor fc : visitor.getCalledFunctions()) {
			RelevancyAnalysisUtils.findCalledProgramAndToScene(scene, 
					dynJAlloyBinding, fc);
		}
	}




	private static void findCalledProgramAndToScene(Scene scene, JDynAlloyBinding dynJAlloyBinding,
			FunctionCallAlloyFormulaDescriptor fc) {
		JProgramCall programCall = new JProgramCall(false, 
				fc.getfunctionCallInAlloyFormulaInfo().getFunctionId(), 
				fc.getfunctionCallInAlloyFormulaInfo().getParameters());
		JProgramDeclaration program = dynJAlloyBinding.resolve(programCall);
		if (program == null){
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("fun_set_size") &&
					fc.getArgumentsTypes().size() == 1){
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("fun_reach") &&
					fc.getArgumentsTypes().size() == 3){
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("fun_set_contains") &&
					fc.getArgumentsTypes().size() == 2){
				return;
			}
			//			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("fun_java_primitive_integer_value_size_of") &&
			//					fc.getArgumentsTypes().size() == 1){
			//				return;
			//			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("add") && 
					fc.getArgumentsTypes().get(0).toString().equals("Int") &&
					fc.getArgumentsTypes().get(1).toString().equals("Int")){
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("sub") && 
					fc.getArgumentsTypes().get(0).toString().equals("Int") &&
					fc.getArgumentsTypes().get(1).toString().equals("Int")){
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("rem") && 
					fc.getArgumentsTypes().get(0).toString().equals("Int") &&
					fc.getArgumentsTypes().get(1).toString().equals("Int")){
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("mul") && 
					fc.getArgumentsTypes().get(0).toString().equals("Int") &&
					fc.getArgumentsTypes().get(1).toString().equals("Int")){
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("div") && 
					fc.getArgumentsTypes().get(0).toString().equals("Int") &&
					fc.getArgumentsTypes().get(1).toString().equals("Int")){
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("negate") && 
					fc.getArgumentsTypes().get(0).toString().equals("Int")){
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("arrayElements") && 
					fc.getArgumentsTypes().get(0).equals(JType.ALLOY_INT_ARRAY_CONTAINS_TYPE) &&
					fc.getArgumentsTypes().get(1).toString().contains("java_lang_IntArray")){ //in this line we had equals("java_lang_IntArray+null")
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("arrayElements") && 
					fc.getArgumentsTypes().get(0).toString().equals("Int set") &&
					fc.getArgumentsTypes().get(1).toString().contains("java_lang_ObjectArray")){ //in this line we had equals("java_lang_ObjectArray+null")
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("arrayLength") && 
					fc.getArgumentsTypes().get(0).toString().contains("java_lang_IntArray") && //in this line we had equals("java_lang_IntArray+null")
					fc.getArgumentsTypes().get(1).toString().contains("JavaPrimitiveIntegerValue")){ //in this line we had equals("JavaPrimitiveIntegerValue")
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("arrayLength") && 
					fc.getArgumentsTypes().get(0).toString().contains("java_lang_LongArray") && //in this line we had equals("java_lang_LongArray+null")
					fc.getArgumentsTypes().get(1).toString().contains("JavaPrimitiveIntegerValue")){ //in this line we had equals("JavaPrimitiveIntegerValue")
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("arrayLength") && 
					fc.getArgumentsTypes().get(0).toString().contains("java_lang_CharArray") && //in this line we had equals("java_lang_CharArray+null")
					fc.getArgumentsTypes().get(1).toString().contains("JavaPrimitiveIntegerValue")){ //in this line we had equals("JavaPrimitiveIntegerValue")
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("arrayLength") &&
					fc.getArgumentsTypes().get(0).toString().contains("java_lang_ObjectArray") && //in this line we had equals("java_lang_CharArray+null")
					fc.getArgumentsTypes().get(1).toString().contains("JavaPrimitiveIntegerValue")){ //in this line we had equals("JavaPrimitiveIntegerValue")
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("arrayAccess") && 
					fc.getArgumentsTypes().get(0).toString().contains("java_lang_IntArray") && //in this line we had equals("java_lang_IntArray+null")
					fc.getArgumentsTypes().get(1).toString().contains("java_lang_IntArray")){ //in this line we had equals("java_lang_IntArray")
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("arrayAccess") && 
					fc.getArgumentsTypes().get(0).toString().contains("java_lang_LongArray") && //in this line we had equals("java_lang_LongArray+null")
					fc.getArgumentsTypes().get(1).toString().contains("java_lang_LongArray")){ //in this line we had equals("java_lang_LongArray")
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("arrayAccess") && 
					fc.getArgumentsTypes().get(0).toString().contains("java_lang_CharArray") && //in this line we had equals("java_lang_CharArray+null")
					fc.getArgumentsTypes().get(1).toString().contains("java_lang_CharArray")){ //in this line we had equals("java_lang_CharArray")
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("arrayAccess") &&
					fc.getArgumentsTypes().get(0).toString().contains("java_lang_ObjectArray") && //in this line we had equals("java_lang_CharArray+null")
					fc.getArgumentsTypes().get(1).toString().contains("java_lang_ObjectArray")){ //in this line we had equals("java_lang_CharArray")
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("Not") && 
					fc.getArgumentsTypes().get(0).toString().equals("boolean")){
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("fun_univ_equals") && 
					fc.getArgumentsTypes().get(0).toString().equals("boolean") &&
					fc.getArgumentsTypes().get(1).toString().equals("boolean")){
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("fun_univ_equals") && 
					fc.getArgumentsTypes().get(0).toString().equals("Int") &&
					fc.getArgumentsTypes().get(1).toString().equals("Int")){
				return;
			}
			if (fc.getfunctionCallInAlloyFormulaInfo().getFunctionId().equals("arrayLength") &&
					fc.getArgumentsTypes().get(0).toString().contains("java_lang_IntArray") &&
					!fc.getArgumentsTypes().get(1).toString().contains("JavaPrimitiveIntegerValue")){
				return;
			}
			Set<String> javaPrimitiveIntegerValueFunctionsSet = new HashSet<String>();
			Collections.addAll(javaPrimitiveIntegerValueFunctionsSet,
					"fun_java_primitive_integer_value_add",
					"fun_java_primitive_integer_value_sub",
					"fun_java_primitive_integer_value_negate",
					"fun_java_primitive_integer_value_div",
					"fun_java_primitive_integer_value_mul",
					"fun_java_primitive_integer_value_rem",
					"fun_java_primitive_integer_value_size_of",
					"fun_java_primitive_integer_value_sshr",
					"fun_java_primitive_integer_value_java_util_set_size",
					"fun_java_primitive_char_value_addCharCharToJavaPrimitiveIntegerValue",
					"fun_java_primitive_char_value_subCharCharToJavaPrimitiveIntegerValue",
					"fun_java_primitive_char_value_addCharIntToJavaPrimitiveIntegerValue",
					"fun_java_primitive_char_value_subCharIntToJavaPrimitiveIntegerValue",
					"fun_java_primitive_char_value_addIntCharToJavaPrimitiveIntegerValue",
					"fun_java_primitive_char_value_subIntCharToJavaPrimitiveIntegerValue",
					"fun_cast_char_to_int",
					"fun_narrowing_cast_int_to_char",
					"fun_narrowing_cast_long_to_int");
			if (javaPrimitiveIntegerValueFunctionsSet.contains(fc.getfunctionCallInAlloyFormulaInfo().getFunctionId()))
				return;

			Set<String> javaPrimitiveLongValueFunctionsSet = new HashSet<String>();
			Collections.addAll(javaPrimitiveLongValueFunctionsSet,
					"fun_java_primitive_long_value_add",
					"fun_java_primitive_long_value_sub",
					"fun_java_primitive_long_value_negate",
					"fun_java_primitive_long_value_div",
					"fun_java_primitive_long_value_mul",
					"fun_java_primitive_long_value_rem",
					"fun_java_primitive_long_value_size_of",
					"fun_java_primitive_long_value_sshr",
					"fun_cast_char_to_long",
					"fun_cast_int_to_long",
					"fun_java_primitive_char_value_addCharLongToJavaPrimitiveLongValue",
					"fun_java_primitive_char_value_addLongCharToJavaPrimitiveLongValue",
					"fun_java_primitive_char_value_subCharLongToJavaPrimitiveLongValue",
					"fun_java_primitive_char_value_subLongCharToJavaPrimitiveLongValue",
					"fun_long_int_to_long_add",
					"fun_int_long_to_long_add",
					"fun_long_int_to_long_sub");
			if (javaPrimitiveLongValueFunctionsSet.contains(fc.getfunctionCallInAlloyFormulaInfo().getFunctionId()))
				return;



			Set<String> javaPrimitiveFloatValueFunctionsSet = new HashSet<String>();
			Collections.addAll(javaPrimitiveFloatValueFunctionsSet,
					"fun_java_primitive_float_value_add",
					"fun_java_primitive_float_value_sub",
					"fun_java_primitive_float_value_mul",
					"fun_java_primitive_float_value_div",
					"fun_java_lang_float_isNaN");
			if (javaPrimitiveFloatValueFunctionsSet.contains(fc.getfunctionCallInAlloyFormulaInfo().getFunctionId()))
				return;


			Set<String> javaPrimitiveCharValueFunctionsSet = new HashSet<String>();
			Collections.addAll(javaPrimitiveCharValueFunctionsSet,"fun_narrowing_cast_long_to_char");
			if (javaPrimitiveCharValueFunctionsSet.contains(fc.getfunctionCallInAlloyFormulaInfo().getFunctionId()))
				return;

   			throw new JDynAlloyException("Program " + fc.getfunctionCallInAlloyFormulaInfo().getFunctionId() + " called in specification cannot be found.");
		}
		scene.addProgram(program);
	}

	/**
	 * Adds to the scene what an object invariant reaches: the same as {@link #analyzeFormula}, but
	 * without the function calls, so a program that the invariant reaches only through a function is
	 * not added. Unclear: whether leaving out the function calls is intended; the code is otherwise a
	 * copy of {@code analyzeFormula}.
	 *
	 * @param scene the scene to add to
	 * @param objectInvariant the invariant's formula
	 * @param symbolTable the analysis's table, with {@code thiz} in scope
	 * @param dynJAlloyBinding the bindings
	 * @param modules every module of the run
	 * @throws JDynAlloyException "Binding not found for: null" or "Module '...' not found."
	 */
	public static void analyzeObjectInvariant(Scene scene, AlloyFormula objectInvariant,
			RelevancyAnalysisSymbolTable symbolTable,
			JDynAlloyBinding dynJAlloyBinding, List<JDynAlloyModule> modules) {

		PredicateAndFunctionCallRelevancyVisitor visitor = new PredicateAndFunctionCallRelevancyVisitor(symbolTable);

		RelevantAnalysisExpressionTypeResolver expressionTypeResolver = new RelevantAnalysisExpressionTypeResolver(
				visitor, symbolTable);
		visitor.setExpressionVisitor(expressionTypeResolver);

		symbolTable.setEnableRelevantAnalysis(true);
		symbolTable.setRelevantTypes(new HashSet<JType>());

		objectInvariant.accept(visitor);

		//		visitor.getCalledFunctions().addAll(((RelevantAnalysisExpressionTypeResolver)visitor.getDfsExprVisitor()).getCalledFunctionsNames());
		processCollectedFunctionNames(scene, visitor, modules);

		symbolTable.setEnableRelevantAnalysis(false);
		// For each c in MethodCall in P (*)
		// 		scene.Add( c.Method )
		// End For each
		retriveBindingsTypeSupport(scene, visitor, dynJAlloyBinding);

		// For each f in FieldExpression in P (*)
		// 		Scene.Add( f.Prefix.Type )
		// 		scene.Add( f.Field )
		// End For each
		// For each v in Quantified Variable in P (*)
		// 		scene.Add( v.Type )
		// End For each

		for (JType type : symbolTable.getRelevantTypes()) {
			RelevancyAnalysisUtils.findModuleAndToScene(scene, type, modules);
		}

		// For each constant C in P (*)
		//   scene.Add (C.Type)
		// End For
		for (JType type : expressionTypeResolver.getRelevantTypes()) {
			String module_id = type.dpdTypeNameExtract();

			JDynAlloyModule module = RelevancyAnalysisUtils.findModuleByName(
					module_id, modules);

			if (module != null) {
				RelevancyAnalysisUtils.findModuleAndToScene(scene, type,
						modules);
			}
		}


	}


	private static void retriveBindingsTypeSupport(Scene scene,
			PredicateAndFunctionCallRelevancyVisitor visitor,
			JDynAlloyBinding dynJAlloyBinding) {
		for (PredicateCallAlloyFormulaDescriptor element : visitor
				.getPredicatesCollected()) {
			JProgramDeclaration programDeclaration = dynJAlloyBinding
					.resolve(element.getPredicateCallAlloyFormula());
			if (programDeclaration == null) {
				throw new JDynAlloyException("Binding not found for: "
						+ programDeclaration);
			}
			scene.addProgram(programDeclaration);
		}
	}


	private static void processCollectedFunctionNames(Scene scene, PredicateAndFunctionCallRelevancyVisitor visitor, List<JDynAlloyModule> modules){
		if (visitor.getCalledFunctions().contains("fun_java_primitive_integer_value_size_of")){
			for (JDynAlloyModule module : modules){
				if (module.getModuleId().contains("JavaPrimitiveIntegerLiteral")){
					String strNum = module.getModuleId().substring(module.getModuleId().lastIndexOf("l") + 1);
					int actualLiteral;
					if (strNum.startsWith("Minus")){
						strNum = module.getModuleId().substring(module.getModuleId().lastIndexOf("l") + 6);
						actualLiteral = -Integer.valueOf(strNum);
					} else {
						actualLiteral = Integer.valueOf(strNum);
					}
					if (0 <= actualLiteral && actualLiteral < Math.pow(2, bitWidth - 1)){
						scene.addModule(module);
					}
				}
			}

		}
	}
}

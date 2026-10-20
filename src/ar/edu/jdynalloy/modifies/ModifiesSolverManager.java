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
package ar.edu.jdynalloy.modifies;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.JDynAlloyException;
import ar.edu.jdynalloy.MethodToCheckNotFoundException;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyPrinter;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.binding.fieldcollector.FieldCollectorVisitor;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.xlator.JDynAlloyBinding;

/**
 * Adds frame conditions to the method under analysis from its {@code modifies} clauses: step 4
 * of TACO's {@code JDynAlloyStage}, which runs it in every run, with or without the relevancy
 * analysis: after the slicing, if there is one, and before the bindings are built again for the
 * translation (docs/ARCHITECTURE.md, "How TACO calls it").
 *
 * <p>{@code process} returns a new list with the same modules, except that each module whose id
 * is {@code classToCheck} is replaced by what {@link ReplaceModifiesModuleVisitor} returns for
 * it: a new module object, even when nothing in it changed. For that module it collects the
 * fields of all the modules into a new {@code SymbolTable}, and it checks that one of the
 * module's programs is the method under analysis: {@code <program>(<parameter types>)} equal to
 * the configuration's {@code methodToCheck}, compared as in
 * {@code RelevancyAnalysisUtils.findMethodToCheckDeclaration} (docs/NAMING.md). If none is, it
 * throws a {@link MethodToCheckNotFoundException} ("The method to check was not found"), after
 * the visit. In practice no frame condition is added: the visitor's own test for the method under
 * analysis never matches TACO's names (docs/KNOWN-LIMITATIONS.md, "{@code modifies} clauses add
 * no frame conditions"; docs/OPEN-QUESTIONS.md, item 2). Reads {@code classToCheck} and
 * {@code methodToCheck} from {@code JDynAlloyConfig.getInstance()}.
 *
 * <p>The {@code dynJAlloyBinding} argument of {@code process} is stored in the visitor and never
 * read (TACO passes null). {@link #setJavaArithmetic(boolean)}, which TACO does not call, sets
 * the flag of the symbol table, which only {@code SymbolTable.lookupField} reads; nothing that
 * runs here calls it. No static state besides the logger; TACO creates one instance per run.
 */
public class ModifiesSolverManager {
	private static Logger log = Logger.getLogger(ModifiesSolverManager.class);
	private boolean javaArithmetic;

	
	public boolean getJavaArithmetic(){
		return javaArithmetic;
	}
	
	public void setJavaArithmetic(boolean b){
		this.javaArithmetic = b;
	}
	
	/**
	 * This method replace predicate calls with the modifies clausule in 
	 * 
	 */
	public List<JDynAlloyModule> process(List<JDynAlloyModule> modules, JDynAlloyBinding dynJAlloyBinding, boolean isJavaArith) {
		
		JProgramDeclaration methodToCheckDeclaration = null;
		
		boolean checkedMethodFound = false;
		
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
		
		
		
		log.debug("Resolving JDynAlloy modifies: ");
		List<JDynAlloyModule> modulesWithoutModifies = new ArrayList<JDynAlloyModule>();
		for (JDynAlloyModule dynJAlloyModule : modules) {
			if (dynJAlloyModule.getModuleId().equals(classToCheck)) {
				

				SymbolTable symbolTable = new SymbolTable();		
				symbolTable.setJavaArithmetic(this.javaArithmetic);
				FieldCollectorVisitor fieldCollectorVisitor = new FieldCollectorVisitor(symbolTable, isJavaArith);
				
				for (JDynAlloyModule aModule : modules) {
					aModule.accept(fieldCollectorVisitor);
				}

				for (JProgramDeclaration programDeclaration : dynJAlloyModule
						.getPrograms()) {
					String qualifiedMethodName = programDeclaration.getProgramId() + "(" + filterParameters(programDeclaration.getParameters()) + ")";
					if (methodToCheck.equals(qualifiedMethodName)) {
						methodToCheckDeclaration = programDeclaration;
						checkedMethodFound = true;
					}
				}
								
				log.debug("Module: " + dynJAlloyModule.getModuleId());
				
				//fixing impedance mismatch between TACO and Stryker version. String "methodToCheck" below may not be the right string to pass.
				ReplaceModifiesModuleVisitor replaceModifiesModuleVisitor = new ReplaceModifiesModuleVisitor(dynJAlloyBinding, symbolTable, methodToCheck, isJavaArith);
				JDynAlloyModule dynJAlloyModuleWithOutModifies = (JDynAlloyModule) dynJAlloyModule.accept(replaceModifiesModuleVisitor);
	
				JDynAlloyPrinter printer = new JDynAlloyPrinter(isJavaArith);
				log.debug("New Module WITHOUT Modifies: ");
				log.debug(dynJAlloyModuleWithOutModifies.accept(printer));
				
				modulesWithoutModifies.add(dynJAlloyModuleWithOutModifies);
			} else {
				modulesWithoutModifies.add(dynJAlloyModule);
			}
		}
		
		if (!checkedMethodFound) {
			String moreInfo = "classToCheck: " + JDynAlloyConfig.getInstance().getClassToCheck() + ". methodToCheck: " + JDynAlloyConfig.getInstance().getMethodToCheck();  
			throw new MethodToCheckNotFoundException("The method to check was not found. Please check the configurations keys 'classToCheck' and 'methodToCheck'. " + moreInfo );
		}
		
		return modulesWithoutModifies;
	}

	private String filterParameters(List<JVariableDeclaration> parameters) {
		String params = "";
		boolean hasParams = false;
		for (JVariableDeclaration v : parameters){
			if (!v.getVariable().getVariableId().getString().equals("thiz") && !v.getVariable().getVariableId().getString().equals("throw") && !v.getVariable().getVariableId().getString().equals("return") &&!v.getVariable().getVariableId().getString().startsWith("returnVarFromSpecMethodCall")){
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
}

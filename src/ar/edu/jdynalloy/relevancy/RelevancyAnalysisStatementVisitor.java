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

import java.util.List;

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JAssert;
import ar.edu.jdynalloy.ast.JAssignment;
import ar.edu.jdynalloy.ast.JAssume;
import ar.edu.jdynalloy.ast.JBlock;
import ar.edu.jdynalloy.ast.JCreateObject;
import ar.edu.jdynalloy.ast.JHavoc;
import ar.edu.jdynalloy.ast.JIfThenElse;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JSkip;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.ast.JWhile;
import ar.edu.jdynalloy.factory.JPredicateFactory;
import ar.edu.jdynalloy.xlator.JDynAlloyBinding;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;

/**
 * Analyses the body of a program for the relevancy analysis: {@link RelevancyAnalysisManager}
 * runs it on the body of each program in the scene, and it adds to the {@link Scene} what the
 * statements reach. The results of the visits are {@code JDynAlloyVisitor}'s and are not used.
 *
 * <p>The conditions of {@code assert}, {@code assume}, {@code if} and {@code while}, assignments
 * (as the formula {@code lvalue = rvalue}) and {@code havoc} expressions go through
 * {@link RelevancyAnalysisUtils#analyzeFormula}; a {@code call} adds its program and has its
 * arguments analysed; {@code createObject} and variable declarations add the modules of their
 * types. A variable declaration also declares the variable in the symbol table, in the scope of
 * its block: each block opens a scope. Loop invariants are not analysed: {@code JDynAlloyVisitor}
 * visits them and does nothing (see {@link RelevancyAnalysisManager}). No static state.
 */
public class RelevancyAnalysisStatementVisitor extends JDynAlloyVisitor {
	Scene scene;
	JDynAlloyBinding dynJAlloyBinding;
	List<JDynAlloyModule> modules;
	RelevancyAnalysisSymbolTable symbolTable;

	public RelevancyAnalysisStatementVisitor(JDynAlloyBinding dynJAlloyBinding, List<JDynAlloyModule> modules, RelevancyAnalysisSymbolTable symbolTable, Scene scene, boolean isJavaArith) {
		super(isJavaArith);
		this.dynJAlloyBinding = dynJAlloyBinding;
		this.modules = modules;
		this.symbolTable = symbolTable;
		this.scene = scene;
	}

	/**
	 * Analyses the condition, and adds the module {@code java_lang_AssertionError} if there is one: a
	 * failed {@code assert} assigns {@code java_lang_AssertionErrorLit} to {@code throw}
	 * (docs/ARCHITECTURE.md, "JDynAlloyXlatorVisitor: modules, programs, statements").
	 *
	 * @param node the assert
	 * @return {@code JDynAlloyVisitor}'s result
	 */
	@Override
	public Object visit(JAssert node) {
		RelevancyAnalysisUtils.analyzeFormula(scene, node.getCondition(), symbolTable, dynJAlloyBinding, modules);
		for (JDynAlloyModule m : this.modules)
			if (m.getModuleId().equals("java_lang_AssertionError"))
				scene.addModule(m);
		return super.visit(node);
	}

	@Override
	public Object visit(JAssignment node) {
		//DPD::work arround to use AnalyzeFormula
		EqualsFormula equalsFormula = new EqualsFormula(node.getLvalue(), node.getRvalue());
		RelevancyAnalysisUtils.analyzeFormula(scene, equalsFormula, symbolTable, dynJAlloyBinding, modules);
		return super.visit(node);
	}

	@Override
	public Object visit(JAssume node) {
		RelevancyAnalysisUtils.analyzeFormula(scene, node.getCondition(), symbolTable, dynJAlloyBinding, modules);
		return super.visit(node);
	}

	@Override
	public Object visit(JCreateObject node) {
		RelevancyAnalysisUtils.findModuleAndToScene(scene, JType.parse(node.getSignatureId()), modules);

		
		JType type = symbolTable.lookup(node.getLvalue().getVariableId()).getType();
		RelevancyAnalysisUtils.findModuleAndToScene(scene, type, modules);		
		
		return super.visit(node);
	}

	@Override
	public Object visit(JHavoc node) {
		//DPD::work arround to use AnalyzeFormula
		AlloyFormula formula = JPredicateFactory.liftExpression(node.getExpression());
		RelevancyAnalysisUtils.analyzeFormula(scene, formula, symbolTable, dynJAlloyBinding, modules);
		
		return super.visit(node);
	}

	@Override
	public Object visit(JIfThenElse node) {
		RelevancyAnalysisUtils.analyzeFormula(scene, node.getCondition(), symbolTable, dynJAlloyBinding, modules);
		return super.visit(node);
	}

	/**
	 * Adds the program the call is bound to, and analyses the call as the predicate formula
	 * {@code <program>[<arguments>]}, for the types and calls in its arguments.
	 *
	 * @param node the call
	 * @return {@code JDynAlloyVisitor}'s result
	 * @throws IllegalArgumentException if the binding has no program for the call ("Argument must not
	 *         be null")
	 */
	@Override
	public Object visit(JProgramCall node) {
		JProgramDeclaration programDeclaration = this.dynJAlloyBinding.resolve(node);
		scene.addProgram(programDeclaration);
		//JPG::workaround to use AnalyzeFormula
		PredicateFormula predFormula = new PredicateFormula(null,node.getProgramId(),node.getArguments());
		RelevancyAnalysisUtils.analyzeFormula(scene, predFormula, symbolTable, dynJAlloyBinding, modules);
		
		return super.visit(node);
	}

	@Override
	public Object visit(JSkip n) {
		return super.visit(n);
	}

	@Override
	public Object visit(JVariableDeclaration node) {
		symbolTable.insertLocal(node.getVariable().getVariableId(), node.getType());
		RelevancyAnalysisUtils.findModuleAndToScene(scene, node.getType(), modules);
		
		return super.visit(node);
	}

	@Override
	public Object visit(JWhile node) {
		RelevancyAnalysisUtils.analyzeFormula(scene, node.getCondition(), symbolTable, dynJAlloyBinding, modules);
		return super.visit(node);
	}
	
	@Override
	public Object visit(JBlock node) {
		this.symbolTable.beginScope();
		Object c = super.visit(node);
		this.symbolTable.endScope();
		return c;
	}
	
	

}

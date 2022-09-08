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

	@Override
	public Object visit(JAssert node) {
		RelevancyAnalysisUtils.analyzeFormula(scene, node.getCondition(), symbolTable, dynJAlloyBinding, modules);		
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

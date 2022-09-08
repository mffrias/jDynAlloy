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
package ar.edu.jdynalloy.binding.callbinding;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Vector;

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JAlloyCondition;
import ar.edu.jdynalloy.ast.JAssert;
import ar.edu.jdynalloy.ast.JAssertionDeclaration;
import ar.edu.jdynalloy.ast.JAssignment;
import ar.edu.jdynalloy.ast.JAssume;
import ar.edu.jdynalloy.ast.JBlock;
import ar.edu.jdynalloy.ast.JClassConstraint;
import ar.edu.jdynalloy.ast.JClassInvariant;
import ar.edu.jdynalloy.ast.JCreateObject;
import ar.edu.jdynalloy.ast.JField;
import ar.edu.jdynalloy.ast.JHavoc;
import ar.edu.jdynalloy.ast.JIfThenElse;
import ar.edu.jdynalloy.ast.JLoopInvariant;
import ar.edu.jdynalloy.ast.JModifies;
import ar.edu.jdynalloy.ast.JObjectConstraint;
import ar.edu.jdynalloy.ast.JObjectInvariant;
import ar.edu.jdynalloy.ast.JPostcondition;
import ar.edu.jdynalloy.ast.JPrecondition;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JRepresents;
import ar.edu.jdynalloy.ast.JSignature;
import ar.edu.jdynalloy.ast.JSkip;
import ar.edu.jdynalloy.ast.JSpecCase;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.ast.JWhile;
import ar.edu.jdynalloy.binding.JBindingKey;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.factory.JTypeFactory;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.formulas.IProgramCall;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaVisitor;

public class SemanticCheckVisitor extends JDynAlloyVisitor {

	private SymbolTable symbolTable;
	IdentityHashMap<IProgramCall, JBindingKey> callBindings;
	private boolean isClassLevel = true;
	private boolean javaArithmetic;


	public boolean getJavaArithmetic(){
		return javaArithmetic;
	}

	public void setJavaArithmetic(boolean b){
		this.javaArithmetic = b;
	}

	public IdentityHashMap<IProgramCall, JBindingKey> getCallBindings() {
		return callBindings;
	}

	public void setCallBindings(IdentityHashMap<IProgramCall, JBindingKey> callBindings) {
		this.callBindings = callBindings;
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	public void setSymbolTable(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	public SemanticCheckVisitor(SymbolTable symbolTable, boolean isJavaArithmetic) {
		super(isJavaArithmetic);		
		this.symbolTable = symbolTable;
		this.callBindings = new IdentityHashMap<IProgramCall, JBindingKey>();
	}

	public SemanticCheckVisitor(boolean isJavaArithmetic) {
		super(isJavaArithmetic);
		this.symbolTable = new SymbolTable();
		symbolTable.setJavaArithmetic(this.javaArithmetic);
		this.callBindings = new IdentityHashMap<IProgramCall, JBindingKey>();
	}

	@Override
	public Object visit(JDynAlloyModule node) {
		Object returnValue;
		this.symbolTable.beginScope();

		JType jtype = JTypeFactory.buildReference(node.getModuleId());
		// this "quick" fix provides declaration for "thiz" and "throw" in
		// precondition/postconidtion/class invariant/represent spec
		// this.symbolTable.insertLocal(new VariableId(node.getModuleId() + "."
		// + JExpressionFactory.THIS_VARIABLE.getVariableId().getString()),
		// jtype);
		this.symbolTable.insertLocal(JExpressionFactory.THIS_VARIABLE.getVariableId(), jtype);

		returnValue = super.visit(node);

		this.symbolTable.endScope();
		return returnValue;
	}

	@Override
	public Object visit(JAlloyCondition condition) {
		// TODO Auto-generated method stub
		return super.visit(condition);
	}

	// @Override
	// public Object visit(JAlloySpec spec) {
	// // TODO Auto-generated method stub
	// return super.visit(spec);
	// }

	@Override
	public Object visit(JAssert n) {
		// TODO Auto-generated method stub
		return super.visit(n);
	}

	@Override
	public Object visit(JAssertionDeclaration assertion) {
		// TODO Auto-generated method stub
		return super.visit(assertion);
	}

	@Override
	public Object visit(JAssignment n) {
		// TODO We can implement data flow analisis here to infiere jtype
		return super.visit(n);
	}

	@Override
	public Object visit(JAssume node) {
		PredicateAndFunctionCallCollectorVisitor predicateAndFunctionCallCollectorVisitor = new PredicateAndFunctionCallCollectorVisitor(this.symbolTable);
		node.getCondition().accept(predicateAndFunctionCallCollectorVisitor);
		for (PredicateCallAlloyFormulaDescriptor predicateCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getPredicatesCollected()) {
			processIProgramCallSupport(predicateCallAlloyFormulaDescriptor.getPredicateCallAlloyFormula(), predicateCallAlloyFormulaDescriptor
					.getArgumentsTypes());
		}
		//		for (FunctionCallAlloyFormulaDescriptor functionCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getCalledFunctions()) {
		//			processIProgramCallSupport(functionCallAlloyFormulaDescriptor.getfunctionCallInAlloyFormulaInfo(), 
		//					functionCallAlloyFormulaDescriptor.getReturnType(), 
		//					functionCallAlloyFormulaDescriptor.getArgumentsTypes());
		//		}

		return super.visit(node);
	}

	@Override
	public Object visit(JBlock node) {
		Object returnValue;
		this.symbolTable.beginScope();
		returnValue = super.visit(node);
		this.symbolTable.endScope();
		return returnValue;

	}

	@Override
	public Object visit(JCreateObject n) {
		return super.visit(n);
	}

	@Override
	public Object visit(JField node) {
		return super.visit(node);
	}

	@Override
	public Object visit(JHavoc n) {
		return super.visit(n);
	}

	@Override
	public Object visit(JIfThenElse n) {
		// TODO Auto-generated method stub
		return super.visit(n);
	}

	@Override
	public Object visit(JObjectInvariant node) {
		PredicateAndFunctionCallCollectorVisitor predicateAndFunctionCallCollectorVisitor = new PredicateAndFunctionCallCollectorVisitor(this.symbolTable);
		if (node.getFormula() != null) {
			node.getFormula().accept(predicateAndFunctionCallCollectorVisitor);
			for (PredicateCallAlloyFormulaDescriptor predicateCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getPredicatesCollected()) {
				processIProgramCallSupport(predicateCallAlloyFormulaDescriptor.getPredicateCallAlloyFormula(), predicateCallAlloyFormulaDescriptor
						.getArgumentsTypes());
			}
			//			for (FunctionCallAlloyFormulaDescriptor functionCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getCalledFunctions()) {
			//				processIProgramCallSupport(functionCallAlloyFormulaDescriptor.getfunctionCallInAlloyFormulaInfo(), 
			//						functionCallAlloyFormulaDescriptor.getReturnType(), 
			//						functionCallAlloyFormulaDescriptor.getArgumentsTypes());
			//			}

		}

		return super.visit(node);
	}



	@Override
	public Object visit(JClassInvariant node) {
		PredicateAndFunctionCallCollectorVisitor predicateAndFunctionCallCollectorVisitor = new PredicateAndFunctionCallCollectorVisitor(this.symbolTable);
		if (node.getFormula() != null) {
			node.getFormula().accept(predicateAndFunctionCallCollectorVisitor);
			for (PredicateCallAlloyFormulaDescriptor predicateCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getPredicatesCollected()) {
				processIProgramCallSupport(predicateCallAlloyFormulaDescriptor.getPredicateCallAlloyFormula(), predicateCallAlloyFormulaDescriptor
						.getArgumentsTypes());
			}
			//			for (FunctionCallAlloyFormulaDescriptor functionCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getCalledFunctions()) {
			//				processIProgramCallSupport(functionCallAlloyFormulaDescriptor.getfunctionCallInAlloyFormulaInfo(), 
			//						functionCallAlloyFormulaDescriptor.getReturnType(), 
			//						functionCallAlloyFormulaDescriptor.getArgumentsTypes());
			//			}

		}

		return super.visit(node);
	}

	@Override
	public Object visit(JObjectConstraint node) {
		PredicateAndFunctionCallCollectorVisitor predicateAndFunctionCallCollectorVisitor = new PredicateAndFunctionCallCollectorVisitor(this.symbolTable);
		if (node.getFormula() != null) {
			node.getFormula().accept(predicateAndFunctionCallCollectorVisitor);
			for (PredicateCallAlloyFormulaDescriptor predicateCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getPredicatesCollected()) {
				processIProgramCallSupport(predicateCallAlloyFormulaDescriptor.getPredicateCallAlloyFormula(), predicateCallAlloyFormulaDescriptor
						.getArgumentsTypes());
			}
			//			for (FunctionCallAlloyFormulaDescriptor functionCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getCalledFunctions()) {
			//				processIProgramCallSupport(functionCallAlloyFormulaDescriptor.getfunctionCallInAlloyFormulaInfo(), 
			//						functionCallAlloyFormulaDescriptor.getReturnType(), 
			//						functionCallAlloyFormulaDescriptor.getArgumentsTypes());
			//			}

		}

		return super.visit(node);
	}

	@Override
	public Object visit(JClassConstraint node) {
		PredicateAndFunctionCallCollectorVisitor predicateAndFunctionCallCollectorVisitor = new PredicateAndFunctionCallCollectorVisitor(this.symbolTable);
		if (node.getFormula() != null) {
			node.getFormula().accept(predicateAndFunctionCallCollectorVisitor);
			for (PredicateCallAlloyFormulaDescriptor predicateCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getPredicatesCollected()) {
				processIProgramCallSupport(predicateCallAlloyFormulaDescriptor.getPredicateCallAlloyFormula(), predicateCallAlloyFormulaDescriptor
						.getArgumentsTypes());
			}
			//			for (FunctionCallAlloyFormulaDescriptor functionCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getCalledFunctions()) {
			//				processIProgramCallSupport(functionCallAlloyFormulaDescriptor.getfunctionCallInAlloyFormulaInfo(), 
			//						functionCallAlloyFormulaDescriptor.getReturnType(), 
			//						functionCallAlloyFormulaDescriptor.getArgumentsTypes());
			//			}

		}

		return super.visit(node);
	}

	@Override
	public Object visit(JLoopInvariant node) {
		PredicateAndFunctionCallCollectorVisitor predicateAndFunctionCallCollectorVisitor = new PredicateAndFunctionCallCollectorVisitor(this.symbolTable);
		if (node.getFormula() != null) {
			node.getFormula().accept(predicateAndFunctionCallCollectorVisitor);
			for (PredicateCallAlloyFormulaDescriptor predicateCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getPredicatesCollected()) {
				processIProgramCallSupport(predicateCallAlloyFormulaDescriptor.getPredicateCallAlloyFormula(), predicateCallAlloyFormulaDescriptor
						.getArgumentsTypes());
			}
			//			for (FunctionCallAlloyFormulaDescriptor functionCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getCalledFunctions()) {
			//				processIProgramCallSupport(functionCallAlloyFormulaDescriptor.getfunctionCallInAlloyFormulaInfo(), 
			//						functionCallAlloyFormulaDescriptor.getReturnType(), 
			//						functionCallAlloyFormulaDescriptor.getArgumentsTypes());
			//			}

		}
		return super.visit(node);
	}

	@Override
	public Object visit(JPostcondition node) {
		PredicateAndFunctionCallCollectorVisitor predicateAndFunctionCallCollectorVisitor = new PredicateAndFunctionCallCollectorVisitor(this.symbolTable);
		if (node.getFormula() != null) {
			node.getFormula().accept(predicateAndFunctionCallCollectorVisitor);
			for (PredicateCallAlloyFormulaDescriptor predicateCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getPredicatesCollected()) {
				processIProgramCallSupport(predicateCallAlloyFormulaDescriptor.getPredicateCallAlloyFormula(), predicateCallAlloyFormulaDescriptor
						.getArgumentsTypes());
			}
			//			for (FunctionCallAlloyFormulaDescriptor functionCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getCalledFunctions()) {
			//				processIProgramCallSupport(functionCallAlloyFormulaDescriptor.getfunctionCallInAlloyFormulaInfo(), 
			//						functionCallAlloyFormulaDescriptor.getReturnType(), 
			//						functionCallAlloyFormulaDescriptor.getArgumentsTypes());
			//			}

		}
		return super.visit(node);
	}

	@Override
	public Object visit(JPrecondition node) {
		PredicateAndFunctionCallCollectorVisitor predicateAndFunctionCallCollectorVisitor = new PredicateAndFunctionCallCollectorVisitor(this.symbolTable);
		if (node.getFormula() != null) {
			node.getFormula().accept(predicateAndFunctionCallCollectorVisitor);
			for (PredicateCallAlloyFormulaDescriptor predicateCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getPredicatesCollected()) {
				processIProgramCallSupport(predicateCallAlloyFormulaDescriptor.getPredicateCallAlloyFormula(), predicateCallAlloyFormulaDescriptor
						.getArgumentsTypes());
			}
			//			for (FunctionCallAlloyFormulaDescriptor functionCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getCalledFunctions()) {
			//				processIProgramCallSupport(functionCallAlloyFormulaDescriptor.getfunctionCallInAlloyFormulaInfo(), 
			//						functionCallAlloyFormulaDescriptor.getReturnType(), 
			//						functionCallAlloyFormulaDescriptor.getArgumentsTypes());
			//			}

		}
		return super.visit(node);
	}

	@Override
	public Object visit(JProgramCall programCall) {
		Object returnValue = super.visit(programCall);
		List<JType> arguments = new ArrayList<JType>();
		for (AlloyExpression alloyExpression : programCall.getArguments()) {
			// TODO DOB::JFormulaVisitor needs ExpressionTypeResolver and vice
			// versa!
			ExpressionTypeResolver expressionTypeResolver = new ExpressionTypeResolver(null, symbolTable);
			JFormulaVisitor jForumlaVisitor = new JFormulaVisitor(expressionTypeResolver);
			expressionTypeResolver.setFormulaVisitor(jForumlaVisitor);
			JType type = (JType) alloyExpression.accept(expressionTypeResolver);
			arguments.add(type);
		}

		processIProgramCallSupport(programCall, arguments);
		return returnValue;
	}


	private void processIProgramCallSupport(ExprFunction functionCall, JType returnType, List<JType> arguments) {
		JBindingKey binding = null;
		String receiverType = extractReceiverTypeName(arguments.get(0));
		arguments.add(1, returnType);
		String programId = functionCall.getFunctionId();
		binding = new JBindingKey(receiverType, programId, arguments);
		IProgramCall programCall = new JProgramCall(false, programId, functionCall.getParameters());
		callBindings.put(programCall, binding);

	}



	private void processIProgramCallSupport(IProgramCall programCall, List<JType> arguments) {
		// TODO DOB:: Por ahora considero que si no es estatico, el primer
		// parametro es el de la clase
		JBindingKey binding;
		if (programCall.isStatic()) {
			binding = new JBindingKey(programCall.getProgramId(), arguments);
		} else {
			String receiverType = extractReceiverTypeName(arguments.get(0));
			binding = new JBindingKey(receiverType, programCall.getProgramId(), arguments);
		}

		callBindings.put(programCall, binding);
	}

	// TODO DOB::Extract to a common method (static or not ) in JType
	private String extractReceiverTypeName(JType type) {
		String returnValue;
		// if (JType.fromIncludesNull(type)) {
		// returnValue = JType.getBaseType(type);
		// } else {
		// returnValue = type.singletonFrom();
		// }
		returnValue = type.dpdTypeNameExtract();
		return returnValue;
	}

	@Override
	public Object visit(JProgramDeclaration node) {
		isClassLevel = false;
		this.symbolTable.beginScope();
		Vector<Object> result = new Vector<Object>();

		Vector<Object> varResults = new Vector<Object>();
		boolean firstParameter = true;
		for (JVariableDeclaration child : node.getParameters()) {
			// "thiz" variable was added to SymbolTable when the method was
			// started to be analized
			// We need to skip "thiz" analysis to avoid redeclare variable (only
			// forn non-static methods)
			if (!node.isStatic() && firstParameter) {
				assert (child.getVariable().equals(JExpressionFactory.THIS_EXPRESSION.getVariable()));
				varResults.add(null/* emulate super's behavior */);
				firstParameter = false;
			} else {
				varResults.add(child.accept(this));
			}
		}

		result.add(varResults);

		Vector<Object> specCasesResult = new Vector<Object>();
		for (JSpecCase child : node.getSpecCases()) {
			specCasesResult.add(child.accept(this));
		}
		result.add(specCasesResult);

		Object bodyResult = node.getBody().accept(this);
		result.add(bodyResult);

		this.symbolTable.endScope();
		isClassLevel = true;
		return result;
	}

	@Override
	public Object visit(JRepresents node) {
		PredicateAndFunctionCallCollectorVisitor predicateAndFunctionCallCollectorVisitor = new PredicateAndFunctionCallCollectorVisitor(this.symbolTable);
		if (node.getFormula() != null) {
			node.getFormula().accept(predicateAndFunctionCallCollectorVisitor);
			for (PredicateCallAlloyFormulaDescriptor predicateCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getPredicatesCollected()) {
				processIProgramCallSupport(predicateCallAlloyFormulaDescriptor.getPredicateCallAlloyFormula(), predicateCallAlloyFormulaDescriptor
						.getArgumentsTypes());
			}
			//			for (FunctionCallAlloyFormulaDescriptor functionCallAlloyFormulaDescriptor : predicateAndFunctionCallCollectorVisitor.getCalledFunctions()) {
			//				processIProgramCallSupport(functionCallAlloyFormulaDescriptor.getfunctionCallInAlloyFormulaInfo(), 
			//						functionCallAlloyFormulaDescriptor.getReturnType(), 
			//						functionCallAlloyFormulaDescriptor.getArgumentsTypes());
			//			}


		}
		return super.visit(node);
	}

	@Override
	public Object visit(JSignature node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public Object visit(JSkip n) {
		return super.visit(n);
	}

	@Override
	public Object visit(JVariableDeclaration variableDeclaration) {
		Object returnValue;
		returnValue = super.visit(variableDeclaration);

		this.symbolTable.insertLocal(variableDeclaration.getVariable().getVariableId(), variableDeclaration.getType());
		return returnValue;
	}

	@Override
	public Object visit(JWhile n) {
		return super.visit(n);
	}

	@Override
	public Object visit(JSpecCase node) {
		return super.visit(node);
	}

	@Override
	public Object visit(JModifies node) {
		return super.visit(node);
	}

}

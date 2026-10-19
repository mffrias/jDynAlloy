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

import java.util.List;

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateCallAlloyFormula;

/**
 * A function call ({@code ExprFunction}) found in a formula, with the types of its arguments and
 * of its result. Built by {@link FunctionCallCollectorExpressionVisitor} for the bindings, where
 * nothing reads it, and by {@code RelevantAnalysisExpressionTypeResolver} for the relevancy
 * analysis, which looks the call up as a program. It has no {@code equals}: two descriptors are
 * equal only if they are the same object.
 */
public class FunctionCallAlloyFormulaDescriptor {
	
	private ExprFunction functionCallInAlloyFormulaInfo;
	private List<JType> argumentsTypes;
	private JType returnType;
	
	public ExprFunction getfunctionCallInAlloyFormulaInfo() {
		return functionCallInAlloyFormulaInfo;
	}
	public void setFunctionCallInAlloyFormulaInfo(ExprFunction functionCallInAlloyFormulaInfo) {
		this.functionCallInAlloyFormulaInfo = functionCallInAlloyFormulaInfo;
	}
	public List<JType> getArgumentsTypes() {
		return argumentsTypes;
	}
	public void setArgumentsTypes(List<JType> argumentsTypes) {
		this.argumentsTypes = argumentsTypes;
	}
	public JType getReturnType(){
		return returnType;
	}
	public void setReturnType(JType returnType){
		this.returnType = returnType;
	}
	
	public FunctionCallAlloyFormulaDescriptor(ExprFunction functionCallInAlloyFormulaData, JType returnType, List<JType> argumentsTypes) {
		this.argumentsTypes = argumentsTypes;
		this.functionCallInAlloyFormulaInfo = functionCallInAlloyFormulaData;
		this.returnType = returnType;
	}
	
	@Override
	public String toString() {
		return "(" + this.functionCallInAlloyFormulaInfo + " argument types " + this.argumentsTypes + "). Return type " + this.returnType ;
	}
	
	
	
}

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
package ar.edu.jdynalloy.binding.fieldcollector;

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JField;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.xlator.JType;

public class FieldCollectorVisitor extends JDynAlloyVisitor {

	private SymbolTable symbolTable;
	private String moduleName;
	private boolean javaArithmetic;

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	public void setSymbolTable(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	public boolean getJavaArithmetic(){
		return this.javaArithmetic;
	}

	public void setJavaArithmetic(boolean b){
		this.javaArithmetic = b;
	}

	public FieldCollectorVisitor(boolean isJavaArithmetic) {
		super(isJavaArithmetic);
		this.symbolTable = new SymbolTable();
		this.symbolTable.setJavaArithmetic(this.javaArithmetic);
	}

	public FieldCollectorVisitor(SymbolTable symbolTable, boolean isJavaArithmetic) {
		super(isJavaArithmetic);
		this.symbolTable = symbolTable;
	}

	@Override
	public Object visit(JDynAlloyModule node) {
		if (node.getModuleId().contains("ClassFields")){
			System.out.println("FieldCollectorVisitor");
		}
		this.moduleName = node.getModuleId();
		return super.visit(node);
	}

	@Override
	public Object visit(JField field) {
		String fieldName = field.getFieldVariable().getVariableId().getString();
		JType fieldType = field.getFieldType();
		this.symbolTable.insertField(this.moduleName, fieldName, fieldType);
		return null;
	}
	//	//TODO DOB::Extract to a common method (static or not ) in JType
	//	private String extractReciverTypeName(JType type) {
	//		String returnValue;
	//		if (JType.fromIncludesNull(type)) { 
	//			returnValue = JType.getBaseType(type);		
	//		} else {
	//			returnValue = type.singletonFrom();
	//		}
	//		return returnValue;
	//	}


}

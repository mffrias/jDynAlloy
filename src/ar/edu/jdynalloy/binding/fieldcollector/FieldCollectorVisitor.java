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

/**
 * Collects the fields of the modules it visits into a {@link SymbolTable}: each field goes in
 * under the id of its module and its name, with the type of its values
 * ({@link SymbolTable#insertField}). Callers run it on every module with
 * {@code module.accept(visitor)} and then read the table, not the visits' results.
 *
 * <p>Used by {@code BindingManager.execute()} (before {@code SemanticCheckVisitor}, which needs
 * the fields), by {@code RelevancyAnalysisManager} and {@code ModifiesSolverManager} (each with
 * its own table), and by TACO's {@code JDynAlloyParsingStage}, which takes the field names from
 * the table for its second parsing pass. Only {@code visit(JDynAlloyModule)} and
 * {@code visit(JField)} do something; the rest is {@code JDynAlloyVisitor}'s traversal.
 *
 * <p>Its own {@code javaArithmetic} flag ({@code getJavaArithmetic}/{@code setJavaArithmetic},
 * which {@code RelevancyAnalysisManager} sets) is read by nothing; the base class's
 * {@code isJavaArithmetic} is a separate field. No static state.
 */
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

	/**
	 * Creates a visitor with a new, empty symbol table. The table's {@code javaArithmetic} is set
	 * from this visitor's own flag, still false here, not from the parameter. Unused.
	 *
	 * @param isJavaArithmetic passed to {@code JDynAlloyVisitor}
	 */
	public FieldCollectorVisitor(boolean isJavaArithmetic) {
		super(isJavaArithmetic);
		this.symbolTable = new SymbolTable();
		this.symbolTable.setJavaArithmetic(this.javaArithmetic);
	}

	/**
	 * Creates a visitor that adds the fields to the given table. Every caller uses this one.
	 *
	 * @param symbolTable the table to fill; its {@code javaArithmetic} is left as the caller set it
	 * @param isJavaArithmetic passed to {@code JDynAlloyVisitor}
	 */
	public FieldCollectorVisitor(SymbolTable symbolTable, boolean isJavaArithmetic) {
		super(isJavaArithmetic);
		this.symbolTable = symbolTable;
	}

	/**
	 * Remembers the module's id, under which {@link #visit(JField)} inserts the fields, and visits
	 * the module as {@code JDynAlloyVisitor} does. The base visit catches any exception thrown
	 * while it visits the fields, prints {@code Module: <id> FieldsSize = <n>} to standard output
	 * and goes on; so a duplicated field does not reach the caller, and the module's fields after
	 * it are not collected.
	 *
	 * @param node the module
	 * @return the base visitor's result, which the callers ignore
	 */
	@Override
	public Object visit(JDynAlloyModule node) {
//		if (node.getModuleId().contains("ClassFields")){
//			System.out.println("FieldCollectorVisitor");
//		}
		this.moduleName = node.getModuleId();
		return super.visit(node);
	}

	/**
	 * Inserts the field into the table under the id of the module being visited.
	 *
	 * @param field the field
	 * @return null
	 * @throws ar.edu.jdynalloy.JDynAlloySemanticException if the table already has a field of
	 *         that name in that module ("Duplicated field"); caught by the module's visit, see
	 *         {@link #visit(JDynAlloyModule)}
	 */
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

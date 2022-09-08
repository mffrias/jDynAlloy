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
package ar.edu.jdynalloy.binding.symboltable;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

import ar.edu.jdynalloy.JDynAlloySemanticException;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.VariableId;

public class SymbolTable implements Cloneable {
	
	private Deque<Scope> table;
	
	private Scope globalScope;
	
	private boolean javaArithmetic;
	
	/**
	 * JType x string  
	 */	
	private Map<FieldDescriptor,JType> fieldsBinding;
	
	public boolean getJavaArithmetic(){
		return this.javaArithmetic;
	}
	
	public void setJavaArithmetic(boolean b){
		this.javaArithmetic = b;
	}
	
	
	public SymbolTable() {
		table = new ArrayDeque<Scope>();		
		//prepare global scope
		globalScope = new Scope();
		table.addFirst(globalScope);		
		fieldsBinding = new HashMap<FieldDescriptor, JType>();
	}
	
	public void insertGlobal (VariableId variableId, JType type) {
		insertSupport(variableId, type, globalScope);		
	}

	public void insertLocal (VariableId variableId, JType type) {
		insertSupport(variableId, type, table.getFirst());		
	}
	
	public void insertField(String moduleName, String fieldName, JType fieldType) {

		FieldDescriptor fieldDescriptor =  new FieldDescriptor(moduleName, fieldName);
		if (this.fieldsBinding.containsKey(fieldDescriptor)) {
			throw new JDynAlloySemanticException("Duplicated field: " + moduleName + "." + fieldName);
		}
		this.fieldsBinding.put(fieldDescriptor, JType.parse(fieldType.dpdTypeNameExtract()));
	}

	private void insertSupport(VariableId variableId, JType type,Scope scope) {
		if (lookup(variableId) != null) {
			throw new JDynAlloySemanticException("Duplicate variable: " + variableId); 
		}
		SymbolCell aSymbolCell = new SymbolCell(variableId, type);
		scope.insert(variableId, aSymbolCell);
	}

	public SymbolCell lookup(VariableId variableId) {
		for(Scope scope : table)
		{		
			SymbolCell aSymbolCell = scope.lookup(variableId);
			if (aSymbolCell != null) {
				return aSymbolCell;
			}
		}
		return null;
	}
	
	public JType lookupField(String moduleName, String fieldName) {
		if (javaArithmetic){
			if (fieldName.equals("java_lang_IntArray_contents")){
				return JType.INT_ARRAY_CONTAINS_TYPE;
			}
			if (fieldName.equals("java_lang_CharArray_contents")){
				return JType.CHAR_ARRAY_CONTAINS_TYPE;
			}
			if (fieldName.equals("java_lang_LongArray_contents")){
				return JType.LONG_ARRAY_CONTAINS_TYPE;
			}
			if (fieldName.equals("java_lang_ObjectArray_contents")){
				return JType.OBJECT_ARRAY_CONTAINS_TYPE;
			}
		} else {
			if (fieldName.equals("java_lang_IntArray_contents")){
				return JType.ALLOY_INT_ARRAY_CONTAINS_TYPE;
			}
			if (fieldName.equals("java_lang_ObjectArray_contents")){
				return JType.ALLOY_OBJECT_ARRAY_CONTAINS_TYPE;
			}
		}
			
		FieldDescriptor fieldDescriptor =  new FieldDescriptor(moduleName, fieldName);
		if (!this.fieldsBinding.containsKey(fieldDescriptor)) {
			
			for (FieldDescriptor fd : this.fieldsBinding.keySet()) {
				if (fd.getFieldName().equals(fieldName))
					return this.fieldsBinding.get(fd);
			}
			
			if (fieldName.startsWith("SK_jml_pred_java_primitive")){
				FieldDescriptor fieldDescriptor_inv =  new FieldDescriptor(moduleName, fieldName + "_0");
				if (!this.fieldsBinding.containsKey(fieldDescriptor_inv)) {
					for (FieldDescriptor fd : this.fieldsBinding.keySet()) {
						if (fd.getFieldName().equals(fieldName+"_0"))
							return this.fieldsBinding.get(fd);
					}
				} else {
					return this.fieldsBinding.get(fieldDescriptor);		
				}
			}
			
			if (fieldName.startsWith("SK_pred_java_primitive")){
				FieldDescriptor fieldDescriptor_inv =  new FieldDescriptor(moduleName, fieldName+"_0");
				if (!this.fieldsBinding.containsKey(fieldDescriptor_inv)) {
					for (FieldDescriptor fd : this.fieldsBinding.keySet()) {
						if (fd.getFieldName().equals(fieldName+"_0"))
							return this.fieldsBinding.get(fd);
					}
				} else {
					return this.fieldsBinding.get(fieldDescriptor);		
				}
			}
			
			throw new JDynAlloySemanticException("Field doesn't exist: " + moduleName + "." + fieldName);
		}
		return this.fieldsBinding.get(fieldDescriptor);		
	}

	public Set<FieldDescriptor> getFieldSet() {
		return Collections.unmodifiableSet(this.fieldsBinding.keySet());
	}

	public Set<FieldDescriptor> getFieldSet(String moduleId) {
		Set<FieldDescriptor> returnValue = new HashSet<FieldDescriptor>();
		for (FieldDescriptor fieldDescriptor : this.getFieldSet()) {
			if (fieldDescriptor.getType().equals(moduleId)) {
				returnValue.add(fieldDescriptor);
			}
		}
		return returnValue;
	}
	
	
	public void beginScope() {
		table.addFirst(new Scope());
	}
	
	public void endScope() {
		if (table.size() == 1) {
			throw new NoSuchElementException("Scope enque is empty. Only Global scope left");
		}
		table.pollFirst();
	}
	
	public String toString() {
		String s ="SymbolTable:" + "\n";
		s += "Scopes: [" + "\n";
		for(Scope scope : this.table) {
			s += scope + "\n"; 
		}
		s += "]" + "\n"; ;
		s += "Fields:" + "\n"; 
		for (Entry<FieldDescriptor,JType> entry : this.fieldsBinding.entrySet()) {
			s += entry.getKey() + "=" + entry.getValue() + "\n"; 
		}
		return s;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		SymbolTable symbolTable = new SymbolTable();
		symbolTable.table = new ArrayDeque<Scope>(this.table);
		symbolTable.fieldsBinding = this.fieldsBinding;
		symbolTable.javaArithmetic = this.javaArithmetic;
		return symbolTable;
	}

	
}

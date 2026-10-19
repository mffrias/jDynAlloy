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

/**
 * The names the visitors of {@code binding}, {@code relevancy} and {@code modifies} need to type
 * expressions: a stack of {@link Scope}s for variables (innermost first, the global scope at the
 * bottom), and the fields of the modules, keyed by {@link FieldDescriptor}, with the type of
 * their values.
 *
 * <p>A {@code FieldCollectorVisitor} fills the fields first; then a visitor opens and closes
 * scopes as it walks the AST ({@link #beginScope()}, {@link #endScope()}) and declares
 * {@code thiz}, parameters, local and bound variables in the innermost one. A variable cannot
 * be declared twice anywhere in the stack, so no declaration shadows another.
 *
 * <p>Each use builds its own table: {@code BindingManager.execute()} one per call,
 * {@code RelevancyAnalysisManager} a {@code RelevancyAnalysisSymbolTable} (a subclass that also
 * records the types it sees), {@code ModifiesSolverManager} one for the module of the class
 * under analysis, and TACO's {@code JDynAlloyParsingStage} one only for the field names. Set
 * {@code javaArithmetic} ({@code setJavaArithmetic}, default false) before any
 * {@link #lookupField}: it decides the types of the array contents fields.
 * {@code FieldCollectorVisitor} does not set it. No static state.
 */
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
	
	/**
	 * Declares a variable in the global scope, at the bottom of the stack. Unused.
	 *
	 * @param variableId the variable
	 * @param type its type
	 * @throws JDynAlloySemanticException if a scope of the stack already declares it ("Duplicate
	 *         variable")
	 */
	public void insertGlobal (VariableId variableId, JType type) {
		insertSupport(variableId, type, globalScope);		
	}

	/**
	 * Declares a variable in the innermost scope. It stays declared until that scope is closed
	 * with {@link #endScope()}.
	 *
	 * @param variableId the variable
	 * @param type its type
	 * @throws JDynAlloySemanticException if a scope of the stack, not only the innermost one,
	 *         already declares it ("Duplicate variable")
	 */
	public void insertLocal (VariableId variableId, JType type) {
		insertSupport(variableId, type, table.getFirst());		
	}
	
	/**
	 * Records a field of a module. What is stored is not {@code fieldType} itself but the type of
	 * the field's values, {@code JType.parse(fieldType.dpdTypeNameExtract())}: the first
	 * signature of the field's image other than {@code null}, or the module of a special type
	 * (for example {@code java_lang_IntArray} for the contents of int arrays).
	 * {@code dpdTypeNameExtract} reads {@code JDynAlloyConfig.getInstance()}, which must be set.
	 *
	 * @param moduleName the id of the module that declares the field
	 * @param fieldName the field's name
	 * @param fieldType the field's type, as in the module ({@code C -> one T})
	 * @throws JDynAlloySemanticException if the module already has a field of that name
	 *         ("Duplicated field")
	 */
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

	/**
	 * Finds a variable, from the innermost scope to the global one. Fields are not searched:
	 * {@code ExpressionTypeResolver} calls {@link #lookupField} when this returns null.
	 *
	 * @param variableId the variable
	 * @return its cell, or null if no scope declares it
	 */
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
	
	/**
	 * Returns the type recorded for a field (see {@link #insertField}). In this order:
	 * <ul>
	 * <li>the contents fields of arrays get a special type instead, whatever the module:
	 *     {@code java_lang_IntArray_contents} and {@code java_lang_ObjectArray_contents} always,
	 *     and with {@code javaArithmetic} also {@code java_lang_CharArray_contents} and
	 *     {@code java_lang_LongArray_contents} (the {@code JType.*_CONTAINS_TYPE} constants,
	 *     the {@code ALLOY_} ones without {@code javaArithmetic});</li>
	 * <li>the field of that name in that module;</li>
	 * <li>otherwise the field of that name in any module, the first one the hash map yields.
	 *     This is how a field inherited from a superclass's module is found, and what
	 *     {@code ExpressionTypeResolver}'s {@code lookupField(null, name)} relies on; field names
	 *     carry their class (docs/NAMING.md), so normally there is only one;</li>
	 * <li>otherwise, for a name that starts with {@code SK_jml_pred_java_primitive} or
	 *     {@code SK_pred_java_primitive}, the field of that name followed by {@code _0}, in any
	 *     module. If that module itself has the {@code _0} field, the result is null (the code
	 *     then reads the map with the original name). Unclear: which fields get the {@code _0}
	 *     suffix, and why only these names fall back to it.</li>
	 * </ul>
	 *
	 * @param moduleName the id of the module of the receiver, or null for any module
	 * @param fieldName the field's name
	 * @return the field's type as above; null only in the {@code _0} case
	 * @throws JDynAlloySemanticException if no field is found ("Field doesn't exist")
	 */
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

	/**
	 * Returns every field recorded so far. TACO's {@code JDynAlloyParsingStage} reads it.
	 *
	 * @return an unmodifiable view of the fields, which follows later insertions
	 */
	public Set<FieldDescriptor> getFieldSet() {
		return Collections.unmodifiableSet(this.fieldsBinding.keySet());
	}

	/**
	 * Returns the fields declared in one module; inherited fields are not included.
	 *
	 * @param moduleId the module's id
	 * @return a new set with the fields whose module id is {@code moduleId}
	 */
	public Set<FieldDescriptor> getFieldSet(String moduleId) {
		Set<FieldDescriptor> returnValue = new HashSet<FieldDescriptor>();
		for (FieldDescriptor fieldDescriptor : this.getFieldSet()) {
			if (fieldDescriptor.getType().equals(moduleId)) {
				returnValue.add(fieldDescriptor);
			}
		}
		return returnValue;
	}
	
	
	/**
	 * Opens a new, empty innermost scope. Each call must be matched by an {@link #endScope()}.
	 */
	public void beginScope() {
		table.addFirst(new Scope());
	}
	
	/**
	 * Closes the innermost scope and forgets its variables.
	 *
	 * @throws NoSuchElementException if only the global scope is left ("Scope enque is empty.
	 *         Only Global scope left")
	 */
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
	
	/**
	 * Returns a shallow copy, always a plain {@code SymbolTable}: a new stack with the same
	 * {@link Scope} objects, the same field map (fields inserted in either table appear in
	 * both), and the same {@code javaArithmetic}. The copy's global scope, the one
	 * {@link #insertGlobal} uses, is a new scope that is not on its stack. Unused.
	 *
	 * @return the copy
	 * @throws CloneNotSupportedException never
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		SymbolTable symbolTable = new SymbolTable();
		symbolTable.table = new ArrayDeque<Scope>(this.table);
		symbolTable.fieldsBinding = this.fieldsBinding;
		symbolTable.javaArithmetic = this.javaArithmetic;
		return symbolTable;
	}

	
}

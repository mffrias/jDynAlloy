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

import java.util.HashSet;
import java.util.Set;

import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.VariableId;

/**
 * The symbol table of the relevancy analysis: a {@code SymbolTable} that, while recording is
 * enabled, also collects the types of the variables declared and of the fields looked up
 * ({@link #getRelevantTypes()}). {@link RelevancyAnalysisUtils#analyzeFormula} replaces the set,
 * enables recording for the visit of a formula and disables it after, and then adds the modules
 * of the recorded types to the scene. Recording starts enabled; what is recorded before the first
 * formula is discarded when that formula replaces the set. One instance per
 * {@code RelevancyAnalysisManager.process}; no static state.
 */
public class RelevancyAnalysisSymbolTable extends SymbolTable {

	private Set<JType> relevantTypes = new HashSet<JType>();
	private boolean enableRelevantAnalysis;

	public RelevancyAnalysisSymbolTable() {
		super();
		enableRelevantAnalysis = true;
	}
	
	protected boolean isEnableRelevantAnalysis() {
		return enableRelevantAnalysis;
	}

	protected void setEnableRelevantAnalysis(boolean enableRelevantAnalysis) {
		this.enableRelevantAnalysis = enableRelevantAnalysis;
	}

	/**
	 * Declares the variable as {@code SymbolTable.insertLocal} does and, while recording is enabled,
	 * adds its type to {@link #getRelevantTypes()}.
	 *
	 * @param variableId the variable
	 * @param type its type
	 * @throws ar.edu.jdynalloy.JDynAlloySemanticException if a scope already declares the variable
	 */
	@Override
	public void insertLocal(VariableId variableId, JType type) {
		super.insertLocal(variableId, type);
		if (enableRelevantAnalysis) {
			getRelevantTypes().add(type);
		}

	}

	/**
	 * Returns the type of a field as {@code SymbolTable.lookupField} does and, while recording is
	 * enabled, adds it to {@link #getRelevantTypes()}.
	 *
	 * @param moduleName the id of the module of the receiver, or null for any module
	 * @param fieldName the field's name
	 * @return the field's type
	 * @throws ar.edu.jdynalloy.JDynAlloySemanticException if no field is found
	 */
	@Override
	public JType lookupField(String moduleName, String fieldName) {
		JType type = super.lookupField(moduleName, fieldName);
		if (enableRelevantAnalysis) {
			getRelevantTypes().add(type);
		}

		return type;
	}

	public Set<JType> getRelevantTypes() {
		return relevantTypes;
	}
	
	public void setRelevantTypes(Set<JType> relevantTypes) {
		this.relevantTypes = relevantTypes;
	}
}

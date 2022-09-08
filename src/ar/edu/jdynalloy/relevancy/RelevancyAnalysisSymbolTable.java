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

	@Override
	public void insertLocal(VariableId variableId, JType type) {
		super.insertLocal(variableId, type);
		if (enableRelevantAnalysis) {
			getRelevantTypes().add(type);
		}

	}

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

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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ar.edu.jdynalloy.JDynAlloySemanticException;
import ar.uba.dc.rfm.alloy.VariableId;

public class Scope {
	Map<VariableId,SymbolCell> localTable ;
	
	public Scope() {
		localTable = new HashMap<VariableId, SymbolCell>();
	}
	
	public void insert(VariableId variableId, SymbolCell symbolCell) {
		if (this.localTable.containsKey(variableId)) {
			throw new JDynAlloySemanticException("Duplicate variable in scope: " + variableId);
		}
		this.localTable.put(variableId,symbolCell);
	}	
	
	public SymbolCell lookup (VariableId variableId) {
		return this.localTable.get(variableId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((localTable == null) ? 0 : localTable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Scope other = (Scope) obj;
		if (localTable == null) {
			if (other.localTable != null)
				return false;
		} else if (!localTable.equals(other.localTable))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String s = "Scope: [";
		for(Entry<VariableId,SymbolCell> entry : localTable.entrySet()) {
			s += "(" + entry.getValue() + ") "; 
		}
		s += "]";
		return s;
	}
	
}

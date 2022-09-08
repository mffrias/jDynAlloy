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

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.VariableId;

public class SymbolCell {
	
	private VariableId variableId;
	private JType type;
	
	public SymbolCell(VariableId variableId, JType type) {
		this.variableId = variableId;
		this.type = type;
	}

	public VariableId getVariableId() {
		return variableId;
	}
	public void setVariableId(VariableId variableId) {
		this.variableId = variableId;
	}
	public JType getType() {
		return type;
	}
	public void setType(JType type) {
		this.type = type;
	}
	public SymbolCell() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((variableId == null) ? 0 : variableId.hashCode());
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
		SymbolCell other = (SymbolCell) obj;
		if (variableId == null) {
			if (other.variableId != null)
				return false;
		} else if (!variableId.equals(other.variableId))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "SymbolCell [" + this.getVariableId() + "=" + this.getType() + "]";
	}
	

}

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

package ar.edu.jdynalloy.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.dynalloy.parser.IDynalloyProgramParseContext;

public class JDynAlloyProgramParseContext implements
		IDynalloyProgramParseContext, Cloneable {

	private final Map<String, AlloyVariable> ctxVariables;
	private final Map<String, AlloyVariable> ctxFields;

	public JDynAlloyProgramParseContext(JDynAlloyProgramParseContext context) {
		ctxVariables = new HashMap<String, AlloyVariable>();
		ctxFields = new HashMap<String, AlloyVariable>();
		ctxVariables.putAll(context.ctxVariables);
		ctxFields.putAll(context.ctxFields);
	}

	public JDynAlloyProgramParseContext(Collection<AlloyVariable> ctxVariables,
			Collection<AlloyVariable> ctxFields, boolean allowPrimed) {
		this.ctxVariables = new HashMap<String, AlloyVariable>();
		this.ctxFields = new HashMap<String, AlloyVariable>();

		for (AlloyVariable v : ctxVariables) {
			this.ctxVariables.put(v.toString(), v);
			if (allowPrimed) {
				AlloyVariable vPrimed = new AlloyVariable(v.getVariableId(),
						true);
				this.ctxVariables.put(vPrimed.toString(), vPrimed);
			}
		}

		for (AlloyVariable v : ctxFields) {
			this.ctxFields.put(v.toString(), v);
			if (allowPrimed) {
				AlloyVariable vPrimed = new AlloyVariable(v.getVariableId(),
						true);
				this.ctxFields.put(vPrimed.toString(), vPrimed);
			}
		}
	}

	public AlloyVariable getAlloyVariable(String token) {
		if (ctxVariables.containsKey(token)) {
			return ctxVariables.get(token);
		} else if (ctxFields.containsKey(token)) {
			return ctxFields.get(token);
		} else {
			return null;
		}
	}

	public boolean isVariableName(String token) {
		if (ctxVariables.containsKey(token)) {
			return true;
		} else if (ctxFields.containsKey(token)) {
			return true;
		} else {
			return false;
		}
	}

	public int getIntLiteral(String token) {
		return Integer.parseInt(token);
	}

	public boolean isIntLiteral(String token) {
		try {
			Integer.parseInt(token);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public void addAlloyVariable(AlloyVariable variable) {
		ctxVariables.put(variable.getVariableId().getString(), variable);
		if (variable.isPrimed()) {
		    throw new JDynAlloyParsingException("The variable shouldn't be primed");
		}
		AlloyVariable primedVariable = new AlloyVariable(variable.getVariableId().getString(), true);
		ctxVariables.put(primedVariable.toString(), primedVariable);
	}

	public void addAlloyField(AlloyVariable variable) {
		ctxFields.put(variable.getVariableId().getString(), variable);
		if (variable.isPrimed()) {
		    throw new JDynAlloyParsingException("The variable shouldn't be primed");
		}
		AlloyVariable primedVariable = new AlloyVariable(variable.getVariableId().getString(), true);
		ctxFields.put(primedVariable.toString(), primedVariable);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Map<String, AlloyVariable> ctxVariables = new HashMap<String, AlloyVariable>();
		Map<String, AlloyVariable> ctxFields = new HashMap<String, AlloyVariable>();
		ctxVariables.putAll(ctxVariables);
		ctxFields.putAll(ctxFields);

		return new JDynAlloyProgramParseContext(ctxVariables.values(),
				ctxFields.values(), false);
	}
}

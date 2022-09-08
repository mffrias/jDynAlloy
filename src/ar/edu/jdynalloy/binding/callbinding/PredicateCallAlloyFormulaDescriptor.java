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
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateCallAlloyFormula;

public class PredicateCallAlloyFormulaDescriptor {
	private PredicateCallAlloyFormula predicateCallAlloyFormula;
	private List<JType> argumentsTypes;
	public PredicateCallAlloyFormula getPredicateCallAlloyFormula() {
		return predicateCallAlloyFormula;
	}
	public void setPredicateCallAlloyFormula(PredicateCallAlloyFormula predicateCallAlloyFormula) {
		this.predicateCallAlloyFormula = predicateCallAlloyFormula;
	}
	public List<JType> getArgumentsTypes() {
		return argumentsTypes;
	}
	public void setArgumentsTypes(List<JType> argumentsTypes) {
		this.argumentsTypes = argumentsTypes;
	}
	public PredicateCallAlloyFormulaDescriptor(PredicateCallAlloyFormula predicateCallAlloyFormula,List<JType> argumentsTypes) {
		this.argumentsTypes = argumentsTypes;
		this.predicateCallAlloyFormula = predicateCallAlloyFormula;
	}
	
	@Override
	public String toString() {
		return "(" + this.predicateCallAlloyFormula + " argument types " + this.argumentsTypes + ")" ;
	}
	
	
	
}

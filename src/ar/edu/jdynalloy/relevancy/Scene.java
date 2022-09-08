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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JField;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public class Scene {

	private List<JProgramDeclaration> programs;
	private List<JDynAlloyModule> modules;
	private List<JField> fields;
//	private HashMap<ExprVariable, JType> varsEncodingValueOfArithmeticOperationsInContracts;
//	private List<AlloyFormula> predsEncodingValueOfArithmeticOperationsInContracts;


	public Scene() {
		programs = new ArrayList<JProgramDeclaration>();
		modules = new ArrayList<JDynAlloyModule>();
		fields = new ArrayList<JField>();
//		varsEncodingValueOfArithmeticOperationsInContracts = new HashMap<ExprVariable, JType>();
//		predsEncodingValueOfArithmeticOperationsInContracts = new ArrayList<AlloyFormula>();

	}

	public Scene(Scene scene) {
		this.programs = new ArrayList<JProgramDeclaration>(scene.programs);
		this.modules = new ArrayList<JDynAlloyModule>(scene.modules);
		this.fields = new ArrayList<JField>(scene.fields);
//		this.varsEncodingValueOfArithmeticOperationsInContracts = new HashMap<ExprVariable, JType>(scene.varsEncodingValueOfArithmeticOperationsInContracts);
//		this.predsEncodingValueOfArithmeticOperationsInContracts = new ArrayList<AlloyFormula>(scene.predsEncodingValueOfArithmeticOperationsInContracts);
	}

	/**
	 * @return the programs
	 */
	public List<JProgramDeclaration> getPrograms() {
		return Collections.unmodifiableList(this.programs);
	}

	/**
	 * @return the types
	 */
	public List<JDynAlloyModule> getModules() {
		return Collections.unmodifiableList(this.modules);
	}

	/**
	 * @return the types
	 */
	public List<JField> getFields() {
		return Collections.unmodifiableList(this.fields);
	}
	
	
//	public HashMap<ExprVariable, JType> getVarsEncodingValueOfArithmeticOperationsInContracts(){
//		return this.varsEncodingValueOfArithmeticOperationsInContracts;
//	}
//	
//	public List<AlloyFormula> getPredsEncodingValueOfArithmeticOperationsInContracts(){
//		return this.predsEncodingValueOfArithmeticOperationsInContracts;
//	}
	
	
	
	public void addModule(JDynAlloyModule module) {
		if (module == null) {
			throw new IllegalArgumentException("Argument must not be null");
		}
		
		if (!this.modules.contains(module)) {
			this.modules.add(module);
		}
	}

	public void addProgram(JProgramDeclaration program) {
		if (program == null) {
			throw new IllegalArgumentException("Argument must not be null");
		}
		
		if (!this.programs.contains(program)) {
			this.programs.add(program);
		}
	}
	
	public void addField(JField field) {
		if (field == null) {
			throw new IllegalArgumentException("Argument must not be null");
		}
		
		
		if (!this.fields.contains(field)) {
			this.fields.add(field);
		}
	}	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + ((programs == null) ? 0 : programs.hashCode());
		result = prime * result + ((modules == null) ? 0 : modules.hashCode());
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
		Scene other = (Scene) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (programs == null) {
			if (other.programs != null)
				return false;
		} else if (!programs.equals(other.programs))
			return false;
		if (modules == null) {
			if (other.modules != null)
				return false;
		} else if (!modules.equals(other.modules))
			return false;
		return true;
	}

	@Override
	public String toString() {

		return "[ Programs: " + programs + " Types: " + modules + " Fields: " + fields + "]";
	}

	public void assignation(Scene scene) {
		this.programs = scene.programs;
		this.modules = scene.modules;
		this.fields = scene.fields;		
	}

	public boolean isContainedBy(Set<Object> set) {
		if (!set.containsAll(this.programs)) {
			return false;
		}
		
		if (!set.containsAll(this.modules)) {
			return false;
		}

		if (!set.containsAll(this.fields)) {
			return false;
		}
		
		return true;
	}

}

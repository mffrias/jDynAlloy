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
package ar.edu.jdynalloy.binding;

import java.util.ArrayList;
import java.util.List;

import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.xlator.JType;

public class JBindingKey implements Comparable<JBindingKey> {

	private String moduleId;
	private String programId;
	private List<JType> arguments;
//	private int numArgs;
//	private boolean comesFromFunctionCall;

//	public boolean comesFromFunctionCall(){
//		return comesFromFunctionCall;
//	}
	
	public int getNumArgs(){
		return this.arguments.size();
	}
	
	public JBindingKey(JProgramDeclaration programDeclaration) {
		// this constructor need to be changed to a method in some place, maybe
		// an static method here.)
		this(extractSignatureId(programDeclaration), extractProgramId(programDeclaration), extracParameterTypesList(programDeclaration));
		if (programDeclaration.isConstructor()){
			this.programId = "Constructor";
		}
	}

	public JBindingKey(String moduleId, String programId, List<JType> arguments) {
		super();
		this.moduleId = moduleId;
		this.programId = programId;
		this.arguments = removeExceptionResultThrowArgument(arguments);
	}

//	private static int extractNumArgsIncludingThrow(JProgramDeclaration programDeclaration) {
//		return programDeclaration.getParameters().size();
//	}

	private static String extractProgramId(JProgramDeclaration programDeclaration) {
		if (programDeclaration.isStatic()) {
			return programDeclaration.getSignatureId() + "_" + programDeclaration.getProgramId();
		} else {
			return programDeclaration.getProgramId();
		}
	}

	private static String extractSignatureId(JProgramDeclaration programDeclaration) {
		if (programDeclaration.isStatic()) {
			return null;
		} else {
			return programDeclaration.getSignatureId();
		}
	}

	public JBindingKey(String programId, List<JType> arguments) {
		this(null, programId, arguments);
	}



	/* Method removeExceptionResultThrowArgument strongly assumes that the receiver object 
	 * is the first argument.
	 */
	private List<JType> removeExceptionResultThrowArgument(List<JType> arguments) {
		List<JType> argumentsWithoutThrow = new ArrayList<JType>();
		boolean isFirstArgument = true;
		for (JType type : arguments) {
			if (isFirstArgument || !type.dpdTypeNameExtract().equals("java_lang_Throwable")) {
				argumentsWithoutThrow.add(type);
				isFirstArgument = false;
			}
		}

		return argumentsWithoutThrow;
	}

	private static List<JType> extracParameterTypesList(JProgramDeclaration programDeclaration) {
		List<JType> parametersTypes = new ArrayList<JType>();
		for (JVariableDeclaration variableDeclaration : programDeclaration.getParameters()) {
//			if (!programDeclaration.getVarsResultOfArithmeticOperationsInContracts().contains(variableDeclaration.getVariable()))
					parametersTypes.add(variableDeclaration.getType());
		}
		return parametersTypes;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getProgramId() {
		return programId;
	}

	public void setProgramId(String programId) {
		this.programId = programId;
	}

	public List<JType> getArguments() {
		return arguments;
	}

	public void setArguments(List<JType> arguments) {
		this.arguments = arguments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
		result = prime * result + ((moduleId == null) ? 0 : moduleId.hashCode());
		result = prime * result + ((programId == null) ? 0 : programId.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JBindingKey other = (JBindingKey) obj;
		if (arguments == null) {
			if (other.arguments != null)
				return false;
		} else if (!arguments.equals(other.arguments))
			return false;
		if (moduleId == null) {
			if (other.moduleId != null)
				return false;
		} else if (!moduleId.equals(other.moduleId))
			return false;
		if (programId == null) {
			if (other.programId != null)
				return false;
		} else if (!programId.equals(other.programId)) {
			return false;
		} 

		return true;
	}

	@Override
	public String toString() {
		String s = "";
		if (this.moduleId != null) {
			s += this.moduleId + "::";
		}
		s += programId + "(";
		for (int i = 0; i < this.arguments.size(); i++) {
			JType type = this.arguments.get(i);
			if (i > 0) {
				s += ",";
			}
			s += type;

		}
		s += ")";
		s += ":" + this.arguments.size();
		return s;
	}

	public boolean equalsWithOutModuleId(JBindingKey obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JBindingKey other = (JBindingKey) obj;
		if (arguments == null) {
			if (other.arguments != null)
				return false;
		} else if (!arguments.equals(other.arguments))
			return false;
		if (programId == null) {
			if (other.programId != null)
				return false;
		} else if (!programId.equals(other.programId))
			return false;
		return true;
	}

	@Override
	public int compareTo(JBindingKey otherBindingKey) {
		if (this.getModuleId() == null) {
			if (otherBindingKey.getModuleId() != null) {
				return -1;
			}
		} else {
			if (otherBindingKey.getModuleId() != null) {
				int moduleCompare = this.getModuleId().compareTo(otherBindingKey.getModuleId());
				if (moduleCompare!=0) {
					return moduleCompare;
				}
			}
			//if not, continue
		}

		int programCompare = this.getProgramId().compareTo(otherBindingKey.getProgramId());
		if (programCompare!=0) {
			return programCompare;
		}

		int argumentTypeCompare = this.getArguments().toString().compareTo(otherBindingKey.getArguments().toString());
		if (argumentTypeCompare!=0) {
			return argumentTypeCompare;
		}		
		return 0;
	}
}
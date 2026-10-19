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

/**
 * The key under which the bindings match calls with programs: a module id, a program id and the
 * argument types. {@code ProgramDeclarationCollectorVisitor} keys every program with
 * {@link #JBindingKey(JProgramDeclaration)}; {@code SemanticCheckVisitor} keys every call with
 * the type of its first argument (the receiver) as module id, or none for a static call, and the
 * call's program id; {@code CallBindingResolver} compares the two.
 *
 * <p>A static program (one whose first parameter is {@code throw}) has no module id and the
 * program id {@code <signature>_<program>}, which is how TACO names its calls; an instance
 * program has its signature as module id and its own name as program id; every constructor has
 * the program id {@code Constructor}. The argument types are the parameter types without
 * {@code throw} (see {@link #JBindingKey(String, String, List)}). See docs/NAMING.md.
 */
public class JBindingKey implements Comparable<JBindingKey> {

	private String moduleId;
	private String programId;
	private List<JType> arguments;
//	private int numArgs;
//	private boolean comesFromFunctionCall;

//	public boolean comesFromFunctionCall(){
//		return comesFromFunctionCall;
//	}
	
	/**
	 * Returns the number of argument types, {@code throw} excluded.
	 *
	 * @return the size of {@link #getArguments()}
	 */
	public int getNumArgs(){
		return this.arguments.size();
	}
	
	/**
	 * Builds the key of a program: module id and program id as in the class comment, and the types
	 * of its parameters, filtered as in {@link #JBindingKey(String, String, List)}. For a
	 * constructor the program id is {@code Constructor}, whatever the program is named.
	 *
	 * @param programDeclaration the program; it must have at least one parameter ({@code thiz} or
	 *        {@code throw}), which {@code isStatic()} reads
	 */
	public JBindingKey(JProgramDeclaration programDeclaration) {
		// this constructor need to be changed to a method in some place, maybe
		// an static method here.)
		this(extractSignatureId(programDeclaration), extractProgramId(programDeclaration), extracParameterTypesList(programDeclaration));
		if (programDeclaration.isConstructor()){
			this.programId = "Constructor";
		}
	}

	/**
	 * Builds a key from its parts. The argument types are copied without {@code throw}: the first
	 * one is always kept (the receiver of an instance call, or {@code throw} itself for a static
	 * one), and every later one whose type name ({@code dpdTypeNameExtract()}) is
	 * {@code java_lang_Throwable} is dropped, so a parameter declared {@code Throwable} is dropped
	 * too.
	 *
	 * @param moduleId the receiver's signature id, or null for a static program or call
	 * @param programId the program id
	 * @param arguments the argument types, the receiver (or {@code throw}) first
	 */
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

	/**
	 * Builds a key without module id, as for a static call.
	 *
	 * @param programId the program id ({@code <signature>_<program>} for a static program)
	 * @param arguments the argument types, {@code throw} first
	 */
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

	/**
	 * Returns the module id.
	 *
	 * @return the receiver's signature id, or null for a static program or call
	 */
	public String getModuleId() {
		return moduleId;
	}

	/**
	 * Sets the module id. Unused; it changes {@link #hashCode()}, so it must not be called on a key
	 * that is in a map.
	 *
	 * @param moduleId the new module id
	 */
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	/**
	 * Returns the program id.
	 *
	 * @return {@code Constructor}, {@code <signature>_<program>} for a static program, or the
	 *         program's own name
	 */
	public String getProgramId() {
		return programId;
	}

	/**
	 * Sets the program id. Unused; it changes {@link #hashCode()}.
	 *
	 * @param programId the new program id
	 */
	public void setProgramId(String programId) {
		this.programId = programId;
	}

	/**
	 * Returns the argument types, {@code throw} excluded.
	 *
	 * @return the list itself, not a copy
	 */
	public List<JType> getArguments() {
		return arguments;
	}

	/**
	 * Sets the argument types as given: unlike the constructors, it does not remove {@code throw}.
	 * Unused; it changes {@link #hashCode()}.
	 *
	 * @param arguments the new argument types
	 */
	public void setArguments(List<JType> arguments) {
		this.arguments = arguments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	/**
	 * Returns a hash code of the module id, the program id and the argument types.
	 *
	 * @return the hash code
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
	/**
	 * Compares the module id, the program id and the argument types ({@code JType.equals}).
	 *
	 * @param obj the object to compare with
	 * @return true if {@code obj} is a {@code JBindingKey} with equal parts
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

	/**
	 * Returns {@code <module>::<program>(<types>):<count>}, without {@code <module>::} when
	 * there is no module id; used in log and error messages.
	 *
	 * @return the key as text
	 */
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

	/**
	 * Compares the program id and the argument types, ignoring the module id; {@link
	 * SearchImplementors} uses it to match a program with those of the subclasses. The argument
	 * types include the receiver's ({@code thiz}), which for a program of a subclass is that class,
	 * so a program and its override never compare equal.
	 *
	 * @param obj the key to compare with
	 * @return true if both keys have the same program id and argument types
	 */
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

	/**
	 * Orders keys by module id, then program id, then the text of the argument types. A key without
	 * module id comes before one with a module id, but a key with a module id compared with one
	 * without goes on to the program ids, so the order is not antisymmetric across the two kinds
	 * (the {@code Comparable} contract). {@code CallBindingResolver} sorts only candidates of one
	 * kind, for its "Ambiguous call" message.
	 *
	 * @param otherBindingKey the key to compare with
	 * @return a negative number, zero or a positive number
	 */
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
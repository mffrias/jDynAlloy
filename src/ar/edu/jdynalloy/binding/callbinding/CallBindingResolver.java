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

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ar.edu.jdynalloy.JDynAlloySemanticException;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.binding.JBindingKey;
import ar.edu.jdynalloy.xlator.JDynAlloyContext;
import ar.edu.jdynalloy.xlator.JType;
import ar.edu.jdynalloy.xlator.JTypeHelper;
import ar.uba.dc.rfm.alloy.ast.formulas.IProgramCall;

public class CallBindingResolver extends JDynAlloyVisitor {

	private static Logger log = Logger.getLogger(CallBindingResolver.class);

	private Map<JBindingKey, JProgramDeclaration> programBindings;
	private IdentityHashMap<IProgramCall, JBindingKey> callBindings;
	private JDynAlloyContext dynJAlloyContext;
	private SortedSet<String> bindingsErrors = new TreeSet<String>();
	private boolean stopOnFirstBindingError = true; // FALSE is EXPERIMENTAL. TRUE is a stable choice
	/**
	 * The result of binding resolver will be put in this map
	 */
	private final IdentityHashMap<IProgramCall, JProgramDeclaration> binding = new IdentityHashMap<IProgramCall, JProgramDeclaration>();

	public CallBindingResolver(boolean isJavaArithmetic) {
		super(isJavaArithmetic);
	}

	public CallBindingResolver(JDynAlloyContext dynJAlloyContext, Map<JBindingKey, JProgramDeclaration> programBindings,
			IdentityHashMap<IProgramCall, JBindingKey> callBindings, boolean isJavaArithmetic) {
		super(isJavaArithmetic);
		this.dynJAlloyContext = dynJAlloyContext;
		this.callBindings = callBindings;
		this.programBindings = programBindings;
	}

	public Map<JBindingKey, JProgramDeclaration> getProgramBindings() {
		return programBindings;
	}

	//	public void setProgramBindings(Map<JBindingKey, JProgramDeclaration> programBindings) {
	//		this.programBindings = programBindings;
	//	}

	public IdentityHashMap<IProgramCall, JBindingKey> getCallBindings() {
		return callBindings;
	}

	public void setCallBindings(IdentityHashMap<IProgramCall, JBindingKey> callBindings) {
		this.callBindings = callBindings;
	}

	public IdentityHashMap<IProgramCall, JProgramDeclaration> getBinding() {
		return binding;
	}

	public void resolveBinding() {
		//redundant error check to avoid continue on error
		boolean hasError = false;
		for (Entry<IProgramCall, JBindingKey> entry : callBindings.entrySet()) {
			try {
				JBindingKey bindingKey = entry.getValue();
				Set<JBindingKey> potencialPrograms = generatePotentialCandidatesSet(bindingKey);
				JBindingKey selectedBinding = resolveOverloading(bindingKey, potencialPrograms);
				JProgramDeclaration selectedProgramDeclaration = programBindings.get(selectedBinding);
				if (selectedProgramDeclaration == null) {
					if (stopOnFirstBindingError) {
						throw new JDynAlloySemanticException("Problem in binding resolver. The program declaration of this method can't be found:" + selectedBinding);
					} else {
						hasError = true;
					}
				} else {
					binding.put(entry.getKey(), selectedProgramDeclaration);
				}
			} catch (JDynAlloyBindingException e) {
				bindingsErrors.add(e.getMessage());
			}				
		}

		if (!bindingsErrors.isEmpty()) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("Bindings error: (stopOnFirstError is disabled)\r\n");
			int i=0;
			for (String s : bindingsErrors) {
				i++;
				buffer.append((i<10 ? "0" : "") +  i + ". " + s + "\r\n");
			}
			throw new JDynAlloySemanticException(buffer.toString());
		} else if (bindingsErrors.isEmpty() && hasError) {
			throw new JDynAlloySemanticException("Binding resolver error. Redundant error control detected an unnotified error");
		}

	}

	private Set<JBindingKey> generatePotentialCandidatesSet(JBindingKey bindingKey) {
		String moduleId = bindingKey.getModuleId();
		String programId = bindingKey.getProgramId();

		boolean moduleOrCompatibleHasMethods = false;
		boolean moduleOrCompatibleHasMethodWithSameProgramId = false;
		boolean moduleOrCompatibleHasMethodWithSameProgramIdAndSameArgumentCount = false;

		//We first separate all the bindings key that have the same moduleId.
		//This simplifies debugging.
		HashSet<JBindingKey> bindingKeysOfSameModule = new HashSet<JBindingKey>();

		Set<JBindingKey> potencialCandidates = new HashSet<JBindingKey>();
		for (JBindingKey potentialCandidate : programBindings.keySet()) {
			if ((moduleId == null && potentialCandidate.getModuleId() == null)
					|| ((moduleId != null && potentialCandidate.getModuleId() != null) &&
							(isAssignable(bindingKey.getArguments().get(0), potentialCandidate.getArguments().get(0)))
							)

					){
				moduleOrCompatibleHasMethods = true;
				bindingKeysOfSameModule.add(potentialCandidate);
			}
		}




		for (JBindingKey potencialCandidate : bindingKeysOfSameModule) {

			if (potencialCandidate.getProgramId().equals(programId)) {
				moduleOrCompatibleHasMethodWithSameProgramId = true;

				if (potencialCandidate.getNumArgs() == bindingKey.getNumArgs())
				{
					moduleOrCompatibleHasMethodWithSameProgramIdAndSameArgumentCount = true;
					potencialCandidates.add(potencialCandidate);
				}
			}

		}

		if (!moduleOrCompatibleHasMethods) {			
			String bindingErrorMsg = "Binding error. Module doesn't have program: " + bindingKey + ". Explanation: The module " + (bindingKey.getModuleId() == null ? " STATIC " : bindingKey.getModuleId()) + " or compatible module (parent) don't declare any program";
			log.error("Binding error: " + bindingErrorMsg);
			if (stopOnFirstBindingError) {				
				throw new JDynAlloySemanticException(bindingErrorMsg);
			} else {
				//				bindingsErrors.add(bindingErrorMsg);
				throw new JDynAlloyBindingException(bindingErrorMsg);
			}
		} 

		if (!moduleOrCompatibleHasMethodWithSameProgramId) {
			String bindingErrorMsg = "Program not found " + ( bindingKey.getModuleId() != null ? bindingKey.getModuleId() + "::" : "") + bindingKey.getProgramId() + ". Explanation: The module " + (bindingKey.getModuleId() == null ? " STATIC " : bindingKey.getModuleId()) + " or compatible module (parent) does not declare any program called " + bindingKey.getProgramId() +". Program call binding: " + bindingKey;;
			log.error("Binding error: " + bindingErrorMsg);
			if (stopOnFirstBindingError) {				
				throw new JDynAlloySemanticException(bindingErrorMsg);
			} else {
				//				bindingsErrors.add(bindingErrorMsg);
				throw new JDynAlloyBindingException(bindingErrorMsg);
			}
		} 	

		if (!moduleOrCompatibleHasMethodWithSameProgramIdAndSameArgumentCount) {
			String bindingErrorMsg = "Program overload not found  " + ( bindingKey.getModuleId() != null ? bindingKey.getModuleId() + "::" : "") + bindingKey.getProgramId() + ". Explanation: The module " + (bindingKey.getModuleId() == null ? " STATIC " : bindingKey.getModuleId()) + " or compatible module (parent) don't declare any program called " + bindingKey.getProgramId() + " with " + bindingKey.getArguments().size()+ " arguments" +". Program call binding: " + bindingKey;;
			log.error("Binding error: " + bindingErrorMsg);
			if (stopOnFirstBindingError) {				
				throw new JDynAlloySemanticException(bindingErrorMsg);
			} else {
				//				bindingsErrors.add(bindingErrorMsg);
				throw new JDynAlloyBindingException(bindingErrorMsg);
			}
		} 
		//		} else {
		//			for (JBindingKey potentialCandidate : programBindings.keySet()) {
		//				if (((moduleId == null && potentialCandidate.getModuleId() == null)
		//						|| (moduleId != null && potentialCandidate.getModuleId() != null && moduleId.equals(potentialCandidate.getModuleId()))) 
		//						&& (programId.equals(moduleId+"_"+potentialCandidate.getProgramId())) &&
		//						isAssignable(bindingKey.getArguments().get(0), potentialCandidate.getArguments().get(0))
		//						&& bindingKey.getNumArgs() == potentialCandidate.getNumArgs()){
		//					potencialCandidates.add(potentialCandidate);
		//				}
		//			}
		//		}
		return potencialCandidates;
	}


	private JBindingKey resolveOverloading(JBindingKey bindingKey, Set<JBindingKey> potencialPrograms) {
		//one posible candidate! it's an easy choice!
		if (potencialPrograms.size() == 1) {
			return potencialPrograms.iterator().next();
		}

		Set<JBindingKey> originalPotencialPrograms = new TreeSet<JBindingKey>(potencialPrograms);


		JBindingKey selectedBinding;
		if (potencialPrograms.isEmpty()) {
			String bindingErrorMsg = "Binding error. Binding not found. Couldn't bind function: " + bindingKey + ". Explanation: Programs of " + (bindingKey.getModuleId() == null ? " STATIC " : bindingKey.getModuleId()) + "(or compatible) module with called" + bindingKey.getProgramId() + " don't have compatible arguments.";
			log.error("Binding error: " + bindingErrorMsg);
			//			if (stopOnFirstBindingError) {				
			throw new JDynAlloySemanticException(bindingErrorMsg);
			//			} else {
			//				//now error is handled in generatePotencialCandidatesSet
			////				bindingsErrors.add(bindingErrorMsg);
			//			}
			//			return null;
		} else {

			//nonempty candidate set
			while (potencialPrograms.size() > 1) {
				Set<JBindingKey> newPotencialPrograms = new HashSet<JBindingKey>();

				for (JBindingKey inspectedCandidateBinding : potencialPrograms) {
					boolean mustBeRejected = false;
					if (!isAssignable(bindingKey.getArguments(), inspectedCandidateBinding.getArguments())) {
						mustBeRejected = true;
					}
					for (JBindingKey otherCandidateBinding : potencialPrograms) {
						if (inspectedCandidateBinding != otherCandidateBinding) {
							if (isAssignable(otherCandidateBinding.getArguments(), inspectedCandidateBinding.getArguments())) {
								mustBeRejected = true;
							}
						}
					}
					if (!mustBeRejected) {
						newPotencialPrograms.add(inspectedCandidateBinding);
					}
				}
				potencialPrograms = newPotencialPrograms;
			}

			if (potencialPrograms.isEmpty()) {
				String bindingErrorMsg = "Binding error. Ambiguous call: " + bindingKey + ". potencial bindings: " + originalPotencialPrograms + ". Explanation: Most specific method can't be chosen";
				log.error("Ambygous call: " + bindingErrorMsg);
				if (stopOnFirstBindingError) {				
					throw new JDynAlloySemanticException(bindingErrorMsg);
				} else {
					//					bindingsErrors.add(bindingErrorMsg);
					throw new JDynAlloyBindingException(bindingErrorMsg);
				}

				//				//return null on error
				//				return null; 

			} else {

				// get first binding!
				selectedBinding = potencialPrograms.iterator().next();

				return selectedBinding;
			}
		}
	}

	/**
	 * Returns true when the first parameter list can be assignated to the
	 * second one. It means than the fromTypeList[i] must be assigned to
	 * toTypeList[i], for all 0<i<lenght(fromTypeList)
	 * 
	 * @param fromTypeList
	 * @param toTypeList
	 * @return
	 */
	private boolean isAssignable(List<JType> fromTypeList, List<JType> toTypeList) {
		if (fromTypeList.size() != toTypeList.size()) {
			return false;
		}

		boolean returnValue = true;
		for (int i = 0; i < fromTypeList.size(); i++) {
			JType fromType = fromTypeList.get(i);
			JType toType = toTypeList.get(i);
			returnValue &= isAssignable(fromType, toType);
		}
		return returnValue;
	}

	/**
	 * Returns true if a variable of type fromType can be assigned t
	 * 
	 * @param fromType
	 * @param toType
	 * @return
	 */
	private boolean isAssignable(JType fromType, JType toType) {
		//null is compatible with everytype
		if (fromType.isNull()) {
			return true;
		}

		//		if (JType.fromIncludesNull(fromType)) {
		//			if (!JType.fromIncludesNull(toType)) {
		//				return false;
		//			}
		//		}

		String fromTypeAsString = fromType.dpdTypeNameExtract();
		String toTypeAsString = toType.dpdTypeNameExtract();
		Set<String> decendantsOfToType = this.dynJAlloyContext.descendantsOf(toTypeAsString);

		if (fromTypeAsString.equals(toTypeAsString) || decendantsOfToType.contains(fromTypeAsString)) {
			return true;
		} else {
			return false;
		}
	}

	// TODO DOB::Extract to a common method (static or not ) in JType
	private String extractReceiverTypeName(JType type) {
		String returnValue;
		if (JTypeHelper.fromIncludesNull(type)) {
			returnValue = JTypeHelper.getBaseType(type);
		} else {
			returnValue = type.singletonFrom();
		}
		return returnValue;
	}

	/**
	 * @return the dynJAlloyContext
	 */
	public JDynAlloyContext getDynJAlloyContext() {
		return dynJAlloyContext;
	}

	/**
	 * @param dynJAlloyContext
	 *            the dynJAlloyContext to set
	 */
	public void setDynJAlloyContext(JDynAlloyContext dynJAlloyContext) {
		this.dynJAlloyContext = dynJAlloyContext;
	}

}

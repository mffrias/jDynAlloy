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

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.binding.callbinding.CallBindingResolver;
import ar.edu.jdynalloy.binding.callbinding.ProgramDeclarationCollectorVisitor;
import ar.edu.jdynalloy.binding.callbinding.SemanticCheckVisitor;
import ar.edu.jdynalloy.binding.fieldcollector.FieldCollectorVisitor;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.xlator.JDynAlloyBinding;
import ar.edu.jdynalloy.xlator.JDynAlloyContext;
import ar.uba.dc.rfm.alloy.ast.formulas.IProgramCall;


public class BindingManager {

	private boolean isJavaArithmetic;

	private static Logger log = Logger.getLogger(BindingManager.class);

	private List<ar.edu.jdynalloy.ast.JDynAlloyModule> modules;
	private JDynAlloyBinding dynJAlloyBinding;

//	private boolean javaArithmetic;

	public void setJavaArithmetic(boolean isJavaArithmetic) {
		this.isJavaArithmetic = isJavaArithmetic;
	}

	public boolean getIsJavaArithmetic() {
		return this.isJavaArithmetic;
	}


	public JDynAlloyBinding getDynJAlloyBinding() {
		return dynJAlloyBinding;
	}

	public List<JDynAlloyModule> getModules() {
		return modules;
	}

	public BindingManager(List<JDynAlloyModule> modules) {
		this.modules = modules;
	}


	public void execute() {

		JDynAlloyContext dynJAlloyContext = new JDynAlloyContext();
		for (JDynAlloyModule dynJAlloyModule : modules) {
			dynJAlloyContext.load(dynJAlloyModule);
		}
		SymbolTable symbolTable = new SymbolTable();
		symbolTable.setJavaArithmetic(isJavaArithmetic);
		ProgramDeclarationCollectorVisitor programDeclarationCollectorVisitor = new ProgramDeclarationCollectorVisitor(isJavaArithmetic);
		SemanticCheckVisitor semanticCheckVisitor = new SemanticCheckVisitor(symbolTable, isJavaArithmetic);
		FieldCollectorVisitor fieldCollectorVisitor = new FieldCollectorVisitor(symbolTable, isJavaArithmetic);

		//ProgramDeclarationCollectorVisitor and fieldCollectorVisitor don't have inter-dependences.
		//They are run together. But semanticCheckVisitor visitor needs the Fields collected by fieldCollectorVisitor.
		//fieldCollectorVisitor must be run BEFORE semanticCheckVisitor
		for (JDynAlloyModule dynJAlloyModule : modules) {
			dynJAlloyModule.accept(programDeclarationCollectorVisitor);
			dynJAlloyModule.accept(fieldCollectorVisitor);
		}

		for (JDynAlloyModule dynJAlloyModule : modules) {
			dynJAlloyModule.accept(semanticCheckVisitor);			
		}

		CallBindingResolver callBindingResolver = new CallBindingResolver(dynJAlloyContext, 
				programDeclarationCollectorVisitor.getProgramBindings(), 
				semanticCheckVisitor.getCallBindings(), isJavaArithmetic);
		callBindingResolver.resolveBinding();			
		IdentityHashMap<IProgramCall, JProgramDeclaration> bindings = callBindingResolver.getBinding();		

		SearchImplementors searchImplementors = new SearchImplementors(programDeclarationCollectorVisitor.getProgramBindings(), dynJAlloyContext);			
		IdentityHashMap<JProgramDeclaration, List<JProgramDeclaration>> implementors = searchImplementors.searchImplementors();

		this.dynJAlloyBinding = new JDynAlloyBinding(bindings, implementors);

		log.debug("bindings:");
		for (Entry<IProgramCall, JProgramDeclaration> entry : bindings.entrySet()) {
			log.debug(entry);
		}
		log.debug("implementors:");
		for (Entry<JProgramDeclaration, List<JProgramDeclaration>> entry : implementors.entrySet()) {
			log.debug(entry);
		}		
	}
}

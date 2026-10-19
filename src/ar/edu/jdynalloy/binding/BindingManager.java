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


/**
 * Resolves which program each call invokes: the first step of TACO's {@code JDynAlloyStage},
 * which runs it on all modules before the relevancy analysis and again on the sliced modules
 * before the translation (docs/ARCHITECTURE.md, "How TACO calls it", steps 1 and 5).
 *
 * <p>{@link #execute()} collects the programs ({@link ProgramDeclarationCollectorVisitor}) and
 * the fields ({@link FieldCollectorVisitor}) of the modules, types the arguments of every call
 * ({@link SemanticCheckVisitor}), resolves each call to a program
 * ({@link CallBindingResolver}) and looks for the implementors of {@code virtual} programs
 * ({@link SearchImplementors}, which never finds any: docs/KNOWN-LIMITATIONS.md, "Dynamic
 * dispatch does not work"). The result is a {@link JDynAlloyBinding}; TACO then sets its
 * {@code unfoldScopeForRecursiveCode} to its loop-unrolling bound.
 *
 * <p>No static state besides the logger; TACO creates one instance per call.
 */
public class BindingManager {

	private boolean isJavaArithmetic;

	private static Logger log = Logger.getLogger(BindingManager.class);

	private List<ar.edu.jdynalloy.ast.JDynAlloyModule> modules;
	private JDynAlloyBinding dynJAlloyBinding;

//	private boolean javaArithmetic;

	/**
	 * Sets whether Java arithmetic is in use (TACO's {@code useJavaArithmetic}: primitive values
	 * are the {@code JavaPrimitive*Value} modules). {@link #execute()} passes it to its symbol
	 * table and visitors; call this before it. The default is false.
	 *
	 * @param isJavaArithmetic true with Java arithmetic
	 */
	public void setJavaArithmetic(boolean isJavaArithmetic) {
		this.isJavaArithmetic = isJavaArithmetic;
	}

	/**
	 * Returns the flag set by {@link #setJavaArithmetic(boolean)}.
	 *
	 * @return whether Java arithmetic is in use
	 */
	public boolean getIsJavaArithmetic() {
		return this.isJavaArithmetic;
	}


	/**
	 * Returns the binding built by the last {@link #execute()}.
	 *
	 * @return the binding, or null before {@code execute()} has run
	 */
	public JDynAlloyBinding getDynJAlloyBinding() {
		return dynJAlloyBinding;
	}

	/**
	 * Returns the modules given to the constructor.
	 *
	 * @return the same list, not a copy
	 */
	public List<JDynAlloyModule> getModules() {
		return modules;
	}

	/**
	 * Creates a manager for the given modules; nothing is computed until {@link #execute()}.
	 *
	 * @param modules the modules to bind: every call in them must resolve to a program of one of
	 *        them
	 */
	public BindingManager(List<JDynAlloyModule> modules) {
		this.modules = modules;
	}


	/**
	 * Builds the binding of the modules and stores it (see {@link #getDynJAlloyBinding()}).
	 *
	 * <p>Collects every program, keyed by {@link JBindingKey}, and every field, into a new
	 * {@link SymbolTable}; then keys every call by its argument types: the {@code call} statements
	 * of program bodies, and the {@code callSpec}/{@code staticCallSpec} formulas of invariants,
	 * constraints, {@code represents}, {@code requires}, {@code ensures}, loop invariants and
	 * {@code assume} conditions (not those of {@code assert}, {@code if} and {@code while}
	 * conditions); then resolves each call to the most specific program, and searches implementors.
	 * The {@link JDynAlloyContext} it builds reads {@code getRelevantClasses()} from
	 * {@code JDynAlloyConfig} while loading the modules. Logs the bindings at debug level.
	 *
	 * @throws ar.edu.jdynalloy.JDynAlloySemanticException if a call matches no program or is
	 *         ambiguous ("Program not found", "Program overload not found", "Ambiguous call", see
	 *         {@link CallBindingResolver#resolveBinding()}), if a name is declared twice, or if
	 *         an argument uses an unknown variable or field. A field declared twice in a module
	 *         is not reported: {@code JDynAlloyVisitor.visit(JDynAlloyModule)} catches the
	 *         exception and only prints the module's id
	 * @throws ar.edu.jdynalloy.JDynAlloyNotImplementedYetException if a specification calls a
	 *         function whose type {@code ExpressionTypeResolver} does not know
	 */
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

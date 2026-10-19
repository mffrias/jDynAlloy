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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.xlator.JDynAlloyContext;


/**
 * Meant to find, for each {@code virtual} program, the programs that implement it in the
 * subclasses of its signature, for {@code VAlloyVisitor}'s dynamic dispatch.
 * {@link BindingManager#execute()} runs it and stores the result in the
 * {@code JDynAlloyBinding}.
 *
 * <p>It never finds anything: {@link #searchImplementors()} compares each program's key with
 * itself and never puts a list in the map it returns. See docs/KNOWN-LIMITATIONS.md ("Dynamic
 * dispatch does not work") and docs/OPEN-QUESTIONS.md (4).
 */
public class SearchImplementors {

	Map<JBindingKey, JProgramDeclaration> programBindings;
	JDynAlloyContext dynJAlloyContext;
	
	/**
	 * Creates a search over the given programs.
	 *
	 * @param programBindings every program, keyed by {@link JBindingKey} (from
	 *        {@code ProgramDeclarationCollectorVisitor})
	 * @param dynJAlloyContext the context of the same modules, for the class hierarchy
	 */
	public SearchImplementors(Map<JBindingKey, JProgramDeclaration> programBindings, JDynAlloyContext dynJAlloyContext) {
		super();
		this.programBindings = programBindings;
		this.dynJAlloyContext = dynJAlloyContext;
	}

	/**
	 * Returns the programs searched.
	 *
	 * @return the map given to the constructor or to {@link #setProgramBindings(Map)}
	 */
	public Map<JBindingKey, JProgramDeclaration> getProgramBindings() {
		return programBindings;
	}

	/**
	 * Replaces the programs to search.
	 *
	 * @param programBindings every program, keyed by {@link JBindingKey}
	 */
	public void setProgramBindings(Map<JBindingKey, JProgramDeclaration> programBindings) {
		this.programBindings = programBindings;
	}

	/**
	 * Returns the context used for the class hierarchy.
	 *
	 * @return the context
	 */
	public JDynAlloyContext getDynJAlloyContext() {
		return dynJAlloyContext;
	}

	/**
	 * Replaces the context used for the class hierarchy.
	 *
	 * @param dynJAlloyContext the new context
	 */
	public void setDynJAlloyContext(JDynAlloyContext dynJAlloyContext) {
		this.dynJAlloyContext = dynJAlloyContext;
	}

	/**
	 * Returns an empty map. It is meant to map each {@code virtual} program to the programs
	 * with the same key, module id aside, in the strict subclasses of its signature. But the
	 * inner loop reads {@code entry} where it means {@code inspectedEntry}, so it compares
	 * each key with itself, whose module is not a strict subclass of itself; and the list it
	 * builds is never put in the map. Even with both fixed,
	 * {@link JBindingKey#equalsWithOutModuleId(JBindingKey)} compares the receiver types,
	 * which differ between a class and its subclasses.
	 *
	 * @return an identity map from virtual programs to their implementors; always empty
	 */
	public IdentityHashMap<JProgramDeclaration, List<JProgramDeclaration>> searchImplementors() {
		IdentityHashMap<JProgramDeclaration, List<JProgramDeclaration>> implementationsFoundSet = new IdentityHashMap<JProgramDeclaration, List<JProgramDeclaration>>();
		for(Entry<JBindingKey, JProgramDeclaration> entry : programBindings.entrySet()) {
			List<JProgramDeclaration> implementorsOfThisEntry = new ArrayList<JProgramDeclaration>(); 
			JProgramDeclaration programDeclaration = entry.getValue();
			if (programDeclaration.isVirtual()) {
				JBindingKey bindingKey = entry.getKey();				
				
				//busca entre todos los metodos declarados, alguno que sea hijo de la misma clase y 
				//que tenga un programa que tenga la misma bindingKey (sin considerar el nombre del modulo)
				
				//obtengo todos los decendiente del modulo al que pertenece el programa que quiero analizar
				Set<String> decendingModules = this.dynJAlloyContext.descendantsOf(programDeclaration.getSignatureId());
				
				//recorro todos los programas e inspecciono solo los que pertencen a los modulos del paso anterior
				for(Entry<JBindingKey, JProgramDeclaration> inspectedEntry : programBindings.entrySet()) {
					JProgramDeclaration inspectedProgramDeclaration = entry.getValue();
					JBindingKey inspectedBindingKey = entry.getKey();
					
					if ( decendingModules.contains(inspectedBindingKey.getModuleId())) {
						//verifico que el nombre y los argumentos del metodo inspeccionado coincidan
						if (bindingKey.equalsWithOutModuleId(inspectedBindingKey)) {
							//lo agrego a la lista de implementaciones encontradas
							implementorsOfThisEntry.add(programBindings.get(inspectedBindingKey));
						}
					}
					
				}
				
			}
		}
		return implementationsFoundSet;		
	}
	
}

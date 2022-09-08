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


public class SearchImplementors {

	Map<JBindingKey, JProgramDeclaration> programBindings;
	JDynAlloyContext dynJAlloyContext;
	
	public SearchImplementors(Map<JBindingKey, JProgramDeclaration> programBindings, JDynAlloyContext dynJAlloyContext) {
		super();
		this.programBindings = programBindings;
		this.dynJAlloyContext = dynJAlloyContext;
	}

	public Map<JBindingKey, JProgramDeclaration> getProgramBindings() {
		return programBindings;
	}

	public void setProgramBindings(Map<JBindingKey, JProgramDeclaration> programBindings) {
		this.programBindings = programBindings;
	}

	public JDynAlloyContext getDynJAlloyContext() {
		return dynJAlloyContext;
	}

	public void setDynJAlloyContext(JDynAlloyContext dynJAlloyContext) {
		this.dynJAlloyContext = dynJAlloyContext;
	}

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

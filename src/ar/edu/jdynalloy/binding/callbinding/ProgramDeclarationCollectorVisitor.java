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

import java.util.HashMap;
import java.util.Map;

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.binding.JBindingKey;

public class ProgramDeclarationCollectorVisitor extends JDynAlloyVisitor {



	public ProgramDeclarationCollectorVisitor(boolean isJavaArithmetic) {
		super(isJavaArithmetic);
	}


	final Map<JBindingKey, JProgramDeclaration> programBindings = new HashMap<JBindingKey, JProgramDeclaration>();
	String moduleId;

	@Override
	public Object visit(JDynAlloyModule node) {
		this.moduleId = node.getModuleId();
		return super.visit(node);
	}

	@Override
	public Object visit(JProgramDeclaration programDeclaration) {
		JBindingKey bindingKey = new JBindingKey(programDeclaration);
		programBindings.put(bindingKey, programDeclaration);

		return null;
	}

	/**
	 * @return the programBindings
	 */
	public Map<JBindingKey, JProgramDeclaration> getProgramBindings() {
		return programBindings;
	}

}

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
package ar.edu.jdynalloy.buffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JClassConstraint;
import ar.edu.jdynalloy.ast.JClassInvariant;
import ar.edu.jdynalloy.ast.JField;
import ar.edu.jdynalloy.ast.JObjectConstraint;
import ar.edu.jdynalloy.ast.JObjectInvariant;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JRepresents;
import ar.edu.jdynalloy.ast.JSignature;
import ar.edu.jdynalloy.factory.JSignatureFactory;
import ar.edu.taco.simplejml.builtin.IBuiltInModule;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public class StaticFieldsModuleBuilder implements IBuiltInModule {

	private static StaticFieldsModuleBuilder instance;
	private List<JField> fields;
	private Set<JClassInvariant> staticInvariants;
	private Set<JClassConstraint> staticConstraints;
	private Semaphore mySemaphore = new Semaphore(1);

	private StaticFieldsModuleBuilder() {
		fields = new LinkedList<JField>();
		staticInvariants = new HashSet<JClassInvariant>();
		staticConstraints = new HashSet<JClassConstraint>();
	}

	public void addStaticField(JField field) {
		try {
			mySemaphore.acquire();
			if (!this.fields.contains(field))
				this.fields.add(field);
		} catch (InterruptedException e){
			System.out.println("Concurrent access problem..");
		} finally {
			mySemaphore.release();
		}
	}
	
	public void addStaticInvariant(JClassInvariant invariant) {
		this.staticInvariants.add(invariant);
	}

	public void addStaticConstraint(JClassConstraint constraint) {
		this.staticConstraints.add(constraint);
	}

	@Override
	public JDynAlloyModule getModule() {
		JSignature signature = JSignatureFactory.buildClassFieldsSignature();

		final JSignature classSignature;
		classSignature = null;

		JDynAlloyModule module = new JDynAlloyModule(signature.getSignatureId(), signature, classSignature, null, this.fields, this.staticInvariants, 
				this.staticConstraints, Collections.<JObjectInvariant> emptySet(), Collections.<JObjectConstraint> emptySet(), 
				Collections.<JRepresents> emptySet(), Collections.<JProgramDeclaration> emptySet(), new AlloyTyping(), new ArrayList<AlloyFormula>(), false);

		return module;
	}

	public static StaticFieldsModuleBuilder getInstance() {
		if (instance == null) {
			instance = new StaticFieldsModuleBuilder();
		}
		
		return instance;
	}

}

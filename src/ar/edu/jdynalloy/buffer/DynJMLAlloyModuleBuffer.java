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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
import ar.edu.jdynalloy.factory.JTypeFactory;
import ar.edu.jdynalloy.xlator.JDynAlloyTyping;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * Ex NormaJavaVisitor.NormaJavaVisitorBuffer representa la data recopilada por
 * el visitor Esta es la informacion parcial acumulada, en la que finalmente
 * tendra todo el programa DynJAlloy
 * 
 * @author diegodob
 * 
 */
public class DynJMLAlloyModuleBuffer {
	private JDynAlloyTyping fields = new JDynAlloyTyping();

	private JDynAlloyTyping staticFields = new JDynAlloyTyping();

	private String superClassSignatureId = null;

	private Set<String> superInterfaces = new HashSet<String>();

	private boolean isAbstract = false;

	private JType thisType = null;

	// private final Set<JProgramDeclaration> programs = new
	// HashSet<JProgramDeclaration>();
	private final List<JProgramDeclaration> programs = new ArrayList<JProgramDeclaration>();

	private Vector<AlloyFormula> invariants = new Vector<AlloyFormula>();
	private Vector<AlloyFormula> staticInvariants = new Vector<AlloyFormula>();

	private Vector<AlloyFormula> constraints = new Vector<AlloyFormula>();
	private Vector<AlloyFormula> staticConstraints = new Vector<AlloyFormula>();

	private Vector<Represents> represents = new Vector<Represents>();

	private Map<String, List<AlloyVariable>> inGroupClauses = new HashMap<String, List<AlloyVariable>>();

	private String signatureId;

	private boolean isInterface;

	private String inSignatureId;

	private AlloyFormula fact;

	public JDynAlloyTyping getFields() {
		return fields;
	}

	public void setFields(JDynAlloyTyping fields) {
		this.fields = fields;
	}

	public JDynAlloyTyping getStaticFields() {
		return staticFields;
	}

	public void setStaticFields(JDynAlloyTyping staticFields) {
		this.staticFields = staticFields;
	}

	public String getSuperClassSignatureId() {
		return superClassSignatureId;
	}

	public void setSuperClassSignatureId(String superClassSignatureId) {
		this.superClassSignatureId = superClassSignatureId;
	}

	public Set<String> getSuperInterfaces() {
		return superInterfaces;
	}

	public void setSuperInterfaces(Set<String> superInterfaces) {
		this.superInterfaces = superInterfaces;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public JType getThisType() {
		return thisType;
	}

	public void setThisType(JType thisType) {
		this.thisType = thisType;
	}

	public String getSignatureId() {
		return signatureId;
	}

	public void setSignatureId(String signatureId) {
		this.signatureId = signatureId;
	}

	public boolean isInterface() {
		return isInterface;
	}

	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}

	// public String getPreModule() {
	// return preModule;
	// }

	// public void setPreModule(String preModule) {
	// this.preModule = preModule;
	// }

	public Vector<AlloyFormula> getInvariants() {
		return invariants;
	}

	public void setInvariants(Vector<AlloyFormula> invariants) {
		this.invariants = invariants;
	}

	public Vector<AlloyFormula> getStaticInvariants() {
		return staticInvariants;
	}

	public void setStaticInvariants(Vector<AlloyFormula> staticInvariants) {
		this.staticInvariants = staticInvariants;
	}

	public Vector<AlloyFormula> getConstraints() {
		return constraints;
	}

	public void setConstraints(Vector<AlloyFormula> constraints) {
		this.constraints = constraints;
	}

	public Vector<AlloyFormula> getStaticConstraints() {
		return staticConstraints;
	}

	public void setStaticConstraints(Vector<AlloyFormula> staticConstraints) {
		this.staticConstraints = staticConstraints;
	}

	public Vector<Represents> getRepresents() {
		return represents;
	}

	public void setRepresents(Vector<Represents> represents) {
		this.represents = represents;
	}

	public List<JProgramDeclaration> getPrograms() {
		return programs;
	}

	// private String preModule;

	private JSignature buildSignature() {
		JSignature result;
		if (this.isInterface || (this.inSignatureId != null) ) {
			Set<AlloyFormula> facts = null;
			if (this.fact != null) {
				facts = Collections.singleton(this.fact);
			}
			
			// At this point we don't know the interface implementors
			result = JSignatureFactory.buildInterface(this.signatureId, this.superInterfaces, facts, inSignatureId);

		} else {
			result = JSignatureFactory.buildClass(this.isAbstract, this.signatureId, new JDynAlloyTyping(), this.superClassSignatureId, this.superInterfaces);
		}
		return result;
	}

	public JDynAlloyModule getModule() {
		JSignature signature = buildSignature();
		String moduleId = signature.getSignatureId();

		JSignature classSig;
		classSig = null;

		List<JField> fields = new LinkedList<JField>();

		processFields(signature, fields);

		for (AlloyVariable v : this.staticFields.varSet()) {
			JType fieldType = JType.parse(JSignatureFactory.buildClassFieldsSignature().getSignatureId() + "->one(" + this.staticFields.get(v) + ")");

			v = new AlloyVariable(moduleId + "_" + v.getVariableId().getString());
			JField field = new JField(v, fieldType);
			StaticFieldsModuleBuilder.getInstance().addStaticField(field);
		}

		Set<JObjectInvariant> invariants = new HashSet<JObjectInvariant>();
		for (AlloyFormula f : this.invariants) {
			invariants.add(new JObjectInvariant(f));
		}

		Set<JClassInvariant> staticInvariants = new HashSet<JClassInvariant>();
		for (AlloyFormula f : this.staticInvariants) {
			StaticFieldsModuleBuilder.getInstance().addStaticInvariant(new JClassInvariant(f));
		}

		Set<JObjectConstraint> constraints = new HashSet<JObjectConstraint>();
		for (AlloyFormula f : this.constraints) {
			constraints.add(new JObjectConstraint(f));
		}

		Set<JClassConstraint> staticConstraints = new HashSet<JClassConstraint>();
		for (AlloyFormula f : this.staticConstraints) {
			StaticFieldsModuleBuilder.getInstance().addStaticConstraint(new JClassConstraint(f));
		}

		Set<JRepresents> represents = new HashSet<JRepresents>();
		for (Represents entry : this.represents) {
			represents.add(new JRepresents(entry.expr, entry.exprType, entry.formula));
		}

		return new JDynAlloyModule(moduleId, signature, classSig, null /* literal_signature */, fields, staticInvariants, staticConstraints, invariants,
				constraints, represents, new HashSet<JProgramDeclaration>(this.programs), new AlloyTyping(), new ArrayList<AlloyFormula>(), false);
	}

	protected void processFields(JSignature signature, List<JField> field_list) {
		for (AlloyVariable v : this.fields) {
			JType image = this.fields.getJAlloyType(v);

			JType fieldType;
			if (image.isSet()) {
				StringBuffer sb = new StringBuffer();
				for (String from : image.from()) {
					if (!sb.toString().isEmpty())
						sb.append("+");
					sb.append(from);
				}
				fieldType = JType.parse(signature.getSignatureId() + "->(" + sb.toString() + ")");
			} else if (image.isSequence()) {
				fieldType = JTypeFactory.buildFieldSeq(signature.getSignatureId(), image);
			} else if (v.getVariableId().getString().contains("SK_jml_pred_java_primitive_")) {
				fieldType = image;
			} else {
				fieldType = JType.parse(signature.getSignatureId() + "->one(" + image + ")");
			}

			JField field = new JField(v, fieldType);
			field_list.add(field);
		}
	}

	/**
	 * @return the inGroupClauses
	 */
	public Map<String, List<AlloyVariable>> getInGroupClauses() {
		return inGroupClauses;
	}

	/**
	 * @param inGroupClauses
	 *            the inGroupClauses to set
	 */
	public void setInGroupClauses(Map<String, List<AlloyVariable>> inGroupClauses) {
		this.inGroupClauses = inGroupClauses;
	}

	public void setFact(AlloyFormula fact) {
		this.fact = fact;
	}

	public AlloyFormula getFact() {
		return fact;
	}

	public void setInSignatureId(String inSignatureId) {
		this.inSignatureId = inSignatureId;
	}

	public String getInSignatureId() {
		return inSignatureId;
	}

}

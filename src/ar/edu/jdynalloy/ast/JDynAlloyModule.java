package ar.edu.jdynalloy.ast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.util.ArgAppenderExpressionMutator;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;

public final class JDynAlloyModule implements JDynAlloyASTNode {

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classSingleton == null) ? 0 : classSingleton.hashCode());
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime
				* result
				+ ((object_invariants == null) ? 0 : object_invariants
						.hashCode());
		result = prime
				* result
				+ ((literalSingleton == null) ? 0 : literalSingleton.hashCode());
		result = prime * result
				+ ((moduleId == null) ? 0 : moduleId.hashCode());
		result = prime * result
				+ ((programs == null) ? 0 : programs.hashCode());
		result = prime * result
				+ ((represents == null) ? 0 : represents.hashCode());
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JDynAlloyModule other = (JDynAlloyModule) obj;
		if (classSingleton == null) {
			if (other.classSingleton != null)
				return false;
		} else if (!classSingleton.equals(other.classSingleton))
			return false;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (object_invariants == null) {
			if (other.object_invariants != null)
				return false;
		} else if (!object_invariants.equals(other.object_invariants))
			return false;
		if (literalSingleton == null) {
			if (other.literalSingleton != null)
				return false;
		} else if (!literalSingleton.equals(other.literalSingleton))
			return false;
		if (moduleId == null) {
			if (other.moduleId != null)
				return false;
		} else if (!moduleId.equals(other.moduleId))
			return false;
		if (programs == null) {
			if (other.programs != null)
				return false;
		} else if (!programs.equals(other.programs))
			return false;
		if (represents == null) {
			if (other.represents != null)
				return false;
		} else if (!represents.equals(other.represents))
			return false;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}

	public JSignature getClassSingleton() {
		return classSingleton;
	}

	public void setClassSingleton(JSignature classSingleton) {
		this.classSingleton = classSingleton;
	}

	public JSignature getLiteralSingleton() {
		return literalSingleton;
	}

	public void setLiteralSingleton(JSignature literalSingleton) {
		this.literalSingleton = literalSingleton;
	}

	@Override
	public String toString() {
		return this.moduleId;
	}

	public boolean pinnedForNonRelevancyAnalysisForStryker = false;

	private final String moduleId;
	private final JSignature signature;
	private final Set<JProgramDeclaration> programs;
	private final List<JField> fields;

	private final Set<JClassInvariant> class_invariants;
	private final Set<JClassConstraint> class_constraints;

	private final Set<JObjectInvariant> object_invariants;
	private final Set<JObjectConstraint> object_constraints;
	private final Set<JRepresents> represents;

	private JSignature classSingleton;
	private JSignature literalSingleton;
	private AlloyTyping varsEncodingValueOfArithmeticOperationsInObjectInvariants = new AlloyTyping();
	private List<AlloyFormula> predsEncodingValueOfArithmeticOperationsInObjectInvariants = new ArrayList<AlloyFormula>();

	public JDynAlloyModule(String moduleId, 
			JSignature signature,
			JSignature class_singleton, 
			JSignature literal_signature,
			List<JField> fields, 
			Set<JClassInvariant> class_invariants,
			Set<JClassConstraint> class_constraints,
			Set<JObjectInvariant> object_invariants,
			Set<JObjectConstraint> object_constraints,
			Set<JRepresents> represents, 
			Set<JProgramDeclaration> programs,
			AlloyTyping varsEncodingValueOfArithmeticOperationsInObjectInvariants,
			List<AlloyFormula> predsEncodingValueOfArithmeticOperationsInObjectInvariants,
			boolean pinnedForStryker) {
		super();
		this.moduleId = moduleId;
		this.signature = signature;
		this.classSingleton = class_singleton;
		this.literalSingleton = literal_signature;
		this.fields = fields;
		this.class_invariants = class_invariants;
		this.class_constraints = class_constraints;
		this.object_invariants = object_invariants;
		this.object_constraints = object_constraints;
		this.represents = represents;
		this.programs = programs;
		this.predsEncodingValueOfArithmeticOperationsInObjectInvariants = predsEncodingValueOfArithmeticOperationsInObjectInvariants;
		this.varsEncodingValueOfArithmeticOperationsInObjectInvariants = varsEncodingValueOfArithmeticOperationsInObjectInvariants;
		this.pinnedForNonRelevancyAnalysisForStryker = pinnedForStryker;
	}

	public String getModuleId() {
		return moduleId;
	}

	public JSignature getSignature() {
		return signature;
	}

	public Set<JProgramDeclaration> getPrograms() {
		return programs;
	}

	public Object accept(IJDynAlloyVisitor visitor) {
		return visitor.visit(this);
	}

	public List<JField> getFields() {
		return fields;
	}

	public Set<JObjectInvariant> getObjectInvariants() {
		return object_invariants;
	}

	public Set<JObjectConstraint> getObjectConstraints() {
		return object_constraints;
	}

	public Set<JRepresents> getRepresents() {
		return represents;
	}

	public Set<JClassInvariant> getClassInvariants() {
		return class_invariants;
	}

	public Set<JClassConstraint> getClassConstraints() {
		return class_constraints;
	}
	
	


	public AlloyTyping getVarsEncodingValueOfArithmeticOperationsInObjectInvariants() {
		return this.varsEncodingValueOfArithmeticOperationsInObjectInvariants;
	}
	
	public List<AlloyFormula> getPredsEncodingValueOfArithmeticOperationsInObjectInvariants() {
		return this.predsEncodingValueOfArithmeticOperationsInObjectInvariants;
	}


	public void setVarsEncodingValueOfArithmeticOperationsInObjectInvariants(AlloyTyping at) {
		this.varsEncodingValueOfArithmeticOperationsInObjectInvariants = at;
	}
	
	public void setPredsEncodingValueOfArithmeticOperationsInObjectInvariants(List<AlloyFormula> predsEncodingValueOfArithmeticOperationsInObjectInvariants2) {
		this.predsEncodingValueOfArithmeticOperationsInObjectInvariants = predsEncodingValueOfArithmeticOperationsInObjectInvariants2;
	}

	public void prefixArgs() {
		Set<JProgramDeclaration> modPrograms = this.programs;
		for (JProgramDeclaration jpd : modPrograms){
			JStatement[] newSentences = null;
			List<JVariableDeclaration> params = jpd.getParameters();
			List<JVariableDeclaration> newParams = new ArrayList<JVariableDeclaration>();
			int offset;
			newParams.add(params.get(0));
			if (jpd.isStatic()){
				newSentences = new JStatement[2*(params.size() - 1)+1];
				offset = 1;
			} else {
				newSentences = new JStatement[2*(params.size() - 2)+1];
				offset = 2;
				newParams.add(params.get(1));
			}
			JStatement oldBody = jpd.getBody();
			HashSet<AlloyVariable> varsToPrefix = new HashSet<AlloyVariable>();
			int index = 0;
			while (index+offset < params.size()){
				JVariableDeclaration jvd = params.get(index+offset);
				varsToPrefix.add(jvd.getVariable());
				JType argType = jvd.getType();
				AlloyVariable av = jvd.getVariable();
				String varName = av.getVariableId().getString();
				
				JVariableDeclaration newJVD = new JVariableDeclaration(new AlloyVariable("arg_"+varName), argType);
				newParams.add(newJVD);
				
				JAssignment newJAss = new JAssignment(new ExprVariable(jvd.getVariable()), new ExprVariable(newJVD.getVariable()));
				newSentences[2*index] = jvd;
				newSentences[2*index+1] = newJAss;
				index = index + 1;
			}
			newSentences[2*(params.size()-offset)] = oldBody;
			JStatement newBody = new JBlock(newSentences);
			jpd.setParameters(newParams);
			jpd.setBody(newBody);
			
			// Fix specifications so that they refer to the new parameters.
			List<JSpecCase> specs = jpd.getSpecCases();

			ArgAppenderExpressionMutator argAppenderEM = new ArgAppenderExpressionMutator(varsToPrefix);
			FormulaMutator argAppenderFM = new FormulaMutator(argAppenderEM);

			for (JSpecCase jsc : specs){

				List<JPrecondition> precs = jsc.getRequires();
				for (JPrecondition jp : precs){
//					HashSet<JVariableDeclaration> varsToPrefix  = new HashSet<JVariableDeclaration>();
//					for (int ind = offset; ind < params.size(); ind++){
//						varsToPrefix.add(params.get(ind));
//					}
					
					jp.setFormula((AlloyFormula)jp.getFormula().accept(argAppenderFM));
				}
				
				List<JPostcondition> posts = jsc.getEnsures();
				for (JPostcondition jp : posts){
//					HashSet<JVariableDeclaration> varsToPrefix  = new HashSet<JVariableDeclaration>();
//					for (int ind = offset; ind < params.size(); ind++){
//						varsToPrefix.add(params.get(ind));
//					}
					
					jp.setFormula((AlloyFormula)jp.getFormula().accept(argAppenderFM));
				}

			}
			
			List<AlloyFormula> newPreds = new ArrayList<AlloyFormula>();
			for (AlloyFormula af : jpd.getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures()){
				newPreds.add((AlloyFormula)af.accept(argAppenderFM));
			}
			jpd.setPredsEncodingValueOfArithmeticOperationsInContracts(newPreds);
		}
	}
}

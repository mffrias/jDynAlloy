package ar.edu.jdynalloy.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public final class JProgramDeclaration implements JDynAlloyASTNode {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isAbstract ? 1231 : 1237);
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((programId == null) ? 0 : programId.hashCode());
		result = prime * result + ((signatureId == null) ? 0 : signatureId.hashCode());
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
		JProgramDeclaration other = (JProgramDeclaration) obj;
		if (isAbstract != other.isAbstract)
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (programId == null) {
			if (other.programId != null)
				return false;
		} else if (!programId.equals(other.programId))
			return false;
		if (signatureId == null) {
			if (other.signatureId != null)
				return false;
		} else if (!signatureId.equals(other.signatureId))
			return false;
		return true;
	}

	public boolean isAbstract() {
		return isAbstract;
	}
	

	public void setSpecCases(List<JSpecCase> specCases) {
		this.specCases = specCases;
	}

	public List<JSpecCase> getSpecCases() {
		return specCases;
	}

	private final String programId;
	
	private List<JVariableDeclaration> parameters;

	private JStatement body;

	private final String signatureId;

	private final boolean isAbstract;
	
	private final boolean isConstructor;

	private List<JSpecCase> specCases;
	
	public AlloyTyping varsResultOfArithmeticOperationsInRequiresAndEnsures = new AlloyTyping();
	public List<AlloyFormula> predsEncodingValueOfArithmeticOperationsInRequiresAndEnsures = new ArrayList<AlloyFormula>();

	private List<AlloyFormula> predsEncodingValueOfArithmeticOperationsInObjectInvariants = new ArrayList<AlloyFormula>();
	private AlloyTyping varsResultOfArithmeticOperationsInObjectInvariants = new AlloyTyping();

	private boolean isPure;	
			
	public boolean isPure(){
		return isPure;
	}
	
	public void setPure(boolean b){
		isPure = b;
	}
	
	
	public static JProgramDeclaration buildJProgramDeclaration(
			boolean isAbstract, boolean isConstructor, boolean isPure, String signatureId, String programId,
			List<JVariableDeclaration> parameters, Set<JPrecondition> requires,
			Set<JModifies> modifies, Set<JPostcondition> ensures, JStatement body, 
			AlloyTyping varsResultOfArithmeticOperationsInRequiresAndEnsures,
			List<AlloyFormula> predsEncodingValueOfArithmeticOperationsInRequiresAndEnsures) {

		List<JSpecCase> specCasesList;
		if (requires.isEmpty() && ensures.isEmpty()) {
			specCasesList = new LinkedList<JSpecCase>();
		} else {

			JSpecCase specCase = new JSpecCase(new LinkedList<JPrecondition>(
					requires), new LinkedList<JPostcondition>(ensures),
					new LinkedList<JModifies>(modifies));
			specCasesList = Collections.singletonList(specCase);
		}
		
		return new JProgramDeclaration(isAbstract, isConstructor, isPure, signatureId, programId,
				parameters, specCasesList, body, 
				varsResultOfArithmeticOperationsInRequiresAndEnsures, 
				predsEncodingValueOfArithmeticOperationsInRequiresAndEnsures);

	}

	public JProgramDeclaration(
			boolean isAbstract, 
			boolean isConstructor,
			boolean isPure,
			String signatureId,
			String programId, 
			List<JVariableDeclaration> parameters,
			List<JSpecCase> specCases, 
			JStatement body,
			AlloyTyping varsResultOfArithmeticOperationsInRequiresAndEnsures,
			List<AlloyFormula> predsEncodingValueOfArithmeticOperationsInRequiresAndEnsures
		) {
		super();
		this.isAbstract = isAbstract;
		this.isConstructor = isConstructor;
		this.isPure = isPure;
		this.signatureId = signatureId;
		this.programId = programId;
		this.parameters = parameters;
		this.specCases = specCases;
		this.body = body;
		this.varsResultOfArithmeticOperationsInRequiresAndEnsures = varsResultOfArithmeticOperationsInRequiresAndEnsures;
		this.predsEncodingValueOfArithmeticOperationsInRequiresAndEnsures = predsEncodingValueOfArithmeticOperationsInRequiresAndEnsures;
	}

	public AlloyTyping getVarsResultOfArithmeticOperationsInRequiresAndEnsures(){
		return varsResultOfArithmeticOperationsInRequiresAndEnsures;
	}
	
	public List<AlloyFormula> getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures(){
		return predsEncodingValueOfArithmeticOperationsInRequiresAndEnsures;
	}
	
	public void setPredsEncodingValueOfArithmeticOperationsInContracts(List<AlloyFormula> newPreds){
		this.predsEncodingValueOfArithmeticOperationsInRequiresAndEnsures = newPreds;
	}


	public List<AlloyFormula> getPredsEncodingValueOfArithmeticOperationsInObjectInvariants(){
		return predsEncodingValueOfArithmeticOperationsInObjectInvariants;
	}
	
	public AlloyTyping getVarsResultOfArithmeticOperationsInObjectInvariants(){
		return varsResultOfArithmeticOperationsInObjectInvariants;
	}
	

	
	public String getProgramId() {
		return programId;
	}

	public List<JVariableDeclaration> getParameters() {
		return parameters;
	}
	
	public void setParameters(List<JVariableDeclaration> newParameters){
		this.parameters = newParameters;
	}

	public JStatement getBody() {
		return body;
	}

	public void setBody(JStatement newBody){
		this.body = newBody;
	}
	
	public Object accept(IJDynAlloyVisitor visitor) {
		return visitor.visit(this);
	}

	public String getSignatureId() {
		return signatureId;
	}

	public boolean isVirtual() {
		return isAbstract;
	}

	@Override
	public String toString() {
		return this.signatureId + "::" + this.programId + "["
				+ this.parameters.toString() + "]" + "{...}";
	}

	public boolean isConstructor(){
		return isConstructor;
	}
	
	public boolean isStatic() {
	    if (this.parameters.get(0).getVariable().equals(JExpressionFactory.THROW_VARIABLE) ||
	    		this.parameters.get(0).getVariable().equals(JExpressionFactory.ARG_THROW_VARIABLE)) {
		return true;
	    }
	    return false;
	}

}

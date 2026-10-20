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

/**
 * A program: one method or constructor of a class (docs/LANGUAGE.md, "Programs"). It has the id
 * of its signature, its own id, its parameters ({@link JVariableDeclaration}s: {@code thiz},
 * {@code throw}, {@code return} if the method returns something, then the method's; a static
 * program starts with {@code throw}), its specification cases ({@link JSpecCase}), its body,
 * three flags (virtual, constructor, pure), and the variables and predicates that encode the
 * arithmetic in its {@code requires} and {@code ensures}. TACO builds one for each method of the
 * classes under analysis, and the parser one for each {@code program} of a {@code .djals} file.
 * {@code JDynAlloyXlatorVisitor} translates it into a DynAlloy program whose parameters are the
 * program's, then every field, then {@code usedObjects}; for the method under analysis, if it has
 * specification cases, it also builds the assertion {@code check_<program>}
 * (docs/ARCHITECTURE.md).
 *
 * <p>Names (docs/NAMING.md). The program id is {@code <declaring class>_<method>} for TACO's
 * classes ({@code ximena_MutRecursive_isEven}), {@code <class>_<class simple name>} for their
 * constructors, and the bare name in {@code .djals} files ({@code toString},
 * {@code Constructor}); overloads share their id. {@code VAlloyVisitor} rebuilds every program
 * with the same id (or {@code <id>_0}, for a second pure overload without a {@code Throwable}
 * parameter), with no signature id (null) and not virtual; {@code RecursionUnfolderVisitor} adds
 * copies named {@code <id>_unfold_<i>}. The method under analysis is found by comparing the id
 * with {@code methodToCheck} (docs/NAMING.md, "Where the method under analysis is looked up").
 *
 * <p>Flags. {@link #isAbstract()} and {@link #isVirtual()} return the same flag; a virtual
 * program would be dispatched on the class of the receiver by {@code VAlloyVisitor}
 * (docs/KNOWN-LIMITATIONS.md, "Dynamic dispatch does not work"). The parser marks as a
 * constructor a program named {@code Constructor}; the bindings key every constructor by the id
 * {@code Constructor}, whatever its name ({@code JBindingKey}). Only {@code VAlloyVisitor}'s
 * {@code _0} depends on {@link #isPure()}; TACO passes JML's {@code pure} to the constructor, its
 * JFSL front end sets it afterwards ({@link #setPure}), and the parser cannot set it
 * (docs/LANGUAGE.md, "The parser").
 *
 * <p>Mutable state. {@link #setParameters} and {@link #setBody} have no caller but
 * {@link JDynAlloyModule#prefixArgs()}, which has none, and {@link #setSpecCases} has none. The
 * lists are the caller's, not copies; TACO's JFSL front end adds specification cases to
 * {@link #getSpecCases()}. The arithmetic encodings of the contracts are public fields, which
 * TACO fills (the parser and the other producers pass empty ones);
 * {@code JDynAlloyXlatorVisitor.visit(JProgramDeclaration)} replaces the predicates, during the
 * translation, with a list in which the quantifiers are removed when {@code removeQuantifiers} is
 * set, through {@link #setPredsEncodingValueOfArithmeticOperationsInContracts}, the setter of
 * {@link #getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures()} despite its name.
 * The encodings for object invariants
 * ({@link #getPredsEncodingValueOfArithmeticOperationsInObjectInvariants()},
 * {@link #getVarsResultOfArithmeticOperationsInObjectInvariants()}) are always empty: nothing
 * sets or reads them; those of a module are in {@link JDynAlloyModule}. Unclear: whether they are
 * still needed.
 *
 * <p>Equality. {@code equals} and {@code hashCode} use the signature id, the id, the parameters
 * and the virtual flag; not the body, the specification cases or the other flags, so a rebuilt
 * program with the same header equals the original. Modules keep their programs in
 * {@code HashSet}s, {@code SceneSlicerManager} keeps the programs that the relevancy analysis's
 * {@code Scene} contains (by {@code equals}), and {@code JDynAlloyBinding} keys the implementors
 * of virtual programs by them. The hash code changes with {@link #setParameters} and with the
 * type of a parameter ({@code JType.setAsNonNull()}, which {@code ReplaceModifiesModuleVisitor}
 * calls), so a program changed so while in a set is no longer found there. {@code VAlloyVisitor}
 * keys programs by identity. No static state.
 */
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
	
	
	/**
	 * Builds a program with at most one specification case, as some of TACO's built-in classes
	 * do. If {@code requires} and {@code ensures} are both empty, the program has no specification
	 * case and {@code modifies} is dropped; otherwise its one case has the three sets as lists, in
	 * their iteration order, and the list of cases cannot be changed
	 * ({@code Collections.singletonList}).
	 *
	 * @param isAbstract true for a virtual program
	 * @param isConstructor true for a constructor
	 * @param isPure true for a pure method
	 * @param signatureId the id of the signature the program belongs to
	 * @param programId the program's id (docs/NAMING.md)
	 * @param parameters the parameters, {@code thiz} or {@code throw} first
	 * @param requires the {@code requires} of the case
	 * @param modifies the {@code modifies} of the case
	 * @param ensures the {@code ensures} of the case
	 * @param body the body
	 * @param varsResultOfArithmeticOperationsInRequiresAndEnsures the variables that encode the
	 *        arithmetic in the contracts
	 * @param predsEncodingValueOfArithmeticOperationsInRequiresAndEnsures the predicates over
	 *        those variables
	 * @return the program
	 */
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

	/**
	 * Creates a program; the lists and the typing are kept, not copied.
	 *
	 * @param isAbstract true for a virtual program (see the class comment)
	 * @param isConstructor true for a constructor
	 * @param isPure true for a pure method (JML's {@code pure})
	 * @param signatureId the id of the signature the program belongs to
	 * @param programId the program's id (docs/NAMING.md)
	 * @param parameters the parameters, {@code thiz} or {@code throw} first; {@link #isStatic()}
	 *        reads the first, so the list must not be empty
	 * @param specCases the specification cases; empty for none
	 * @param body the body
	 * @param varsResultOfArithmeticOperationsInRequiresAndEnsures the variables that encode the
	 *        arithmetic in the contracts; empty but for TACO's classes
	 * @param predsEncodingValueOfArithmeticOperationsInRequiresAndEnsures the predicates over
	 *        those variables
	 */
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
	
	/**
	 * Returns whether the program is static: whether its first parameter is {@code throw} or
	 * {@code arg_throw} rather than {@code thiz} (nothing creates {@code arg_throw}). TACO gives
	 * every method it translates a {@code thiz} parameter, so only the static programs of the
	 * built-in modules are static (docs/NAMING.md). {@code JBindingKey} keys the programs by it.
	 * The program must have a parameter.
	 *
	 * @return true for a static program
	 */
	public boolean isStatic() {
	    if (this.parameters.get(0).getVariable().equals(JExpressionFactory.THROW_VARIABLE) ||
	    		this.parameters.get(0).getVariable().equals(JExpressionFactory.ARG_THROW_VARIABLE)) {
		return true;
	    }
	    return false;
	}

}

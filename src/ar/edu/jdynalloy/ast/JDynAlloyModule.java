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

/**
 * A JDynAlloy module: one class or interface (or a module of TACO's own, such as
 * {@code ClassFields} or a literal), with its signature, its class singleton and literal
 * singleton signatures, its instance fields, its class and object invariants and constraints, its
 * {@code represents} clauses, its programs, and the variables and predicates that encode the
 * arithmetic in its object invariants (docs/LANGUAGE.md, "Modules"). TACO builds the modules of
 * the classes under analysis with {@code DynJMLAlloyModuleBuffer} and those of its built-in
 * classes directly, and parses its models of JDK classes; jDynAlloy's steps take the list of all
 * of them, and {@code JDynAlloyXlatorVisitor} translates each into a DynAlloy module of the same
 * name (docs/ARCHITECTURE.md). The module id is the signature's id, the class name with
 * underscores ({@code java_util_Date}, docs/NAMING.md); the parser rejects a module whose name
 * differs.
 *
 * <p>Mutable state. The getters return the collections given to the constructor, not copies, and
 * callers change them: {@code SceneSlicerManager} removes fields from {@link #getFields()} in
 * place, and TACO's JFSL front end adds fields, object invariants and {@code represents}. TACO
 * sets the literal singleton of its built-in exception modules after creating them, and
 * {@code ReplaceModifiesModuleVisitor} on its copies ({@link #setLiteralSingleton}); nothing
 * calls {@link #setClassSingleton}. Only TACO fills the arithmetic encodings: its
 * {@code JDynAlloyASTVisitor} sets them on every module of a compilation unit after translating
 * it, and its {@code JDynAlloyParsingStage} sets empty ones on the modules it parses from files;
 * the other producers pass empty ones. {@code JDynAlloyXlatorVisitor.visit(JDynAlloyModule)}
 * replaces the predicates, during the translation, with a list in which the quantifiers are
 * removed when {@code removeQuantifiers} is set. {@link #prefixArgs()} changes the programs in
 * place.
 *
 * <p>{@link #pinnedForNonRelevancyAnalysisForStryker}, a public field set by the constructor,
 * keeps a module through the relevancy analysis and the slicing whether or not the method under
 * analysis reaches it. It is true only for the literal modules of a fixed input: those that
 * {@code JDynAlloyTranslator} creates, and those that TACO's {@code PrecompiledModules} and
 * {@code JDynAlloyVisitor} ask the {@code JavaPrimitive*Value} singletons for. Those singletons
 * cache their literal modules for the life of the JVM, so a literal module keeps the flag it was
 * first created with. Every copy ({@code JDynAlloyMutator}, {@code PruneVisitor},
 * {@code SceneSlicerManager}, {@code ReplaceModifiesModuleVisitor}) passes the flag on.
 *
 * <p>Equality. {@code equals} and {@code hashCode} use the id, the signature, the two singletons,
 * the fields, the object invariants, the {@code represents} and the programs; not the class
 * invariants, the constraints, the arithmetic encodings or the pinned flag. As
 * {@link JSignature#equals} is identity in effect, two modules are equal only if they share their
 * signature object, as the copies of {@code PruneVisitor}, {@code SceneSlicerManager} and
 * {@code ReplaceModifiesModuleVisitor} do and those of {@code JDynAlloyMutator}, which builds a
 * new signature, do not. The relevancy analysis's {@code Scene} compares modules with
 * {@code equals}, and TACO's {@code PrecompiledModules} collects literal modules in a
 * {@code HashSet}. {@code toString()} is the module id. No static state.
 */
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

	/**
	 * Creates a module from its parts; the collections are kept, not copied.
	 *
	 * @param moduleId the module's name, equal to the signature's id
	 * @param signature the signature of the class
	 * @param class_singleton the signature {@code <Sig>Class}, or null (TACO's classes have none)
	 * @param literal_signature the signature {@code <Sig>Lit}, or null
	 * @param fields the instance fields ({@link JField}); static fields belong to
	 *        {@code ClassFields}
	 * @param class_invariants the class invariants
	 * @param class_constraints the class constraints
	 * @param object_invariants the object invariants
	 * @param object_constraints the object constraints
	 * @param represents the abstraction functions
	 * @param programs the programs; equal programs ({@link JProgramDeclaration}) are kept once
	 * @param varsEncodingValueOfArithmeticOperationsInObjectInvariants the variables that encode
	 *        the arithmetic in the object invariants; empty but for TACO's classes
	 * @param predsEncodingValueOfArithmeticOperationsInObjectInvariants the predicates over those
	 *        variables
	 * @param pinnedForStryker true to keep the module through the relevancy analysis and the
	 *        slicing (see the class comment)
	 */
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

	/**
	 * Makes every program of the module take its parameters by value. Each parameter {@code x}
	 * after {@code thiz} and {@code throw} (after {@code throw}, for a static program) becomes
	 * {@code arg_x}; the body starts with {@code var x: T; x := arg_x;} for each, and the
	 * {@code requires}, the {@code ensures} and the predicates for the arithmetic of the
	 * contracts refer to {@code arg_x} instead of {@code x} (dynalloy4's
	 * {@code ArgAppenderExpressionMutator}). Changes the programs in place
	 * ({@code setParameters}, {@code setBody}, {@code JPrecondition.setFormula},
	 * {@code JPostcondition.setFormula}) while they are in the module's set, whose hash codes
	 * depend on the parameters.
	 *
	 * <p>Nothing in jDynAlloy or TACO calls it. It renames {@code return} too, which then becomes
	 * a local variable of the body. Unclear: what it was meant for, and whether it is still
	 * needed.
	 */
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

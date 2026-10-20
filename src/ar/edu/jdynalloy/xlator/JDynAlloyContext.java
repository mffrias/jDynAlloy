package ar.edu.jdynalloy.xlator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JField;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;

/**
 * What the translation knows about all the modules of a run: the class hierarchy, every field with
 * its type, the relevant modules and, once {@link JDynAlloyTranslator} has built it, the modifies
 * table (program id to the fields it may assign, itself or through its callees). Two classes build
 * one: {@code BindingManager.execute()}, which uses only the hierarchy ({@link #descendantsOf}, for
 * {@code CallBindingResolver} and {@code SearchImplementors}), and
 * {@code JDynAlloyTranslator.translateAll}, which loads every module before preparing them, sets the
 * modifies table at the end of the preparation, and passes the context to every
 * {@code JDynAlloyXlatorVisitor} (docs/ARCHITECTURE.md, "The translation").
 *
 * <p>The modules it holds are the ones given to {@link #load}: in the translator, the modules as
 * TACO passed them, before {@code VAlloyVisitor} and the other preparation steps, to which the
 * translator adds the literal modules it creates ({@link #getRelevantModules()}). The visitors read
 * the invariants, constraints and {@code represents} of the relevant modules from there; the
 * modifies table, in contrast, is keyed by the ids of the prepared programs.
 *
 * <p>No static state; one instance per {@code BindingManager} or translator run. But {@link #load}
 * reads {@code getRelevantClasses()} from {@code JDynAlloyConfig.getInstance()}, so the relevant
 * modules are those of TACO's {@code relevantClasses} at that moment, which the relevancy analysis
 * sets when it runs (docs/ARCHITECTURE.md, "How TACO calls it", step 2).
 */
public final class JDynAlloyContext {

	public Map<String, Set<AlloyVariable>> getModifiesTable() {
		return modifiesTable;
	}

	public void setModifiesTable(Map<String, Set<AlloyVariable>> modifiesTable) {
		this.modifiesTable = modifiesTable;
	}

	/**
	 * Returns the relevant modules: the list itself, not a copy. {@code JDynAlloyTranslator} adds
	 * to it the literal modules it creates for the translation.
	 *
	 * @return the modules loaded whose id is in {@code relevantClasses}, in the order loaded
	 */
	public List<JDynAlloyModule> getRelevantModules() {
		return relevantModules;
	}

	public static JDynAlloyContext buildNewContext() {
		JDynAlloyContext result = new JDynAlloyContext();
		// result.loadJavaLang();
		return result;
	}

	private final JDynAlloyTyping allFields;

	private final Graph<String> extendsGraph;

	private final List<JDynAlloyModule> relevantModules;
	
	private Map<String,Set<AlloyVariable>> modifiesTable;

	public JDynAlloyContext() {
		super();
		this.extendsGraph = new Graph<String>(false);
		this.allFields = new JDynAlloyTyping();
		this.relevantModules = new LinkedList<JDynAlloyModule>();
	}

	/**
	 * Adds a module: its signature to the class hierarchy, with an edge to the signature it extends;
	 * each of its fields, with its type, to {@link #allFields()} (a field already there takes the new
	 * type); and the module itself to the relevant modules if its id is in
	 * {@code JDynAlloyConfig.getInstance().getRelevantClasses()}.
	 *
	 * @param module the module
	 * @throws IllegalArgumentException if the edge to the signature it extends closes a cycle in the
	 *         hierarchy
	 */
	public void load(JDynAlloyModule module) {
		String moduleId = module.getSignature().getSignatureId();
		final String signatureId = moduleId;
		final String superSignatureId = module.getSignature().getExtSigId();

		extendsGraph.addNode(signatureId);
		if (superSignatureId != null)
			extendsGraph.addEdge(signatureId, superSignatureId);

		for (JField field : module.getFields())
			allFields.put(field.getFieldVariable(), field.getFieldType());

		List<String> relevantClasses = JDynAlloyConfig.getInstance()
				.getRelevantClasses();
		if (relevantClasses.contains(moduleId)) {
			relevantModules.add(module);
		}
	}

	/**
	 * Returns the signatures that extend a signature, directly or not, among the modules loaded so
	 * far. It calls {@code Graph.ascendentsOf}, since the edges of the hierarchy go from a signature
	 * to the one it extends.
	 *
	 * @param signatureId a signature id
	 * @return its subclasses, without itself; empty if it has none or was not loaded
	 */
	public Set<String> descendantsOf(String signatureId) {
		return extendsGraph.ascendentsOf(signatureId);
	}

	public JDynAlloyTyping allFields() {
		return this.allFields;
	}

	/**
	 * Returns every field, in a new list, in the order of {@link #allFields()}'s key set (a hash
	 * map's: no particular order). The translation uses this order for the field parameters of every
	 * program and the field arguments of every call, so they agree as long as all the modules of a
	 * run are translated with the same context.
	 *
	 * @return the fields
	 */
	public List<AlloyVariable> fieldList() {
		return new LinkedList<AlloyVariable>(this.allFields.varSet());
	}
}

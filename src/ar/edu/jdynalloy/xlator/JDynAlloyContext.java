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

public final class JDynAlloyContext {

	public Map<String, Set<AlloyVariable>> getModifiesTable() {
		return modifiesTable;
	}

	public void setModifiesTable(Map<String, Set<AlloyVariable>> modifiesTable) {
		this.modifiesTable = modifiesTable;
	}

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

	public Set<String> descendantsOf(String signatureId) {
		return extendsGraph.ascendentsOf(signatureId);
	}

	public JDynAlloyTyping allFields() {
		return this.allFields;
	}

	public List<AlloyVariable> fieldList() {
		return new LinkedList<AlloyVariable>(this.allFields.varSet());
	}
}

package ar.edu.jdynalloy.slicer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JField;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.factory.JPreludeFactory;
import ar.edu.jdynalloy.factory.JSignatureFactory;
import ar.edu.jdynalloy.relevancy.Scene;
import ar.edu.jdynalloy.xlator.JType;

public class SceneSlicerManager {

	public List<JDynAlloyModule> process(List<JDynAlloyModule> modules,
			List<String> relevantClassesList, Scene scene) {

		List<JDynAlloyModule> result = new ArrayList<JDynAlloyModule>();
		
		boolean useJavaPrimitiveIntegerValue = relevantClassesList.contains("JavaPrimitiveIntegerValue");

		for (JDynAlloyModule module : modules) {
			if (relevantClassesList.contains(module.getModuleId()) || module.pinnedForNonRelevancyAnalysisForStryker) {

				String classFieldsSignatureId = JSignatureFactory
						.buildClassFieldsSignature().getSignatureId();

				if (module.getModuleId().equals(classFieldsSignatureId)) {
					if (!module.getFields().isEmpty())
						result.add(module);
				} else {
					result.add(module);
				}
			}
		}

		if (!relevantClassesList.contains("java_lang_SystemArray")) {
			JPreludeFactory.unregister_int_java_lang_SystemArray();
		} else if (useJavaPrimitiveIntegerValue == true){
			JPreludeFactory.register_JavaPrimitiveIntegerValue_java_lang_SystemArray();
		} else {
			JPreludeFactory.register_int_java_lang_SystemArray();
		}

		if (!result.contains("java_util_Map")) {
			JPreludeFactory.unregister_java_util_Map();
		} else
			JPreludeFactory.register_java_util_Map();

		if (!result.contains("java_util_List")) {
			JPreludeFactory.unregister_java_util_List();
		} else
			JPreludeFactory.register_java_util_List();

		if (!result.contains("java_lang_String")) {
			JPreludeFactory.unregister_java_lang_String();
		} else
			JPreludeFactory.register_java_lang_String();

		List<String> relevant_and_primitives = new LinkedList<String>();
		relevant_and_primitives.addAll(relevantClassesList);
		relevant_and_primitives.add("null");
		relevant_and_primitives.add("Int");
		relevant_and_primitives.add("univ");
		relevant_and_primitives.add("boolean");
		relevant_and_primitives.add("char");

		for (JDynAlloyModule relevantModule : result) {
			List<JField> to_remove = new LinkedList<JField>();
			for (JField field : relevantModule.getFields()) {
				JType field_type = field.getFieldType();
				if (!relevant_and_primitives.containsAll(field_type.used_types())) {
					to_remove.add(field);
				}

			}
			relevantModule.getFields().removeAll(to_remove);
		}

		// prune unused programs
		List<JDynAlloyModule> pruned_program_modules = new LinkedList<JDynAlloyModule>();
		for (JDynAlloyModule module : result) {
			Set<JProgramDeclaration> to_remove = new HashSet<JProgramDeclaration>();
			Set<JProgramDeclaration> to_add = new HashSet<JProgramDeclaration>();
			for (JProgramDeclaration program : module.getPrograms()) {
				if (!scene.getPrograms().contains(program)) {
					to_remove.add(program);
				} else {
					to_add.add(program);
				}
			}
			if (!to_remove.isEmpty()) {
				JDynAlloyModule pruned_module = new JDynAlloyModule(module
						.getModuleId(), module.getSignature(), module
						.getClassSingleton(), module.getLiteralSingleton(),
						module.getFields(), module.getClassInvariants(), module
								.getClassConstraints(), module
								.getObjectInvariants(), module
								.getObjectConstraints(),
								module.getRepresents(), to_add, module.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants(), 
								module.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants(), module.pinnedForNonRelevancyAnalysisForStryker);				
				pruned_program_modules.add(pruned_module);

			} else
				pruned_program_modules.add(module);
		}

		return pruned_program_modules;
	}

}

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

/**
 * Drops from the scene what the relevancy analysis did not reach: modules, fields and programs.
 * Step 3 of TACO's {@code JDynAlloyStage}, which calls it only with {@code relevancyAnalisys},
 * after {@code RelevancyAnalysisManager.process}, and gives the result to
 * {@code ModifiesSolverManager} (docs/ARCHITECTURE.md, "How TACO calls it").
 *
 * <p>The class has no state of its own, but {@link #process} sets {@link JPreludeFactory}'s
 * static registrations, which survive the run (docs/ARCHITECTURE.md, "State that outlives a
 * run"). TACO creates one instance per run.
 */
public class SceneSlicerManager {

	/**
	 * Slices the modules to the relevant ones, and decides which parts of the prelude are
	 * registered:
	 * <ol>
	 * <li>keeps, in their order, the modules whose id is in {@code relevantClassesList} and those
	 *     pinned for Stryker ({@code pinnedForNonRelevancyAnalysisForStryker}); the module
	 *     {@code ClassFields} only if it has fields (checked before step 3, so it can end up
	 *     with none);</li>
	 * <li>sets {@link JPreludeFactory}'s registrations. For {@code java_lang_SystemArray}: if it
	 *     is not relevant, unregisters the {@code int} variant; if it is, registers the
	 *     {@code JavaPrimitiveIntegerValue} variant when that module is relevant and the
	 *     {@code int} one otherwise, and never unregisters the other. The
	 *     {@code java_util_Map}, {@code java_util_List} and {@code java_lang_String} parts are
	 *     always unregistered: the test asks the list of kept modules, not the list of names,
	 *     whether it contains those strings (docs/KNOWN-LIMITATIONS.md, "Parts of the prelude
	 *     are always left out"; docs/OPEN-QUESTIONS.md, item 5). Of these flags,
	 *     {@code JPreludeFactory} reads only the {@code java_util_Map} one, which guards the
	 *     prelude's {@code updateMap} action; its read of the {@code java_util_List} flag is
	 *     commented out, the getter of the {@code java_lang_String} flag has no caller, and the
	 *     {@code SystemArray} flags are never read;</li>
	 * <li>removes, in place, from each kept module's field list the fields whose type
	 *     ({@code JType.used_types()}) mentions a type that is not relevant and not
	 *     {@code null}, {@code Int}, {@code univ}, {@code boolean} or {@code char}; the
	 *     caller's module objects change too;</li>
	 * <li>replaces each kept module that has a program not in {@code scene.getPrograms()}
	 *     (compared by signature, name, parameters and abstractness) by a new module with only
	 *     the programs in the scene, in a {@code HashSet}, and the other parts of the original
	 *     (the same lists and sets, not copies).</li>
	 * </ol>
	 * The prelude flags stay set for later runs in the same JVM, including runs without
	 * relevancy analysis, which do not call this method.
	 *
	 * @param modules every module of the run
	 * @param relevantClassesList the ids of the relevant modules (TACO passes the configuration's
	 *        {@code getRelevantClasses()}, which it has just set from the relevancy analysis)
	 * @param scene the relevancy analysis's scene ({@code RelevancyAnalysisManager.getScene()}),
	 *        whose programs are kept
	 * @return a new list with the kept modules, some of them new objects
	 */
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

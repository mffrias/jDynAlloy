package ar.edu.jdynalloy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ar.edu.jdynalloy.JDynAlloyConfig.LoopResolutionEnum;

public abstract class JDynAlloyConfigImpl implements IJDynAlloyConfig {
	public List<String> getBuiltInModules() {
		return builtInModules;
	}

	public String getMethodToCheck() {
		return methodToCheck;
	}

	public String getClassToCheck() {
		return classToCheck;
	}

	private static final String OUTPUT_FIELD = "output";

	private static final String CLASSES_FIELD = "classes";

	private static final String PROJECT_FIELD = "project";

	private static final String CLASS_EXTENDS_OBJECT_FIELD = "classExtendsObject";

	private static final boolean DEFAULT_CLASS_EXTENDS_OBJECT = true;

	private static final String TYPE_SAFETY_FIELD = "typeSafety";

	private static final boolean DEFAULT_TYPE_SAFETY = true;

	private static final String OUTPUT_DYNJALLOY_FIELD = "outputDynJAlloy";

	private static final String DEFAULT_OUTPUT_DYNJALLOY = null;

	private static final String DYNAMIC_JAVA_LANG_FIELDS = "dynamicJavaLangFields";

	private static final boolean DEFAULT_DYNAMIC_JAVA_LANG_FIELDS = false;

	private static final String DYNJALLOY_ANNOTATIONS_FIELD = "dynJAlloyAnnotations";

	private static final boolean DEFAULT_DYNJALLOY_ANNOTATIONS = false;

	private static final String QUANTIFIER_INCLUDES_NULL_FIELD = "quantifierIncludesNull";

	private static final boolean DEFAULT_QUANTIFIER_INCLUDES_NULL = true;

	private static final String CHECK_NULL_DE_REFERENCE_FIELD = "checkNullDereference";

	private static final boolean DEFAULT_CHECK_NULL_DEREFERENCE = true;

	private static final String ABSTRACT_SIGNATURE_OBJECT_FIELD = "abstractSignatureObject";

	private static final boolean DEFAULT_ABSTRACT_SIGNATURE_OBJECT = true;

	private static final String CLASS_TO_CHECK_FIELD = "classToCheck";

	private static final String DEFAULT_CLASS_TO_CHECK = null;

	private static final String METHOD_TO_CHECK_FIELD = "methodToCheck";

	private static final String DEFAULT_METHOD_TO_CHECK = null;

	private static final String USE_CLASS_SINGLETONS_FIELD = "useClassSingletons";

	private static final boolean DEFAULT_USE_CLASS_SINGLETONS = true;

	private static final String IGNORE_JML_ANNOTATIONS = "ignoreJmlAnnotations";

	private static final boolean DEFAULT_IGNORE_JML_ANNOTATIONS = false;

	private static final String IGNORE_JSFL_ANNOTATIONS = "ignoreJsflAnnotations";

	private static final boolean DEFAULT_IGNORE_JSFL_ANNOTATIONS = true;

	private static final String ASSERT_IS_ASSUME = "assertIsAssume";

	private static final boolean DEFAULT_ASSERT_IS_ASSUME = true;

	private static final String LOOP_RESOLUTION = "loopResolution";

	private static final LoopResolutionEnum DEFAULT_LOOP_RESOLUTION = LoopResolutionEnum.AnnotatedLoop;

	private static final String SIM_PREDICATES = "simPredicates";

	private static final boolean DEFAULT_SIM_PREDICATES = true;

	private static final String RUN_PROGRAMS = "runPrograms";

	private static final boolean DEFAULT_RUN_PROGRAMS = true;

	private static final String CHECK_COMMAND = "checkCommand";

	private static final boolean DEFAULT_CHECK_COMMAND = true;

	private static final String USE_CUSTOM_RELATIONAL_OVERRIDE = "useCustomRelationalOverride";

	private static final boolean DEFAULT_USE_CUSTOM_RELATIONAL_OVERRIDE = false;

	private static final String RELEVANT_CLASSES = "relevantClasses";

	private static final List<String> DEFAULT_RELEVANT_CLASSES = new LinkedList<String>();

	private static final String BUILT_IN_MODULES = "builtInModules";

	private static final List<String> DEFAULT_BUILT_IN_MODULES = new LinkedList<String>();

	private static final String SKOLEMIZE_INSTANCE_INVARIANT = "skolemizeInstanceInvariant";

	private static final boolean DEFAULT_SKOLEMIZE_INSTANCE_INVARIANT = false;

	private static final String SKOLEMIZE_INSTANCE_ABSTRACTION = "skolemizeInstanceAbstraction";

	private static final boolean DEFAULT_SKOLEMIZE_INSTANCE_ABSTRACTION = false;

	private static final String JML_OBJECT_SEQUENCE_TO_ALLOY_SEQUENCE = "JMLObjectSequenceToAlloySequence";

	private static final boolean DEFAULT_JML_OBJECT_SEQUENCE_TO_ALLOY_SEQUENCE = false;

	private static final String NEW_EXCEPTIONS_ARE_LITERALS = "newExceptionsAreLiterals";

	private static final boolean DEFAULT_NEW_EXCEPTIONS_ARE_LITERALS = false;

	private static final String ASSERTION_ARGUMENTS = "assertionArguments";

	private static final String DEFAULT_ASSERTION_ARGUMENTS = "";

	private static final String GENERATE_CHECK = "generateCheck";

	private static final boolean DEFAULT_GENERATE_CHECK = true;

	private static final String GENERATE_RUN = "generateRun";

	private static final boolean DEFAULT_GENERATE_RUN = false;

	private static final String ARRAY_IS_INT = "arrayIsInt";

	private static final boolean DEFAULT_ARRAY_IS_INT = false;

	private static final String MODULAR_REASONING = "modular_reasoning";

	private static final boolean DEFAULT_MODULAR_REASONING = false;

	private static final String INCLUDE_SIMULATION_PROGRAM_DECLARATION = "include_simulation_program_declaration";

	private static final boolean DEFAULT_INCLUDE_SIMULATION_PROGRAM_DECLARATION = false;

	private static final String OUTPUT_MODULAR_JDYNALLOY = "output_modular_JDynAlloy";

	private static final String DEFAULT_OUTPUT_MODULAR_JDYNALLOY = null;

	private static final String INCLUDE_BRANCH_LABELS = "include_branch_labels";

	private static final boolean DEFAULT_INCLUDE_BRANCH_LABELS = false;

	private static final String DISABLE_INTEGER_LITERAL_REDUCTION = "disableIntegerLiteralReduction";

	private static final boolean DEFAULT_DISABLE_INTEGER_LITERAL_REDUCTION = false;

	private final String project;

	private final String[] classes;

	private final String output;

	private final boolean classExtendsObject;

	private final boolean typeSafety;

	private final String outputDynJAlloy;

	private final boolean dynamicJavaLangFields;

	private final boolean dynjalloyAnnotations;

	private final boolean quantifierIncluesNull;

	private final boolean checkNullDereference;

	private final boolean abstractSignatureObject;

	private final String classToCheck;

	private final String methodToCheck;

	private final boolean useClassSingletons;

	private final boolean ignoreJmlAnnotations;

	private final boolean ignoreJsflAnnotations;

	private final boolean assertIsAssume;

	private final String loopResolution;

	private boolean simPredicates;

	private boolean runPrograms;

	private boolean checkCommand;

	private boolean useCustomRelationalOverride;

	private final List<String> relevantClasses;

	private final List<String> builtInModules;

	private final boolean skolemizeInstanceInvariant;

	private final boolean skolemizeInstanceAbstraction;

	private final boolean jmlObjectSequenceToAlloySequence;

	private boolean newExceptionsAreLiterals;

	private String assertionArguments;

	private boolean generateCheck;

	private boolean generateRun;

	private final boolean arrayIsInt;

	private final boolean modularReasoning;

	private final boolean includeSimulationProgramDeclaration;

	private final String output_modular_JDynAlloy;

	private final boolean include_branch_labels;

	private final boolean disable_integer_literal_reduction;

	public boolean getDynamicJavaLangFields() {
		return dynamicJavaLangFields;
	}

	// START - MAJOR DIFERENCES WITH PREVIOUS VERSION

	public JDynAlloyConfigImpl(String configFilename,
			Properties overridingProperties) {
		this(readConfigFile(configFilename));
	}

	private static Map<String, String[]> readConfigFile(String configFilename) {
		Map<String, String[]> result = new HashMap<String, String[]>();
		FileReader fr = null;
		BufferedReader br = null;

		try {
			fr = new FileReader(configFilename);
			br = new BufferedReader(fr);

			String line = null;
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
					String[] kv = line.split("=");
					String fieldName = kv[0];
					String fieldValueStr = kv[1];
					String[] fieldValueArray = fieldValueStr.split(",");
					result.put(fieldName, fieldValueArray);
				}
			}

			br.close();
			fr.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	// END - MAJOR DIFFERENCES WITH PREVIOUS VERSION

	private JDynAlloyConfigImpl(Map<String, String[]> entries) {
		project = entries.get(PROJECT_FIELD)[0];

		classes = entries.get(CLASSES_FIELD);

		output = entries.get(OUTPUT_FIELD)[0];

		classExtendsObject = optionalBoolean(entries
				.get(CLASS_EXTENDS_OBJECT_FIELD), DEFAULT_CLASS_EXTENDS_OBJECT);

		typeSafety = optionalBoolean(entries.get(TYPE_SAFETY_FIELD),
				DEFAULT_TYPE_SAFETY);

		outputDynJAlloy = optionalString(entries.get(OUTPUT_DYNJALLOY_FIELD),
				DEFAULT_OUTPUT_DYNJALLOY);

		dynamicJavaLangFields = optionalBoolean(entries
				.get(DYNAMIC_JAVA_LANG_FIELDS),
				DEFAULT_DYNAMIC_JAVA_LANG_FIELDS);

		dynjalloyAnnotations = optionalBoolean(entries
				.get(DYNJALLOY_ANNOTATIONS_FIELD),
				DEFAULT_DYNJALLOY_ANNOTATIONS);

		quantifierIncluesNull = optionalBoolean(entries
				.get(QUANTIFIER_INCLUDES_NULL_FIELD),
				DEFAULT_QUANTIFIER_INCLUDES_NULL);

		checkNullDereference = optionalBoolean(entries
				.get(CHECK_NULL_DE_REFERENCE_FIELD),
				DEFAULT_CHECK_NULL_DEREFERENCE);

		abstractSignatureObject = optionalBoolean(entries
				.get(ABSTRACT_SIGNATURE_OBJECT_FIELD),
				DEFAULT_ABSTRACT_SIGNATURE_OBJECT);

		classToCheck = optionalString(entries.get(CLASS_TO_CHECK_FIELD),
				DEFAULT_CLASS_TO_CHECK);

		methodToCheck = optionalString(entries.get(METHOD_TO_CHECK_FIELD),
				DEFAULT_METHOD_TO_CHECK);

		useClassSingletons = optionalBoolean(entries
				.get(USE_CLASS_SINGLETONS_FIELD), DEFAULT_USE_CLASS_SINGLETONS);

		ignoreJmlAnnotations = optionalBoolean(entries
				.get(IGNORE_JML_ANNOTATIONS), DEFAULT_IGNORE_JML_ANNOTATIONS);

		ignoreJsflAnnotations = optionalBoolean(entries
				.get(IGNORE_JSFL_ANNOTATIONS), DEFAULT_IGNORE_JSFL_ANNOTATIONS);

		assertIsAssume = optionalBoolean(entries.get(ASSERT_IS_ASSUME),
				DEFAULT_ASSERT_IS_ASSUME);

		loopResolution = optionalString(entries.get(LOOP_RESOLUTION),
				DEFAULT_LOOP_RESOLUTION.toString());

		simPredicates = optionalBoolean(entries.get(SIM_PREDICATES),
				DEFAULT_SIM_PREDICATES);

		runPrograms = optionalBoolean(entries.get(RUN_PROGRAMS),
				DEFAULT_RUN_PROGRAMS);

		checkCommand = optionalBoolean(entries.get(CHECK_COMMAND),
				DEFAULT_CHECK_COMMAND);

		useCustomRelationalOverride = optionalBoolean(entries
				.get(USE_CUSTOM_RELATIONAL_OVERRIDE),
				DEFAULT_USE_CUSTOM_RELATIONAL_OVERRIDE);

		relevantClasses = optionalStringList(entries.get(RELEVANT_CLASSES),
				DEFAULT_RELEVANT_CLASSES);

		builtInModules = optionalStringList(entries.get(BUILT_IN_MODULES),
				DEFAULT_BUILT_IN_MODULES);

		skolemizeInstanceInvariant = optionalBoolean(entries
				.get(SKOLEMIZE_INSTANCE_INVARIANT),
				DEFAULT_SKOLEMIZE_INSTANCE_INVARIANT);

		skolemizeInstanceAbstraction = optionalBoolean(entries
				.get(SKOLEMIZE_INSTANCE_ABSTRACTION),
				DEFAULT_SKOLEMIZE_INSTANCE_ABSTRACTION);

		jmlObjectSequenceToAlloySequence = optionalBoolean(entries
				.get(JML_OBJECT_SEQUENCE_TO_ALLOY_SEQUENCE),
				DEFAULT_JML_OBJECT_SEQUENCE_TO_ALLOY_SEQUENCE);

		newExceptionsAreLiterals = optionalBoolean(entries
				.get(NEW_EXCEPTIONS_ARE_LITERALS),
				DEFAULT_NEW_EXCEPTIONS_ARE_LITERALS);

		assertionArguments = optionalString(entries.get(ASSERTION_ARGUMENTS),
				DEFAULT_ASSERTION_ARGUMENTS);

		generateCheck = optionalBoolean(entries.get(GENERATE_CHECK),
				DEFAULT_GENERATE_CHECK);

		generateRun = optionalBoolean(entries.get(GENERATE_RUN),
				DEFAULT_GENERATE_RUN);

		arrayIsInt = optionalBoolean(entries.get(ARRAY_IS_INT),
				DEFAULT_ARRAY_IS_INT);

		modularReasoning = optionalBoolean(entries.get(MODULAR_REASONING),
				DEFAULT_MODULAR_REASONING);

		includeSimulationProgramDeclaration = optionalBoolean(entries
				.get(INCLUDE_SIMULATION_PROGRAM_DECLARATION),
				DEFAULT_INCLUDE_SIMULATION_PROGRAM_DECLARATION);

		output_modular_JDynAlloy = optionalString(entries
				.get(OUTPUT_MODULAR_JDYNALLOY),
				DEFAULT_OUTPUT_MODULAR_JDYNALLOY);

		include_branch_labels = optionalBoolean(entries
				.get(INCLUDE_BRANCH_LABELS), DEFAULT_INCLUDE_BRANCH_LABELS);

		disable_integer_literal_reduction = optionalBoolean(entries
				.get(DISABLE_INTEGER_LITERAL_REDUCTION),
				DEFAULT_DISABLE_INTEGER_LITERAL_REDUCTION);

		if ((getMethodToCheck() != null) && (getClassToCheck() == null))
			throw new IllegalConfigurationException("Cannot check method "
					+ methodToCheck + " with no class to check");

		if (getIgnoreJmlAnnotations() == false
				&& getIgnoreJsflAnnotations() == false) {
			throw new IllegalConfigurationException(
					"Cannot handle simultaneously JSFL and JML annotations");
		}
	}

	private List<String> optionalStringList(String[] actualValue,
			List<String> defaultValue) {
		if (actualValue == null)
			return defaultValue;
		return Arrays.asList(actualValue);
	}

	private boolean optionalBoolean(String[] optBooleanStr, boolean defaultValue) {
		if (optBooleanStr == null)
			return defaultValue;
		else {
			if (optBooleanStr.length != 1)
				throw new IllegalArgumentException("field "
						+ CLASS_EXTENDS_OBJECT_FIELD + " is a singleton field.");

			return new Boolean(optBooleanStr[0]);
		}
	}

	private String optionalString(String[] optStr, String defaultValue) {
		if (optStr == null)
			return defaultValue;
		else {
			if (optStr.length != 1)
				throw new IllegalArgumentException(
						"field is a singleton field.");

			return optStr[0];
		}
	}

	public String getProject() {
		return project;
	}

	public String[] getClasses() {
		return classes;
	}

	public String getOutput() {
		return output;
	}

	public boolean getClassExtendsObject() {
		return classExtendsObject;
	}

	public boolean getTypeSafety() {
		return typeSafety;
	}

	public String getOutputDynJAlloy() {
		return outputDynJAlloy;
	}

	public boolean getDynJAlloyAnnotations() {
		return dynjalloyAnnotations;
	}

	public boolean getQuantifierIncludesNull() {
		return quantifierIncluesNull;
	}

	public boolean getCheckNullDereference() {
		return checkNullDereference;
	}

	public boolean getAbstractSignatureObject() {
		return abstractSignatureObject;
	}

	public boolean hasClassToCheck() {
		return classToCheck != null;
	}

	public boolean hasMethodToCheck() {
		return methodToCheck != null;
	}

	public boolean getUseClassSingletons() {
		return useClassSingletons;
	}

	public boolean getIgnoreJmlAnnotations() {
		return ignoreJmlAnnotations;
	}

	public boolean getIgnoreJsflAnnotations() {
		return ignoreJsflAnnotations;
	}

	public boolean getAssertIsAssume() {
		return assertIsAssume;
	}

	public LoopResolutionEnum getLoopResolution() {
		return LoopResolutionEnum.valueOf(loopResolution);
	}

	public boolean getSimPredicates() {
		return simPredicates;
	}

	public boolean getUseCustomRelationalOverride() {
		return this.useCustomRelationalOverride;
	}

	public List<String> getRelevantClasses() {
		return this.relevantClasses;
	}

	/**
	 * This method is added to help in merge with new version of JDynalloy
	 */
	@Override
	public boolean getUseQualifiedNamesForJTypes() {
		return false;
	}

	@Override
	public boolean getSkolemizeInstanceInvariant() {
		return skolemizeInstanceInvariant;
	}

	@Override
	public boolean getSkolemizeInstanceAbstraction() {
		return skolemizeInstanceAbstraction;
	}

	@Override
	public boolean getJMLObjectSequenceToAlloySequence() {
		return jmlObjectSequenceToAlloySequence;
	}

	@Override
	public boolean getNewExceptionsAreLiterals() {
		return newExceptionsAreLiterals;
	}

	@Override
	public boolean getJMLObjectSetToAlloySet() {
		return true;
	}

	@Override
	public String getAssertionArguments() {
		return assertionArguments;
	}

	@Override
	public boolean getGenerateCheck() {
		return generateCheck;
	}

	@Override
	public boolean getGenerateRun() {
		return generateRun;
	}

	@Override
	public boolean getArrayIsInt() {
		return arrayIsInt;
	}

	@Override
	public boolean getModularReasoning() {
		return modularReasoning;
	}

	@Override
	public boolean getIncludeSimulationProgramDeclaration() {
		return includeSimulationProgramDeclaration;
	}

	public String getOutputModularJDynalloy() {
		return output_modular_JDynAlloy;
	}

	@Override
	public boolean getIncludeBranchLabels() {
		return include_branch_labels;
	}

	@Override
	public boolean getDisableIntegerLiteralReduction() {
		return disable_integer_literal_reduction;
	}

}

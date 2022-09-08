package ar.edu.jdynalloy.factory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.ast.JSignature;
import ar.edu.jdynalloy.xlator.JDynAlloyTyping;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public abstract class JSignatureFactory {

	// Primitives
	public static final JSignature NULL = JSignatureFactory
			.buildPrimitive("null");

	public static final JSignature BOOLEAN = JSignatureFactory
			.buildPrimitive("boolean");

	public static final JSignature TRUE = JSignatureFactory
			.buildPrimitive("true");

	public static final JSignature FALSE = JSignatureFactory
			.buildPrimitive("false");

	public static final JSignature INT = JSignatureFactory
			.buildPrimitive("Int");

	public static final JSignature CHAR = JSignatureFactory
			.buildPrimitive("char");

	public static final JType ALLOY_INT = JType.parse("Int");
	
	public static final JType JAVA_PRIMITIVE_CHAR_VALUE = JType.parse("JavaPrimitiveCharValue");
	
	public static final JType JAVA_PRIMITIVE_INTEGER_VALUE = JType.parse("JavaPrimitiveIntegerValue");
	
	public static final JSignature ALLOY_SEQ = JSignatureFactory
			.buildPrimitive("Int -> univ");

	public static final JType JAVA_PRIMITIVE_LONG_VALUE = JType.parse("JavaPrimitiveLongValue");

	public static final JType INT_ARRAY_TYPE = JType.parse("java_lang_IntArray+null");
	
	public static final JType CHAR_ARRAY_TYPE = JType.parse("java_lang_CharArray+null");

	public static final JType LONG_ARRAY_TYPE = JType.parse("java_lang_LongArray+null");

	public static final JType OBJECT_ARRAY_TYPE = JType.parse("java_lang_ObjectArray+null");

	public static final JType JAVA_LANG_OBJECT_TYPE = JType.parse("java_lang_Object+null");

	public static final JType JAVA_PRIMITIVE_FLOAT_VALUE = JType.parse("JavaPrimitiveFloatValue");

	public static final JType BOOLEAN_TYPE = JType.parse("boolean");
	
	public static final JType JAVA_UTIL_SET_TYPE = JType.parse("java_util_Set");

	public static final JType JAVA_PRIMITIVE_SHORT_VALUE = JType.parse("JavaPrimitiveShortValue");


	public static JSignature buildClass(String className, String superClassName) {
		return buildClass(false, className, new JDynAlloyTyping(),
				superClassName, new HashSet<String>());
	}

	public static JSignature buildClass(boolean isAbstract, String className,
			JDynAlloyTyping fields, String superClassName,
			Set<String> superInterfaces) {
		return new JSignature(false, isAbstract, className, fields, false,
				superClassName, null, superInterfaces, Collections
						.<AlloyFormula> emptySet()/* facts */, Collections
						.<String> emptyList(), Collections.<String> emptyList());
	}

	public static JSignature buildInterface(String interfaceName,
			Set<String> superInterfaces, Set<AlloyFormula> facts) {
		return buildInterface(interfaceName, superInterfaces, facts, null);
	}

	public static JSignature buildInterface(String interfaceName,
			Set<String> superInterfaces, Set<AlloyFormula> facts,
			String inSignatureId) {
		String realInSignatureId;
		if (inSignatureId == null) {
			realInSignatureId = (JDynAlloyConfig.getInstance()
					.getUseQualifiedNamesForJTypes() ? "java_lang_Object"
					: "Object");
		} else {
			realInSignatureId = inSignatureId;
		}
		return new JSignature(false, false, interfaceName,
				new JDynAlloyTyping(), false, null, realInSignatureId,
				superInterfaces, facts, Collections.<String> emptyList(),
				Collections.<String> emptyList());
	}

	public static JSignature buildPrimitive(String primitiveName) {
		return new JSignature(false, false, primitiveName,
				new JDynAlloyTyping(), true, null, null, null, Collections
						.<AlloyFormula> emptySet()/* facts */, Collections
						.<String> emptyList(), Collections.<String> emptyList());
	}

	public static JSignature buildPrimitiveValue(String primitive_value_name,
			JDynAlloyTyping alloy_fields, Set<AlloyFormula> alloy_facts,
			List<String> alloy_predicates, List<String> alloy_functions) {

		return new JSignature(false, false, primitive_value_name, alloy_fields,
				true, (String) null, (String) null, Collections
						.<String> emptySet(), alloy_facts, alloy_predicates,
				alloy_functions);
	}

	public static JSignature buildParameterizedClass(String className,
			String superClassName, String... typeParameters) {
		return new JSignature(false, false, className, null, false,
				superClassName, null, new HashSet<String>(), Collections
						.<AlloyFormula> emptySet()/* facts */, Collections
						.<String> emptyList(),
				Collections.<String> emptyList(), typeParameters);
	}

	public static JSignature buildClassSingleton(String className) {
		return buildClassSingleton(className, new JDynAlloyTyping());
	}

	public static JSignature buildClassSingleton(String className,
			JDynAlloyTyping staticFields) {
		return new JSignature(true, false, classSingleton(className),
				staticFields, false, (JDynAlloyConfig.getInstance()
						.getUseQualifiedNamesForJTypes() ? "java_lang_Class"
						: "Class"), null, Collections.<String> emptySet(),
				Collections.<AlloyFormula> emptySet() /* facts */, Collections
						.<String> emptyList(), Collections.<String> emptyList());
	}

	private static String classSingleton(String className) {
		return className + "Class";
	}

	public static JSignature buildLiteralSingleton(String className) {
		String literalSignatureId = String.format("%sLit", className);
		return new JSignature(true, false, literalSignatureId,
				new JDynAlloyTyping(), false, className, null, Collections
						.<String> emptySet(), Collections
						.<AlloyFormula> emptySet() /* facts */, Collections
						.<String> emptyList(), Collections.<String> emptyList());
	}

	public static JSignature buildAssertionFailureSignature() {
		return new JSignature(
				true,
				false,
				"AssertionFailure",
				new JDynAlloyTyping(),
				false,
				(JDynAlloyConfig.getInstance().getUseQualifiedNamesForJTypes() ? "java_lang_Throwable"
						: "Throwable"), null, Collections.<String> emptySet(),
				Collections.<AlloyFormula> emptySet() /* facts */, Collections
						.<String> emptyList(), Collections.<String> emptyList());
	}

	public static JSignature buildClassFieldsSignature() {
		return new JSignature(true, false, "ClassFields",
				new JDynAlloyTyping(), false, null, null, Collections
						.<String> emptySet(), Collections
						.<AlloyFormula> emptySet() /* facts */, Collections
						.<String> emptyList(), Collections.<String> emptyList());
	}

}

package ar.edu.taco.simplejml.builtin;

import static ar.edu.jdynalloy.xlator.JType.parse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JClassConstraint;
import ar.edu.jdynalloy.ast.JClassInvariant;
import ar.edu.jdynalloy.ast.JField;
import ar.edu.jdynalloy.ast.JObjectConstraint;
import ar.edu.jdynalloy.ast.JObjectInvariant;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JRepresents;
import ar.edu.jdynalloy.ast.JSignature;
import ar.edu.jdynalloy.factory.JSignatureFactory;
import ar.edu.jdynalloy.parser.JDynAlloyParserManager;
import ar.edu.jdynalloy.xlator.JDynAlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;

public class JavaPrimitiveFloatValue implements IBuiltInModule {

	private static JavaPrimitiveFloatValue instance;

	private static final int PRECISION = Float.SIZE;

	public static JavaPrimitiveFloatValue getInstance() {
		if (instance == null)
			instance = new JavaPrimitiveFloatValue();

		return instance;
	}

	private final JDynAlloyModule module;

	@Override
	public JDynAlloyModule getModule() {
		return module;
	}

	private static final String JAVA_PRIMITIVE_FLOAT_VALUE = "JavaPrimitiveFloatValue";

	private JavaPrimitiveFloatValue() {

		JDynAlloyTyping bitVector = new JDynAlloyTyping();
		List<JField> fields = new LinkedList<JField>();

		for (int i = 0; i < PRECISION; i++) {
			AlloyVariable alloy_field_name;
			alloy_field_name = new AlloyVariable(bit_field(i));
			bitVector.put(alloy_field_name, parse("boolean"));
		}

		Set<AlloyFormula> alloy_facts = new HashSet<AlloyFormula>();
		alloy_facts.add(fact_integrity_check());

		List<String> alloy_predicates = new LinkedList<String>();

		String resource_file_str = read_Float32_resource_file();
		alloy_predicates.add(resource_file_str);

		List<String> alloy_functions = new LinkedList<String>();

		JSignature signature = JSignatureFactory.buildPrimitiveValue(
				JAVA_PRIMITIVE_FLOAT_VALUE, bitVector, alloy_facts,
				alloy_predicates, alloy_functions);

		JSignature classSignature;
		classSignature = null;

		this.module = new JDynAlloyModule(JAVA_PRIMITIVE_FLOAT_VALUE,
				signature, classSignature, null, fields,
				Collections.<JClassInvariant> emptySet(),
				Collections.<JClassConstraint> emptySet(),
				Collections.<JObjectInvariant> emptySet(),
				Collections.<JObjectConstraint> emptySet(),
				Collections.<JRepresents> emptySet(),
				Collections.<JProgramDeclaration> emptySet(), new AlloyTyping(), new ArrayList<AlloyFormula>(), false);

	}

	private AlloyFormula fact_integrity_check() {
		return new PredicateFormula(null,
				"pred_java_primitive_float_value_integrity_check",
				Collections.<AlloyExpression> emptyList());
	}

	private String bit_field(int i) {
		String bit_field = String.format("b%s%s", (i < 10 ? "0" : ""), i);
		return bit_field;
	}

	private Map<Float, JDynAlloyModule> float_literals = new HashMap<Float, JDynAlloyModule>();

	public ExprConstant toJavaPrimitiveFloatLiteral(float float_literal, boolean isPinnedForStryker) {
		JDynAlloyModule literal_module;

		if (!float_literals.containsKey(float_literal)) {

			JSignature literal_signature = create_literal_signature(float_literal);
			String moduleId = literal_signature.getSignatureId();
			literal_module = new JDynAlloyModule(moduleId, literal_signature,
					null, null, Collections.<JField> emptyList(),
					Collections.<JClassInvariant> emptySet(),
					Collections.<JClassConstraint> emptySet(),
					Collections.<JObjectInvariant> emptySet(),
					Collections.<JObjectConstraint> emptySet(),
					Collections.<JRepresents> emptySet(),
					Collections.<JProgramDeclaration> emptySet(), new AlloyTyping(), new ArrayList<AlloyFormula>(), isPinnedForStryker);
			float_literals.put(float_literal, literal_module);
		}
		literal_module = float_literals.get(float_literal);

		String signatureId = literal_module.getSignature().getSignatureId();

		return new ExprConstant(null, signatureId);
	}

	private boolean[] create_float_bit_vector(float float_value) {
		int i = Float.floatToIntBits(float_value);
		boolean[] bit_vector = new boolean[Integer.SIZE];
		for (int k = 0; k < Integer.SIZE; k++) {
			if ((i & (1 << k)) != 0) {
				bit_vector[k] = true;
			} else {
				bit_vector[k] = false;
			}
		}
		return bit_vector;
	}

	private String build_java_primitive_float_literal_predicate_id(float i) {
		String predicateId = "pred_java_primitive_float_value_literal_"
				+ float_to_String(i);
		return predicateId;
	}

	private String pred_java_primitive_long_value_literal(float i) {
		StringBuffer buff = new StringBuffer();

		String header_str;

		String predicateId = build_java_primitive_float_literal_predicate_id(i);

		header_str = "pred " + predicateId
				+ "[ret: JavaPrimitiveFloatValue] {\n";

		buff.append(header_str);

		boolean[] bit_vector = create_float_bit_vector(i);
		for (int k = 0; k < bit_vector.length; k++) {

			String bit_field_str;
			if (bit_vector[k] == true) {
				bit_field_str = String.format(" ret.%s=true \n", bit_field(k));
			} else {
				bit_field_str = String.format(" ret.%s=false \n", bit_field(k));
			}

			buff.append(bit_field_str);

		}

		buff.append("}\n");
		return buff.toString();
	}

	private JSignature create_literal_signature(float float_literal) {

		String float_bit_vector_str = float_to_String(float_literal);

		List<String> alloy_preds = new LinkedList<String>();

		String value_literal = pred_java_primitive_long_value_literal(float_literal);
		alloy_preds.add(value_literal);

		String literal_signature_id;
		literal_signature_id = "JavaPrimitiveFloatLiteral"
				+ float_bit_vector_str;

		Set<AlloyFormula> alloy_facts = new HashSet<AlloyFormula>();

		AlloyFormula alloy_fact = new PredicateFormula(null,
				build_java_primitive_float_literal_predicate_id(float_literal),
				Collections.<AlloyExpression> singletonList(ExprConstant
						.buildExprConstant(literal_signature_id)));

		alloy_facts.add(alloy_fact);

		JSignature literal_signature = new JSignature(true, false,
				literal_signature_id, new JDynAlloyTyping(), false,
				"JavaPrimitiveFloatValue", null,
				Collections.<String> emptySet(), alloy_facts, alloy_preds,
				Collections.<String> emptyList());

		return literal_signature;
	}

	private String float_to_String(float float_literal) {
		int int_value = Float.floatToIntBits(float_literal);
		String hex_string = Integer.toHexString(int_value).toUpperCase();
		return hex_string;
	}

	public Collection<JDynAlloyModule> get_float_literal_modules() {
		return float_literals.values();
	}

	private String read_Float32_resource_file() {
		InputStreamReader inputStreamReader = JDynAlloyParserManager
				.createReaderFromResource("ar/edu/taco/simplejml/builtin/Float32.als");

		StringBuffer buff = new StringBuffer();
		try {
			int curr_char = inputStreamReader.read();
			while (curr_char != -1) {
				buff.append((char) curr_char);
				curr_char = inputStreamReader.read();
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		String string = buff.toString();
		return string;
	}

}

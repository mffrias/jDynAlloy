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

public class JavaPrimitiveLongValue implements IBuiltInModule {

	private static JavaPrimitiveLongValue instance;

	private static final int PRECISION = Long.SIZE;

	public static JavaPrimitiveLongValue getInstance() {
		if (instance == null)
			instance = new JavaPrimitiveLongValue();

		return instance;
	}

	private final JDynAlloyModule module;

	@Override
	public JDynAlloyModule getModule() {
		return module;
	}

	private static final String JAVA_PRIMITIVE_LONG_VALUE = "JavaPrimitiveLongValue";

	private JavaPrimitiveLongValue() {

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
		alloy_predicates.add(pred_integrity_long());

		String resource_file_str = read_Long64_resource_file();
		alloy_predicates.add(resource_file_str);
		alloy_predicates.add(pred_java_primitive_long_value_sshr());

		List<String> alloy_functions = new LinkedList<String>();
		alloy_functions.add(fun_java_primitive_long_value_add());
		alloy_functions.add(fun_java_primitive_long_value_sub());
		alloy_functions.add(fun_java_primitive_long_value_sshr());

		JSignature signature = JSignatureFactory.buildPrimitiveValue(
				JAVA_PRIMITIVE_LONG_VALUE, bitVector, alloy_facts,
				alloy_predicates, alloy_functions);

		JSignature classSignature;
		classSignature = null;

		this.module = new JDynAlloyModule(JAVA_PRIMITIVE_LONG_VALUE, signature,
				classSignature, null, fields, Collections
						.<JClassInvariant> emptySet(), Collections
						.<JClassConstraint> emptySet(), Collections
						.<JObjectInvariant> emptySet(), Collections
						.<JObjectConstraint> emptySet(), Collections
						.<JRepresents> emptySet(), Collections
						.<JProgramDeclaration> emptySet(), new AlloyTyping(), new ArrayList<AlloyFormula>(), false);

	}

	private AlloyFormula fact_integrity_check() {
		return new PredicateFormula(null, "pred_integrity_long", Collections
				.<AlloyExpression> emptyList());
	}

	private String pred_integrity_long() {
		StringBuffer buff = new StringBuffer();
		buff.append("pred pred_integrity_long[] {\n");
		buff
				.append(" all m, n : JavaPrimitiveLongValue | pred_java_primitive_long_value_eq[m,n] implies m = n \n");
		buff.append("}\n");
		return buff.toString();
	}

	private String fun_java_primitive_long_value_add() {
		StringBuffer buff = new StringBuffer();
		buff
				.append("fun fun_java_primitive_long_value_add[a: JavaPrimitiveLongValue, \n");
		buff
				.append("                                      b: JavaPrimitiveLongValue]: JavaPrimitiveLongValue {\n");

		buff
				.append("  { result: JavaPrimitiveLongValue | some overflow: boolean | pred_java_primitive_long_value_add[a,b,result,overflow]} \n");

		buff.append("}\n");
		return buff.toString();

	}

	private String fun_java_primitive_long_value_sub() {
		StringBuffer buff = new StringBuffer();
		buff
				.append("fun fun_java_primitive_long_value_sub[a: JavaPrimitiveLongValue, \n");
		buff
				.append("                                      b: JavaPrimitiveLongValue]: JavaPrimitiveLongValue {\n");

		buff
				.append(" {result: JavaPrimitiveLongValue | some overflow: boolean | pred_java_primitive_long_value_add[b,result,a,overflow] }\n");

		buff.append("}\n");
		return buff.toString();

	}

	private String pred_java_primitive_long_value_sshr() {
		StringBuffer buff = new StringBuffer();
		buff
				.append("pred pred_java_primitive_long_value_sshr[a: JavaPrimitiveLongValue, \n");
		buff
				.append("                                          ret: JavaPrimitiveLongValue] {\n");

		final int last_bit_index = PRECISION - 1;

		buff.append(" a." + bit_field(last_bit_index) + "= ret."
				+ bit_field(last_bit_index) + " \n");
		for (int i = last_bit_index - 1; i > 0; i--) {
			buff.append(" a." + bit_field(i) + "= ret." + bit_field(i - 1)
					+ " \n");
		}

		buff.append("}\n");
		return buff.toString();
	}

	private String bit_field(int i) {
		String bit_field = String.format("b%s%s", (i < 10 ? "0" : ""), i);
		return bit_field;
	}

	private String fun_java_primitive_long_value_sshr() {
		StringBuffer buff = new StringBuffer();
		buff
				.append("fun fun_java_primitive_long_value_sshr[a: JavaPrimitiveLongValue]: JavaPrimitiveLongValue {\n");
		buff
				.append("  { ret: JavaPrimitiveLongValue | pred_java_primitive_long_value_sshr[a,ret] } \n");
		buff.append("}\n");
		return buff.toString();
	}

	private Map<Long, JDynAlloyModule> long_literals = new HashMap<Long, JDynAlloyModule>();

	public ExprConstant toJavaPrimitiveLongLiteral(long long_literal, boolean isPinnedForStryker) {
		JDynAlloyModule literal_module;
		if (!long_literals.containsKey(long_literal)) {
			JSignature literal_signature = create_literal_signature(long_literal);
			String moduleId = literal_signature.getSignatureId();
			literal_module = new JDynAlloyModule(moduleId, literal_signature,
					null, null, Collections.<JField> emptyList(), Collections
							.<JClassInvariant> emptySet(), Collections
							.<JClassConstraint> emptySet(), Collections
							.<JObjectInvariant> emptySet(), Collections
							.<JObjectConstraint> emptySet(), Collections
							.<JRepresents> emptySet(), Collections
							.<JProgramDeclaration> emptySet(), new AlloyTyping(), new ArrayList<AlloyFormula>(), isPinnedForStryker);
			long_literals.put(long_literal, literal_module);
		}
		literal_module = long_literals.get(long_literal);

		String signatureId = literal_module.getSignature().getSignatureId();

		return new ExprConstant(null, signatureId);
	}

	private boolean[] create_long_bit_vector(long i) {
		String binary_str = Long.toBinaryString(i);
		String padded_binary_str = leftPad(binary_str);

		boolean[] bit_vector = new boolean[Long.SIZE];
		for (int k = 0; k < padded_binary_str.length(); k++) {
			if (padded_binary_str.charAt(Long.SIZE - k -1) == '1') {
				bit_vector[k] = true;
			} else {
				bit_vector[k] = false;
			}
		}
		return bit_vector;
	}

	private String leftPad(String str) {
		StringBuilder sb = new StringBuilder();
		for (int toprepend=Long.SIZE-str.length(); toprepend>0; toprepend--) {
		    sb.append('0');
		}
		sb.append(str);
		String result = sb.toString();
		return result;

	}

	private String build_java_primitive_long_literal_predicate_id(long i) {
		String predicateId;
		if (i >= 0)
			predicateId = String.format(
					"pred_java_primitive_long_value_literal_%s", i);
		else {
			String i_string = String.valueOf(i);
			String positive_i_string = i_string.substring(1);
			predicateId = String.format(
					"pred_java_primitive_long_value_literal_minus_%s", positive_i_string);
		}
		return predicateId;
	}

	private String pred_java_primitive_long_value_literal(long i) {
		StringBuffer buff = new StringBuffer();

		String header_str;

		String predicateId = build_java_primitive_long_literal_predicate_id(i);

		header_str = "pred " + predicateId
				+ "[ret: JavaPrimitiveLongValue] {\n";

		buff.append(header_str);

		boolean[] bit_vector = create_long_bit_vector(i);
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

	private JSignature create_literal_signature(long long_literal) {

		List<String> alloy_preds = new LinkedList<String>();

		String value_literal = pred_java_primitive_long_value_literal(long_literal);
		alloy_preds.add(value_literal);

		String literal_signature_id;
		if (long_literal < 0){
			String the_long_String = String.valueOf(long_literal);
			String the_positive_long_String = the_long_String.substring(1);
			literal_signature_id = "JavaPrimitiveLongLiteralMinus"
					+ the_positive_long_String;
		} else
			literal_signature_id = "JavaPrimitiveLongLiteral" + long_literal;

		Set<AlloyFormula> alloy_facts = new HashSet<AlloyFormula>();

		AlloyFormula alloy_fact = new PredicateFormula(null,
				build_java_primitive_long_literal_predicate_id(long_literal),
				Collections.<AlloyExpression> singletonList(ExprConstant
						.buildExprConstant(literal_signature_id)));

		alloy_facts.add(alloy_fact);

		JSignature literal_signature = new JSignature(true, false,
				literal_signature_id, new JDynAlloyTyping(), false,
				"JavaPrimitiveLongValue", null,
				Collections.<String> emptySet(), alloy_facts, alloy_preds,
				Collections.<String> emptyList());

		return literal_signature;
	}

	public Collection<JDynAlloyModule> get_long_literal_modules() {
		return long_literals.values();
	}

	private String read_Long64_resource_file() {
		InputStreamReader inputStreamReader = JDynAlloyParserManager
				.createReaderFromResource("ar/edu/taco/simplejml/builtin/Long64.als");

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

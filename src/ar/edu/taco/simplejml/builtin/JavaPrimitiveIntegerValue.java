/*
 * TACO: Translation of Annotated COde
 * Copyright (c) 2010 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
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
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula.Operator;

public class JavaPrimitiveIntegerValue implements IBuiltInModule {

	private static final String JAVA_PRIMITIVE_INTEGER_VALUE = "JavaPrimitiveIntegerValue";

	private final JDynAlloyModule module;

	private static final int PRECISION = Integer.SIZE;

	private JavaPrimitiveIntegerValue() {

		JDynAlloyTyping bitVector = new JDynAlloyTyping();
		List<JField> fields = new LinkedList<JField>();

		for (int i = 0; i < PRECISION; i++) {
			AlloyVariable alloy_field_name;
			alloy_field_name = new AlloyVariable(bit_field(i));
			bitVector.put(alloy_field_name, parse("boolean"));
		}

		Set<AlloyFormula> alloy_facts = new HashSet<AlloyFormula>();
		alloy_facts.add(fact_integrity_check());

		String resource_file_str = read_Integer32_resource_file();

		List<String> alloy_predicates = new LinkedList<String>();
		alloy_predicates.add(resource_file_str);

		List<String> alloy_functions = new LinkedList<String>();

//		int bitwidth = TacoConfigurator.getInstance().getBitwidth();

		// minus 1
		String pred_literal_minus_1 = pred_java_primitive_integer_value_literal(-1);
		String fun_literal_minus_1 = fun_java_primitive_integer_value_literal(-1);
		alloy_predicates.add(pred_literal_minus_1);
		alloy_functions.add(fun_literal_minus_1);

		int_literals_already_defined.add(-1);

		JSignature signature = JSignatureFactory.buildPrimitiveValue(JAVA_PRIMITIVE_INTEGER_VALUE, bitVector, alloy_facts, alloy_predicates, alloy_functions);

		JSignature classSignature;
		classSignature = null;

		this.module = new JDynAlloyModule(JAVA_PRIMITIVE_INTEGER_VALUE, signature, classSignature, null, fields, Collections.<JClassInvariant> emptySet(),
				Collections.<JClassConstraint> emptySet(), Collections.<JObjectInvariant> emptySet(), Collections.<JObjectConstraint> emptySet(),
				Collections.<JRepresents> emptySet(), Collections.<JProgramDeclaration> emptySet(), new AlloyTyping(), new ArrayList<AlloyFormula>(), false);

	}

	private String read_Integer32_resource_file() {
		InputStreamReader inputStreamReader = JDynAlloyParserManager.createReaderFromResource("ar/edu/taco/simplejml/builtin/Integer32.als");

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

	private String bit_field(int i) {
		String bit_field = String.format("b%s%s", (i < 10 ? "0" : ""), i);
		return bit_field;
	}

	public String pred_java_primitive_integer_value_size_of(int number_of_non_negatives) {
		StringBuffer buff = new StringBuffer();

		buff.append("pred pred_java_primitive_integer_value_size_of[s: set univ, ret: JavaPrimitiveIntegerValue] {\n");

		for (int i = 0; i < number_of_non_negatives; i++) {
			String line_str = "  ";
			if (i > 0)
				line_str += "else ";

			line_str += String.format("(#(s)=%s) => pred_java_primitive_integer_value_literal_%s[ret] ", i, i);
			line_str += "\n";

			buff.append(line_str);

		}
		buff.append("  else pred_java_primitive_integer_value_literal_minus_1[ret] \n");

		buff.append("}\n");
		return buff.toString();
	}

	public String fun_java_primitive_integer_value_literal(int i) {
		StringBuffer buff = new StringBuffer();

		String header_str;
		String line_str;

		if (i >= 0) {
			header_str = String.format("fun fun_java_primitive_integer_value_literal_%s[]: one JavaPrimitiveIntegerValue {\n", i);

			line_str = String.format(" { ret: JavaPrimitiveIntegerValue | pred_java_primitive_integer_value_literal_%s[ret] }", i);

		} else {
			header_str = String.format("fun fun_java_primitive_integer_value_literal_minus_%s[]: one JavaPrimitiveIntegerValue {\n", Math.abs(i));
			line_str = String.format(" { ret: JavaPrimitiveIntegerValue | pred_java_primitive_integer_value_literal_minus_%s[ret] }", Math.abs(i));
		}

		buff.append(header_str);
		buff.append(line_str + "\n");

		buff.append("}\n");

		return buff.toString();
	}

	public String pred_java_primitive_integer_value_literal(int i) {
		StringBuffer buff = new StringBuffer();

		String header_str;

		String predicateId = build_java_primitive_integer_literal_predicate_id(i);

		header_str = "pred " + predicateId + "[ret: JavaPrimitiveIntegerValue] {\n";

		buff.append(header_str);

		boolean[] bit_vector = create_integer_bit_vector(i);
		for (int k = 0; k < bit_vector.length; k++) {

			String bit_field_str;
			if (bit_vector[k] == true) {
				bit_field_str = String.format(" ret.b%s%s=true ", (k < 10 ? "0" : ""), k);
			} else {
				bit_field_str = String.format(" ret.b%s%s=false ", (k < 10 ? "0" : ""), k);
			}

			buff.append(bit_field_str + "\n");

		}

		buff.append("}\n");
		return buff.toString();
	}

	private String build_java_primitive_integer_literal_predicate_id(int i) {
		String predicateId;
		if (i >= 0)
			predicateId = String.format("pred_java_primitive_integer_value_literal_%s", i);
		else {
			String i_string = String.valueOf(i);
			String positive_i_string = i_string.substring(1);
			predicateId = String.format("pred_java_primitive_integer_value_literal_minus_%s", positive_i_string);
		}
		return predicateId;
	}

	private AlloyFormula fact_integrity_check() {
		// all m, n : intType | intTypeEQ[m,n] implies m = n

		List<String> names = new LinkedList<String>();
		List<AlloyExpression> exprs = new LinkedList<AlloyExpression>();

		names.add("m");
		exprs.add(new ExprConstant(null, "JavaPrimitiveIntegerValue"));

		names.add("n");
		exprs.add(new ExprConstant(null, "JavaPrimitiveIntegerValue"));

		List<AlloyExpression> pred_exprs = new LinkedList<AlloyExpression>();
		pred_exprs.add(ExprVariable.buildExprVariable("m"));
		pred_exprs.add(ExprVariable.buildExprVariable("n"));

		PredicateFormula java_primitive_integer_value_eq = new PredicateFormula(null, "pred_java_primitive_integer_value_eq", pred_exprs);

		EqualsFormula equals_form = new EqualsFormula(ExprVariable.buildExprVariable("m"), ExprVariable.buildExprVariable("n"));

		ImpliesFormula implies_form = new ImpliesFormula(java_primitive_integer_value_eq, equals_form);

		QuantifiedFormula integrity_check_fact = new QuantifiedFormula(Operator.FOR_ALL, names, exprs, implies_form);

		return integrity_check_fact;
	}

	private static JavaPrimitiveIntegerValue instance = null;

	public static JavaPrimitiveIntegerValue getInstance() {
		if (instance == null)
			instance = new JavaPrimitiveIntegerValue();
		return instance;
	}

	@Override
	public JDynAlloyModule getModule() {
		return module;
	}

	private Map<Integer, JDynAlloyModule> integer_literals = new HashMap<Integer, JDynAlloyModule>();

	public Collection<JDynAlloyModule> get_integer_literal_modules() {
		return integer_literals.values();
	}

	public ExprConstant toJavaPrimitiveIntegerLiteral(int int_literal, boolean isPinnedForStryker) {

		JDynAlloyModule literal_module;
		if (!integer_literals.containsKey(int_literal)) {
			
			if (!int_literals_already_defined.contains(int_literal)) {
				String value_literal = pred_java_primitive_integer_value_literal(int_literal);
				this.module.getSignature().getAlloyPredicates().add(value_literal);
			}
			
			JSignature literal_signature = create_literal_signature(int_literal);
			String moduleId = literal_signature.getSignatureId();
			literal_module = new JDynAlloyModule(moduleId, literal_signature, null, null, Collections.<JField> emptyList(),
					Collections.<JClassInvariant> emptySet(), Collections.<JClassConstraint> emptySet(), Collections.<JObjectInvariant> emptySet(),
					Collections.<JObjectConstraint> emptySet(), Collections.<JRepresents> emptySet(), Collections.<JProgramDeclaration> emptySet(), 
					new AlloyTyping(), new ArrayList<AlloyFormula>(), isPinnedForStryker);
			integer_literals.put(int_literal, literal_module);
			int_literals_already_defined.add(int_literal);
		}
		literal_module = integer_literals.get(int_literal);

		String signatureId = literal_module.getSignature().getSignatureId();

		return new ExprConstant(null, signatureId);
	}

	private boolean[] create_integer_bit_vector(int i) {
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

	private JSignature create_literal_signature(int int_literal) {

		List<String> alloy_preds = new LinkedList<String>();



		String literal_signature_id;
		if (int_literal < 0){
			String negative_int_literal_string = String.valueOf(int_literal);
			String positive_version_of_negative_int_literal_string = negative_int_literal_string.substring(1);
			literal_signature_id = "JavaPrimitiveIntegerLiteralMinus" + positive_version_of_negative_int_literal_string;
		} else
			literal_signature_id = "JavaPrimitiveIntegerLiteral" + int_literal;

		Set<AlloyFormula> alloy_facts = new HashSet<AlloyFormula>();

		AlloyFormula alloy_fact = new PredicateFormula(null, build_java_primitive_integer_literal_predicate_id(int_literal),
				Collections.<AlloyExpression> singletonList(ExprConstant.buildExprConstant(literal_signature_id)));

		alloy_facts.add(alloy_fact);

		JSignature literal_signature = new JSignature(true, false, literal_signature_id, new JDynAlloyTyping(), false, "JavaPrimitiveIntegerValue", null,
				Collections.<String> emptySet(), alloy_facts, alloy_preds, Collections.<String> emptyList());

		return literal_signature;
	}

	private Set<Integer> int_literals_already_defined = new HashSet<Integer>();

	public String fun_java_primitive_integer_value_size_of() {
		StringBuffer buff = new StringBuffer();
		buff.append("fun fun_java_primitive_integer_value_size_of[s: set univ]: one JavaPrimitiveIntegerValue {\n");

		buff.append("  { ret: JavaPrimitiveIntegerValue | pred_java_primitive_integer_value_size_of[s,ret]} \n");
		buff.append("}\n");
		return buff.toString();
	}

	public boolean is_int_literal_already_defined(int i) {
		return this.int_literals_already_defined.contains(i);
	}
}

package ar.edu.jdynalloy.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.JDynAlloyException;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;

public abstract class JPreludeFactory {

	private static DynalloyModule instance = null;

	public static final int DEFAULT_BITWIDTH = 6;

	public static final DynalloyModule buildPreludeModule(HashSet<String> moduleIds) {
		instance = createNewInstance(moduleIds);
		return instance;
	}

	private static DynalloyModule createNewInstance(HashSet<String> moduleIds) {

		StringBuffer bff = new StringBuffer();
		bff.append("module moduleId \n");
		bff.append("\n");
		bff.append("open util/integer \n");
		bff.append("open util/sequniv as sequniv \n");
		bff.append("\n");
		// null
		bff.append("one sig null {} \n");
		bff.append("\n");
		// reach
		bff.append("fun fun_reach[h: univ, \n");
		bff.append("              type: set univ, \n");
		bff.append("              field: univ -> univ\n");
		bff.append("]: set univ { \n");
		bff.append("  h.*(field & type->(type+null)) & type \n");
		bff.append("}\n");
		bff.append("\n");
		//loosely typed reach
		bff.append("fun fun_weak_reach[h: univ, \n");
		bff.append("              type: set univ, \n");
		bff.append("              field: univ -> univ\n");
		bff.append("]: set univ { \n");
		bff.append("  h.*(field) & type \n");
		bff.append("}\n");
		bff.append("\n");


//		bff.append("one sig AssertionErrorLit extends java_lang_AssertionError {} \n");

		if (integer_complement) {
			StringBuffer complement_func = new StringBuffer();

			int max_integer = (int) Math.pow(2, DEFAULT_BITWIDTH - 1) - 1;
			int min_integer = (int) -Math.pow(2, DEFAULT_BITWIDTH - 1);
			int curr_max = max_integer;
			for (int index = min_integer; index <= max_integer; index++) {
				if (complement_func.length() != 0)
					complement_func.append("\n    +");
				complement_func.append(String.format("  %s->%s", index,
						curr_max));
				curr_max--;
			}

			bff.append("fun complement[x: Int]: Int {\n");
			bff.append(" x.(\n");
			bff.append(complement_func.toString());
			bff.append("   )\n");
			bff.append("}\n");
			bff.append("\n");
		}

		if (reach_JMLObjectSet) {

			// reach_JMLObjectSet
			bff.append("fun fun_reach_JMLObjectSet[h: univ, \n");
			bff.append("              type: set univ, \n");
			bff.append("              field: univ -> univ, \n");
			bff
			.append("              JMLObjectSet_contains: org_jmlspecs_models_JMLObjectSet -> set univ \n");
			bff.append("]: org_jmlspecs_models_JMLObjectSet { \n");
			bff.append("   {\n");
			bff.append("      x : org_jmlspecs_models_JMLObjectSet | \n");
			bff
			.append("        x.JMLObjectSet_contains = (h.*(field & type->(type+null)) & type) \n");
			bff.append("   }\n");
			bff.append("}\n");
			bff.append("\n");

		}

		// boolean
		bff.append("abstract sig boolean {} \n");
		bff.append("one sig true extends boolean {} \n");
		bff.append("one sig false extends boolean {} \n");
		bff.append("\n");

		if (char_is_registered) {
			// char
			bff.append("abstract sig char {} \n");
			bff.append("\n");
		}

		// predicates
		bff.append("pred TruePred[] {} \n");
		bff.append("pred FalsePred[] { not TruePred[] } \n");
		bff.append("pred equ[l,r:univ] {l=r} \n");
		bff.append("pred neq[l,r:univ] {l!=r} \n");
		bff.append("\n");

		// arithmetic extra-functions
		bff.append("fun shl[l,r: Int]: Int { l << r } \n");
		bff.append("fun sshr[l,r: Int]: Int { l >> r } \n");
		bff.append("fun ushr[l,r: Int]: Int { l >>> r } \n");
		bff.append("\n");

		// object functions
		bff.append("fun fun_univ_equals[\n");
		bff.append("  l:univ, \n");
		bff.append("  r: univ \n");
		bff.append("]: boolean { \n");
		bff.append("  (equ[l,r]) => true else false \n");
		bff.append("} \n");
		bff.append("\n");

		// set functions
		bff.append("fun fun_set_add[\n");
		bff.append("  l: set univ,\n");
		bff.append("  e: univ\n");
		bff.append("]: set univ { \n");
		bff.append("  l+e \n");
		bff.append("} \n");
		bff.append("\n");

		bff.append("fun fun_set_remove[\n");
		bff.append("  l: set univ,\n");
		bff.append("  e: univ\n");
		bff.append("]: set univ {\n");
		bff.append("  l-e\n");
		bff.append("}\n");

		bff.append("fun fun_set_contains[\n");
		bff.append("  l: set univ,\n");
		bff.append("  e: univ\n");
		bff.append("]: boolean {\n");
		bff.append("  (e in l) => true else false \n");
		bff.append("} \n");



		bff.append("pred isSubset[\n");
		bff.append("  l: set univ,\n");
		bff.append("  r: set univ\n");
		bff.append("] {\n");
		bff.append("  (l in r) \n");
		bff.append("} \n");

		bff.append("pred isNotSubset[\n");
		bff.append("  l: set univ,\n");
		bff.append("  r: set univ\n");
		bff.append("] {\n");
		bff.append("  (l !in r) \n");
		bff.append("} \n");

		bff.append("fun fun_set_size[s: set univ]: Int { #s } \n");
		bff.append("\n");

		bff
		.append("fun fun_not_empty_set[s: set univ]: boolean { (no s) => false else true } \n");
		bff.append("\n");

		bff.append("fun fun_set_is_empty[s: set univ]: boolean { (no s) => true else false } \n");
		bff.append("\n");

		bff.append("pred pred_empty_set[l: set univ] { (no l) } \n");
		bff.append("\n");

		bff.append("pred pred_set_some[l: set univ] { some l } \n");
		bff.append("\n");

		bff.append("pred pred_set_one[l: set univ] { one l } \n");
		bff.append("\n");

		bff.append("pred pred_set_lone[l: set univ] { lone l } \n");
		bff.append("\n");

		bff.append("pred pred_Object_subset[\n");
		bff.append("  s: set univ\n");
		bff.append("] {\n");
		bff.append("  s in " + javaLangPackage() + "Object+null\n");
		bff.append("}\n");
		bff.append("\n");

		bff.append("fun fun_set_intersection[\n");
		bff.append("  l: set univ,\n");
		bff.append("  r: set univ\n");
		bff.append("]: set univ {\n");
		bff.append("  l & r \n");
		bff.append("} \n");

		bff.append("fun fun_set_difference[\n");
		bff.append("  l: set univ,\n");
		bff.append("  r: set univ\n");
		bff.append("]: set univ {\n");
		bff.append("  l - r \n");
		bff.append("} \n");

		bff.append("fun fun_rel_difference[ \n");
		bff.append("  rel: univ -> univ, \n");
		bff.append("  l: univ,\n");
		bff.append("  r: univ\n");
		bff.append("]: univ->univ {\n");
		bff.append(" rel - (l->r) \n");
		bff.append("}\n");

		bff.append("fun fun_rel_add[ \n");
		bff.append("  rel: univ -> univ, \n");
		bff.append("  l: univ,\n");
		bff.append("  r: univ\n");
		bff.append("]: univ->univ {\n");
		bff.append(" rel + (l->r) \n");
		bff.append("}\n");

		bff.append("fun fun_set_sum[\n");
		bff.append("  s: set Int\n");
		bff.append("]: Int {\n");
		bff.append("  sum s \n");
		bff.append("} \n");

		// list functions
		bff.append("pred pred_empty_list[l: Int -> univ] { (no l) } \n");
		bff.append("\n");

		bff.append("fun fun_list_add[\n");
		bff.append("  l: Int -> univ,\n");
		bff.append("  e: univ\n");
		bff.append("]: Int -> univ {\n");
		bff.append("  l + (Int[#(l.univ)]->e)\n");
		bff.append("} \n");
		bff.append("\n");

		bff.append("fun fun_list_get[\n");
		bff.append("  l: Int -> univ, \n");
		bff.append("  index: Int\n");
		bff.append("]: univ { \n");
		bff.append("  index.l \n");
		bff.append("} \n");
		bff.append("\n");

		bff.append("fun fun_list_contains[\n");
		bff.append("  l: Int -> univ, \n");
		bff.append("  e: univ\n");
		bff.append("]: boolean { \n");
		bff.append("  (e in Int.l) => true else false \n");
		bff.append("} \n");
		bff.append("\n");

		bff.append("fun fun_list_remove[\n");
		bff.append("  l: Int -> univ, \n");
		bff.append("  index: Int\n");
		bff.append("]: Int -> univ { \n");
		bff.append("  prevs[index]<:(l-(index->univ)) + next.(nexts[index]<:(l-(index->univ))) \n");
		bff.append("} \n");
		bff.append("\n");

		bff.append("fun fun_list_size[s: Int -> univ]: Int { #s } \n");
		bff.append("\n");

		bff.append("fun fun_list_equals[\n");
		bff.append("  s1:Int -> univ, \n");
		bff.append("  s2: Int -> univ\n");
		bff.append("]: boolean { \n");
		bff.append("  (s1=s2) => true else false \n");
		bff.append("} \n");
		bff.append("\n");

		bff
		.append("fun fun_list_empty[s: Int -> univ]: boolean { (#s = 0) => true else false } \n");
		bff.append("\n");

		// map functions
		bff.append("pred pred_empty_map[map: univ -> univ] { (no map) } \n");
		bff.append("\n");

		bff.append("fun fun_map_put[\n");
		bff.append("  map: univ->univ, \n");
		bff.append("  k: univ, \n");
		bff.append("  v: univ\n");
		bff.append("]: univ-> univ { \n");
		bff.append("  map ++ (k->v) \n");
		bff.append("}\n");
		bff.append("\n");

		bff.append("fun fun_map_contains_key[\n");
		bff.append("  map: univ -> univ, \n");
		bff.append("  k: univ\n");
		bff.append("]: boolean { \n");
		bff.append("  (some k.map) => true else false \n");
		bff.append("}\n");
		bff.append("\n");

		bff.append("fun fun_map_remove[\n");
		bff.append("  map: univ -> univ, \n");
		bff.append("  k: univ\n");
		bff.append("]: univ->univ {\n");
		bff.append("  map - (k->univ) \n");
		bff.append("} \n");
		bff.append("\n");

		bff.append("fun fun_map_get[\n");
		bff.append("  map: univ -> univ, \n");
		bff.append("  k: univ\n");
		bff.append("]: univ { \n");
		bff.append("  (some k.map) => k.map else null \n");
		bff.append("} \n\n");

		bff.append("fun fun_map_is_empty[\n");
		bff.append("  map: univ -> univ, \n");
		bff.append("]: boolean { \n");
		bff.append("  (some map) => false else true \n");
		bff.append("}\n");
		bff.append("\n");

		bff.append("fun fun_map_clear[\n");
		bff.append("  mapEntries1: univ -> univ -> univ, \n");
		bff.append("  map: univ\n");
		bff.append("]: univ -> univ -> univ { \n");
		bff.append("  mapEntries1 - (map -> univ -> univ)\n");
		bff.append("}\n");
		bff.append("\n");

		bff.append("fun fun_map_size[\n");
		bff.append("  map: univ -> univ, \n");
		bff.append("]: univ {\n");
		bff.append("  #map \n");
		bff.append("} \n");
		bff.append("\n");

		// null derefence check
		bff.append("pred isEmptyOrNull[u: univ] { u in null } \n");

		// closure
		bff.append("fun fun_closure[\n");
		bff.append("  rel: univ -> univ \n");
		bff.append("]: univ -> univ {\n");
		bff.append("  ^rel \n");
		bff.append("} \n\n");

		// reflexive closure
		bff.append("fun fun_reflexive_closure[\n");
		bff.append("  rel: univ -> univ \n");
		bff.append("]: univ -> univ {\n");
		bff.append("  *rel \n");
		bff.append("} \n\n");

		// transpose
		bff.append("fun fun_transpose[\n");
		bff.append("  rel: univ -> univ \n");
		bff.append("]: univ -> univ {\n");
		bff.append("  ~rel \n");
		bff.append("} \n\n");

		// lift expression

		bff.append("pred liftExpression[\n");
		bff.append("  expr: univ \n");
		bff.append("] {\n");
		bff.append("  expr=true \n");
		bff.append("} \n\n");

		// updateMap
		if ((JDynAlloyConfig.getInstance().getBuiltInModules().isEmpty() || JDynAlloyConfig
				.getInstance().getBuiltInModules().contains("JMap"))
				&& java_util_Map_is_registered) {

			bff.append("pred updateMapPost[\n");
			bff.append("  f1:univ->univ->univ,\n");
			bff.append("  f0:univ->univ->univ,\n");
			bff.append("  map:univ, \n");
			bff.append("  entries:univ->univ\n");
			bff.append("]{ \n");
			bff.append("  f1 = f0 ++ (map->entries) \n");
			bff.append("} \n\n");

			bff.append("action updateMap[\n");
			bff.append("  Map_entries:" + javaUtilPackage()
			+ "Map -> (univ set -> lone univ),\n");
			bff.append("  map:" + javaUtilPackage()
			+ "Map,entries: univ->univ\n");
			bff.append("]{ \n");

			bff.append("  pre{TruePred[]} \n");
			bff
			.append("  post{updateMapPost[Map_entries',Map_entries,map,entries]} \n");
			bff.append("}\n");
		}
		// updateList

		//		if ((JDynAlloyConfig.getInstance().getBuiltInModules().isEmpty() || JDynAlloyConfig
		//				.getInstance().getBuiltInModules().contains("JList"))
		//				&& java_util_List_is_registered) {
		//
		//			bff.append("pred updateListPost[\n");
		//			bff.append("  f1:univ->(seq univ),\n");
		//			bff.append("  f0:univ->(seq univ),\n");
		//			bff.append("  list:univ, \n");
		//			bff.append("  entries:seq univ\n");
		//			bff.append("]{ \n");
		//			bff.append("  f1 = f0 ++ (list->entries) \n");
		//			bff.append("} \n\n");
		//
		//			bff.append("action updateList[\n");
		//			bff.append("  List_contains:" + javaUtilPackage()
		//					+ "List->(seq univ),\n");
		//			bff.append("  list:" + javaUtilPackage() + "List,\n");
		//			bff.append("  entries: seq univ\n");
		//			bff.append("]{\n");
		//			bff.append("  pre{TruePred[]} \n");
		//			bff
		//					.append("  post{updateListPost[List_contains',List_contains,list,entries]} \n");
		//			bff.append("}\n");
		//			bff.append("\n");
		//		}

		// custom relational override
		bff.append("fun rel_override[\n");
		bff.append("  r:univ->univ,\n");
		bff.append("  k:univ, \n");
		bff.append("  v:univ\n");
		bff.append("]: univ->univ { \n");
		bff.append("  r - (k->univ) + (k->v) \n");
		bff.append("} \n\n");

		// boolean operations

		bff.append("fun Not[a: boolean]: boolean {\n");
		bff.append("    (a=true) => false else true \n");
		bff.append("}\n");

		bff.append("fun Or[a: boolean, b: boolean]: boolean {\n");
		bff.append("    (a=true or b=true) => true else false\n");
		bff.append("}\n");

		bff.append("fun And[a: boolean, b: boolean]: boolean {\n");
		bff.append("    (a=true and b=true) => true else false\n");
		bff.append("}\n");

		bff.append("fun Xor[a: boolean, b: boolean]: boolean {\n");
		bff
		.append("    ((a=true and b=false) or (a=false and b=true)) => true else false\n");
		bff.append("}\n");

		bff
		.append("fun AdderCarry[a: boolean, b: boolean, cin: boolean]: boolean {\n");
		bff.append("    Or[ And[a,b], And[cin, Xor[a,b]]] \n");
		bff.append("}\n");

		bff
		.append("fun AdderSum[a: boolean, b: boolean, cin: boolean]: boolean {\n");
		bff.append("    Xor[Xor[a, b], cin]\n");
		bff.append("}\n");

		// updateVariable
		PredicateFormula updateVarPre = PredicateFormula
				.buildPredicate("TruePred");
		PredicateFormula updateVarPost = PredicateFormula.buildPredicate("equ",
				new AlloyVariable("l", true), new AlloyVariable("r"));
		AlloyTyping updateVarTyping = new AlloyTyping();
		updateVarTyping.put(new AlloyVariable("l"), "univ");
		updateVarTyping.put(new AlloyVariable("r"), "univ");
		List<VariableId> updateVarPs = Arrays.<VariableId> asList(
				new VariableId("l"), new VariableId("r"));
		ActionDeclaration updateVar = new ActionDeclaration("updateVariable",
				updateVarPs, updateVarPre, updateVarPost, updateVarTyping);

		// updateField
		bff.append("pred updateFieldPost[\n");
		bff.append("  f1:univ->univ,\n");
		bff.append("  f0:univ->univ,\n");
		bff.append("  l:univ,\n");
		bff.append("  r:univ\n");
		bff.append("]{ \n");
		bff.append("  (r=none) => f1=f0-(l->univ) else f1 = f0 ++ (l->r) \n");
		bff.append("} \n\n");

		PredicateFormula updateFieldPre = PredicateFormula
				.buildPredicate("TruePred");
		PredicateFormula updateFieldPost = PredicateFormula.buildPredicate(
				"updateFieldPost", new AlloyVariable("f", true),
				new AlloyVariable("f"), new AlloyVariable("l"),
				new AlloyVariable("r"));
		AlloyTyping updateFieldTyping = new AlloyTyping();
		updateFieldTyping.put(new AlloyVariable("l"), "univ");
		updateFieldTyping.put(new AlloyVariable("r"), "univ");
		updateFieldTyping.put(new AlloyVariable("f"), "univ->univ");
		List<VariableId> updateFieldPs = Arrays.<VariableId> asList(
				new VariableId("l"), new VariableId("f"), new VariableId("r"));
		ActionDeclaration updateField = new ActionDeclaration("updateField",
				updateFieldPs, updateFieldPre, updateFieldPost,
				updateFieldTyping);

		// havocVar
		bff.append("pred havocVarPost[u:univ]{} \n");
		PredicateFormula havocVarPre = PredicateFormula
				.buildPredicate("TruePred");
		PredicateFormula havocVarPost = PredicateFormula.buildPredicate(
				"havocVarPost", new AlloyVariable("v", true));
		AlloyTyping havocVarTyping = new AlloyTyping();
		havocVarTyping.put(new AlloyVariable("v"), "univ");
		List<VariableId> havocVarPs = Arrays
				.<VariableId> asList(new VariableId("v"));
		ActionDeclaration havocVar = new ActionDeclaration("havocVariable",
				havocVarPs, havocVarPre, havocVarPost, havocVarTyping);

		// havocVariable2
		bff.append("pred havocVariable2Post[u:univ->univ]{}\n");
		bff.append("action havocVariable2[u:univ->univ]{\n");
		bff.append(" pre{TruePred[]}\n");
		bff.append(" post{havocVariable2Post[u']}\n");
		bff.append("}\n");
		bff.append("\n");

		// havocVariable3

		//seq univ seq univ seq univ seq univ
		bff.append("pred havocVariable3Post[u:univ->(seq univ)]{}\n");
		bff.append("action havocVariable3[u:univ->(seq univ)]{\n");
		bff.append(" pre{TruePred[]}\n");
		bff.append(" post{havocVariable3Post[u']}\n");
		bff.append("}\n");
		bff.append("\n");

		// havocField
		bff.append("pred havocFieldPost[f0,f1: univ->univ, u:univ]{ \n");
		bff.append("  u<:f0 = u<:f1 \n");
		bff.append("  some u.f1  \n");
		bff.append("} \n");
		bff.append("\n");


		//mfrias 17-03-2013: Functions added for specifying the size of a java.util.Set		
		if (moduleIds.contains("java_util_Set")) {
			if (!moduleIds.contains("JavaPrimitiveIntegerValue")) {
				bff.append("fun fun_alloy_int_java_util_set_size[\n");
				bff.append("   s : java_util_Set,\n");
				bff.append("   java_util_Set_elems : java_util_Set -> univ] : Int { \n");
				bff.append("      #(s.java_util_Set_elems)\n");
				bff.append("} \n");
				bff.append("\n");
			} else {
				bff.append("fun fun_java_primitive_integer_value_java_util_set_size[\n");
				bff.append("   s : java_util_Set,\n");
				bff.append("   java_util_Set_elems : java_util_Set -> univ] : JavaPrimitiveIntegerValue { \n");
				bff.append("      fun_java_primitive_integer_value_size_of[s.java_util_Set_elems]\n");
				bff.append("} \n");
				bff.append("\n");
			}

			bff.append("fun fun_java_util_set_contains[\n");
			bff.append("   s : java_util_Set, o : java_lang_Object + null,\n");
			bff.append("   java_util_Set_elems : java_util_Set -> univ] : boolean { \n");
			bff.append("      (o in s.java_util_Set_elems) => true else false\n");
			bff.append("} \n");
			bff.append("\n");
		}

		// havocArrayContents
		if ((moduleIds.contains("java_lang_ObjectArray") || moduleIds.contains("java_lang_IntArray") || moduleIds.contains("java_lang_CharArray")) && !moduleIds.contains("JavaPrimitiveIntegerValue")){
			bff.append("pred havocArrayContentsPost[array:  univ,\n");
			bff.append("                            domain: set univ,\n");
			bff.append("                            Array_0: univ -> (Int set -> lone univ),\n");
			bff.append("                            Array_1: univ -> (Int set -> lone univ)\n");
			bff.append("                           ] {\n");
			bff.append("  Array_1 - (array->(domain->univ)) = Array_0 - (array->(domain->univ))\n");
			bff.append("  (array.Array_1).univ = (array.Array_0).univ\n");
			bff.append("}");
			bff.append("action havocArrayContents[\n");
			bff.append("                          array: univ,\n");
			bff.append("                          domain: set univ,\n");
			bff.append("                          Array: univ -> (Int set -> lone univ)\n");
			bff.append("                         ] {\n");
			bff.append("pre { TruePred[] }\n");
			bff.append("post { havocArrayContentsPost[\n");
			bff.append("                              array,\n");
			bff.append("                              domain,\n");
			bff.append("                              Array,\n");
			bff.append("                              Array'\n");
			bff.append("                             ]\n");
			bff.append("     }\n");
			bff.append("}\n");
		} else if (moduleIds.contains("java_lang_ObjectArray") || moduleIds.contains("java_lang_IntArray") || moduleIds.contains("java_lang_LongArray") || moduleIds.contains("java_lang_CharArray")) {
			bff.append("pred havocArrayContentsPost[array:  univ,\n");
			bff.append("                            domain: set univ,\n");
			bff.append("                            Array_0: univ -> (JavaPrimitiveIntegerValue set -> lone univ),\n");
			bff.append("                            Array_1: univ -> (JavaPrimitiveIntegerValue set -> lone univ)\n");
			bff.append("                           ] {\n");
			bff.append("  Array_1 - (array->(domain->univ)) = Array_0 - (array->(domain->univ))\n");
			bff.append("  (array.Array_1).univ = (array.Array_0).univ\n");
			bff.append("}");
			bff.append("action havocArrayContents[\n");
			bff.append("                          array: univ,\n");
			bff.append("                          domain: set univ,\n");
			bff.append("                          Array: univ -> (JavaPrimitiveIntegerValue set -> lone univ)\n");
			bff.append("                         ] {\n");
			bff.append("pre { TruePred[] }\n");
			bff.append("post { havocArrayContentsPost[\n");
			bff.append("                              array,\n");
			bff.append("                              domain,\n");
			bff.append("                              Array,\n");
			bff.append("                              Array'\n");
			bff.append("                             ]\n");
			bff.append("     }\n");
			bff.append("}\n");
		}

		// havocFieldContents
		bff.append("pred havocFieldContentsPost[target: univ, \n");
		bff.append("                            field_0: univ -> univ, \n");
		bff.append("                            field_1: univ -> univ] { \n");
		bff.append("  field_1 - (target->univ) = field_0 - (target->univ) \n");
		bff.append("}\n");
		bff.append("\n");
		bff.append("action havocFieldContents[target: univ, \n");
		bff.append("                          field: univ->univ] {\n");
		bff.append("  pre  { TruePred[] }\n");
		bff.append("  post { havocFieldContentsPost[target,field,field'] }\n");
		bff.append("}\n");
		bff.append("\n");


		// havocListSeq
		bff.append("pred havocListSeqPost[target: univ,\n");
		bff.append("                            field_0: univ -> Int -> univ, \n");
		bff.append("                            field_1: univ -> Int -> univ] { \n");
		bff.append("  field_1 - (target->Int->univ) = field_0 - (target->Int->univ) \n");
		bff.append("}\n");
		bff.append("\n");
		bff.append("action havocListSeq[target: univ,\n");
		bff.append("                          field: univ->Int->univ] {\n");
		bff.append("  pre  { TruePred[] }\n");
		bff.append("  post { havocListSeqPost[target,field,field'] }\n");
		bff.append("}\n");
		bff.append("\n");



		PredicateFormula havocFieldPre = PredicateFormula
				.buildPredicate("TruePred");
		PredicateFormula havocFieldPost = PredicateFormula.buildPredicate(
				"havocFieldPost", new AlloyVariable("f", false),
				new AlloyVariable("f", true), new AlloyVariable("u"));
		AlloyTyping havocFieldTyping = new AlloyTyping();
		havocFieldTyping.put(new AlloyVariable("f"), "univ->univ");
		havocFieldTyping.put(new AlloyVariable("u"), "univ");
		List<VariableId> havocFieldPs = Arrays.<VariableId> asList(
				new VariableId("f"), new VariableId("u"));
		ActionDeclaration havocField = new ActionDeclaration("havocField",
				havocFieldPs, havocFieldPre, havocFieldPost, havocFieldTyping);

		// in
		bff.append("pred pred_in[n: univ, t: set univ] { n in t } \n");
		bff.append("\n");

		// instanceof
		bff.append("pred instanceOf[n: univ, t: set univ] { n in t } \n");
		bff.append("\n");

		// isCasteableTo
		bff
		.append("pred isCasteableTo[n: univ, t: set univ] { (n in t) or (n = null) } \n");
		bff.append("\n");

		// getUnusedObject
		bff.append("pred getUnusedObjectPost[\n");
		bff.append("  usedObjects1:set " + javaLangPackage() + "Object, \n");
		bff.append("  usedObjects0:set " + javaLangPackage() + "Object,\n");
		bff.append("  n1: " + javaLangPackage() + "Object+null\n");
		bff.append("]{ \n");
		bff.append("  n1 !in usedObjects0 \n");
		bff.append("  usedObjects1 = usedObjects0 + (n1)\n");
		bff.append("} \n\n");

		PredicateFormula getUnusedObjectPre = PredicateFormula
				.buildPredicate("TruePred");
		PredicateFormula getUnusedObjectPost = PredicateFormula.buildPredicate(
				"getUnusedObjectPost", new AlloyVariable("usedObjects", true),
				new AlloyVariable("usedObjects"), new AlloyVariable("n", true));

		AlloyTyping getUnusedObjectTyping = new AlloyTyping();
		getUnusedObjectTyping.put(new AlloyVariable("n"), javaLangPackage()
				+ "Object+null");
		getUnusedObjectTyping.put(new AlloyVariable("usedObjects"), "set "
				+ javaLangPackage() + "Object");

		List<VariableId> getUnusedObjectPs = Arrays.<VariableId> asList(
				new VariableId("n"), new VariableId("usedObjects"));
		ActionDeclaration getUnusedObject = new ActionDeclaration(
				"getUnusedObject", getUnusedObjectPs, getUnusedObjectPre,
				getUnusedObjectPost, getUnusedObjectTyping);

		HashSet<ActionDeclaration> actionDeclarations = new HashSet<ActionDeclaration>(
				Arrays.<ActionDeclaration> asList(new ActionDeclaration[] {
						updateVar, updateField, havocVar, havocField,
						getUnusedObject }));


		if (moduleIds.contains("JavaPrimitiveFloatValue")){
			bff.append("\n");
			bff.append("fun fun_java_lang_float_isNaN[f : JavaPrimitiveFloatValue] : boolean {\n");
			bff.append("	pred_java_primitive_float_value_is_NaN[f] => true else false\n");
			bff.append("}\n");
		}

		//mfrias: prelude for object array with or without Java Integers

		if (moduleIds.contains("java_lang_ObjectArray") && !moduleIds.contains("JavaPrimitiveIntegerValue")) {
			// Arrays
			bff.append("pred updateArrayPost[\n");
			bff.append("     Object_Array1: " + javaLangPackage() + "ObjectArray -> (Int set -> lone univ), \n");
			bff.append("     Object_Array0: " + javaLangPackage() + "ObjectArray -> (Int set -> lone univ),\n");
			bff.append("     array: " + javaLangPackage() + "ObjectArray+null,\n");
			bff.append("     index:Int,\n");
			bff.append("     elem:univ\n");
			bff.append("]{ \n");
			bff.append("     Object_Array1 = Object_Array0 ++ \n");
			bff.append("     (array->(array.Object_Array0++(index->elem)))\n");
			bff.append("} \n");
			bff.append("\n");


			bff.append("fun arrayAccess[\n");
			bff.append("     Object_Array:" + javaLangPackage() + "ObjectArray, \n");
			bff.append("     array_field: " + javaLangPackage() + "ObjectArray -> (Int set -> lone (" + javaLangPackage() + "Object + null)), \n");
			bff.append("     index: Int\n");
			bff.append("]: univ {\n");
			bff.append("     some (Object_Array.array_field)[index] implies (Object_Array.array_field)[index] else null\n");
			bff.append("}\n");
			bff.append("\n");



			bff.append("fun arrayLength[\n");
			bff.append("     array: " + javaLangPackage() + "ObjectArray+null,\n");
			bff.append("     length_field: " + javaLangPackage() + "ObjectArray -> one Int\n");
			bff.append("]: Int {\n");
			bff.append("     array.length_field\n");
			bff.append("}\n");
			bff.append("\n");


			bff.append("fun arrayElements[\n");
			bff.append("  array_contents:" + javaLangPackage()
			+ "ObjectArray->(Int set -> lone univ), \n");
			bff.append("  array: " + javaLangPackage() + "ObjectArray+null\n");
			bff.append("]: set univ {\n");
			bff.append("  Int.(array.array_contents)\n");
			bff.append("}\n");
			bff.append("\n");

			PredicateFormula updateArrayPre = PredicateFormula
					.buildPredicate("TruePred");
			PredicateFormula updateArrayPost = PredicateFormula.buildPredicate(
					"updateArrayPost", new AlloyVariable("Object_Array", true),
					new AlloyVariable("Object_Array"), new AlloyVariable(
							"array"), new AlloyVariable("index"),
					new AlloyVariable("elem"));

			AlloyTyping updateArrayTyping = new AlloyTyping();
			updateArrayTyping.put(new AlloyVariable("Object_Array"), ""
					+ javaLangPackage() + "ObjectArray->(Int set -> lone univ)");
			updateArrayTyping.put(new AlloyVariable("array"), ""
					+ javaLangPackage() + "ObjectArray+null");
			updateArrayTyping.put(new AlloyVariable("index"), "Int");
			updateArrayTyping.put(new AlloyVariable("elem"), "univ");
			List<VariableId> updateArrayPs = Arrays.<VariableId> asList(
					new VariableId("Object_Array"), new VariableId("array"),
					new VariableId("index"), new VariableId("elem"));

			ActionDeclaration updateArray = new ActionDeclaration(
					"updateArray", updateArrayPs, updateArrayPre,
					updateArrayPost, updateArrayTyping);

			actionDeclarations.add(updateArray);


		} else if (moduleIds.contains("java_lang_ObjectArray")) {


			bff.append("pred updateArrayPost[\n");
			bff.append("  Object_Array1: " + javaLangPackage()
			+ "ObjectArray -> (JavaPrimitiveIntegerValue set -> lone univ), \n");
			bff.append("  Object_Array0: " + javaLangPackage()
			+ "ObjectArray -> (JavaPrimitiveIntegerValue set -> lone univ),\n");
			bff.append("  array: " + javaLangPackage() + "ObjectArray+null,\n");
			bff.append("  index:JavaPrimitiveIntegerValue,\n");
			bff.append("  elem:univ\n");
			bff.append("]{ \n");
			bff.append("  Object_Array1 = Object_Array0 ++ \n");
			bff.append("  (array->(array.Object_Array0++(index->elem)))\n");
			bff.append("} \n");
			bff.append("\n");


			bff.append("fun arrayAccess[\n");
			bff.append("     Object_Array:" + javaLangPackage() + "ObjectArray, \n");
			bff.append("     array_field: " + javaLangPackage() + "ObjectArray -> (JavaPrimitiveIntegerValue set -> lone (" + javaLangPackage() + "Object + null)), \n");
			bff.append("     index : JavaPrimitiveIntegerValue\n");
			bff.append("]: univ {\n");
			bff.append("     some (Object_Array.array_field)[index] implies (Object_Array.array_field)[index] else null\n");
			bff.append("}\n");
			bff.append("\n");



			//mfrias-mffrias-10/11/2012: put removed parameter back

			bff.append("fun arrayLength[\n");
			bff.append("  array: " + javaLangPackage() + "ObjectArray+null,\n");
			bff.append("  length_field: " + javaLangPackage() + "ObjectArray -> one JavaPrimitiveIntegerValue \n");
			bff.append("]: JavaPrimitiveIntegerValue {\n");
			bff.append("  array.length_field\n");
			bff.append("}\n");
			bff.append("\n");

			bff.append("fun arrayElements[\n");
			bff.append("  array_contents:" + javaLangPackage()
			+ "ObjectArray -> (JavaPrimitiveIntegerValue set -> lone univ), \n");
			bff.append("  array: " + javaLangPackage() + "ObjectArray+null\n");
			bff.append("]: set univ {\n");
			bff.append("  JavaPrimitiveIntegerValue.(array.array_contents)\n");
			bff.append("}\n");
			bff.append("\n");

			PredicateFormula updateArrayPre = PredicateFormula
					.buildPredicate("TruePred");
			PredicateFormula updateArrayPost = PredicateFormula.buildPredicate(
					"updateArrayPost", new AlloyVariable("Object_Array", true),
					new AlloyVariable("Object_Array"), new AlloyVariable(
							"array"), new AlloyVariable("index"),
					new AlloyVariable("elem"));

			AlloyTyping updateArrayTyping = new AlloyTyping();
			updateArrayTyping.put(new AlloyVariable("Object_Array"), ""
					+ javaLangPackage() + "ObjectArray -> (JavaPrimitiveIntegerValue set -> lone univ)");
			updateArrayTyping.put(new AlloyVariable("array"), ""
					+ javaLangPackage() + "ObjectArray+null");
			updateArrayTyping.put(new AlloyVariable("index"), "JavaPrimitiveIntegerValue");
			updateArrayTyping.put(new AlloyVariable("elem"), "univ");
			List<VariableId> updateArrayPs = Arrays.<VariableId> asList(
					new VariableId("Object_Array"), new VariableId("array"),
					new VariableId("index"), new VariableId("elem"));

			ActionDeclaration updateArray = new ActionDeclaration(
					"updateArray", updateArrayPs, updateArrayPre,
					updateArrayPost, updateArrayTyping);

			actionDeclarations.add(updateArray);
		}





		//mfrias: prelude for Int arrays with or without Java Integers

		if (moduleIds.contains("java_lang_IntArray") && !moduleIds.contains("JavaPrimitiveIntegerValue")) {
			// Arrays
			bff.append("pred updateArrayPost[\n");
			bff.append("  Int_Array1: " + javaLangPackage()
			+ "IntArray -> (Int set -> lone Int), \n");
			bff.append("  Int_Array0: " + javaLangPackage()
			+ "IntArray -> (Int set -> lone Int),\n");
			bff.append("  array: " + javaLangPackage() + "IntArray+null,\n");
			bff.append("  index:Int,\n");
			bff.append("  elem:Int\n");
			bff.append("]{ \n");
			bff.append("  Int_Array1 = Int_Array0 ++ \n");
			bff.append("  (array->(array.Int_Array0++(index->Int)))\n");
			bff.append("} \n");
			bff.append("\n");

			bff.append("fun arrayAccess[\n");
			bff.append("  Int_Array:" + javaLangPackage() + "IntArray, \n");
			bff.append("  array_field:" + javaLangPackage() + "IntArray -> (Int set -> lone Int),\n");
			bff.append("  index: Int\n");
			bff.append("]: Int {\n");
			bff.append("  some (Int_Array.array_field)[index] implies (Int_Array.array_field)[index] else 0\n");
			bff.append("}\n");
			bff.append("\n");


			//mfrias-mffrias-10/11/2012: put removed parameter back

			bff.append("fun arrayLength[\n");
			bff.append("  array: " + javaLangPackage() + "IntArray+null,\n");
			bff.append("  length_field: " + javaLangPackage() + "IntArray -> one Int\n");
			bff.append("]: Int {\n");
			bff.append("  array.length_field\n");
			bff.append("}\n");
			bff.append("\n");

			bff.append("fun arrayElements[\n");
			bff.append("  array_contents:" + javaLangPackage()
			+ "IntArray->(Int set -> lone Int), \n");
			bff.append("  array: " + javaLangPackage() + "IntArray+null\n");
			bff.append("]: set Int {\n");
			bff.append("  Int.(array.array_contents)\n");
			bff.append("}\n");
			bff.append("\n");

			PredicateFormula updateArrayPre = PredicateFormula
					.buildPredicate("TruePred");
			PredicateFormula updateArrayPost = PredicateFormula.buildPredicate(
					"updateArrayPost", new AlloyVariable("Int_Array", true),
					new AlloyVariable("Int_Array"), new AlloyVariable(
							"array"), new AlloyVariable("index"),
					new AlloyVariable("elem"));

			AlloyTyping updateArrayTyping = new AlloyTyping();
			updateArrayTyping.put(new AlloyVariable("Int_Array"), ""
					+ javaLangPackage() + "IntArray->(Int set -> lone Int)");
			updateArrayTyping.put(new AlloyVariable("array"), ""
					+ javaLangPackage() + "IntArray+null");
			updateArrayTyping.put(new AlloyVariable("index"), "Int");
			updateArrayTyping.put(new AlloyVariable("elem"), "Int");
			List<VariableId> updateArrayPs = Arrays.<VariableId> asList(
					new VariableId("Int_Array"), new VariableId("array"),
					new VariableId("index"), new VariableId("elem"));

			ActionDeclaration updateArray = new ActionDeclaration(
					"updateArray", updateArrayPs, updateArrayPre,
					updateArrayPost, updateArrayTyping);

			actionDeclarations.add(updateArray);


		} else if (moduleIds.contains("java_lang_IntArray")) {


			bff.append("pred updateArrayPost[\n");
			bff.append("  Int_Array1: " + javaLangPackage()
			+ "IntArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveIntegerValue), \n");
			bff.append("  Int_Array0: " + javaLangPackage()
			+ "IntArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveIntegerValue),\n");
			bff.append("  array: " + javaLangPackage() + "IntArray+null,\n");
			bff.append("  index:JavaPrimitiveIntegerValue,\n");
			bff.append("  elem:JavaPrimitiveIntegerValue\n");
			bff.append("]{ \n");
			bff.append("  Int_Array1 = Int_Array0 ++ \n");
			bff.append("  (array->(array.Int_Array0++(index->elem)))\n");
			bff.append("} \n");
			bff.append("\n");


			bff.append("fun arrayAccess[\n");
			bff.append("  Int_Array:" + javaLangPackage() + "IntArray, \n");
			bff.append("  array_field:" + javaLangPackage() + "IntArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveIntegerValue), \n");
			bff.append("  index: JavaPrimitiveIntegerValue\n");
			bff.append("]: JavaPrimitiveIntegerValue {\n");
			bff.append("  some (Int_Array.array_field)[index] implies (Int_Array.array_field)[index] else JavaPrimitiveIntegerLiteral0\n");
			bff.append("}\n");
			bff.append("\n");


			if (!moduleIds.contains("JavaPrimitiveIntegerLiteral0")){
				moduleIds.add("JavaPrimitiveIntegerLiteral0");
				bff.append("one sig JavaPrimitiveIntegerLiteral0 extends JavaPrimitiveIntegerValue {}\n");
			}

			bff.append("fun arrayLength[\n");
			bff.append("  array: " + javaLangPackage() + "IntArray+null,\n");
			bff.append("  length_field: " + javaLangPackage() + "IntArray -> one JavaPrimitiveIntegerValue\n");
			bff.append("]: JavaPrimitiveIntegerValue {\n");
			bff.append("  array.length_field\n");
			bff.append("}\n");
			bff.append("\n");

			bff.append("fun arrayElements[\n");
			bff.append("  array_contents:" + javaLangPackage()
			+ "IntArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveIntegerValue), \n");
			bff.append("  array: " + javaLangPackage() + "IntArray+null\n");
			bff.append("]: set JavaPrimitiveIntegerValue {\n");
			bff.append("  JavaPrimitiveIntegerValue.(array.array_contents)\n");
			bff.append("}\n");
			bff.append("\n");

			PredicateFormula updateArrayPre = PredicateFormula
					.buildPredicate("TruePred");
			PredicateFormula updateArrayPost = PredicateFormula.buildPredicate(
					"updateArrayPost", new AlloyVariable("Int_Array", true),
					new AlloyVariable("Int_Array"), new AlloyVariable(
							"array"), new AlloyVariable("index"),
					new AlloyVariable("elem"));

			AlloyTyping updateArrayTyping = new AlloyTyping();
			updateArrayTyping.put(new AlloyVariable("Int_Array"), ""
					+ javaLangPackage() + "IntArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveIntegerValue)");
			updateArrayTyping.put(new AlloyVariable("array"), ""
					+ javaLangPackage() + "IntArray+null");
			updateArrayTyping.put(new AlloyVariable("index"), "JavaPrimitiveIntegerValue");
			updateArrayTyping.put(new AlloyVariable("elem"), "univ");
			List<VariableId> updateArrayPs = Arrays.<VariableId> asList(
					new VariableId("Int_Array"), new VariableId("array"),
					new VariableId("index"), new VariableId("elem"));

			ActionDeclaration updateArray = new ActionDeclaration(
					"updateArray", updateArrayPs, updateArrayPre,
					updateArrayPost, updateArrayTyping);

			actionDeclarations.add(updateArray);
		}


		if (moduleIds.contains("java_lang_CharArray") && !moduleIds.contains("JavaPrimitiveIntegerValue")) {
			throw new JDynAlloyException("Attempting to use LongArray type but Java arithmetic is not enabled.");
		} else if (moduleIds.contains("java_lang_CharArray")){
			bff.append("pred updateArrayPost[\n");
			bff.append("  Char_Array1: " + javaLangPackage()
			+ "CharArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveCharValue), \n");
			bff.append("  Char_Array0: " + javaLangPackage()
			+ "CharArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveCharValue),\n");
			bff.append("  array: " + javaLangPackage() + "CharArray+null,\n");
			bff.append("  index:JavaPrimitiveIntegerValue,\n");
			bff.append("  elem:JavaPrimitiveCharValue\n");
			bff.append("]{ \n");
			bff.append("  Char_Array1 = Char_Array0 ++ \n");
			bff.append("  (array->(array.Char_Array0++(index->elem)))\n");
			bff.append("} \n");
			bff.append("\n");


			bff.append("fun arrayAccess[\n");
			bff.append("  Char_Array:" + javaLangPackage() + "CharArray, \n");
			bff.append("  array_field:" + javaLangPackage() + "CharArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveCharValue), \n");
			bff.append("  index: JavaPrimitiveIntegerValue\n");
			bff.append("]: JavaPrimitiveCharValue {\n");
			bff.append("  some (Char_Array.array_field)[index] implies (Char_Array.array_field)[index] else JavaPrimitiveCharLiteralu0000\n");
			bff.append("}\n");
			bff.append("\n");


			if (!moduleIds.contains("JavaPrimitiveCharLiteralu0000")) {
				moduleIds.add("JavaPrimitiveCharLiteralu0000");
				bff.append("one sig JavaPrimitiveCharLiteralu0000 extends JavaPrimitiveCharValue {}\n");
			}

			bff.append("fun arrayLength[\n");
			bff.append("  array: " + javaLangPackage() + "CharArray+null,\n");
			bff.append("  length_field: " + javaLangPackage() + "CharArray -> one JavaPrimitiveIntegerValue\n");
			bff.append("]: JavaPrimitiveIntegerValue {\n");
			bff.append("  array.length_field\n");
			bff.append("}\n");
			bff.append("\n");

			bff.append("fun arrayElements[\n");
			bff.append("  array_contents:" + javaLangPackage()
			+ "CharArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveCharValue), \n");
			bff.append("  array: " + javaLangPackage() + "CharArray+null\n");
			bff.append("]: set JavaPrimitiveCharValue {\n");
			bff.append("  JavaPrimitiveIntegerValue.(array.array_contents)\n");
			bff.append("}\n");
			bff.append("\n");

			PredicateFormula updateArrayPre = PredicateFormula
					.buildPredicate("TruePred");
			PredicateFormula updateArrayPost = PredicateFormula.buildPredicate(
					"updateArrayPost", new AlloyVariable("Char_Array", true),
					new AlloyVariable("Char_Array"), new AlloyVariable(
							"array"), new AlloyVariable("index"),
					new AlloyVariable("elem"));

			AlloyTyping updateArrayTyping = new AlloyTyping();
			updateArrayTyping.put(new AlloyVariable("Char_Array"), ""
					+ javaLangPackage() + "CharArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveCharValue)");
			updateArrayTyping.put(new AlloyVariable("array"), ""
					+ javaLangPackage() + "CharArray+null");
			updateArrayTyping.put(new AlloyVariable("index"), "JavaPrimitiveIntegerValue");
			updateArrayTyping.put(new AlloyVariable("elem"), "univ");
			List<VariableId> updateArrayPs = Arrays.<VariableId> asList(
					new VariableId("Char_Array"), new VariableId("array"),
					new VariableId("index"), new VariableId("elem"));

			ActionDeclaration updateArray = new ActionDeclaration(
					"updateArray", updateArrayPs, updateArrayPre,
					updateArrayPost, updateArrayTyping);

			actionDeclarations.add(updateArray);
		}

		//mfrias: prelude for Long arrays (only defined when Java Integers are enabled)

		if (moduleIds.contains("java_lang_LongArray") && !moduleIds.contains("JavaPrimitiveIntegerValue")) {
			throw new JDynAlloyException("Attempting to use LongArray type but Java arithmetic is not enabled.");

		} else if (moduleIds.contains("java_lang_LongArray")) {

			bff.append("pred updateArrayPost[\n");
			bff.append("  Long_Array1: " + javaLangPackage()
			+ "LongArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveLongValue), \n");
			bff.append("  Long_Array0: " + javaLangPackage()
			+ "LongArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveLongValue),\n");
			bff.append("  array: " + javaLangPackage() + "LongArray+null,\n");
			bff.append("  index:JavaPrimitiveIntegerValue,\n");
			bff.append("  elem:JavaPrimitiveLongValue\n");
			bff.append("]{ \n");
			bff.append("  Long_Array1 = Long_Array0 ++ \n");
			bff.append("  (array->(array.Long_Array0++(index->elem)))\n");
			bff.append("} \n");
			bff.append("\n");


			bff.append("fun arrayAccess[\n");
			bff.append("  Long_Array:" + javaLangPackage() + "LongArray, \n");
			bff.append("  array_field:" + javaLangPackage() + "LongArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveLongValue), \n");
			bff.append("  index: JavaPrimitiveIntegerValue\n");
			bff.append("]: JavaPrimitiveLongValue {\n");
			bff.append("  some (Long_Array.array_field)[index] implies (Long_Array.array_field)[index] else JavaPrimitiveLongLiteral0\n");
			bff.append("}\n");
			bff.append("\n");


			if (!moduleIds.contains("JavaPrimitiveLongLiteral0")){
				moduleIds.add("JavaPrimitiveLongLiteral0");
				bff.append("one sig JavaPrimitiveLongLiteral0 extends JavaPrimitiveLongValue {}\n");
			}

			bff.append("fun arrayLength[\n");
			bff.append("  array: " + javaLangPackage() + "LongArray+null,\n");
			bff.append("  length_field: " + javaLangPackage() + "LongArray -> one JavaPrimitiveIntegerValue\n");
			bff.append("]: JavaPrimitiveIntegerValue {\n");
			bff.append("  array.length_field\n");
			bff.append("}\n");
			bff.append("\n");

			bff.append("fun arrayElements[\n");
			bff.append("  array_contents:" + javaLangPackage()
			+ "LongArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveLongValue), \n");
			bff.append("  array: " + javaLangPackage() + "LongArray+null\n");
			bff.append("]: set JavaPrimitiveLongValue {\n");
			bff.append("  JavaPrimitiveIntegerValue.(array.array_contents)\n");
			bff.append("}\n");
			bff.append("\n");

			PredicateFormula updateArrayPre = PredicateFormula
					.buildPredicate("TruePred");
			PredicateFormula updateArrayPost = PredicateFormula.buildPredicate(
					"updateArrayPost", new AlloyVariable("Long_Array", true),
					new AlloyVariable("Long_Array"), new AlloyVariable(
							"array"), new AlloyVariable("index"),
					new AlloyVariable("elem"));

			AlloyTyping updateArrayTyping = new AlloyTyping();
			updateArrayTyping.put(new AlloyVariable("Long_Array"), ""
					+ javaLangPackage() + "LongArray -> (JavaPrimitiveIntegerValue set -> lone JavaPrimitiveLongValue)");
			updateArrayTyping.put(new AlloyVariable("array"), ""
					+ javaLangPackage() + "LongArray+null");
			updateArrayTyping.put(new AlloyVariable("index"), "JavaPrimitiveIntegerValue");
			updateArrayTyping.put(new AlloyVariable("elem"), "univ");
			List<VariableId> updateArrayPs = Arrays.<VariableId> asList(
					new VariableId("Long_Array"), new VariableId("array"),
					new VariableId("index"), new VariableId("elem"));

			ActionDeclaration updateArray = new ActionDeclaration(
					"updateArray", updateArrayPs, updateArrayPre,
					updateArrayPost, updateArrayTyping);

			actionDeclarations.add(updateArray);
		}



		DynalloyModule preludeModule = new DynalloyModule("prelude", null, bff
				.toString(), actionDeclarations, Collections
				.<ProgramDeclaration> emptySet(), Collections
				.<AssertionDeclaration> emptySet(), new AlloyTyping(), new ArrayList<AlloyFormula>());

		return preludeModule;
	}

	/**
	 * This function is used to make qualified names for java_lang_Object and
	 * java_lang_SystemArray. Depends on
	 * 
	 * @return
	 */
	private static String javaLangPackage() {
		if (JDynAlloyConfig.getInstance().getUseQualifiedNamesForJTypes()) {
			return "java_lang_";
		} else {
			return "";
		}
	}

	private static String javaUtilPackage() {
		if (JDynAlloyConfig.getInstance().getUseQualifiedNamesForJTypes()) {
			return "java_util_";
		} else {
			return "";
		}
	}

	private static boolean reach_JMLObjectSet = false;

	private static boolean integer_complement = false;

	//	private static boolean java_int_lang_systemArray_is_registered = true;

	//	private static boolean java_JavaPrimitiveIntegerValue_lang_systemArray_is_registered = true;

	private static boolean java_util_Map_is_registered = true;

	private static boolean java_util_List_is_registered = true;

	private static boolean java_lang_String_is_registered = true;

	private static boolean char_is_registered = true;

	private static boolean java_JavaPrimitiveIntegerValue_lang_systemArray_is_registered;

	private static boolean java_int_lang_systemArray_is_registered;

	public static void register_reach_JMLObjectSet() {
		reach_JMLObjectSet = true;
	}

	public static void unregister_reach_JMLObjectSet() {
		reach_JMLObjectSet = false;
	}

	public static void register_integer_complement() {
		integer_complement = true;
	}


	public static void unregister_JavaPrimitiveIntegerValue_java_lang_SystemArray() {
		java_JavaPrimitiveIntegerValue_lang_systemArray_is_registered = false;
	}

	public static void register_JavaPrimitiveIntegerValue_java_lang_SystemArray() {
		java_JavaPrimitiveIntegerValue_lang_systemArray_is_registered = true;
	}


	public static void unregister_int_java_lang_SystemArray() {
		java_int_lang_systemArray_is_registered = false;
	}

	public static void register_int_java_lang_SystemArray() {
		java_int_lang_systemArray_is_registered = true;
	}

	public static void unregister_java_util_Map() {
		java_util_Map_is_registered = false;
	}

	public static void register_java_util_Map() {
		java_util_Map_is_registered = true;
	}

	public static void unregister_java_util_List() {
		java_util_List_is_registered = false;
	}

	public static void register_java_util_List() {
		java_util_List_is_registered = true;
	}

	public static void unregister_char() {
		char_is_registered = false;
	}

	public static void register_char() {
		char_is_registered = true;
	}

	public static void unregister_java_lang_String() {
		java_lang_String_is_registered = false;
	}

	public static void register_java_lang_String() {
		java_lang_String_is_registered = true;
	}

	public static boolean java_lang_String_is_registered() {
		return java_lang_String_is_registered;
	}
}

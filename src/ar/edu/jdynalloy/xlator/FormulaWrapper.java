package ar.edu.jdynalloy.xlator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ar.edu.jdynalloy.xlator.JType;
import ar.edu.jdynalloy.xlator.JType.SpecialType;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.AlloyVariableComparator;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaMutator;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaPrinter;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.ExpressionMutator;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;
import ar.uba.dc.rfm.alloy.util.QFtransformer;
import ar.uba.dc.rfm.alloy.util.VarCollector;
import ar.uba.dc.rfm.alloy.util.VarSubstitutor;

final class FormulaWrapper {

	private HashSet<AlloyVariable> varsToPrefix;


	static class RenamePreStateVariableMutator extends VarSubstitutor {

		@Override
		protected AlloyExpression getExpr(AlloyVariable v) {
			if (v.isPreStateVar()) {
				String pre_id = v.getVariableId().getString() + "''";
				AlloyVariable pre_v = new AlloyVariable(pre_id);
				return ExprVariable.buildExprVariable(pre_v);
			} else
				return ExprVariable.buildExprVariable(v);
		}
	}

	

	private final JDynAlloyTyping fieldsTyping;

	private JDynAlloyTyping parameterVariables = null;
	private JDynAlloyTyping localVariables = null;

	public FormulaWrapper(String modulePrefix, JDynAlloyTyping fieldsTyping, HashSet<AlloyVariable> varsToPrefix) {
		super();
		this.fieldsTyping = fieldsTyping;
		this.modulePrefix = modulePrefix;
		this.varsToPrefix = varsToPrefix;
	}

	public void bindParameterVariables(JDynAlloyTyping _variableTyping) {
		this.parameterVariables = _variableTyping;
	}

	public void bindLocalVariables(JDynAlloyTyping _localVariables) {
		this.localVariables = _localVariables;
	}

	private static final String CONDITION_ID_PREFFIX = "Condition";

	private Map<AlloyFormula, PredicateFormula> cache = new HashMap<AlloyFormula, PredicateFormula>();
	private HashMap<String, PredicateDeclaration> newPredicates = new HashMap<String, PredicateDeclaration>();

	private int predIdCount = -1;

	public final static String UNIV = "univ";

	public final static String UNIV_TO_UNIV = "univ->univ";

	public final static String UNIV_TO_UNIV_TO_UNIV = "univ->univ->univ";

	public final static String SEQ_UNIV = "Int -> univ";

	private static final String UNIV_TO_SEQ_UNIV = "univ -> (Int -> univ)";

	private String modulePrefix;


	private static class Predicate {
		public Predicate(PredicateDeclaration declaration,
				PredicateFormula formula) {
			super();
			this.declaration = declaration;
			this.formula = formula;
		}

		public final PredicateDeclaration declaration;
		public final PredicateFormula formula;
	}



	public PredicateFormula wrapFormula(String formulaId, AlloyFormula formula) {
//		QFtransformer qfprefixer = new QFtransformer(varsToPrefix);
//		FormulaMutator fm = new FormulaMutator(qfprefixer);
//		formula = (AlloyFormula)formula.accept(fm);
		Predicate predicate = buildPredicate(formulaId, formula);
		newPredicates.put(formulaId, predicate.declaration);
		return predicate.formula;
	}


	public PredicateFormula wrapInvFormula(String formulaId, AlloyFormula formula, Set<AlloyVariable> freeArithVarsInContract) {
		Predicate predicate = buildInvPredicate(formulaId, formula, freeArithVarsInContract);
		newPredicates.put(formulaId, predicate.declaration);
		return predicate.formula;
	}



	public PredicateFormula wrapCondition(AlloyFormula f) {
//		QFtransformer qfprefixer = new QFtransformer(varsToPrefix);
//		FormulaMutator fm = new FormulaMutator(qfprefixer);
//		f = (AlloyFormula)f.accept(fm);
		if (!cache.containsKey(f)) {
			String predicateId = generateConditionId();
			Predicate predicate = buildPredicate(predicateId, f);
			cache.put(f, predicate.formula);
			newPredicates.put(predicate.declaration.getPredicateId(),
					predicate.declaration);
		}
		return cache.get(f);
	}

	private boolean prettyPrinting = false;

	private Predicate buildPredicate(String predicateId, AlloyFormula formula) {
		Set<AlloyVariable> vs = collectVariables(formula);
		List<AlloyVariable> varList = toOrderedList(vs);

		/* create formula */
		RenamePreStateVariableMutator exprMutator = new RenamePreStateVariableMutator();
		JFormulaMutator formMutator = new JFormulaMutator(exprMutator);
		exprMutator.setFormulaVisitor(formMutator);
		AlloyFormula freshFormula = (AlloyFormula) formula.accept(formMutator);

		/* create formal parameters */
		AlloyTyping typing = filterTyping(varList);

		List<AlloyVariable> formal_parameters = new LinkedList<AlloyVariable>();
		List<AlloyVariable> actual_parameters = new LinkedList<AlloyVariable>();

		fill_parameter_lists(varList, typing, formal_parameters,
				actual_parameters);

		PredicateDeclaration p = new PredicateDeclaration(predicateId,
				formal_parameters, typing, freshFormula);

		PredicateFormula pf = new PredicateFormula(null, predicateId,
				toExprVariable(actual_parameters));

		Predicate predicate = new Predicate(p, pf);

		return predicate;
	}


	private Predicate buildInvPredicate(String predicateId, AlloyFormula formula, Set<AlloyVariable> freeArithVarsInContract) {
		Set<AlloyVariable> vsWithoutPostfixForArithmeticVars = collectVariables(formula);
		Set<AlloyVariable> vs = new HashSet<AlloyVariable>();
		for (AlloyVariable a : vsWithoutPostfixForArithmeticVars){
			//			boolean b = a.isVariableFromContract();
			//			if (a.getVariableId().getString().startsWith("SK_jml_pred_java_primitive_") && b==true){
			//				a = new AlloyVariable(a.getVariableId().getString() + "_0", a.isPrimed());
			//				a.setIsVariableFromContract();
			//			}
			vs.add(a);
		}
		List<AlloyVariable> varList = toOrderedList(vs);

		/* create formula */
		RenamePreStateVariableMutator exprMutator = new RenamePreStateVariableMutator();
		JFormulaMutator formMutator = new JFormulaMutator(exprMutator);
		exprMutator.setFormulaVisitor(formMutator);
		AlloyFormula freshFormula = (AlloyFormula) formula.accept(formMutator);

		/* create formal parameters */
		AlloyTyping typing = filterInvTyping(varList, freeArithVarsInContract);

		List<AlloyVariable> formal_parameters = new LinkedList<AlloyVariable>();
		List<AlloyVariable> actual_parameters = new LinkedList<AlloyVariable>();

		fill_parameter_lists(varList, typing, formal_parameters,
				actual_parameters);

		PredicateDeclaration p = new PredicateDeclaration(predicateId,
				formal_parameters, typing, freshFormula);

		PredicateFormula pf = new PredicateFormula(null, predicateId,
				toExprVariable(actual_parameters));

		Predicate predicate = new Predicate(p, pf);

		return predicate;
	}



	private void fill_parameter_lists(List<AlloyVariable> varList,
			AlloyTyping typing, List<AlloyVariable> formal_parameters,
			List<AlloyVariable> actual_parameters) {
		for (AlloyVariable v : varList) {
			if (v.isPreStateVar()) {
				AlloyVariable pre_v = new AlloyVariable(v.getVariableId()
						.getString()
						+ "''");
				if (typing.contains(pre_v)) {
					formal_parameters.add(pre_v);
					actual_parameters.add(v);
				}
			} else if (typing.contains(v)) {
				formal_parameters.add(v);
				actual_parameters.add(v);
			}
		}

	}

	private JType getTypeOf(AlloyVariable v) {
		if (this.parameterVariables.contains(v))
			return this.parameterVariables.getJAlloyType(v);
		else if (localVariables.contains(v))
			return this.localVariables.getJAlloyType(v);
		else if (this.fieldsTyping.contains(v) && v.getVariableId().getString().startsWith("SK_jml_pred_java_primitive"))
			return new JType("univ");
		else if (this.fieldsTyping.contains(v))
			return this.fieldsTyping.getJAlloyType(v);
		else
			return null;
	}

	private AlloyTyping filterTyping(List<AlloyVariable> vs) {
		AlloyTyping t = new AlloyTyping();
		for (AlloyVariable v : vs) {
			JType jType = null;
			if (v.isPrimed()) {
				AlloyVariable unprimedVar = new AlloyVariable(v.getVariableId());
				jType = getTypeOf(unprimedVar);
				if (jType == null)
					jType = getTypeOf(v);
			} else if (v.isPreStateVar()) {
				AlloyVariable actual_state_var = new AlloyVariable(v
						.getVariableId());
				jType = getTypeOf(actual_state_var);
				v = new AlloyVariable(v.getVariableId().getString() + "''");
			} else
				jType = getTypeOf(v);

			if (jType == null) {
				if (v.equals(JExpressionFactory.THIS_VARIABLE)) {
					t.put(JExpressionFactory.THIS_VARIABLE,
							FormulaWrapper.UNIV);
				} else if (v.equals(JExpressionFactory.USED_OBJECTS_VARIABLE)) {
					t.put(JExpressionFactory.USED_OBJECTS_VARIABLE,
							FormulaWrapper.UNIV);
				} 
			}

			if (jType != null && jType.isSpecialType()){
				switch (jType.getSpecialType()) {
				case UNIV_TO_UNIV:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV);
					break;
				case ALLOCATED_OBJECT:
					t.put(v, FormulaWrapper.UNIV);
					break;
					//					case SYSTEM_ARRAY://mfrias: no hay mas system array
					//						sb.append("(" + javaLangPackage() + "SystemArray)->(seq univ)");
					//						break;
				case SET_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV);
					break;
				case MAP_ENTRIES:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV_TO_UNIV);
					break;
				case ALLOY_LIST_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_SEQ_UNIV);
					break;
				case JML_OBJECTSET_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV);
					break;
				case JML_OBJECTSEQUENCE_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_SEQ_UNIV);
					break;
				case ITERATOR_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV);
					break;
				case INT_ARRAY_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV_TO_UNIV);
					break;
				case CHAR_ARRAY_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV_TO_UNIV);
					break;
				case LONG_ARRAY_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV_TO_UNIV);
					break;
				case OBJECT_ARRAY_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV_TO_UNIV);
					break;
				case ALLOY_INT_ARRAY_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_SEQ_UNIV);
					break;
				case ALLOY_OBJECT_ARRAY_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_SEQ_UNIV);
					break;
				}
			} else {
				if (jType != null) {
					if (jType.isTernaryRelation())
						t.put(v, FormulaWrapper.UNIV_TO_UNIV_TO_UNIV);
					else if (jType.isBinRelWithSeq())
						t.put(v, FormulaWrapper.UNIV_TO_SEQ_UNIV);
					else if (jType.isBinaryRelation())
						t.put(v, FormulaWrapper.UNIV_TO_UNIV);
					else {
						if (jType.isSequence())
							t.put(v, FormulaWrapper.SEQ_UNIV);
						else
							t.put(v, FormulaWrapper.UNIV);
					}
				}
			}
		}
		return t;
	}


	private AlloyTyping filterInvTyping(List<AlloyVariable> vs, Set<AlloyVariable> freeArithVarsInContract) {
		AlloyTyping t = new AlloyTyping();
		for (AlloyVariable v : vs) {
			JType jType = null;
			if (v.isPrimed()) {
				AlloyVariable unprimedVar = new AlloyVariable(v.getVariableId());
				jType = getTypeOf(unprimedVar);
			} else if (v.isPreStateVar()) {
				AlloyVariable actual_state_var = new AlloyVariable(v
						.getVariableId());
				jType = getTypeOf(actual_state_var);
				v = new AlloyVariable(v.getVariableId().getString() + "''");
			} else
				jType = getTypeOf(v);

			if (jType == null) {
				if (v.equals(JExpressionFactory.THIS_VARIABLE)) {
					t.put(JExpressionFactory.THIS_VARIABLE,
							FormulaWrapper.UNIV);
				} else if (v.equals(JExpressionFactory.USED_OBJECTS_VARIABLE)) {
					t.put(JExpressionFactory.USED_OBJECTS_VARIABLE,
							FormulaWrapper.UNIV);
				} else if (v.isVariableFromContract()){
					AlloyVariable vari1 = new AlloyVariable(v.getVariableId().getString() + "_0", false);
					vari1.setIsVariableFromContract();
					vari1.setMutable(v.isMutable());
					AlloyVariable vari2 = new AlloyVariable(v.getVariableId().getString() + "_1", true);
					vari2.setIsVariableFromContract();
					vari2.setMutable(v.isMutable());
					if (freeArithVarsInContract.contains(vari1) || freeArithVarsInContract.contains(vari2))
						t.put(v,FormulaWrapper.UNIV);
				}
			}

			if (jType != null && jType.isSpecialType()){
				switch (jType.getSpecialType()) {
				case UNIV_TO_UNIV:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV);
					break;
				case ALLOCATED_OBJECT:
					t.put(v, FormulaWrapper.UNIV);
					break;
					//					case SYSTEM_ARRAY://mfrias: no hay mas system array
					//						sb.append("(" + javaLangPackage() + "SystemArray)->(seq univ)");
					//						break;
				case SET_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV);
					break;
				case MAP_ENTRIES:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV_TO_UNIV);
					break;
				case ALLOY_LIST_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_SEQ_UNIV);
					break;
				case JML_OBJECTSET_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV);
					break;
				case JML_OBJECTSEQUENCE_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_SEQ_UNIV);
					break;
				case ITERATOR_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV);
					break;
				case INT_ARRAY_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV_TO_UNIV);
					break;
				case OBJECT_ARRAY_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_UNIV_TO_UNIV);
					break;
				case ALLOY_INT_ARRAY_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_SEQ_UNIV);
					break;
				case ALLOY_OBJECT_ARRAY_CONTAINS:
					t.put(v, FormulaWrapper.UNIV_TO_SEQ_UNIV);
					break;
				}
			} else {
				if (jType != null) {
					if (jType.isTernaryRelation())
						t.put(v, FormulaWrapper.UNIV_TO_UNIV_TO_UNIV);
					else if (jType.isBinRelWithSeq())
						t.put(v, FormulaWrapper.UNIV_TO_SEQ_UNIV);
					else if (jType.isBinaryRelation())
						t.put(v, FormulaWrapper.UNIV_TO_UNIV);
					else {
						if (jType.isSequence())
							t.put(v, FormulaWrapper.SEQ_UNIV);
						else
							t.put(v, FormulaWrapper.UNIV);
					}
				}
			}
		}
		return t;
	}

	private List<AlloyVariable> toOrderedList(Set<AlloyVariable> vs) {
		List<AlloyVariable> r = new LinkedList<AlloyVariable>(vs);
		Collections.sort(r, new AlloyVariableComparator());
		return r;
	}

	private List<AlloyExpression> toExprVariable(List<AlloyVariable> vs) {
		List<AlloyExpression> r = new LinkedList<AlloyExpression>();
		for (AlloyVariable v : vs) {
			r.add(new ExprVariable(v));
		}
		return r;
	}

	private Set<AlloyVariable> collectVariables(AlloyFormula f) {
		VarCollector v = new VarCollector();
		JFormulaVisitor jFormulaVisitor = new JFormulaVisitor(v);
		v.setFormulaVisitor(jFormulaVisitor);
		f.accept(jFormulaVisitor);
		return v.getVariables();
	}

	private String generateConditionId() {
		predIdCount++;
		return modulePrefix + CONDITION_ID_PREFFIX + predIdCount;
	}

	public HashMap<String, PredicateDeclaration> getWrapperPredicates() {
		return newPredicates;
	}


}

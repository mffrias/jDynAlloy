package ar.edu.jdynalloy.xlator;

import java.util.LinkedList;
import java.util.List;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyMutator;
import ar.edu.jdynalloy.ast.JAlloyProgramBuffer;
import ar.edu.jdynalloy.ast.JAssert;
import ar.edu.jdynalloy.ast.JAssume;
import ar.edu.jdynalloy.ast.JHavoc;
import ar.edu.jdynalloy.ast.JModifies;
import ar.edu.jdynalloy.ast.JPostcondition;
import ar.edu.jdynalloy.ast.JPrecondition;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JSpecCase;
import ar.edu.jdynalloy.ast.JStatement;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaMutator;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.util.ExpressionCloner;
import ar.uba.dc.rfm.alloy.util.FormulaCloner;
import ar.uba.dc.rfm.alloy.util.VarSubstitutor;

class ModularMutator extends JDynAlloyMutator {

	public ModularMutator(boolean isJavaArithmetic) {
		super(isJavaArithmetic);
	}

	static class PreStateExprMutator extends VarSubstitutor {

		@Override
		protected AlloyExpression getExpr(AlloyVariable variable) {
			if (variable.isPrimed())
				return ExprVariable.buildExprVariable(new AlloyVariable(
						variable.getVariableId(), false));
			else {
				JFormulaMutator jFormulaMutator = (JFormulaMutator) formulaVisitor;
				if (!jFormulaMutator.isBoundedVariable(variable)) {

					String varId_string = variable.getVariableId().getString();
					AlloyVariable pre_v = AlloyVariable
							.buildPreStateVariable(varId_string);
					return ExprVariable.buildExprVariable(pre_v);
				} else
					return ExprVariable.buildExprVariable(variable);
			}
		}
	}

	private JDynAlloyModule currentModule = null;

	@Override
	public Object visit(JDynAlloyModule node) {
		currentModule = node;
		return super.visit(node);
	}

	@Override
	public Object visit(JProgramDeclaration node) {

		String programId = node.getProgramId();
		String classToCheck = JDynAlloyConfig.getInstance().getClassToCheck();
		String methodToCheck = JDynAlloyConfig.getInstance().getMethodToCheck();

		if (currentModule.getSignature().getSignatureId().equals(classToCheck)
				&& programId.equals(methodToCheck)) {

			JProgramDeclaration impl_program = (JProgramDeclaration) super
					.visit(node);
			return impl_program;

		} else {

			JProgramDeclaration spec_program = buildSpecProgram(node);
			return spec_program;
		}

	}

	private JProgramDeclaration buildSpecProgram(JProgramDeclaration node) {

		String programId = node.getProgramId();
		String signatureId = node.getSignatureId();
		List<JVariableDeclaration> parameters = node.getParameters();
		List<JSpecCase> spec_cases = node.getSpecCases();

		List<AlloyExpression> list_of_modifiable_locations = new LinkedList<AlloyExpression>();
		for (JSpecCase spec_case : spec_cases) {

			/* modifies */
			for (JModifies modifies : spec_case.getModifies()) {
				AlloyExpression expression = modifies.getLocation();
				if (!list_of_modifiable_locations.contains(expression)) {
					list_of_modifiable_locations.add(expression);
				}
			}

		}

		List<AlloyFormula> contract_formulas = new LinkedList<AlloyFormula>();

		List<AlloyFormula> list_of_program_precondition = new LinkedList<AlloyFormula>();
		for (JSpecCase spec_case : spec_cases) {

			/* requires */
			List<AlloyFormula> list_of_spec_case_requires = new LinkedList<AlloyFormula>();
			for (JPrecondition precondition : spec_case.getRequires()) {
				AlloyFormula formula = precondition.getFormula();
				list_of_spec_case_requires.add(formula);
			}

			AlloyFormula spec_case_requires = AndFormula
					.buildAndFormula(list_of_spec_case_requires
							.toArray(new AlloyFormula[] {}));
			list_of_program_precondition.add(spec_case_requires);

			/* modifies */
			List<AlloyExpression> spec_case_modifies = new LinkedList<AlloyExpression>();
			for (JModifies modifies : spec_case.getModifies()) {
				AlloyExpression expression = modifies.getLocation();
				if (!spec_case_modifies.contains(expression)) {
					spec_case_modifies.add(expression);
				}
			}

			/* ensures */
			List<AlloyFormula> list_of_spec_case_ensures = new LinkedList<AlloyFormula>();
			for (JPostcondition postcondition : spec_case.getEnsures()) {
				AlloyFormula formula = postcondition.getFormula();
				list_of_spec_case_ensures.add(formula);
			}

			AlloyFormula formal_spec_case_ensures = AndFormula
					.buildAndFormula(list_of_spec_case_ensures
							.toArray(new AlloyFormula[] {}));

			PreStateExprMutator exprMutator = new PreStateExprMutator();
			JFormulaMutator formMutator = new JFormulaMutator(exprMutator);
			exprMutator.setFormulaVisitor(formMutator);

			AlloyFormula spec_case_ensures = (AlloyFormula) formal_spec_case_ensures
					.accept(formMutator);

			List<AlloyFormula> frame_condition = new LinkedList<AlloyFormula>();
			for (AlloyExpression modifiable_location : list_of_modifiable_locations) {
				if (!spec_case_modifies.contains(modifiable_location)) {
					/* modifiable_locatio is not within the 
					 * modifiable locations for this spec_case */
					ExpressionCloner exprCloner = new ExpressionCloner();
					FormulaCloner formCloner = new FormulaCloner();

					exprCloner.setFormulaVisitor(formCloner);
					formCloner.setExpressionVisitor(exprCloner);

					AlloyExpression post_state = (AlloyExpression) modifiable_location
							.accept(exprCloner);

					PreStateExprMutator pre_state_cloner = new PreStateExprMutator();
					JFormulaMutator anotherMutator = new JFormulaMutator(
							pre_state_cloner);
					pre_state_cloner.setFormulaVisitor(anotherMutator);

					AlloyExpression pre_state = (AlloyExpression) modifiable_location
							.accept(pre_state_cloner);

					EqualsFormula location_is_not_modified = new EqualsFormula(
							post_state, pre_state);

					frame_condition.add(location_is_not_modified);
				}
			}

			List<AlloyFormula> ensures_and_frame_condition = new LinkedList<AlloyFormula>();
			ensures_and_frame_condition.add(spec_case_ensures);
			ensures_and_frame_condition.addAll(frame_condition);

			AlloyFormula spec_case_formula = new ImpliesFormula(
					spec_case_requires, AndFormula
							.buildAndFormula(ensures_and_frame_condition
									.toArray(new AlloyFormula[] {})));

			contract_formulas.add(spec_case_formula);
		}

		AlloyFormula program_precondition = OrFormula
				.buildOrFormula(list_of_program_precondition
						.toArray(new AlloyFormula[] {}));

		/* assert precondition */
		JAssert assert_precondition = new JAssert(program_precondition);

		/* havoc all locations */
		List<JHavoc> havoc_stmts = new LinkedList<JHavoc>();
		for (AlloyExpression modifiable_location : list_of_modifiable_locations) {
			JHavoc havoc_location = new JHavoc(modifiable_location);
			havoc_stmts.add(havoc_location);
		}

		/* assume all spec cases */
		List<JAssume> assume_stmts = new LinkedList<JAssume>();
		for (AlloyFormula contract_formula : contract_formulas) {
			JAssume assume_contract = new JAssume(contract_formula);
			assume_stmts.add(assume_contract);
		}

		JAlloyProgramBuffer programBuffer = new JAlloyProgramBuffer();
		programBuffer.appendProgram(assert_precondition);
		for (JHavoc havoc_stmt : havoc_stmts) {
			programBuffer.appendProgram(havoc_stmt);
		}
		for (JAssume assume_stmt : assume_stmts) {
			programBuffer.appendProgram(assume_stmt);
		}
		JStatement spec_program_body = programBuffer.toJAlloyProgram();

		JProgramDeclaration spec_program = new JProgramDeclaration(false, node.isConstructor(),
				node.isPure(), signatureId, programId, parameters, spec_cases,
				spec_program_body, null, null);

		return spec_program;
	}

}

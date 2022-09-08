package ar.edu.jdynalloy.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ar.edu.jdynalloy.xlator.JDynAlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaMutator;

public class JDynAlloyMutator extends JDynAlloyVisitor {

	@Override
	public Object visit(JClassConstraint n) {
		AlloyFormula f = (AlloyFormula) n.getFormula().accept(formMutator);
		return new JClassConstraint(f);
	}

	@Override
	public Object visit(JClassInvariant n) {
		AlloyFormula f = (AlloyFormula) n.getFormula().accept(formMutator);
		return new JClassInvariant(f);
	}

	@Override
	public Object visit(JModifies node) {
		if (node.isModifiesEverything()) {
			return JModifies.buildModifiesEverything();
		} else {
			AlloyExpression location = node.getLocation();
			AlloyExpression l = (AlloyExpression) location.accept(formMutator
					.getExpressionMutator());
			return new JModifies(l);
		}
	}

	@Override
	public Object visit(JSpecCase node) {
		Vector<Object> vec = (Vector<Object>) super.visit(node);

		Vector<JPrecondition> requiresVec = (Vector<JPrecondition>) vec.get(0);
		Vector<JPostcondition> ensuresVec = (Vector<JPostcondition>) vec.get(1);
		Vector<JModifies> modifiesVec = (Vector<JModifies>) vec.get(2);

		return new JSpecCase(requiresVec, ensuresVec, modifiesVec);
	}

	@Override
	public Object visit(JHavoc n) {
		AlloyExpression expr = (AlloyExpression) n.getExpression().accept(
				formMutator.getExpressionMutator());
		return new JHavoc(expr);
	}

	private JFormulaMutator formMutator;

	public void setFormulaMutator(JFormulaMutator fm){
		formMutator = fm;
	}

	public JDynAlloyMutator(boolean isJavaArithmetic) {
		super(isJavaArithmetic);
		this.formMutator = new JFormulaMutator();
	}

	@Override
	public Object visit(JAssert n) {
		return new JAssert((AlloyFormula) n.getCondition().accept(formMutator));
	}

	@Override
	public Object visit(JAssignment n) {
		AlloyExpression left = (AlloyExpression) n.getLvalue().accept(
				formMutator.getExpressionMutator());
		if (n.getRvalue() == null){
			System.out.println("BUG");
		}
		AlloyExpression right = (AlloyExpression) n.getRvalue().accept(
				formMutator.getExpressionMutator());
		return new JAssignment(left, right);
	}

	@Override
	public Object visit(JBlock n) {
		Vector<JStatement> children = (Vector<JStatement>) super.visit(n);
		return new JBlock(new LinkedList<JStatement>(children));
	}

	@Override
	public Object visit(JIfThenElse n) {
		Vector<JStatement> children = (Vector<JStatement>) super.visit(n);
		AlloyFormula f = (AlloyFormula) n.getCondition().accept(formMutator);
		String branchId = n.getBranchId();
		return new JIfThenElse(f, children.get(0), children.get(1), branchId);
	}

	@Override
	public Object visit(JSkip n) {
		return new JSkip();
	}

	@Override
	public Object visit(JWhile n) {
		Vector<Object> children = (Vector<Object>) super.visit(n);
		AlloyFormula f = (AlloyFormula) n.getCondition().accept(formMutator);

		JStatement body = (JStatement) children.get(1);

		JLoopInvariant annotation = (JLoopInvariant) children.get(0);

		return new JWhile(f, body, annotation, n.getBranchId());
	}

	@Override
	public Object visit(JCreateObject n) {
		return new JCreateObject(n.getSignatureId(), n.getLvalue());
	}

	@Override
	public Object visit(JVariableDeclaration n) {
		return new JVariableDeclaration(n.getVariable(), n.getType());
	}

	@Override
	public Object visit(JDynAlloyModule node) {
		JDynAlloyModuleVisitResult v = (JDynAlloyModuleVisitResult) super
				.visit(node);


		JSignature signature = (JSignature) v.signature_result;

		JSignature class_singleton;
		if (v.class_singleton_result != null) {
			class_singleton = (JSignature) v.class_singleton_result;
		} else
			class_singleton = null;

		JSignature literal_singleton;
		if (v.literal_singleton_result != null) {
			literal_singleton = (JSignature) v.literal_singleton_result;
		} else
			literal_singleton = null;

		List<JField> fields = new LinkedList<JField>();
		Vector<Object> fieldResults = (Vector<Object>) v.fields_result;
		for (Object o : fieldResults) {
			fields.add((JField) o);
		}

		Set<JClassInvariant> class_invariants = new HashSet<JClassInvariant>();
		Vector<Object> class_invariant_results = (Vector<Object>) v.class_invariants_result;
		for (Object o : class_invariant_results) {
			class_invariants.add((JClassInvariant) o);
		}

		Set<JClassConstraint> class_constraints = new HashSet<JClassConstraint>();
		Vector<Object> class_constraint_results = (Vector<Object>) v.class_constraints_result;
		for (Object o : class_constraint_results) {
			class_constraints.add((JClassConstraint) o);
		}

		Set<JObjectInvariant> object_invariants = new HashSet<JObjectInvariant>();
		Vector<Object> object_invariant_results = (Vector<Object>) v.object_invariants_result;
		for (Object o : object_invariant_results) {
			object_invariants.add((JObjectInvariant) o);
		}

		Set<JObjectConstraint> object_constraints = new HashSet<JObjectConstraint>();
		Vector<Object> object_constraint_results = (Vector<Object>) v.object_constraints_result;
		for (Object o : object_constraint_results) {
			object_constraints.add((JObjectConstraint) o);
		}

		Set<JRepresents> represents = new HashSet<JRepresents>();
		Vector<Object> representsResults = (Vector<Object>) v.represents_result;
		for (Object o : representsResults) {
			represents.add((JRepresents) o);
		}

		Set<JProgramDeclaration> programs = new HashSet<JProgramDeclaration>();
		Vector<Object> programResults = (Vector<Object>) v.programs_result;
		for (Object o : programResults) {
			programs.add((JProgramDeclaration) o);
		}

		JDynAlloyModule module = new JDynAlloyModule(node.getModuleId(),
				signature, class_singleton, literal_singleton, fields,
				class_invariants, class_constraints, object_invariants,
				object_constraints, represents, programs, 
				node.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants(), 
				node.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants(),
				node.pinnedForNonRelevancyAnalysisForStryker);

		return module;
	}

	@Override
	public Object visit(JProgramCall n) {
		List<AlloyExpression> args = new LinkedList<AlloyExpression>();
		for (AlloyExpression arg : n.getArguments()) {
			args.add((AlloyExpression) arg.accept(formMutator
					.getExpressionMutator()));
		}
		return new JProgramCall(n.isSuperCall(), n.getProgramId(), args);
	}

	@Override
	public Object visit(JProgramDeclaration node) {
		Vector<Object> v = (Vector<Object>) super.visit(node);

		Vector<JVariableDeclaration> varResults = (Vector<JVariableDeclaration>) v
				.get(0);

		Vector<JSpecCase> specResults = (Vector<JSpecCase>) v.get(1);

		JStatement body = (JStatement) v.get(2);

		List<JSpecCase> specCases = Arrays.asList(specResults
				.<JSpecCase> toArray(new JSpecCase[] {}));

		return new JProgramDeclaration(node.isVirtual(), node.isConstructor(), node.isPure(), node.getSignatureId(),
				node.getProgramId(), new LinkedList<JVariableDeclaration>(
						varResults), specCases, body, node.getVarsResultOfArithmeticOperationsInRequiresAndEnsures(), node.getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures());
	}

	@Override
	public Object visit(JSignature node) {
		final JSignature signature = new JSignature(node.isOne(), node
				.isAbstract(), node.getSignatureId(), (JDynAlloyTyping) node
				.getFields(), node.isPrimitive(), node.getExtSigId(), node
				.getInSignatureId(), node.superInterfaces(), node.getFacts(),
				node.getAlloyPredicates(), node.getAlloyFunctions(), node
				.getTypeParameters());

		return signature;
	}

	@Override
	public Object visit(JField node) {
		return new JField(node.getFieldVariable(), node.getFieldType());
	}

	@Override
	public Object visit(JAssume n) {
		AlloyFormula formula = (AlloyFormula) n.getCondition().accept(
				formMutator);
		return new JAssume(formula);
	}

	@Override
	public Object visit(JLoopInvariant n) {
		AlloyFormula f = (AlloyFormula) n.getFormula().accept(formMutator);
		return new JLoopInvariant(f);
	}

	@Override
	public Object visit(JObjectInvariant n) {
		AlloyFormula f = (AlloyFormula) n.getFormula().accept(formMutator);
		return new JObjectInvariant(f);
	}

	@Override
	public Object visit(JObjectConstraint n) {
		AlloyFormula f = (AlloyFormula) n.getFormula().accept(formMutator);
		return new JObjectConstraint(f);
	}

	@Override
	public Object visit(JRepresents n) {
		AlloyExpression e = (AlloyExpression) n.getExpression().accept(
				formMutator.getExpressionMutator());
		AlloyFormula f = (AlloyFormula) n.getFormula().accept(formMutator);
		return new JRepresents(e, n.getExpressionType(), f);
	}

	@Override
	public Object visit(JPostcondition n) {
		AlloyFormula f = (AlloyFormula) n.getFormula().accept(formMutator);
		return new JPostcondition(f);
	}

	@Override
	public Object visit(JPrecondition n) {
		AlloyFormula f = (AlloyFormula) n.getFormula().accept(formMutator);
		return new JPrecondition(f);
	}

}

package ar.edu.jdynalloy.xlator;

import static ar.edu.jdynalloy.ast.AlloyIntArrayFactory.getArrayContents;
import static ar.edu.jdynalloy.ast.AlloyIntArrayFactory.getArrayIndex;
import static ar.edu.jdynalloy.ast.AlloyIntArrayFactory.isArrayAccess;
import static ar.uba.dc.rfm.dynalloy.ast.programs.Composition.buildComposition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import ar.edu.jdynalloy.IJDynAlloyConfig;
import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.ast.AlloyIntArrayFactory;
import ar.edu.jdynalloy.ast.JAssert;
import ar.edu.jdynalloy.ast.JAssignment;
import ar.edu.jdynalloy.ast.JAssume;
import ar.edu.jdynalloy.ast.JBlock;
import ar.edu.jdynalloy.ast.JClassInvariant;
import ar.edu.jdynalloy.ast.JCreateObject;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyPrinter;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JHavoc;
import ar.edu.jdynalloy.ast.JIfThenElse;
import ar.edu.jdynalloy.ast.JLoopInvariant;
import ar.edu.jdynalloy.ast.JModifies;
import ar.edu.jdynalloy.ast.JObjectConstraint;
import ar.edu.jdynalloy.ast.JObjectInvariant;
import ar.edu.jdynalloy.ast.JPostcondition;
import ar.edu.jdynalloy.ast.JPrecondition;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JRepresents;
import ar.edu.jdynalloy.ast.JSignature;
import ar.edu.jdynalloy.ast.JSkip;
import ar.edu.jdynalloy.ast.JSpecCase;
import ar.edu.jdynalloy.ast.JStatement;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.ast.JWhile;
import ar.edu.jdynalloy.ast.ListAccess;
import ar.edu.jdynalloy.ast.MapAccess;
import ar.edu.jdynalloy.factory.DynalloyFactory;
import ar.edu.jdynalloy.factory.JDynAlloyFactory;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.factory.JPredicateFactory;
import ar.edu.jdynalloy.factory.JSignatureFactory;
import ar.edu.jdynalloy.xlator.ModularMutator.PreStateExprMutator;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprProduct;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaMutator;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula.Operator;
import ar.uba.dc.rfm.alloy.util.ExpressionCloner;
import ar.uba.dc.rfm.alloy.util.ExpressionMutator;
import ar.uba.dc.rfm.alloy.util.FormulaCloner;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;
import ar.uba.dc.rfm.alloy.util.LiteralCollector;
import ar.uba.dc.rfm.alloy.util.LiteralCollectorExpVisitor;
import ar.uba.dc.rfm.alloy.util.QFPostconditionPostfixTransformer;
import ar.uba.dc.rfm.alloy.util.QFPreconditionPostfixTransformer;
import ar.uba.dc.rfm.alloy.util.QFtransformer;
import ar.uba.dc.rfm.alloy.util.VarCollector;
import ar.uba.dc.rfm.dynalloy.TestPredicateLabel;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.Choice;
import ar.uba.dc.rfm.dynalloy.ast.programs.Closure;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.ast.programs.WhileProgram;
import ar.uba.dc.rfm.dynalloy.util.ProgramCloner;

final class JDynAlloyXlatorVisitor extends JDynAlloyVisitor {

	public HashSet<String> getIntsCollected() {
		return intsCollected;
	}

	public void setIntsCollected(HashSet<String> intsCollected) {
		this.intsCollected = intsCollected;
	}

	public HashSet<String> getLongsCollected() {
		return longsCollected;
	}

	public void setLongsCollected(HashSet<String> longsCollected) {
		this.longsCollected = longsCollected;
	}

	public HashSet<String> getCharsCollected() {
		return charsCollected;
	}

	public void setCharsCollected(HashSet<String> charsCollected) {
		this.charsCollected = charsCollected;
	}

	public HashSet<String> getFloatsCollected() {
		return floatsCollected;
	}

	public void setFloatsCollected(HashSet<String> floatsCollected) {
		this.floatsCollected = floatsCollected;
	}


	public HashSet<String> intsCollected = new HashSet<String>();
	public HashSet<String> longsCollected = new HashSet<String>();
	public HashSet<String> charsCollected = new HashSet<String>();
	public HashSet<String> floatsCollected = new HashSet<String>();

	
	@Override
	public Object visit(JModifies node) {
		return node.getLocation();
	}

	@Override
	public Object visit(JSpecCase node) {
		Vector<Object> v = (Vector<Object>) super.visit(node);
		Vector<AlloyFormula> requires = (Vector<AlloyFormula>) v.get(0);
		for (int index = 0; index < requires.size() && requires.get(index) != null; index++){
			LiteralCollectorExpVisitor lce = new LiteralCollectorExpVisitor();
			LiteralCollector lc = new LiteralCollector(lce);
			AlloyFormula af = (AlloyFormula)requires.get(0); 
			af.accept(lc);
			this.getIntsCollected().addAll(lc.intsCollected);
			this.getLongsCollected().addAll(lc.longsCollected);
			this.getCharsCollected().addAll(lc.charsCollected);
			this.getFloatsCollected().addAll(lc.floatsCollected);
			requires.set(index, (AlloyFormula)requires.get(index));
		}

		Vector<AlloyFormula> ensures = (Vector<AlloyFormula>) v.get(1);
		for (int index = 0; index < ensures.size() && ensures.get(index) != null; index++){
			ensures.set(index, (AlloyFormula)ensures.get(index));

		}

		Vector<AlloyExpression> modifies = (Vector<AlloyExpression>) v.get(2);

		Vector<Object> result = new Vector<Object>();
		result.add(requires);
		result.add(ensures);
		result.add(modifies);

		return result;
	}

	
	private boolean isJavaArith;
	
	private FormulaWrapper formulaWrapper = null;

	private JDynAlloyTyping localVariables;

	private JDynAlloyTyping parameterVariables;

	private JDynAlloyContext context;

	private final Stack<String> escapedDynalloy = new Stack<String>();

	private final Stack<JDynAlloyModule> currentModule = new Stack<JDynAlloyModule>();

	private Vector<AlloyFormula> invariants;
	private Vector<AlloyFormula> constraints;
	private Vector<AlloyFormula> represents;
	private HashSet<AlloyVariable> varsToPrefix;

	private boolean removeQuantifiers;

	private String classToCheck;

	private String methodToCheck;

	public boolean getRemoveQuantifiers(){
		return removeQuantifiers;
	}

	public void setRemoveQuantifiers(boolean rq){
		removeQuantifiers = rq;
	}

	public HashSet<AlloyVariable> getVarsToPrefix(){
		return this.varsToPrefix;
	}

	public JDynAlloyXlatorVisitor(JDynAlloyContext _context, HashSet<AlloyVariable> sav, Object inputToFix, boolean isJavaArith, String classToCheck, String methodToCheck) {
		super(isJavaArith);
		this.isJavaArith = isJavaArith;		
		this.context = _context;
		this.varsToPrefix = sav;
		this.inputToFix = inputToFix;
		this.classToCheck = classToCheck;
		this.methodToCheck = methodToCheck;
	}


//	public JDynAlloyXlatorVisitor(JDynAlloyContext _context) {
//		super();
//		this.context = _context;
//	}

	public Set<PredicateDeclaration> getPredicateDeclarations() {
		return new HashSet<PredicateDeclaration>(formulaWrapper.getWrapperPredicates().values());
	}

	public JDynAlloyTyping getTyping() {
		return this.parameterVariables;
	}

	public Object visit(JAssignment n) {
		AlloyExpression lvalue = n.getLvalue();
		AlloyExpression rvalue = n.getRvalue();

		if (lvalue.getClass().equals(ExprVariable.class))
			return DynalloyFactory.updateVariable((ExprVariable) lvalue, rvalue);
		else if (isArrayAccess(lvalue)) {
			return DynalloyFactory.updateArray(getArrayContents(lvalue), getArrayIndex(lvalue), rvalue);
		} else if (MapAccess.isMapAccess(lvalue)) {
			return DynalloyFactory.updateMap(MapAccess.getMap(lvalue), rvalue);
		} else if (ListAccess.isListAccess(lvalue)) {
			return DynalloyFactory.updateList(ListAccess.getList(lvalue), rvalue);
		} else if (lvalue.getClass().equals(ExprJoin.class))
			return DynalloyFactory.updateField((ExprJoin) lvalue, rvalue);

		else
			throw new IllegalArgumentException();

	}

	public Object visit(JAssert n) {

		TestPredicate ifCondition = new TestPredicate(formulaWrapper.wrapCondition(n.getCondition()));
		Composition ifProgram = new Composition(ifCondition, new Skip());

		TestPredicate elseCondition = new TestPredicate(formulaWrapper.wrapCondition(n.getCondition()), false);

		Assigment updateVariable = DynalloyFactory.updateVariable(JExpressionFactory.THROW_EXPRESSION, JExpressionFactory.ASSERTION_FAILURE_EXPRESSION);
		Composition elseProgram = new Composition(elseCondition, updateVariable);

		return new Choice(ifProgram, elseProgram);
	}

	public Object visit(JCreateObject n) {

		ExprVariable exprLValue = new ExprVariable(n.getLvalue());
		String signatureId = n.getSignatureId();

		PredicateFormula typeCondition;
		if (JDynAlloyConfig.getInstance().getUseClassSingletons() == true) {
			AlloyExpression classOf = JExpressionFactory.classOf(exprLValue);
			ExprConstant classConstant = new ExprConstant(null, signatureId);
			typeCondition = JPredicateFactory.eq(classOf, classConstant);
		} else {
			typeCondition = JPredicateFactory.instanceOf(exprLValue, signatureId);
		}

		return buildComposition(DynalloyFactory.getUnusedObject(n.getLvalue()), new TestPredicate(typeCondition));
	}

	public Object visit(JIfThenElse n) {
		Vector<DynalloyProgram> r = (Vector<DynalloyProgram>) super.visit(n);

		// ifProgram
		TestPredicate ifCondition = new TestPredicate(formulaWrapper.wrapCondition(n.getCondition()));
		Composition ifProgram = new Composition(ifCondition, r.get(0));
		// elseProgram
		TestPredicate elseCondition = new TestPredicate(formulaWrapper.wrapCondition(new NotFormula(n.getCondition())));
		Composition elseProgram = new Composition(elseCondition, r.get(1));

		// add labels
		if (n.getBranchId() != null) {
			TestPredicateLabel lblpos = new TestPredicateLabel(true, n.getBranchId());
			TestPredicateLabel lblneg = new TestPredicateLabel(false, n.getBranchId());
			ifCondition.setLabel(lblpos);
			elseCondition.setLabel(lblneg);
		}

		// if+else
		return new Choice(ifProgram, elseProgram);
	}

	public Object visit(JBlock n) {
		Vector<DynalloyProgram> ss = (Vector<DynalloyProgram>) super.visit(n);
		if (ss.size() == 1) {
			return ss.get(0);
		} else if (ss.size() > 1) {
			Composition result = new Composition(ss.get(0), ss.get(1));
			for (int i = 2; i < ss.size(); i++) {
				result.add(ss.get(i));
			}
			return result;
		} else
			throw new IllegalArgumentException();
	}

	public Object visit(JWhile n) {
		Vector<DynalloyProgram> r = (Vector<DynalloyProgram>) super.visit(n);

		// closure part
		PredicateFormula condition = formulaWrapper.wrapCondition(n.getCondition());
		TestPredicate entryCondition = new TestPredicate(condition);

		// body
		DynalloyProgram body = r.get(1);

		DynalloyProgram result;
		if (JDynAlloyConfig.getInstance().getNestedLoopUnroll() == true) {
			// while dynalloy
			result = new WhileProgram(condition, body, null);
		} else {
			// repeat 
			Composition compo = Composition.buildComposition(entryCondition, body);
			Closure closure = new Closure(compo, null);
			TestPredicate exitCondition = new TestPredicate(formulaWrapper.wrapCondition(new NotFormula(n.getCondition())));
			result = Composition.buildComposition(closure, exitCondition);
		}

		return result;
	}

	@Override
	public Object visit(JLoopInvariant n) {
		JLoopInvariant module = (JLoopInvariant) super.visit(n);

		JAssert jAssert = new JAssert(module.getFormula());

		return jAssert.accept(this);
	}

	public Object visit(JSkip n) {
		return new Skip();
	}

	@Override
	public Object visit(JVariableDeclaration n) {
		if (programParameters.contains(n.getVariable()))
			parameterVariables.put(n.getVariable(), n.getType());
		else
			localVariables.put(n.getVariable(), n.getType());
		return new Skip();
	}



	@Override
	public Object visit(JDynAlloyModule node) {
		this.currentModule.push(node);

		this.invariants = new Vector<AlloyFormula>();
		this.constraints = new Vector<AlloyFormula>();
		this.represents = new Vector<AlloyFormula>();

		this.formulaWrapper = new FormulaWrapper(node.getSignature().getSignatureId(), context.allFields(),
				this.varsToPrefix);

		JDynAlloyModuleVisitResult children = (JDynAlloyModuleVisitResult) super.visit(node);



		//		String origSig = (String) children.signature_result;
		String signature = (String) children.signature_result;
		//		String signature = origSig.replaceFirst("\\{\\}", "{\n" + nodeFields + "}");


		String classSingleton;
		String classHierarchyFact;

		if (JDynAlloyConfig.getInstance().getClassExtendsObject() == false && node.getSignature().getSignatureId().equals("Class")) {
			classSingleton = "";
			classHierarchyFact = "";
		} else {
			if (JDynAlloyConfig.getInstance().getUseClassSingletons() == true) {
				classSingleton = (String) children.class_singleton_result;
				String signatureId = node.getSignature().getSignatureId();

				if (JDynAlloyConfig.getInstance().getTypeSafety() == true)
					classHierarchyFact = DynalloyFactory.declareFactClassHierarchy(signatureId, signatureId + "Class", context.descendantsOf(signatureId));
				else
					classHierarchyFact = "";
			} else {
				classSingleton = "";
				classHierarchyFact = "";
			}
		}

		String literalSingleton;
		if (node.getLiteralSingleton() != null) {
			literalSingleton = (String) children.literal_singleton_result;
		} else
			literalSingleton = "";

		Set<ProgramDeclaration> programs = new HashSet<ProgramDeclaration>();
		Set<AssertionDeclaration> assertions = new HashSet<AssertionDeclaration>();

		Vector<ProgramTranslationResult> childPrograms = (Vector<ProgramTranslationResult>) children.programs_result;
		for (ProgramTranslationResult programResult : childPrograms) {
			ProgramDeclaration programWithPrefixedFormulas = new ProgramDeclaration(
					programResult.program.getProgramId(), 
					programResult.program.getParameters(), 
					programResult.program.getLocalVariables(), 
					programResult.program.getBody(), 
					programResult.program.getParameterTyping(), 
					programResult.predsFromArithmeticInContracts, 
					programResult.program.getVarsFromArithInContracts());
			programs.add(programWithPrefixedFormulas);

			if (programResult.simProgram != null)
				programs.add(programResult.simProgram);

			if (programResult.assertion != null)
				assertions.add(programResult.assertion);
		}

		List<AlloyFormula> formsFromArith = new ArrayList<AlloyFormula>();
		QFtransformer qfprefixer = new QFtransformer(varsToPrefix);
		FormulaMutator fm = new FormulaMutator(qfprefixer);
		if (node.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants() != null &&
				!node.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants().isEmpty()){
			for (AlloyFormula af : node.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants()){
				if (removeQuantifiers){
					af = (AlloyFormula)af.accept(fm);
				} 
				formsFromArith.add(af);
			}
			node.setPredsEncodingValueOfArithmeticOperationsInObjectInvariants(formsFromArith);
		}

		StringBuffer sb = new StringBuffer();
		sb.append(signature + "\n");
		sb.append(classSingleton + "\n");
		sb.append(classHierarchyFact + "\n");

		sb.append(literalSingleton + "\n");

		for (PredicateDeclaration predicateDeclaration : this.getPredicateDeclarations()) {

			boolean prettyPrint = true;
			sb.append(predicateDeclaration.toString(prettyPrint) + "\n");
		}


		//Below I considered adding the predicates as part of the dynalloy model, but it ended up complicating the 
		//trnaslation to the als file.
		//		int index = 1;
		//		for (AlloyFormula af : node.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants()){
		//			sb.append("fact arithmeticFormulaFromInvariant_" + index + " { ");
		//			sb.append(af.toString());
		//			sb.append(" }\n\n");
		//			index++;
		//		}
		//		
		//		index = 1;
		//		for (ProgramTranslationResult programResult : childPrograms) {
		//			for (AlloyFormula af : programResult.predsFromArithmeticInContracts){
		//				sb.append("fact arithmeticFormulaFromMethodContract_" + index + " { ");
		//				sb.append(af.toString());
		//				sb.append(" }\n\n");
		//				index++;
		//			}
		//		}


		while (!escapedDynalloy.isEmpty())
			sb.append(escapedDynalloy.pop() + "\n");

		this.currentModule.pop();

		return new DynalloyModule(node.getModuleId(), null, sb.toString(), 
				Collections.<ActionDeclaration> emptySet(), programs, 
				assertions, node.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants(), 
				node.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants());
	}

	@Override
	public Object visit(JSignature node) {
		JDynAlloyPrinter printer = new JDynAlloyPrinter(this.isJavaArith);
		String str = (String) node.accept(printer);
		return str;
	}

	private Set<AlloyVariable> programParameters = new HashSet<AlloyVariable>();

	private static class ProgramTranslationResult {
		public ProgramTranslationResult(ProgramDeclaration program, AssertionDeclaration assertion, 
				ProgramDeclaration simProgram, List<AlloyFormula> preds) {
			super();
			this.assertion = assertion;
			this.program = program;
			this.simProgram = simProgram;
			this.predsFromArithmeticInContracts = preds;
		}

		private final ProgramDeclaration program;

		private final AssertionDeclaration assertion;

		private final ProgramDeclaration simProgram;

		private final List<AlloyFormula> predsFromArithmeticInContracts;
	}

	@Override
	public Object visit(JProgramDeclaration node) {

		formalParameterNames = node.getParameters();
		localVariables = new JDynAlloyTyping();
		parameterVariables = new JDynAlloyTyping();
		formulaWrapper.bindLocalVariables(localVariables);
		formulaWrapper.bindParameterVariables(parameterVariables);
		programParameters = new HashSet<AlloyVariable>();
		List<VariableId> argList = new LinkedList<VariableId>();

		for (JVariableDeclaration parameterDeclaration : node.getParameters()) {
			programParameters.add(parameterDeclaration.getVariable());
			argList.add(parameterDeclaration.getVariable().getVariableId());
		}

		for (AlloyVariable field : context.fieldList()) {
			argList.add(field.getVariableId());
		}


		Vector<Object> children = (Vector<Object>) super.visit(node);


		ObjectCreationDetector newCallDetector = new ObjectCreationDetector();
		DynalloyProgram prog = (DynalloyProgram) children.get(2);
		LinkedList<DynalloyProgram> list = new LinkedList<DynalloyProgram>();
		list.add(new Skip());
		list.add(prog);
		Composition compo = new Composition(list);
		newCallDetector.visit(compo);

		boolean hasObjectCreation = newCallDetector.getGetUnusedObject_was_found();

		argList.add(new VariableId("usedObjects"));

		JDynAlloyTyping typing = new JDynAlloyTyping();
		for (AlloyVariable v : parameterVariables) {
			typing.put(v, parameterVariables.getJAlloyType(v));
		}

		for (AlloyVariable field : context.allFields().varSet()) {
			typing.put(field, context.allFields().getJAlloyType(field));
		}

		List<VariableId> localIds = new LinkedList<VariableId>();
		for (AlloyVariable v : localVariables.varSet()) {
			localIds.add(v.getVariableId());
			typing.put(v, localVariables.getJAlloyType(v));
		}
		//		if (hasObjectCreation){
		final JType type = JType.parse("set (" + javaLangPackage() + "Object)");
		typing.put(new AlloyVariable("usedObjects"), type);
		//		}
		//		Object varResult = children.get(0);

		Vector<Object> specResults = (Vector<Object>) children.get(1);

		DynalloyProgram body = (DynalloyProgram) children.get(2);

		String programId = node.getProgramId();
		ProgramDeclaration programDeclaration = new ProgramDeclaration(programId, argList, localIds, body, typing, 
				node.getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures(), node.getVarsResultOfArithmeticOperationsInRequiresAndEnsures());

		JDynAlloyModule module = currentModule.peek();

		String classToCheck = JDynAlloyConfig.getInstance().getClassToCheck();

		String methodToCheck = JDynAlloyConfig.getInstance().getMethodToCheck();
		String methodToCheckWithoutTyping = methodToCheck.substring(0,methodToCheck.indexOf('('));

		//prefix QF where required in those predicates that come from arithmetic operations.
		QFtransformer qftransform = new QFtransformer(this.varsToPrefix);
		FormulaMutator fm = new FormulaMutator(qftransform);
		List<AlloyFormula> newPreds = new ArrayList<AlloyFormula>();
		if (node.getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures() != null){
			for (AlloyFormula af : node.getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures()){
				if (removeQuantifiers)
					af = (AlloyFormula)af.accept(fm);
				newPreds.add(af);
			}
			node.setPredsEncodingValueOfArithmeticOperationsInContracts(newPreds);
		}

		if (module.getSignature().getSignatureId().equals(classToCheck) && programId.equals(methodToCheckWithoutTyping)) {

			if (specResults.isEmpty())
				return new ProgramTranslationResult(programDeclaration, null, null, newPreds);
			else {

				Vector<AlloyFormula> requires;
				Vector<AlloyFormula> ensures;

				if (specResults.size() == 1) {
					Vector<Object> specVec = (Vector<Object>) specResults.get(0);
					requires = (Vector<AlloyFormula>) specVec.get(0);
					ensures = (Vector<AlloyFormula>) specVec.get(1);
				} else {
					requires = new Vector<AlloyFormula>();
					ensures = new Vector<AlloyFormula>();

					if (specResults.size() == 1) {
						@SuppressWarnings("unchecked")
						Vector<Object> specCase = (Vector<Object>) specResults.get(0);

						Vector<AlloyFormula> specCaseRequires = (Vector<AlloyFormula>) specCase.get(0);
						Vector<AlloyFormula> specCaseEnsures = (Vector<AlloyFormula>) specCase.get(1);

						requires.addAll(specCaseRequires);
						ensures.addAll(specCaseEnsures);

					} else {

						List<AlloyFormula> spec_case_preconditions = new LinkedList<AlloyFormula>();

						for (int i = 0; i < specResults.size(); i++) {
							Vector<Object> specCase = (Vector<Object>) specResults.get(i);

							Vector<AlloyFormula> specCaseRequires = (Vector<AlloyFormula>) specCase.get(0);
							Vector<AlloyFormula> specCaseEnsures = (Vector<AlloyFormula>) specCase.get(1);

							AlloyFormula requiresAndFormula = AndFormula.buildAndFormula(specCaseRequires.toArray(new AlloyFormula[] {}));

							AlloyFormula ensuresAndFormula = AndFormula.buildAndFormula(specCaseEnsures.toArray(new AlloyFormula[] {}));

							ImpliesFormula specCaseFormula = new ImpliesFormula(requiresAndFormula, ensuresAndFormula);

							spec_case_preconditions.add(requiresAndFormula);
							ensures.add(specCaseFormula);

						}

						requires.add(OrFormula.buildOrFormula(spec_case_preconditions.toArray(new AlloyFormula[] {})));
					}
				}


				if (hasObjectCreation){
					// We will add a constraint to the precondition initializing "usedObjects".

					//					 We first determine if the method under analysis is static. If it is, "this" does not exist and cannot be part
					//					 of the used objecs.
					//					boolean isStaticMethod = true;
					//					if (node.getParameters().get(0).getType().dpdTypeNameExtract().equals(module.getSignature().getSignatureId()) &&
					//							node.getParameters().get(0).getVariable().getVariableId().getString().equals("thiz")){
					//						isStaticMethod = false;
					//						
					//					}

					// We will classify object fields between static and non-static. This will determine what are the source objects
					// we will use to compute reachability.

					JDynAlloyTyping staticFields = new JDynAlloyTyping();
					JDynAlloyTyping nonStaticFields = new JDynAlloyTyping();

					for (AlloyVariable e : this.context.allFields()){
						if (this.context.allFields().getJAlloyType(e).toString().contains("(ClassFields)->")){
							staticFields.put(e, this.context.allFields().getJAlloyType(e));
						} else {
							nonStaticFields.put(e, this.context.allFields().getJAlloyType(e));
						}
					}

					AlloyExpression sources = ExprConstant.buildExprConstant("none");
					Set<AlloyVariable> params = this.programParameters;

					Iterator<AlloyVariable> it = params.iterator();
					while (it.hasNext()){
						AlloyVariable av = it.next();
						if (!av.getVariableId().getString().equals("throw"))
							sources = new ExprUnion(sources, new ExprVariable(av));
					}

					for (AlloyVariable av : staticFields){
						sources = new ExprUnion(sources, 
								new ExprJoin(
										ExprConstant.buildExprConstant(JSignatureFactory.buildClassFieldsSignature().getSignatureId()), 
										new ExprVariable(av)
										)
								);
					}


					AlloyExpression traversedFields = new ExprProduct(ExprConstant.buildExprConstant("none"), ExprConstant.buildExprConstant("none"));
					for (AlloyVariable av : nonStaticFields){
						if (nonStaticFields.getJAlloyType(av).isBinaryRelation() && !nonStaticFields.getJAlloyType(av).isSpecialType())
							traversedFields = new ExprUnion(traversedFields, new ExprVariable(av));
					}

					AlloyExpression reachableObjects =  
							ExprFunction.buildExprFunction("fun_weak_reach", sources, ExprConstant.buildExprConstant("java_lang_Object"), traversedFields);

					requires.add(
							new EqualsFormula(
									new ExprVariable(new AlloyVariable("usedObjects")),
									reachableObjects));
				}

				if (node.isConstructor()){
					return visitConstructorProgramToCheck(programDeclaration, requires, ensures, newPreds);
				} else {
					return visitNonConstructorProgramToCheck(programDeclaration, requires, ensures, newPreds);
				}
			}
		} else
			return new ProgramTranslationResult(programDeclaration, null, null, newPreds);
	}


	private ProgramTranslationResult visitConstructorProgramToCheck(ProgramDeclaration programDeclaration, Vector<AlloyFormula> preFormulas,
			Vector<AlloyFormula> postFormulas, List<AlloyFormula> predsFormArith) {


		final String signatureToCheckId = currentModule.peek().getSignature().getSignatureId();

		JDynAlloyTyping typing = (JDynAlloyTyping) programDeclaration.getParameterTyping();
		String programId = programDeclaration.getProgramId();

		List<VariableId> argList = programDeclaration.getParameters();

		// typing
		JDynAlloyTyping assertionTyping = new JDynAlloyTyping();
		for (AlloyVariable v : typing) {
			assertionTyping.put(v, typing.getJAlloyType(v));
		}

		// precondition
		fillPredicateTables();

		Set<AlloyFormula> preconditions = new HashSet<AlloyFormula>();
		List<AlloyFormula> preconditionFormulas = buildConstructorPreconditionFormulas(signatureToCheckId);
		preconditions.addAll(preconditionFormulas);

		if (!preFormulas.isEmpty()) {
			String requiresId = String.format("%s_requires", signatureToCheckId);
			AlloyFormula requires = AndFormula.buildAndFormula(preFormulas.toArray(new AlloyFormula[] {}));

			AlloyFormula requiresPred = formulaWrapper.wrapFormula(requiresId, requires);
			preconditions.add(requiresPred);
		}

		PredicateFormula throw_is_null = JPredicateFactory.eq(JExpressionFactory.THROW_EXPRESSION, JExpressionFactory.NULL_EXPRESSION);
		preconditions.add(throw_is_null);

		AlloyFormula precondition = AndFormula.buildAndFormula(new Vector<AlloyFormula>(preconditions).toArray(new AlloyFormula[] {}));


		// program to check
		AlloyExpression[] args = AlloyExpression.asAlloyExpression(AlloyVariable.asAlloyVariable(argList)).toArray(new AlloyExpression[] {});

		InvokeProgram invokeProgram = InvokeProgram.buildInvokeProgram(programId, args);

		Vector<JStatement> repVec = new Vector<JStatement>();

		for (JDynAlloyModule relevantModule : this.context.getRelevantModules()) {

			final String relevantSignatureId = relevantModule.getSignature().getSignatureId();

			for (JRepresents represent : relevantModule.getRepresents()) {

				if (isModifiable(programId, represent)) {

					AlloyVariable fieldVar = getFieldVar(represent);

					final String fieldId = fieldVar.getVariableId().getString();

					PredicateFormula abstractionPred = abstractionPredicates.get(relevantSignatureId).get(fieldId);

					final boolean skolemizeInstanceAbstracion = JDynAlloyConfig.getInstance().getSkolemizeInstanceAbstraction() == true;

					AlloyFormula abstractionFormula;
					if (relevantSignatureId.equals(signatureToCheckId) && skolemizeInstanceAbstracion) {

						abstractionFormula = (AlloyFormula) abstractionPred.accept(new FormulaCloner());

					} else {

						JFormulaMutator mutator = new JFormulaMutator(new ThizToObjXExprMutator());

						AlloyFormula mutant = (AlloyFormula) abstractionPred.accept(mutator);

						List<String> names = Collections.<String> singletonList("objx");
						List<AlloyExpression> sets = Collections.<AlloyExpression> singletonList(ExprConstant.buildExprConstant(relevantSignatureId));

						abstractionFormula = new QuantifiedFormula(Operator.FOR_ALL, names, sets, mutant);
					}

					JAssume assume = new JAssume(abstractionFormula);

					JHavoc havoc = new JHavoc(new ExprVariable(fieldVar));
					repVec.add(havoc); // clear field
					repVec.add(assume); // assume new value
				}
			}

		}

		DynalloyProgram programToCheck;
		if (!repVec.isEmpty()) {

			JStatement repTail = JDynAlloyFactory.block(repVec.toArray(new JStatement[] {}));

			DynalloyProgram tail = (DynalloyProgram) repTail.accept(this);

			programToCheck = Composition.buildComposition(invokeProgram, tail);

		} else {
			programToCheck = invokeProgram;
		}

		// postcondition
		Vector<AlloyFormula> postconditions = new Vector<AlloyFormula>();
		if (!postFormulas.isEmpty()) {

			String ensuresId = String.format("%s_ensures", signatureToCheckId);
			AlloyFormula ensures = AndFormula.buildAndFormula(postFormulas.toArray(new AlloyFormula[] {}));

			AlloyFormula ensuresPred = formulaWrapper.wrapFormula(ensuresId, ensures);
			postconditions.add(ensuresPred);

		}

		// We need to make an special treatment when throw expression is equal to AssertionFailure 
		postconditions.add(new NotFormula(new EqualsFormula(JExpressionFactory.PRIMED_THROW_EXPRESSION, JExpressionFactory.ASSERTION_FAILURE_EXPRESSION)));

		List<AlloyFormula> postconditionFormulas = buildPostconditionFormulas(signatureToCheckId, programId);
		postconditions.addAll(postconditionFormulas);

		final IJDynAlloyConfig config = JDynAlloyConfig.getInstance();

		AlloyFormula postcondition = AndFormula.buildAndFormula(postconditions.toArray(new AlloyFormula[] {}));

		PredicateFormula preconditionPredicate = formulaWrapper.wrapFormula("precondition_" + programId, precondition);
		QFtransformer qfprefixer = new QFtransformer(varsToPrefix);
		FormulaMutator fm = new FormulaMutator(qfprefixer);

		preconditionPredicate = (PredicateFormula)preconditionPredicate.accept(fm);


		PredicateFormula postconditionPredicate = formulaWrapper.wrapFormula("postcondition_" + programId, postcondition);
		qfprefixer = new QFtransformer(varsToPrefix);
		fm = new FormulaMutator(qfprefixer);

		postconditionPredicate = (PredicateFormula)postconditionPredicate.accept(fm);

		AssertionDeclaration assertionDeclaration = new AssertionDeclaration("check_" + programId, assertionTyping, preconditionPredicate, programToCheck,
				postconditionPredicate);

		ProgramDeclaration simDeclaration = null;
		if (JDynAlloyConfig.getInstance().getIncludeSimulationProgramDeclaration() == true) {
			ExpressionCloner exprCloner = new ExpressionCloner();
			FormulaCloner formCloner = new FormulaCloner();
			exprCloner.setFormulaVisitor(formCloner);
			formCloner.setExpressionVisitor(exprCloner);

			PredicateFormula fresh_precondition = (PredicateFormula) preconditionPredicate.accept(formCloner);

			ProgramCloner programCloner = new ProgramCloner();
			DynalloyProgram fresh_program = (DynalloyProgram) programToCheck.accept(programCloner);

			TestPredicate assume_precondition = new TestPredicate(fresh_precondition);

			ExpressionMutator exprMutator = new PreStateExprMutator();
			JFormulaMutator formMutator = new JFormulaMutator(exprMutator);
			exprMutator.setFormulaVisitor(formMutator);

			PredicateFormula fresh_postcondition = (PredicateFormula) postconditionPredicate.accept(formMutator);

			TestPredicate assume_postcondition = new TestPredicate(fresh_postcondition);

			Composition program_to_simulate = Composition.buildComposition(assume_precondition, fresh_program, assume_postcondition);

			List<VariableId> simulation_formal_parameters = new LinkedList<VariableId>();
			for (AlloyVariable v : assertionTyping) {
				simulation_formal_parameters.add(v.getVariableId());
			}

			simDeclaration = new ProgramDeclaration("simulate_" + programId, simulation_formal_parameters, Collections.<VariableId> emptyList(),
					program_to_simulate, assertionTyping, programDeclaration.getPredsFromArithInContracts(), programDeclaration.getVarsFromArithInContracts());
		}



		return new ProgramTranslationResult(programDeclaration, assertionDeclaration, simDeclaration, predsFormArith);

	}

	private ProgramTranslationResult visitNonConstructorProgramToCheck(ProgramDeclaration programDeclaration, Vector<AlloyFormula> preFormulas,
			Vector<AlloyFormula> postFormulas, List<AlloyFormula> predsFormArith) {

		final String signatureToCheckId = currentModule.peek().getSignature().getSignatureId();

		JDynAlloyTyping typing = (JDynAlloyTyping) programDeclaration.getParameterTyping();
		String programId = programDeclaration.getProgramId();

		List<VariableId> argList = programDeclaration.getParameters();

		// typing
		JDynAlloyTyping assertionTyping = new JDynAlloyTyping();
		for (AlloyVariable v : typing) {
			assertionTyping.put(v, typing.getJAlloyType(v));
		}

		// precondition
		fillPredicateTables();

		Set<AlloyFormula> preconditions = new HashSet<AlloyFormula>();
		List<AlloyFormula> preconditionFormulas = buildPreconditionFormulas(signatureToCheckId);
		preconditions.addAll(preconditionFormulas);

		if (!preFormulas.isEmpty()) {
			String requiresId = String.format("%s_requires", signatureToCheckId);
			AlloyFormula requires = AndFormula.buildAndFormula(preFormulas.toArray(new AlloyFormula[] {}));

			AlloyFormula requiresPred = formulaWrapper.wrapFormula(requiresId, requires);
			preconditions.add(requiresPred);
		}

		PredicateFormula throw_is_null = JPredicateFactory.eq(JExpressionFactory.THROW_EXPRESSION, JExpressionFactory.NULL_EXPRESSION);
		preconditions.add(throw_is_null);

		AlloyFormula precondition = AndFormula.buildAndFormula(new Vector<AlloyFormula>(preconditions).toArray(new AlloyFormula[] {}));

		// program to check
		AlloyExpression[] args = AlloyExpression.asAlloyExpression(AlloyVariable.asAlloyVariable(argList)).toArray(new AlloyExpression[] {});

		InvokeProgram invokeProgram = InvokeProgram.buildInvokeProgram(programId, args);

		Vector<JStatement> repVec = new Vector<JStatement>();

		for (JDynAlloyModule relevantModule : this.context.getRelevantModules()) {

			final String relevantSignatureId = relevantModule.getSignature().getSignatureId();

			for (JRepresents represent : relevantModule.getRepresents()) {

				if (isModifiable(programId, represent)) {

					AlloyVariable fieldVar = getFieldVar(represent);

					final String fieldId = fieldVar.getVariableId().getString();

					PredicateFormula abstractionPred = abstractionPredicates.get(relevantSignatureId).get(fieldId);

					final boolean skolemizeInstanceAbstracion = JDynAlloyConfig.getInstance().getSkolemizeInstanceAbstraction() == true;

					AlloyFormula abstractionFormula;
					if (relevantSignatureId.equals(signatureToCheckId) && skolemizeInstanceAbstracion) {

						abstractionFormula = (AlloyFormula) abstractionPred.accept(new FormulaCloner());

					} else {

						JFormulaMutator mutator = new JFormulaMutator(new ThizToObjXExprMutator());

						AlloyFormula mutant = (AlloyFormula) abstractionPred.accept(mutator);

						List<String> names = Collections.<String> singletonList("objx");
						List<AlloyExpression> sets = Collections.<AlloyExpression> singletonList(ExprConstant.buildExprConstant(relevantSignatureId));

						abstractionFormula = new QuantifiedFormula(Operator.FOR_ALL, names, sets, mutant);
					}

					JAssume assume = new JAssume(abstractionFormula);

					JHavoc havoc = new JHavoc(new ExprVariable(fieldVar));
					repVec.add(havoc); // clear field
					repVec.add(assume); // assume new value
				}
			}

		}

		DynalloyProgram programToCheck;
		if (!repVec.isEmpty()) {

			JStatement repTail = JDynAlloyFactory.block(repVec.toArray(new JStatement[] {}));

			DynalloyProgram tail = (DynalloyProgram) repTail.accept(this);

			programToCheck = Composition.buildComposition(invokeProgram, tail);

		} else {
			programToCheck = invokeProgram;
		}

		// postcondition
		Vector<AlloyFormula> postconditions = new Vector<AlloyFormula>();
		if (!postFormulas.isEmpty()) {

			String ensuresId = String.format("%s_ensures", signatureToCheckId);
			AlloyFormula ensures = AndFormula.buildAndFormula(postFormulas.toArray(new AlloyFormula[] {}));

			AlloyFormula ensuresPred = formulaWrapper.wrapFormula(ensuresId, ensures);
			postconditions.add(ensuresPred);

		}

//		mffrias 110223
		
		// We need to make an special treatment when throw expression is equal to AssertionError 
//		postconditions.add(new NotFormula(new EqualsFormula(JExpressionFactory.PRIMED_THROW_EXPRESSION, JExpressionFactory.ASSERTION_FAILURE_EXPRESSION)));

		List<AlloyFormula> postconditionFormulas = buildPostconditionFormulas(signatureToCheckId, programId);
		postconditions.addAll(postconditionFormulas);

		final IJDynAlloyConfig config = JDynAlloyConfig.getInstance();

		AlloyFormula postcondition = AndFormula.buildAndFormula(postconditions.toArray(new AlloyFormula[] {}));

		//		PredicateFormula preconditionPredicate = formulaWrapper.wrapFormula("precondition_" + programId, precondition);
		//
		//		PredicateFormula postconditionPredicate = formulaWrapper.wrapFormula("postcondition_" + programId, postcondition);

		PredicateFormula preconditionPredicate = formulaWrapper.wrapFormula("precondition_" + programId, precondition);
		if (removeQuantifiers){
			QFtransformer qfprefixer = new QFtransformer(varsToPrefix);
			FormulaMutator fm = new FormulaMutator(qfprefixer);
			preconditionPredicate = (PredicateFormula)preconditionPredicate.accept(fm);
		} 


		PredicateFormula postconditionPredicate = formulaWrapper.wrapFormula("postcondition_" + programId, postcondition);
		if (removeQuantifiers){
			QFtransformer qfprefixer = new QFtransformer(varsToPrefix);
			FormulaMutator fm = new FormulaMutator(qfprefixer);
			postconditionPredicate = (PredicateFormula)postconditionPredicate.accept(fm);
		}
		AssertionDeclaration assertionDeclaration = new AssertionDeclaration("check_" + programId, assertionTyping, preconditionPredicate, programToCheck,
				postconditionPredicate);

		ProgramDeclaration simDeclaration = null;
		if (JDynAlloyConfig.getInstance().getIncludeSimulationProgramDeclaration() == true) {
			ExpressionCloner exprCloner = new ExpressionCloner();
			FormulaCloner formCloner = new FormulaCloner();
			exprCloner.setFormulaVisitor(formCloner);
			formCloner.setExpressionVisitor(exprCloner);

			PredicateFormula fresh_precondition = (PredicateFormula) preconditionPredicate.accept(formCloner);

			ProgramCloner programCloner = new ProgramCloner();
			DynalloyProgram fresh_program = (DynalloyProgram) programToCheck.accept(programCloner);

			TestPredicate assume_precondition = new TestPredicate(fresh_precondition);

			ExpressionMutator exprMutator = new PreStateExprMutator();
			JFormulaMutator formMutator = new JFormulaMutator(exprMutator);
			exprMutator.setFormulaVisitor(formMutator);

			PredicateFormula fresh_postcondition = (PredicateFormula) postconditionPredicate.accept(formMutator);

			TestPredicate assume_postcondition = new TestPredicate(fresh_postcondition);

			Composition program_to_simulate = Composition.buildComposition(assume_precondition, fresh_program, assume_postcondition);

			List<VariableId> simulation_formal_parameters = new LinkedList<VariableId>();
			for (AlloyVariable v : assertionTyping) {
				simulation_formal_parameters.add(v.getVariableId());
			}

			simDeclaration = new ProgramDeclaration("simulate_" + programId, simulation_formal_parameters, Collections.<VariableId> emptyList(),
					program_to_simulate, assertionTyping, programDeclaration.getPredsFromArithInContracts(), programDeclaration.getVarsFromArithInContracts());
		}



		return new ProgramTranslationResult(programDeclaration, assertionDeclaration, simDeclaration, predsFormArith);

	}

	private boolean containsVariable(AlloyVariable variable, AlloyFormula formula) {
		VarCollector varCollector = new VarCollector();
		formula.accept(new FormulaVisitor(varCollector));
		Set<AlloyVariable> vars = varCollector.getVariables();
		return vars.contains(variable);
	}

	private List<AlloyFormula> buildPreconditionFormulas(String signatureToCheckId) {

		AlloyVariable objx = new AlloyVariable("objx");
		List<AlloyFormula> formulas = new LinkedList<AlloyFormula>();

		for (JDynAlloyModule relevantModule : context.getRelevantModules()) {

			String signatureId = relevantModule.getSignature().getSignatureId();

			Vector<PredicateFormula> quantify = new Vector<PredicateFormula>();
			Vector<PredicateFormula> notQuantify = new Vector<PredicateFormula>();

			PredicateFormula class_invariant_pred = class_invariant_predicates.get(signatureId);
			if (class_invariant_pred != null) {
				notQuantify.add(class_invariant_pred);
			}

			PredicateFormula object_invariant_pred = object_invariant_predicates.get(signatureId);
			if (object_invariant_pred != null) {

				final boolean skolemizeInstanceInvariant = JDynAlloyConfig.getInstance().getSkolemizeInstanceInvariant() == true;

				if ((signatureId.equals(signatureToCheckId)) && skolemizeInstanceInvariant) {

					object_invariant_pred = (PredicateFormula) object_invariant_pred.accept(new FormulaCloner());
					notQuantify.add(object_invariant_pred);

				} else {

					JFormulaMutator mutator = new JFormulaMutator(new ThizToObjXExprMutator());

					object_invariant_pred = (PredicateFormula) object_invariant_pred.accept(mutator);

					if (containsVariable(objx, object_invariant_pred)) {
						quantify.add(object_invariant_pred);
					} else
						notQuantify.add(object_invariant_pred);
				}
			}

			Map<String, PredicateFormula> abstractionPreds = abstractionPredicates.get(signatureId);

			if (abstractionPreds != null) {
				for (String abstractionPredicateId : abstractionPreds.keySet()) {

					PredicateFormula abstractionPred = abstractionPreds.get(abstractionPredicateId);

					final boolean skolemizeInstanceAbstraction = JDynAlloyConfig.getInstance().getSkolemizeInstanceAbstraction() == true;

					if ((signatureId.equals(signatureToCheckId)) && skolemizeInstanceAbstraction) {

						abstractionPred = (PredicateFormula) abstractionPred.accept(new FormulaCloner());

					} else {

						JFormulaMutator mutator = new JFormulaMutator(new ThizToObjXExprMutator());

						abstractionPred = (PredicateFormula) abstractionPred.accept(mutator);
					}

					if (containsVariable(objx, abstractionPred)) {
						quantify.add(abstractionPred);
					} else
						notQuantify.add(abstractionPred);

				}
			}

			if (!quantify.isEmpty()) {

				List<String> names = Collections.<String> singletonList(objx.toString());
				List<AlloyExpression> sets = Collections.<AlloyExpression> singletonList(ExprConstant.buildExprConstant(relevantModule.getSignature()
						.getSignatureId()));

				AlloyFormula formula = new QuantifiedFormula(Operator.FOR_ALL, names, sets, AndFormula.buildAndFormula(quantify.toArray(new AlloyFormula[] {})));

				formulas.add(formula);

			}

			formulas.addAll(notQuantify);
		}

		List<AlloyFormula> formulasAfterSKProcessing = new LinkedList<AlloyFormula>();
		HashSet<AlloyVariable> varsFromInv = new HashSet<AlloyVariable>();
		for (AlloyVariable av : this.currentModule.peek().getVarsEncodingValueOfArithmeticOperationsInObjectInvariants().getVarsInTyping())
			varsFromInv.add(av);

		QFPreconditionPostfixTransformer qfpt = new QFPreconditionPostfixTransformer(varsFromInv);
		FormulaMutator fm = new FormulaMutator(qfpt);
		for (AlloyFormula af : formulas){
			formulasAfterSKProcessing.add((AlloyFormula)af.accept(fm));
		}

		return formulasAfterSKProcessing;
	}


	private List<AlloyFormula> buildConstructorPreconditionFormulas(String signatureToCheckId) {

		AlloyVariable objx = new AlloyVariable("objx");
		List<AlloyFormula> formulas = new LinkedList<AlloyFormula>();

		for (JDynAlloyModule relevantModule : context.getRelevantModules()) {

			String signatureId = relevantModule.getSignature().getSignatureId();

			Vector<PredicateFormula> quantify = new Vector<PredicateFormula>();
			Vector<PredicateFormula> notQuantify = new Vector<PredicateFormula>();

			PredicateFormula class_invariant_pred = class_invariant_predicates.get(signatureId);
			if (class_invariant_pred != null) {
				notQuantify.add(class_invariant_pred);
			}

			PredicateFormula object_invariant_pred = object_invariant_predicates.get(signatureId);
			if (object_invariant_pred != null) {

				final boolean skolemizeInstanceInvariant = JDynAlloyConfig.getInstance().getSkolemizeInstanceInvariant() == true;


				if (!signatureId.equals(signatureToCheckId)){

					if (skolemizeInstanceInvariant) {

						object_invariant_pred = (PredicateFormula) object_invariant_pred.accept(new FormulaCloner());
						notQuantify.add(object_invariant_pred);

					} else {

						JFormulaMutator mutator = new JFormulaMutator(new ThizToObjXExprMutator());

						object_invariant_pred = (PredicateFormula) object_invariant_pred.accept(mutator);

						if (containsVariable(objx, object_invariant_pred)) {
							quantify.add(object_invariant_pred);
						} else
							notQuantify.add(object_invariant_pred);
					}
				}
			}

			Map<String, PredicateFormula> abstractionPreds = abstractionPredicates.get(signatureId);

			if (abstractionPreds != null) {
				for (String abstractionPredicateId : abstractionPreds.keySet()) {

					PredicateFormula abstractionPred = abstractionPreds.get(abstractionPredicateId);

					final boolean skolemizeInstanceAbstraction = JDynAlloyConfig.getInstance().getSkolemizeInstanceAbstraction() == true;

					if ((signatureId.equals(signatureToCheckId)) && skolemizeInstanceAbstraction) {

						abstractionPred = (PredicateFormula) abstractionPred.accept(new FormulaCloner());

					} else {

						JFormulaMutator mutator = new JFormulaMutator(new ThizToObjXExprMutator());

						abstractionPred = (PredicateFormula) abstractionPred.accept(mutator);
					}

					if (containsVariable(objx, abstractionPred)) {
						quantify.add(abstractionPred);
					} else
						notQuantify.add(abstractionPred);

				}
			}

			if (!quantify.isEmpty()) {

				List<String> names = Collections.<String> singletonList(objx.toString());
				List<AlloyExpression> sets = Collections.<AlloyExpression> singletonList(ExprConstant.buildExprConstant(relevantModule.getSignature()
						.getSignatureId()));

				AlloyFormula formula = new QuantifiedFormula(Operator.FOR_ALL, names, sets, AndFormula.buildAndFormula(quantify.toArray(new AlloyFormula[] {})));

				formulas.add(formula);

			}

			formulas.addAll(notQuantify);
		}

		List<AlloyFormula> formulasAfterSKProcessing = new LinkedList<AlloyFormula>();
		HashSet<AlloyVariable> varsFromInv = new HashSet<AlloyVariable>();
		for (AlloyVariable av : this.currentModule.peek().getVarsEncodingValueOfArithmeticOperationsInObjectInvariants().getVarsInTyping())
			varsFromInv.add(av);

		QFPreconditionPostfixTransformer qfpt = new QFPreconditionPostfixTransformer(varsFromInv);
		FormulaMutator fm = new FormulaMutator(qfpt);
		for (AlloyFormula af : formulas){
			formulasAfterSKProcessing.add((AlloyFormula)af.accept(fm));
		}



		return formulasAfterSKProcessing;
	}


	/**
	 * SignatureId -> PredicateFormula
	 */
	private Map<String, PredicateFormula> object_invariant_predicates = null;

	/**
	 * SignatureId -> PredicateFormula
	 */
	private Map<String, PredicateFormula> class_invariant_predicates = null;

	/***
	 * SignatureId -> FieldId -> PredicateFormula
	 */
	private Map<String, Map<String, PredicateFormula>> abstractionPredicates = null;

	private HashMap<String, PredicateFormula> class_constraint_predicates = null;

	private HashMap<String, PredicateFormula> object_constraint_predicates = null;

	private void fillPredicateTables() {

		class_invariant_predicates = new HashMap<String, PredicateFormula>();
		class_constraint_predicates = new HashMap<String, PredicateFormula>();

		object_invariant_predicates = new HashMap<String, PredicateFormula>();
		object_constraint_predicates = new HashMap<String, PredicateFormula>();

		abstractionPredicates = new HashMap<String, Map<String, PredicateFormula>>();

		Vector<JDynAlloyModule> allModules = new Vector<JDynAlloyModule>();

		allModules.addAll(context.getRelevantModules());
		allModules.add(this.currentModule.peek());

		for (JDynAlloyModule relevantModule : allModules) {

			fill_class_invariant_predicates(relevantModule);

			fill_object_invariant_predicates(relevantModule);

			fill_object_constraint_predicates(relevantModule);

			fill_abstraction_function_predicates(relevantModule);

		}
	}

	private void fill_abstraction_function_predicates(JDynAlloyModule module) {
		String signatureId = module.getSignature().getSignatureId();
		for (JRepresents relevantRepresents : module.getRepresents()) {

			AlloyVariable fieldVariable = getFieldVar(relevantRepresents);

			final String fieldId = fieldVariable.getVariableId().getString();

			//			QFtransformer qft = new QFtransformer(this.getVarsToPrefix());
			//			FormulaMutator fv = new FormulaMutator(qft);

			AlloyFormula abstractionFormula = relevantRepresents.getFormula();
			//			abstractionFormula = (AlloyFormula)abstractionFormula.accept(fv);



			String abstractionPredicateId = String.format("%s_%s_abstraction", signatureId, fieldId);

			PredicateFormula abstractionPred = formulaWrapper.wrapFormula(abstractionPredicateId, abstractionFormula);
			if (!abstractionPredicates.containsKey(signatureId))
				abstractionPredicates.put(signatureId, new HashMap<String, PredicateFormula>());

			abstractionPredicates.get(signatureId).put(fieldId, abstractionPred);
		}
	}

	private void fill_object_invariant_predicates(JDynAlloyModule module) {
		String signatureId = module.getSignature().getSignatureId();
		List<AlloyFormula> object_invariants = new LinkedList<AlloyFormula>();
		for (JObjectInvariant relevant_object_invariant : module.getObjectInvariants()) {

			//			QFtransformer qft = new QFtransformer(this.getVarsToPrefix());
			//			FormulaMutator fv = new FormulaMutator(qft);

			AlloyFormula invariantFormula = relevant_object_invariant.getFormula();
			//			invariantFormula = (AlloyFormula)invariantFormula.accept(fv);

			object_invariants.add(invariantFormula);
		}
		if (!object_invariants.isEmpty()) {
			AlloyFormula moduleInvariant = AndFormula.buildAndFormula(object_invariants.<AlloyFormula> toArray(new AlloyFormula[] {}));

			String invariantId = String.format("%s_object_invariant", signatureId);

			PredicateFormula invariantPred = formulaWrapper.wrapInvFormula(invariantId, moduleInvariant, module.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants().getVarsInTyping());

			object_invariant_predicates.put(signatureId, invariantPred);

		}
	}

	private void fill_class_invariant_predicates(JDynAlloyModule module) {
		String signatureId = module.getSignature().getSignatureId();
		List<AlloyFormula> class_invariants = new LinkedList<AlloyFormula>();
		for (JClassInvariant relevant_class_invariant : module.getClassInvariants()) {

			//			QFtransformer qft = new QFtransformer(this.getVarsToPrefix());
			//			FormulaMutator fv = new FormulaMutator(qft);

			AlloyFormula invariant_formula = relevant_class_invariant.getFormula();
			//			invariant_formula = (AlloyFormula)invariant_formula.accept(fv);

			class_invariants.add(invariant_formula);
		}
		if (!class_invariants.isEmpty()) {
			AlloyFormula class_invariant = AndFormula.buildAndFormula(class_invariants.<AlloyFormula> toArray(new AlloyFormula[] {}));

			String invariantId = String.format("%s_class_invariant", signatureId);
			PredicateFormula invariantPred = formulaWrapper.wrapFormula(invariantId, class_invariant);

			class_invariant_predicates.put(signatureId, invariantPred);
		}
	}

	private AlloyVariable getFieldVar(JRepresents represent) {

		// expected (thiz.fieldName)
		AlloyExpression expression = represent.getExpression();
		if (!(expression instanceof ExprJoin)) {
			throw new IllegalArgumentException("cannot process represent expression " + expression.toString());
		}
		ExprJoin join = (ExprJoin) expression;

		if (!(join.getRight() instanceof ExprVariable)) {
			throw new IllegalArgumentException("cannot process represent expression " + expression.toString());
		}
		ExprVariable exprVar = (ExprVariable) join.getRight();
		return exprVar.getVariable();
	}

	private boolean isModifiable(String programId, JRepresents represent) {

		AlloyFormula formula = represent.getFormula();
		AlloyVariable fieldVar = getFieldVar(represent);

		VarCollector varCollector = new VarCollector();

		formula.accept(new JFormulaVisitor(varCollector));

		Set<AlloyVariable> variables = varCollector.getVariables();

		Set<AlloyVariable> fieldsInRepresentsFormula = new HashSet<AlloyVariable>();
		for (AlloyVariable v : variables) {
			if (context.allFields().contains(v) && !fieldVar.equals(v))
				fieldsInRepresentsFormula.add(v);
		}

		Set<AlloyVariable> modifiableFields = context.getModifiesTable().get(programId);

		for (AlloyVariable field : fieldsInRepresentsFormula) {
			if (modifiableFields.contains(field))
				return true;
		}
		return false;
	}

	private DynalloyProgram buildAssertHeader() {
		return new Assigment(JExpressionFactory.ASSERTION_FAILURE_EXPR, JExpressionFactory.FALSE_EXPRESSION);
	}

	private DynalloyProgram buildAssertTail() {
		PredicateFormula condition = JPredicateFactory.eq(JExpressionFactory.ASSERTION_FAILURE_EXPR, JExpressionFactory.TRUE_EXPRESSION);

		TestPredicate ifCondition = new TestPredicate(condition);
		Composition ifProgram = new Composition(ifCondition,
				new Assigment(JExpressionFactory.THROW_EXPRESSION, JExpressionFactory.ASSERTION_FAILURE_EXPRESSION));

		TestPredicate elseCondition = new TestPredicate(condition, false);

		Composition elseProgram = new Composition(elseCondition, new Skip());

		return new Choice(ifProgram, elseProgram);
	}

	@Override
	public Object visit(JProgramCall n) {
		String programId = n.getProgramId();
		Vector<AlloyExpression> args = new Vector<AlloyExpression>();
		args.addAll(n.getArguments());
		args.addAll(AlloyExpression.asAlloyExpression(context.fieldList()));
		args.add(new ExprVariable(new AlloyVariable("usedObjects")));
		return InvokeProgram.buildInvokeProgram(programId, args.toArray(new AlloyExpression[] {}));
	}

	@Override
	public Object visit(JAssume n) {
		return new TestPredicate(formulaWrapper.wrapCondition(n.getCondition()));
	}

	@Override
	public Object visit(JObjectInvariant node) {
		//		QFtransformer qfmutator = new QFtransformer(varsToPrefix);
		AlloyFormula af = node.getFormula();
		//		FormulaMutator fm = new FormulaMutator(qfmutator);
		//		af = (AlloyFormula) af.accept(fm);
		invariants.add(af);
		return af;
	}

	@Override
	public Object visit(JRepresents node) {
		represents.add(node.getFormula());
		return null;
	}

	@Override
	public Object visit(JPrecondition node) {
		return node.getFormula();
	}

	@Override
	public Object visit(JPostcondition node) {
		return node.getFormula();
	}

	@Override
	public Object visit(JHavoc n) {
		AlloyExpression expresssion = n.getExpression();

		if (expresssion.getClass().equals(ExprVariable.class)) {
			ExprVariable exprVariable = (ExprVariable) expresssion;
			if (context.fieldList().contains(exprVariable.getVariable())) {

				JType fieldType = context.allFields().getJAlloyType(exprVariable.getVariable());

				if (fieldType.isBinRelWithSeq())
					return DynalloyFactory.havocVariable3(exprVariable);
				else
					return DynalloyFactory.havocVariable2(exprVariable);
			} else
				return DynalloyFactory.havocVariable(exprVariable);
		} else if (isArrayAccess(expresssion)) {
			//mfrias: careful about this, which seems to be ignoring LongArray existence
			AlloyExpression array = AlloyIntArrayFactory.getArrayContents(expresssion);
			return DynalloyFactory.havocArrayContents(array);
		} else if (ListAccess.isListAsSeqAccess(expresssion)) {
			ExprJoin join = (ExprJoin) expresssion;
			AlloyExpression left = join.getLeft();
			ExprVariable right = (ExprVariable) join.getRight();
			return DynalloyFactory.havocListSeq(join);
		} else if (MapAccess.isMapAccess(expresssion)) {
			throw new UnsupportedOperationException("map access havoc translation not yet supported");
		} else if (ListAccess.isListAccess(expresssion)) {
			throw new UnsupportedOperationException("list access havoc translation not yet supported");
		} else if (expresssion.getClass().equals(ExprJoin.class)) {
			ExprJoin join = (ExprJoin) expresssion;
			AlloyExpression left = join.getLeft();
			ExprVariable right = (ExprVariable) join.getRight();
			return DynalloyFactory.havocFieldContents(left, right);

		} else
			throw new IllegalArgumentException();

		// TODO Finish havocing translation
	}
	
	
	
	

	private List<AlloyFormula> buildPostconditionFormulas(String signatureToCheckId, String programId) {

		List<AlloyFormula> formulas = new LinkedList<AlloyFormula>();

		for (JDynAlloyModule relevantModule : context.getRelevantModules()) {

			Set<AlloyFormula> class_invariant_formulas = extract_class_invariant_formulas(programId, relevantModule);
			formulas.addAll(class_invariant_formulas);

			Set<AlloyFormula> object_invariant_formulas = extract_object_invariant_formulas(signatureToCheckId, programId, relevantModule);
			formulas.addAll(object_invariant_formulas);

			Set<AlloyFormula> object_constraint_formulas = extract_object_constraint_formulas(relevantModule);
			formulas.addAll(object_constraint_formulas);

		}
		return formulas;
	}

	private Set<AlloyFormula> extract_object_invariant_formulas(String signatureToCheckId, String programId, JDynAlloyModule module) {
		Set<AlloyFormula> object_invariant_formulas = new HashSet<AlloyFormula>();

		if (isModifiable_object_invariants(programId, module.getObjectInvariants())) {

			String signatureId = module.getSignature().getSignatureId();

			PredicateFormula invariantPred = object_invariant_predicates.get(signatureId);

			if (invariantPred != null) {

				final boolean skolemizeInstanceInvariant = JDynAlloyConfig.getInstance().getSkolemizeInstanceInvariant() == true;

				if (signatureId.equals(signatureToCheckId) && skolemizeInstanceInvariant) {

					invariantPred = (PredicateFormula) invariantPred.accept(new FormulaCloner());

				} else {

					JFormulaMutator mutator = new JFormulaMutator(new ThizToObjXExprMutator());

					invariantPred = (PredicateFormula) invariantPred.accept(mutator);
				}

				PostconditionExprMutator postconditionExprMutator = new PostconditionExprMutator();
				JFormulaMutator postconditionMutator = new JFormulaMutator(postconditionExprMutator);
				postconditionExprMutator.setFormulaVisitor(postconditionMutator);

				invariantPred = (PredicateFormula) invariantPred.accept(postconditionMutator);

				AlloyVariable objx = new AlloyVariable("objx", true);

				if (containsVariable(objx, invariantPred)) {

					List<String> names = Collections.<String> singletonList(objx.toString());
					List<AlloyExpression> sets = Collections.<AlloyExpression> singletonList(ExprConstant.buildExprConstant(module.getSignature()
							.getSignatureId()));

					AlloyFormula formula = new QuantifiedFormula(Operator.FOR_ALL, names, sets, invariantPred);

					object_invariant_formulas.add(formula);
				} else
					object_invariant_formulas.add(invariantPred);
			}

		}

		Set<AlloyFormula> formulasAfterSKProcessing = new HashSet<AlloyFormula>();
		HashSet<AlloyVariable> varsFromInv = new HashSet<AlloyVariable>();
		for (AlloyVariable av : this.currentModule.peek().getVarsEncodingValueOfArithmeticOperationsInObjectInvariants().getVarsInTyping())
			varsFromInv.add(av);

		QFPostconditionPostfixTransformer qfpt = new QFPostconditionPostfixTransformer(varsFromInv);
		FormulaMutator fm = new FormulaMutator(qfpt);
		for (AlloyFormula af : object_invariant_formulas){
			formulasAfterSKProcessing.add((AlloyFormula)af.accept(fm));
		}

		return formulasAfterSKProcessing;
	}

	private Set<AlloyFormula> extract_class_invariant_formulas(String programId, JDynAlloyModule module) {
		Set<AlloyFormula> class_invariant_formulas = new HashSet<AlloyFormula>();
		if (isModifiable_class_invariants(programId, module.getClassInvariants())) {

			String signatureId = module.getSignature().getSignatureId();

			PredicateFormula invariantPred = class_invariant_predicates.get(signatureId);

			if (invariantPred != null) {

				invariantPred = (PredicateFormula) invariantPred.accept(new FormulaCloner());

				JFormulaMutator postconditionMutator = new JFormulaMutator(new PostconditionExprMutator());

				invariantPred = (PredicateFormula) invariantPred.accept(postconditionMutator);

				class_invariant_formulas.add(invariantPred);

			}

		}
		return class_invariant_formulas;
	}

	private boolean isModifiable_class_invariants(String programId, Set<JClassInvariant> class_invariants) {

		Vector<AlloyFormula> formulas = new Vector<AlloyFormula>();
		for (JClassInvariant invariant : class_invariants)
			formulas.add(invariant.getFormula());
		AlloyFormula class_invariant_formula = AndFormula.buildAndFormula(formulas.toArray(new AlloyFormula[] {}));

		Set<AlloyVariable> fields_in_formula = collect_fields_in_formula(class_invariant_formula);

		Set<AlloyVariable> may_update_fields = context.getModifiesTable().get(programId);

		for (AlloyVariable field : fields_in_formula) {
			if (may_update_fields.contains(field))
				return true;
		}
		return false;

	}

	private boolean isModifiable_object_invariants(String programId, Set<JObjectInvariant> object_invariants) {

		Vector<AlloyFormula> formulas = new Vector<AlloyFormula>();
		for (JObjectInvariant invariant : object_invariants)
			formulas.add(invariant.getFormula());


		for (AlloyFormula af : this.currentModule.peek().getPredsEncodingValueOfArithmeticOperationsInObjectInvariants()){
			formulas.add(af);
		}

		AlloyFormula object_invariant_formula = AndFormula.buildAndFormula(formulas.toArray(new AlloyFormula[] {}));

		Set<AlloyVariable> fields_in_invariant_formula = collect_fields_in_formula(object_invariant_formula);

		Set<AlloyVariable> modifiableFields = context.getModifiesTable().get(programId);

		for (AlloyVariable field : fields_in_invariant_formula) {
			if (modifiableFields.contains(field))
				return true;
		}
		return false;
	}

	private Set<AlloyVariable> collect_fields_in_formula(AlloyFormula formula) {
		VarCollector varCollector = new VarCollector();
		formula.accept(new JFormulaVisitor(varCollector));

		Set<AlloyVariable> variables = varCollector.getVariables();

		Set<AlloyVariable> fields_in_invariant_formula = new HashSet<AlloyVariable>();
		for (AlloyVariable v : variables) {
			if (context.allFields().contains(v))
				fields_in_invariant_formula.add(v);
		}
		return fields_in_invariant_formula;
	}

	private void fill_object_constraint_predicates(JDynAlloyModule module) {
		String signatureId = module.getSignature().getSignatureId();

		List<AlloyFormula> object_constraints = new LinkedList<AlloyFormula>();
		for (JObjectConstraint relevant_object_invariant : module.getObjectConstraints()) {

			QFtransformer qft = new QFtransformer(this.getVarsToPrefix());
			FormulaMutator fv = new FormulaMutator(qft);

			AlloyFormula invariantFormula = relevant_object_invariant.getFormula();
			invariantFormula = (AlloyFormula)invariantFormula.accept(fv);

			object_constraints.add(invariantFormula);
		}
		if (!object_constraints.isEmpty()) {
			AlloyFormula object_constraint_formula = AndFormula.buildAndFormula(object_constraints.<AlloyFormula> toArray(new AlloyFormula[] {}));

			String objectConstraintId = String.format("%s_object_constraint", signatureId);

			PredicateFormula invariantPred = formulaWrapper.wrapFormula(objectConstraintId, object_constraint_formula);

			object_constraint_predicates.put(signatureId, invariantPred);

		}
	}

	private Set<AlloyFormula> extract_object_constraint_formulas(JDynAlloyModule module) {
		Set<AlloyFormula> object_constraint_formulas = new HashSet<AlloyFormula>();

		String signatureId = module.getSignature().getSignatureId();

		PredicateFormula object_constraint_pred = object_constraint_predicates.get(signatureId);

		if (object_constraint_pred != null) {

			JFormulaMutator mutator = new JFormulaMutator(new ThizToObjXExprMutator());

			object_constraint_pred = (PredicateFormula) object_constraint_pred.accept(mutator);

			AlloyVariable objx = new AlloyVariable("objx", false);

			if (containsVariable(objx, object_constraint_pred)) {

				List<String> names = Collections.<String> singletonList(objx.toString());
				List<AlloyExpression> sets = Collections
						.<AlloyExpression> singletonList(ExprConstant.buildExprConstant(module.getSignature().getSignatureId()));

				AlloyFormula formula = new QuantifiedFormula(Operator.FOR_ALL, names, sets, object_constraint_pred);

				object_constraint_formulas.add(formula);
			} else
				object_constraint_formulas.add(object_constraint_pred);
		}

		return object_constraint_formulas;
	}

	private static String javaLangPackage() {
		if (JDynAlloyConfig.getInstance().getUseQualifiedNamesForJTypes()) {
			return "java_lang_";
		} else {
			return "";
		}
	}
	
	@Override
	public String getFullyQualifiedMethodName(){
		String className = this.classToCheck.replace('.', '_');
		String classPlusMethod = className + "_" + this.methodToCheck;
		return classPlusMethod;
	}
	
	
	
	
}

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
package ar.edu.jdynalloy.modifies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ar.edu.jdynalloy.JDynAlloyConfig;
import ar.edu.jdynalloy.JDynAlloyException;
import ar.edu.jdynalloy.JDynAlloyNotImplementedYetException;
import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JAssert;
import ar.edu.jdynalloy.ast.JAssignment;
import ar.edu.jdynalloy.ast.JAssume;
import ar.edu.jdynalloy.ast.JBlock;
import ar.edu.jdynalloy.ast.JCreateObject;
import ar.edu.jdynalloy.ast.JField;
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
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.ast.JWhile;
import ar.edu.jdynalloy.binding.symboltable.FieldDescriptor;
import ar.edu.jdynalloy.binding.symboltable.SymbolTable;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.factory.JTypeFactory;
import ar.edu.jdynalloy.xlator.JDynAlloyBinding;
import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.IFormulaVisitor;

/**
 * Turns the {@code modifies} clauses of the method under analysis into frame conditions, extra
 * {@code ensures}: the visitor that {@link ModifiesSolverManager} runs on the module of the class
 * under analysis. {@link #visit(JDynAlloyModule)} returns a new module with each program visited;
 * {@link #visit(JProgramDeclaration)} rebuilds only the program whose
 * {@code <signature>_<program>_} starts {@code methodToCheck}, with its specification cases
 * visited ({@link #visit(JSpecCase)}), and TACO's names never match that test
 * (docs/KNOWN-LIMITATIONS.md, "{@code modifies} clauses add no frame conditions";
 * docs/OPEN-QUESTIONS.md, item 2). Every other node is returned as it is. So, in practice, the
 * result is a new module with the same programs. An unused, reduced copy of this class is in the
 * {@code slicer} package (docs/OPEN-QUESTIONS.md, item 7).
 *
 * <p>The frame conditions it would add are {@code p.g = p.g'} for the fields {@code g} of a
 * parameter {@code p} that the clauses do not name (comitaco's {@code thiz.b = thiz.b'}); it does
 * not use {@link AbstractModifiesCase}.
 *
 * <p>State: the module and the name of the program being visited, and the symbol table it is
 * given, in which {@code visit(JDynAlloyModule)} declares {@code thiz} and
 * {@code visit(JProgramDeclaration)} the parameters, each in a scope of its own. Reads
 * {@code JDynAlloyConfig.getInstance()}. No static state.
 */
public class ReplaceModifiesModuleVisitor extends JDynAlloyVisitor {

	private JDynAlloyBinding dynJAlloyBinding;
	private JDynAlloyModule jDynAlloyModule;
	private SymbolTable symbolTable;
	private String methodUnderAnalyisName = "";

	private boolean isStaticProgram = false;

	public JDynAlloyBinding getDynJAlloyBinding() {
		return dynJAlloyBinding;
	}

	
	/**
	 * Creates a visitor without bindings or symbol table. Unused; {@link #visit(JDynAlloyModule)}
	 * would fail on the missing symbol table.
	 *
	 * @param isJavaArith passed to {@code JDynAlloyVisitor}
	 */
	public ReplaceModifiesModuleVisitor(boolean isJavaArith) {
		super(isJavaArith);
	}
		


	//We assume a modifies clause is followed by a list containing class fields and method parameter's attributes (param.field). 
	/**
	 * Creates the visitor that {@code ModifiesSolverManager} uses.
	 *
	 * @param dynJAlloyBinding stored, never read (TACO passes null)
	 * @param symbolTable the fields of all the modules; the visitor declares variables in it
	 * @param methodUnderAnalysisName not used: {@link #visit(JDynAlloyModule)} replaces it with the
	 *        id of each program before visiting it
	 * @param isJavaArith passed to {@code JDynAlloyVisitor}
	 */
	public ReplaceModifiesModuleVisitor(JDynAlloyBinding dynJAlloyBinding, SymbolTable symbolTable, String methodUnderAnalysisName, boolean isJavaArith) {
		super(isJavaArith);
		this.dynJAlloyBinding = dynJAlloyBinding;
		this.jDynAlloyModule = null;
		this.symbolTable = symbolTable;
		this.methodUnderAnalyisName = methodUnderAnalysisName;
	}

	public String getMethodUnderAnalyisName(){
		return this.methodUnderAnalyisName;
	}

	public void setMethodUnderAnalyisName(String name){
		this.methodUnderAnalyisName = name;
	}

	public JDynAlloyModule getJDynAlloyModule() {
		if (jDynAlloyModule == null) {
			throw new JDynAlloyException("jDynAlloyModule wasn't set");
		}
		return jDynAlloyModule;
	}


	/**
	 * Returns the specification case with its frame conditions as one more {@code ensures}, or the
	 * case itself when there are none to add. Reached only from {@link #visit(JProgramDeclaration)},
	 * which in practice visits no specification case.
	 * <ul>
	 * <li>With no clause, or with one that modifies everything, returns {@code node}.</li>
	 * <li>Each clause must be {@code v.f} or {@code arrayAccess(v.f, ...)}, with {@code v} a
	 *     variable; {@code f} is recorded as modifiable for {@code v}.</li>
	 * <li>For each parameter {@code p} of the programs with the id being visited (all the overloads),
	 *     and each receiver {@code v} whose name contains {@code p}'s name as a substring, it adds
	 *     the conjunction of {@code p.g = p.g'} over the fields {@code g} declared in the module of
	 *     {@code p}'s type (not inherited ones) that are not modifiable for {@code p} and do not
	 *     start with {@code SK_jml_pred_java_primitive}.</li>
	 * </ul>
	 * The result is a new {@code JSpecCase} with the same {@code requires} and {@code modifies}. It
	 * removes {@code null} from the type of each such parameter in place
	 * ({@code JType.setAsNonNull()}), so the program's declaration changes too.
	 *
	 * @param node the specification case
	 * @return a new case, or {@code node}
	 * @throws JDynAlloyException for a clause of another form ("Modifies expression ... not
	 *         supported." or "... includes a non-supported function.")
	 */
	@Override
	public Object visit(JSpecCase node) {
		boolean modifiesEverything = false;
		for (JModifies jModifies : node.getModifies()) {
			if (jModifies.isModifiesEverything()) {
				modifiesEverything = true;
			} 
		}

		//In this case it is not necessary to add any new constraints
		if (modifiesEverything) {
			return node;
		}

		List<JPostcondition> ensuresVec = new ArrayList<JPostcondition>();
		ensuresVec.addAll(node.getEnsures());

		// fields set
		Set<FieldDescriptor> fieldsSet = this.getSymbolTable().getFieldSet(this.jDynAlloyModule.getModuleId());

		// Assume as the default value for the modifies clause, "modifiesEverything".
		Set<String> modifiableFielsdSet = new HashSet<String>();
		if (node.getModifies().isEmpty()) {
			return node;
		}


		List<AbstractModifiesCase> modifiesCases = new ArrayList<AbstractModifiesCase>();
		modifiesCases.add(new FieldModifiesCase(this.getSymbolTable()));
		modifiesCases.add(new FieldArraysModifiesCase(this.getSymbolTable()));

		HashMap<JType, HashSet<String>> mapFromTypesToFields = new HashMap<JType, HashSet<String>>();
		for (FieldDescriptor fd : this.symbolTable.getFieldSet()){
			String moduleName = fd.getType();
			JType jt = new JType(moduleName);
			if (mapFromTypesToFields.containsKey(jt)){
				mapFromTypesToFields.get(jt).add(fd.getFieldName());
			} else {
				HashSet<String> theNewSet = new HashSet<String>();
				theNewSet.add(fd.getFieldName());
				mapFromTypesToFields.put(jt, theNewSet);
			}
		}


		HashMap<VariableId,JType> mapFromParamsToTypes = new HashMap<VariableId,JType>();
		for (JProgramDeclaration program : this.jDynAlloyModule.getPrograms()){
			if (program.getProgramId().equals(this.getMethodUnderAnalyisName())){
				for (JVariableDeclaration varDecl : program.getParameters()){
					mapFromParamsToTypes.put(varDecl.getVariable().getVariableId(), varDecl.getType());
				}
			}
		}


		// store info
		AlloyFormula accumlatorFormula = null;

		HashMap<String,HashSet<String>> mapFromParamsToModifiableFields = new HashMap<String,HashSet<String>>();
		for (JModifies jModifies : node.getModifies()) {
			AlloyExpression e = jModifies.getLocation();
			if (e instanceof ExprJoin) {
				String var = ((ExprVariable)((ExprJoin) e).getLeft()).getVariable().getVariableId().getString();
				String fieldName = ((ExprJoin) e).getRight().toString();
				if (mapFromParamsToModifiableFields.containsKey(var)){
					mapFromParamsToModifiableFields.get(var).add(fieldName);
				} else {
					HashSet<String> newSetOfFields = new HashSet<String>();
					newSetOfFields.add(fieldName);
					mapFromParamsToModifiableFields.put(var, newSetOfFields);
				}
			} else if (e instanceof ExprFunction) {
				if (((ExprFunction)e).getFunctionId().equals("arrayAccess")){
					assert (((ExprFunction) e).getParameters().get(0) instanceof ExprJoin);
					ExprJoin join = (ExprJoin) ((ExprFunction) e).getParameters().get(0);
					String var = ((ExprVariable)join.getLeft()).getVariable().getVariableId().getString();
					String fieldName = join.getRight().toString();
					if (mapFromParamsToModifiableFields.containsKey(var)){
						mapFromParamsToModifiableFields.get(var).add(fieldName);
					} else {
						HashSet<String> newSetOfFields = new HashSet<String>();
						newSetOfFields.add(fieldName);
						mapFromParamsToModifiableFields.put(var, newSetOfFields);
					}
				} else {
					throw new JDynAlloyException("Modifies expression " + e.toString() + " includes a non-supported function.");
				}
			} else {
				throw new JDynAlloyException("Modifies expression " + e.toString() + " not supported.");
			}



			//			AlloyFormula thisIterationFormula = processJModifies(modifiesCases, jModifies, modifiableFielsdSet);
			//			if (accumlatorFormula == null) {
			//				accumlatorFormula = thisIterationFormula;
			//			} else {
			//				accumlatorFormula = AndFormula.buildAndFormula(accumlatorFormula, thisIterationFormula);
			//			}
		}

		AlloyFormula accumulatorFormula = null;
		for (VariableId param : mapFromParamsToTypes.keySet()){
			for (String e : mapFromParamsToModifiableFields.keySet()){
				if (e.contains(param.getString())){
					AlloyFormula thisIterationFormula = buildModifiesFormula(param, mapFromParamsToModifiableFields, mapFromParamsToTypes, mapFromTypesToFields);
					if (accumlatorFormula == null){
						accumlatorFormula = thisIterationFormula;
					} else {
						accumlatorFormula = AndFormula.buildAndFormula(accumlatorFormula, thisIterationFormula);
					}
				}
			}
		}


//		if (!isStaticProgram) {
//			for (FieldDescriptor fieldDescriptor : fieldsSet) {
//				AlloyFormula thisIterationFormula = null;
//				if (!modifiableFielsdSet.contains(fieldDescriptor.getFieldName())) {
//					thisIterationFormula = unmodifiableFieldSupport(fieldDescriptor);
//				}
//
//				if (thisIterationFormula != null) {
//					if (accumlatorFormula == null) {
//						accumlatorFormula = thisIterationFormula;
//					} else {
//						accumlatorFormula = AndFormula.buildAndFormula(accumlatorFormula, thisIterationFormula);
//					}
//				}
//
//			}
//		}

		if (accumlatorFormula != null) {
			ensuresVec.add(new JPostcondition(accumlatorFormula));
		}

		return new JSpecCase(node.getRequires(), ensuresVec, node.getModifies());
	}

	private AlloyFormula buildModifiesFormula(
			VariableId param, HashMap<String, HashSet<String>> mapFromParamsToModifiableFields,
			HashMap<VariableId, JType> mapFromParamsToTypes,
			HashMap<JType, HashSet<String>> mapFromTypesToFields) {

		JType paramType = mapFromParamsToTypes.get(param);
		paramType.setAsNonNull();
		HashSet<String> allFieldsInParamType = mapFromTypesToFields.get(paramType);
		HashSet<String> allModifiableFieldsInParam = mapFromParamsToModifiableFields.get(param.getString());

		AlloyFormula theFormula = null;
		for (String fname : allFieldsInParamType){
			if (!allModifiableFieldsInParam.contains(fname) && !fname.startsWith("SK_jml_pred_java_primitive")){
				AlloyExpression fieldNameExpression = new ExprVariable(new AlloyVariable(fname));
				AlloyExpression paramExpression;
				if (param.getString().equals("thiz")){
					paramExpression = JExpressionFactory.THIS_EXPRESSION;
				} else {
					paramExpression = new ExprVariable(new AlloyVariable(param));
				}
				AlloyExpression leftSide = new ExprJoin(paramExpression,fieldNameExpression);
				
				AlloyExpression primmedfieldNameExpression = primeExpression(fieldNameExpression);
				AlloyExpression rightSide = new ExprJoin(paramExpression, primmedfieldNameExpression);
				AlloyFormula newEqualityFormula = new EqualsFormula(leftSide, rightSide);					
				if (theFormula == null){
					theFormula = newEqualityFormula;
				} else {
					theFormula = AndFormula.buildAndFormula(theFormula,newEqualityFormula);
				}
			}
		}
		return theFormula;
	}

	private AlloyExpression primeExpression(AlloyExpression expression) {

		if (expression instanceof ExprVariable) {
			ExprVariable exprVariable = (ExprVariable) expression;
			return new ExprVariable(new AlloyVariable(exprVariable.getVariable().getVariableId(),true));
		} else if (expression instanceof ExprJoin) {
			ExprJoin exprJoin = (ExprJoin) expression;
			AlloyExpression left = primeExpression(exprJoin.getLeft());
			AlloyExpression right = primeExpression(exprJoin.getRight());
			return ExprJoin.join(left, right);
		} else if (expression instanceof ExprConstant) {
			return expression;
		} else {
			throw new JDynAlloyNotImplementedYetException();
		}
		
	}


	private AlloyFormula processJModifies(List<AbstractModifiesCase> modifiesCases, JModifies modifies, Set<String> modifiablesFieldSet) {
		Iterator<AbstractModifiesCase> iterator = modifiesCases.iterator();

		AlloyFormula alloyFormula = null;
		do {
			AbstractModifiesCase abstractModifiesCase = iterator.next();
			if (abstractModifiesCase.accepts(modifies)) {
				alloyFormula = abstractModifiesCase.generateFormula(modifies);
				modifiablesFieldSet.addAll(abstractModifiesCase.getModifiableFieldSet());
			}
		} while (iterator.hasNext() && alloyFormula == null);

		if (alloyFormula == null) {
			throw new JDynAlloyException("JModifies case not implemented");
		}

		return alloyFormula;
		// for (AbstractModifiesCase abstractModifiesCase : modifiesCases) {
		// if (abstractModifiesCase.generateFormula(modifies)
		// }
	}

	private AlloyFormula unmodifiableFieldSupport(FieldDescriptor fieldDescriptor) {
		AlloyFormula thisIterationFormula;
		thisIterationFormula = ModifiesUtils.createFormulaFieldValueEqualsToOldFieldValue("thiz", fieldDescriptor.getFieldName());
		return thisIterationFormula;
	}

	@Override
	public Object visit(JHavoc n) {
		return n;
	}

	@Override
	public Object visit(JAssert n) {
		return n;
	}

	@Override
	public Object visit(JAssignment n) {
		return n;
	}

	@Override
	public Object visit(JBlock n) {
		return n;
	}

	@Override
	public Object visit(JIfThenElse n) {
		return n;
	}

	@Override
	public Object visit(JSkip n) {
		return n;
	}

	@Override
	public Object visit(JWhile n) {
		return n;
	}

	@Override
	public Object visit(JCreateObject n) {
		return n;
	}

	@Override
	public Object visit(JVariableDeclaration n) {
		return n;
	}

	/**
	 * Returns a new module with the parts of {@code node} (the same objects, not copies) and its
	 * programs visited, in a new {@code HashSet}. Before it visits each program it sets the name of
	 * the program being visited to the program's id; for the visit it declares {@code thiz}, of the
	 * module's type, in a new scope of the symbol table. When {@code node} has a class singleton, the
	 * new module's literal singleton is set to it, and then back to {@code node}'s literal singleton
	 * if there is one; so a module with a class singleton and no literal singleton gets its class
	 * singleton as literal singleton too. Modules built with {@code DynJMLAlloyModuleBuffer}, as
	 * TACO's classes are, have no class singleton.
	 *
	 * @param node the module
	 * @return the new module
	 */
	@Override
	public Object visit(JDynAlloyModule node) {

		symbolTable.beginScope();
		JType jtype = JTypeFactory.buildReference(node.getModuleId());
		this.symbolTable.insertLocal(JExpressionFactory.THIS_VARIABLE.getVariableId(), jtype);

		jDynAlloyModule = node;
		Set<JProgramDeclaration> programs = new HashSet<JProgramDeclaration>();
		for (JProgramDeclaration programDeclaration : node.getPrograms()) {
			this.setMethodUnderAnalyisName(programDeclaration.getProgramId());
			programs.add((JProgramDeclaration) programDeclaration.accept(this));
		}

		JDynAlloyModule module = new JDynAlloyModule(node.getModuleId(), node.getSignature(), node.getClassSingleton(), node.getLiteralSingleton(), 
				node.getFields(), node.getClassInvariants(), node.getClassConstraints(), 
				node.getObjectInvariants(), node.getObjectConstraints(), node.getRepresents(), programs, 
				node.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants(), node.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants(),
				node.pinnedForNonRelevancyAnalysisForStryker);

		if (node.getClassSingleton() != null) {
			module.setLiteralSingleton(node.getClassSingleton());
		}

		if (node.getLiteralSingleton() != null) {
			module.setLiteralSingleton(node.getLiteralSingleton());
		}

		symbolTable.endScope();

		return module;
	}

	@Override
	public Object visit(JProgramCall n) {
		return n;
	}

	/**
	 * For the method under analysis, returns a new program with the same parts and its
	 * specification cases visited ({@link #visit(JSpecCase)}), with its parameters (but the first,
	 * {@code thiz}, of an instance program) declared in a new scope of the symbol table; for any
	 * other program, the program itself. The method under analysis is the program with
	 * {@code methodToCheck.startsWith(<signature>_<program>_)}, for the configuration's
	 * {@code methodToCheck}. With TACO's names {@code <program>} already starts with
	 * {@code <signature>_} and {@code methodToCheck} is {@code <program>(<types>)}, so no program
	 * matches (docs/NAMING.md; docs/KNOWN-LIMITATIONS.md, "{@code modifies} clauses add no frame
	 * conditions").
	 *
	 * @param node the program
	 * @return the new program, or {@code node}
	 */
	@Override
	public Object visit(JProgramDeclaration node) {

		String methodToCheck = JDynAlloyConfig.getInstance().getMethodToCheck();
		String qualifiedMethodName = node.getSignatureId() + "_" + node.getProgramId() + "_";
		if (methodToCheck.startsWith(qualifiedMethodName)) {

			symbolTable.beginScope();

			boolean firstParameter = true;
			for (JVariableDeclaration variableDeclaration : node.getParameters()) {
				// "thiz" variable was added to SymbolTable when the method was
				// started to be analized
				// We need to skip "thiz" analysis to avoid redeclare variable
				// (only
				// for non-static methods)
				if (!node.isStatic() && firstParameter) {
					assert (variableDeclaration.getVariable().equals(JExpressionFactory.THIS_EXPRESSION.getVariable()));
					firstParameter = false;
					// skip "thiz" variable
				} else {
					this.symbolTable.insertLocal(variableDeclaration.getVariable().getVariableId(), variableDeclaration.getType());
				}
			}

			this.isStaticProgram = node.isStatic();

			Vector<JSpecCase> specCasesResult = new Vector<JSpecCase>();
			for (JSpecCase child : node.getSpecCases()) {
				JSpecCase specCase = (JSpecCase) child.accept(this);
				specCasesResult.add(specCase);
			}

			symbolTable.endScope();

			// dynJAlloyBinding.
			return new JProgramDeclaration(node.isVirtual(), node.isConstructor(), node.isPure(),
					node.getSignatureId(), node.getProgramId(), node.getParameters(), specCasesResult, 
					node.getBody(), node.getVarsResultOfArithmeticOperationsInRequiresAndEnsures(), 
					node.getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures());
		} else {
			return node;
		}

	}

	@Override
	public Object visit(JSignature node) {
		return node;
	}

	@Override
	public Object visit(JField node) {
		return node;
	}

	@Override
	public Object visit(JAssume n) {
		return n;
	}

	@Override
	public Object visit(JLoopInvariant n) {
		return n;
	}

	@Override
	public Object visit(JObjectInvariant n) {
		return n;
	}

	@Override
	public Object visit(JObjectConstraint n) {
		return n;
	}

	@Override
	public Object visit(JRepresents n) {
		return n;
	}

	@Override
	public Object visit(JPostcondition n) {
		return n;
	}

	@Override
	public Object visit(JPrecondition n) {
		return n;
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

}

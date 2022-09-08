package ar.edu.jdynalloy.ast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import ar.edu.jdynalloy.xlator.JType;
import ar.edu.taco.simplejml.builtin.JavaPrimitiveCharValue;
import ar.edu.taco.simplejml.builtin.JavaPrimitiveFloatValue;
import ar.edu.taco.simplejml.builtin.JavaPrimitiveIntegerValue;
import ar.edu.taco.simplejml.builtin.JavaPrimitiveLongValue;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;
import ar.uba.dc.rfm.alloy.util.LiteralCollector;
import ar.uba.dc.rfm.alloy.util.LiteralCollectorExpVisitor;
import ar.uba.dc.rfm.alloy.util.QFtransformer;

public class JDynAlloyVisitor implements IJDynAlloyVisitor {

	public Object inputToFix;

	public String specFromMethod = null;

	public List<JVariableDeclaration> formalParameterNames;

	public boolean isJavaArithmetic = false;


	protected static class JDynAlloyModuleVisitResult {

		public Object signature_result;

		public Object class_singleton_result;

		public Object literal_singleton_result;

		public Object fields_result;

		public Object class_invariants_result;

		public Object class_constraints_result;

		public Object object_invariants_result;

		public Object object_constraints_result;

		public Object represents_result;

		public Object programs_result;

		public Object predsEncodingArithmeticConstraintsFromObjectInvariant;

		public Object varsUsedInArithmeticConstraintsFromObjectInvariant;

	}


	public JDynAlloyVisitor(boolean isJavaArithmetic) {
		this.isJavaArithmetic = isJavaArithmetic;	
	}

	public Object visit(JAssert n) {
		return null;
	}

	public Object visit(JCreateObject n) {
		return null;
	}

	public Object visit(JAssignment n) {
		return null;
	}

	public Object visit(JBlock n) {
		Vector<Object> result = new Vector<Object>();
		for (JStatement s : n.getBlock()) {
			result.add(s.accept(this));
		}
		return result;
	}

	public Object visit(JIfThenElse n) {
		Vector<Object> result = new Vector<Object>();
		result.add(n.getThen().accept(this));
		result.add(n.getElse().accept(this));
		return result;
	}

	public Object visit(JSkip n) {
		return null;
	}

	public Object visit(JWhile n) {
		Vector<Object> result = new Vector<Object>();

		if (n.getLoopInvariant() == null)
			result.add(null);
		else
			result.add(n.getLoopInvariant().accept(this));

		result.add(n.getBody().accept(this));

		return result;
	}

	public Object visit(JVariableDeclaration n) {
		return null;
	}

	public Object visit(JProgramCall n) {
		return null;
	}

	public Object visit(ar.edu.jdynalloy.ast.JAssertionDeclaration assertion) {
		throw new IllegalArgumentException(
				"class JAlloyAssertion not supported");
	}

	public Object visit(JAlloyCondition condition) {
		throw new IllegalArgumentException(
				"class JAlloyCondition not supported");
	}

	public Object visit(JProgramDeclaration node) {

		this.specFromMethod = node.getProgramId();

		Vector<Object> result = new Vector<Object>();

		Vector<Object> varResults = new Vector<Object>();
		for (JVariableDeclaration child : node.getParameters()) {
			varResults.add(child.accept(this));
		}
		result.add(varResults);

		Vector<Object> specCasesResult = new Vector<Object>();
		for (JSpecCase child : node.getSpecCases()) {
			specCasesResult.add(child.accept(this));
		}
		result.add(specCasesResult);

		Object bodyResult = node.getBody().accept(this);
		result.add(bodyResult);

		Object predsFromRequiresAndEnsures = node.getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures();
		result.add(predsFromRequiresAndEnsures);

		Object varsFromRequiresAndEnsures = node.getVarsResultOfArithmeticOperationsInRequiresAndEnsures();
		result.add(varsFromRequiresAndEnsures);

		return result;
	}

	public Object visit(JDynAlloyModule node) {

		JDynAlloyModuleVisitResult result = new JDynAlloyModuleVisitResult();

		/*
		 * signature
		 */
		Object signature_result = node.getSignature().accept(this);
		result.signature_result = signature_result;

		/*
		 * class singleton
		 */

		if (node.getClassSingleton() != null) {
			Object class_singleton_result = node.getClassSingleton().accept(
					this);
			result.class_singleton_result = class_singleton_result;
		}

		/*
		 * literal singleton
		 */
		if (node.getLiteralSingleton() != null) {
			Object literal_singleton_result = node.getLiteralSingleton()
					.accept(this);
			result.literal_singleton_result = literal_singleton_result;
		}

		/*
		 * fields 
		 */
		Vector<Object> field_results = new Vector<Object>();
		for (JField child : node.getFields()) {
			field_results.add(child.accept(this));
		}
		result.fields_result = field_results;

		/*
		 * class invariants
		 */
		Vector<Object> class_invariants_result = new Vector<Object>();
		for (JClassInvariant child : node.getClassInvariants()) {
			class_invariants_result.add(child.accept(this));
		}
		result.class_invariants_result = class_invariants_result;

		/*
		 * class constraints
		 */
		Vector<Object> class_constraints_result = new Vector<Object>();
		for (JClassConstraint child : node.getClassConstraints()) {
			class_constraints_result.add(child.accept(this));
		}
		result.class_constraints_result = class_constraints_result;

		/*
		 * object invariants
		 */
		Vector<Object> object_invariant_results = new Vector<Object>();
		for (JObjectInvariant child : node.getObjectInvariants()) {
			object_invariant_results.add(child.accept(this));
		}
		result.object_invariants_result = object_invariant_results;
		result.predsEncodingArithmeticConstraintsFromObjectInvariant = node.getPredsEncodingValueOfArithmeticOperationsInObjectInvariants();
		result.varsUsedInArithmeticConstraintsFromObjectInvariant = node.getVarsEncodingValueOfArithmeticOperationsInObjectInvariants();



		/*
		 * object constraints
		 */
		Vector<Object> object_constraints_result = new Vector<Object>();
		for (JObjectConstraint child : node.getObjectConstraints()) {
			object_constraints_result.add(child.accept(this));
		}
		result.object_constraints_result = object_constraints_result;

		/*
		 * represents
		 */
		Vector<Object> represents_results = new Vector<Object>();
		for (JRepresents child : node.getRepresents()) {
			represents_results.add(child.accept(this));
		}
		result.represents_result = represents_results;

		/*
		 * programs
		 */
		Vector<Object> program_results = new Vector<Object>();
		for (JProgramDeclaration child : node.getPrograms()) {
			Object prgResult = child.accept(this);
			program_results.add(prgResult);
		}
		result.programs_result = program_results;


		return result;
	}

	public Object visit(JSignature node) {
		return null;
	}

	@Override
	public Object visit(JField node) {
		Vector<Object> result = new Vector <Object>();
		result.add(node.getFieldVariable());
		result.add(node.getFieldType());
		return result;
	}

	@Override
	public Object visit(JAssume n) {
		return null;
	}

	@Override
	public Object visit(JLoopInvariant n) {
		return null;
	}

	@Override
	public Object visit(JObjectInvariant node) {
		return null;
	}

	@Override
	public Object visit(JObjectConstraint node) {
		return null;
	}

	@Override
	public Object visit(JRepresents node) {
		return null;
	}

	@Override
	public Object visit(JPrecondition node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(JPostcondition node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(JHavoc n) {
		return null;
	}

	@Override
	public Object visit(JModifies node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(JSpecCase node) {
		Vector<Object> requiresResults = new Vector<Object>();

		if (inputToFix == null){

			for (JPrecondition requires : node.getRequires()) {
				Object result = requires.accept(this);
				requiresResults.add(result);
			}
		} else {
			String specFromMethod = this.specFromMethod;
			String fullyQualifiedMethodName = this.getFullyQualifiedMethodName();
			if (this.specFromMethod.equals(fullyQualifiedMethodName)){
				requiresResults = processInputToFix((HashMap<String, Object>)inputToFix, this.isJavaArithmetic);
				inputToFix = null;
			}
		}

		Vector<Object> ensuresResults = new Vector<Object>();
		for (JPostcondition ensures : node.getEnsures()) {
			Object result = ensures.accept(this);
			//			ensures.accept(this);
			ensuresResults.add(result);
		}

		Vector<Object> modifiesResults = new Vector<Object>();
		for (JModifies modifies : node.getModifies()) {
			Object result = modifies.accept(this);
			modifiesResults.add(result);
		}

		Vector<Object> result = new Vector<Object>();
		result.add(requiresResults);
		result.add(ensuresResults);
		result.add(modifiesResults);

		return result;
	}


	public String getFullyQualifiedMethodName() {
		// TODO Auto-generated method stub
		return null;
	}

	private Vector<Object> processInputToFix(HashMap<String, Object> inputToFix2, boolean isJavaArithmetic) {
		HashMap<Object, AlloyExpression> mapConcreteToExpre = new HashMap<Object, AlloyExpression>();

		String[] metSplit = this.specFromMethod.split("_");
		String methodName = metSplit[metSplit.length - 2];
		Method[] methods = inputToFix2.get("thiz").getClass().getDeclaredMethods();
		int numPars = 0;
		Method theMethod = null;
		for (Method m : methods){
			if (m.getName().equals(methodName)){
				numPars = m.getParameterTypes().length;
				theMethod = m;
				break;
			}
		}

		AlloyFormula fixedInputFormula = null;
		int index = 0;

		for (JVariableDeclaration vd : formalParameterNames){
			if (!vd.getVariable().getVariableId().getString().equals("throw")
					&& !vd.getVariable().getVariableId().getString().equals("return")
					&& !vd.getVariable().getVariableId().getString().startsWith("customvar")){

				if (isJavaArithmetic) {
					if (vd.getType().toString().equals("JavaPrimitiveIntegerValue")) {
						AlloyFormula af = new EqualsFormula(
								new ExprVariable(vd.getVariable()), 
								new ExprConstant("JavaPrimitiveIntegerValue", 
										JavaPrimitiveIntegerValue.getInstance().toJavaPrimitiveIntegerLiteral(((Integer)(inputToFix2.get(vd.getVariable().getVariableId().getString()))).intValue(), true).getConstantId()
										)
								);
						if (fixedInputFormula == null)
							fixedInputFormula = af;
						else
							fixedInputFormula = new AndFormula(fixedInputFormula, af);
					}
					if (vd.getType().toString().equals("JavaPrimitiveLongValue")) {
						AlloyFormula af = new EqualsFormula(
								new ExprVariable(vd.getVariable()), 
								new ExprConstant("JavaPrimitiveLongValue", 
										JavaPrimitiveLongValue.getInstance().toJavaPrimitiveLongLiteral(((Long)(inputToFix2.get(vd.getVariable().getVariableId().getString()))).longValue(), true).getConstantId()
										)
								);
						if (fixedInputFormula == null)
							fixedInputFormula = af;
						else
							fixedInputFormula = new AndFormula(fixedInputFormula, af);

					}
					if (vd.getType().toString().equals("JavaPrimitiveFloatValue")) {
						AlloyFormula af = new EqualsFormula(
								new ExprVariable(vd.getVariable()), 
								new ExprConstant("JavaPrimitiveFloatValue", 
										JavaPrimitiveFloatValue.getInstance().toJavaPrimitiveFloatLiteral(((Float)(inputToFix2.get(vd.getVariable().getVariableId().getString()))).floatValue(), true).getConstantId()
										)
								);
						if (fixedInputFormula == null)
							fixedInputFormula = af;
						else
							fixedInputFormula = new AndFormula(fixedInputFormula, af);

					}
					if (vd.getType().toString().equals("JavaPrimitiveCharValue")) {
						AlloyFormula af = new EqualsFormula(
								new ExprVariable(vd.getVariable()), 
								new ExprConstant("JavaPrimitiveCharValue", 
										JavaPrimitiveCharValue.getInstance().toJavaPrimitiveCharLiteral(((Character)(inputToFix2.get(vd.getVariable().getVariableId().getString()))).charValue(), true).getConstantId()
										)
								);
						if (fixedInputFormula == null)
							fixedInputFormula = af;
						else
							fixedInputFormula = new AndFormula(fixedInputFormula, af);

					}

				} else {
					if (vd.getType().toString().equals("Int")){
						AlloyFormula af = new EqualsFormula(
								new ExprVariable(vd.getVariable()), 
								new ExprConstant("Int", inputToFix2.get(vd.getVariable().getVariableId().getString()).toString())
								);
						if (fixedInputFormula == null)
							fixedInputFormula = af;
						else
							fixedInputFormula = new AndFormula(fixedInputFormula, af);
					}

				}

				if (!mapConcreteToExpre.containsKey(inputToFix2.get(vd.getVariable().getVariableId().getString()))) {
					AlloyFormula f = null;
					if (inputToFix2.get(vd.getVariable().getVariableId().getString()) != null){
						mapConcreteToExpre.put(inputToFix2.get(vd.getVariable().getVariableId().getString()), new ExprVariable(vd.getVariable()));
						f = processInputToFixToFormula(inputToFix2.get(vd.getVariable().getVariableId().getString()), mapConcreteToExpre, isJavaArithmetic);
					} else {
						f = new EqualsFormula(new ExprVariable(vd.getVariable()), new ExprConstant(vd.getType().toString(), "null"));
					}
					if (fixedInputFormula == null)
						fixedInputFormula = f;
					else
						fixedInputFormula = new AndFormula(fixedInputFormula, f);
				} else {
					AlloyFormula aff = new EqualsFormula(new ExprVariable(vd.getVariable()), mapConcreteToExpre.get(inputToFix2.get(vd.getVariable().getVariableId().getString())));
					if (fixedInputFormula == null)
						fixedInputFormula = aff;
					else
						fixedInputFormula = new AndFormula(fixedInputFormula, aff);
				}

				index++;

			}
		}

		Object[] obs = mapConcreteToExpre.keySet().toArray();

		for (int index1 = 0; index1 < obs.length; index1++){
			for (int index2 = index1+1; index2 < obs.length; index2++){
				if (obs[index1] == obs[index2]){
					AlloyFormula f = new EqualsFormula(mapConcreteToExpre.get(obs[index1]), mapConcreteToExpre.get(obs[index2]));
					if (fixedInputFormula == null)
						fixedInputFormula = f;
					else 
						fixedInputFormula = new AndFormula(fixedInputFormula, f);
				} else {
					AlloyFormula f = new NotFormula(new EqualsFormula(mapConcreteToExpre.get(obs[index1]), mapConcreteToExpre.get(obs[index2])));
					if (fixedInputFormula == null)
						fixedInputFormula = f;
					else 
						fixedInputFormula = new AndFormula(fixedInputFormula, f);
				}
			}
		}
		Vector<Object> resultVec = new Vector<Object>();
		resultVec.add(fixedInputFormula);
		return resultVec;
	}




	private static AlloyFormula processInputToFixToFormula(Object inputToFix2, HashMap<Object, 
			AlloyExpression> mapConcreteToExpre, boolean isJavaArithmetic) {
		AlloyFormula accumulator = null;
		if (inputToFix2 != null){
			Class<?> clazz = inputToFix2.getClass();

			if (!clazz.isPrimitive() && !isAutoboxingClass(clazz)) {

				Field[] fields = clazz.getDeclaredFields();
				if (fields.length > 0){
					for (Field f : fields){
						f.setAccessible(true);
						try {
							Object o = f.get(inputToFix2);
							if (o == null){
								AlloyFormula af = new EqualsFormula(new ExprJoin(mapConcreteToExpre.get(inputToFix2), new ExprVariable(new AlloyVariable(
										f.getDeclaringClass().getCanonicalName().replace(".", "_") + "_" + f.getName()
										)
										)
										), 
										new ExprVariable(new AlloyVariable("null"))
										);
								if (accumulator == null){
									accumulator = af;
								} else {
									accumulator = new AndFormula(accumulator, af);
								}
							} else if (o.getClass().isPrimitive()) {

								if (o.getClass().equals(int.class)){
									AlloyFormula af = new EqualsFormula(
											new ExprJoin(
													mapConcreteToExpre.get(inputToFix2), 
													new ExprVariable(new AlloyVariable(f.getDeclaringClass().getCanonicalName().replace(".", "_") + "_" + f.getName()))
													), 
											new ExprConstant("Int", o.toString())
											); 
									if (accumulator == null){
										accumulator = af;
									} else {
										accumulator = new AndFormula(accumulator, af);  
									}
								}
								
								


							}
							else {
								if (mapConcreteToExpre.containsKey(o)){
									AlloyFormula af = new EqualsFormula(new ExprJoin(mapConcreteToExpre.get(inputToFix2), 
											new ExprVariable(
													new AlloyVariable(
															f.getDeclaringClass().getCanonicalName().replace(".", "_") + "_" + f.getName()
															)
													)
											), 
											mapConcreteToExpre.get(o)
											);
									if (accumulator == null) {
										accumulator = af;
									} else {
										accumulator = new AndFormula(accumulator, af);  
									}
								} else {
									mapConcreteToExpre.put(o, new ExprJoin(mapConcreteToExpre.get(inputToFix2), 
											new ExprVariable(
													new AlloyVariable(
															f.getDeclaringClass().getCanonicalName().replace(".", "_") + "_" + f.getName()
															)
													)
											)
											);

									AlloyFormula af = processInputToFixToFormula(o, mapConcreteToExpre, isJavaArithmetic);  
									if (accumulator == null) {
										accumulator = af;
									} else {
										accumulator = new AndFormula(accumulator, af);  
									}
								}
							}
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}

					}
				} else {
					AlloyFormula af = new NotFormula(
							new EqualsFormula(mapConcreteToExpre.get(inputToFix2), new ExprVariable(
									new AlloyVariable("null")
									)
									)
							);
					if (accumulator == null){
						accumulator = af;
					} 
				}

			} else {
				// class is a primitive type or autoboxing
				AlloyFormula af = new EqualsFormula( 
						mapConcreteToExpre.get(inputToFix2),
						(AlloyExpression)getValueAsString(inputToFix2, isJavaArithmetic)
						);
				if (accumulator == null){
					accumulator = af;
				} 
			}
		} else {
			accumulator = 
					new EqualsFormula(mapConcreteToExpre.get(inputToFix2), 
							new ExprVariable(new AlloyVariable("null"))
							);
		}

		return accumulator;

	}

	private static AlloyExpression getValueAsString(Object o, boolean isJavaArith) {
		if (o.getClass().equals(int.class) || o.getClass().equals(Integer.class)){
			if (isJavaArith){
				return new ExprConstant("JavaPrimitiveIntegerValue", 
						JavaPrimitiveIntegerValue.getInstance().toJavaPrimitiveIntegerLiteral(((Integer)o).intValue(), true).toString()
						);
			} else {
				return new ExprConstant("Int", ((Integer)o).toString());
			}
		} 
		if (o.getClass().equals(long.class) || o.getClass().equals(Long.class)){
			if (isJavaArith){
				return new ExprConstant("JavaPrimitiveLongValue", 
						JavaPrimitiveLongValue.getInstance().toJavaPrimitiveLongLiteral(((Long)o).longValue(), true).toString()
						);
			} else {
				return new ExprConstant("Int", o.toString());
			}
		} 
		if (o.getClass().equals(float.class) || o.getClass().equals(Float.class)){
			if (isJavaArith){
				return new ExprConstant("JavaPrimitiveFloatValue", 
						JavaPrimitiveFloatValue.getInstance().toJavaPrimitiveFloatLiteral(((Float)o).floatValue(), true).toString()
						);
			} else {
				return null;
			}
		} 
		if (o.getClass().equals(char.class) || o.getClass().equals(Character.class)){
			if (isJavaArith){
				return new ExprConstant("JavaPrimitiveCharValue", 
						JavaPrimitiveCharValue.getInstance().toJavaPrimitiveCharLiteral(((Character)o).charValue(), true).toString()
						);
			} else {
				return null;
			}
		} 
		
		if (o.getClass().equals(boolean.class) || o.getClass().equals(Boolean.class)){
			return new ExprConstant("boolean", ((Boolean)o).toString());

		} 

		return null;
	}



	@Override
	public Object visit(JClassInvariant n) {
		return null;
	}

	@Override
	public Object visit(JClassConstraint n) {
		return null;
	}

	private static boolean isAutoboxingClass(Class<?> clazz) {
		boolean ret_Value = false;

		ret_Value |= Boolean.class.isAssignableFrom(clazz);
		ret_Value |= Byte.class.isAssignableFrom(clazz);
		ret_Value |= Character.class.isAssignableFrom(clazz);
		ret_Value |= Double.class.isAssignableFrom(clazz);
		ret_Value |= Float.class.isAssignableFrom(clazz);
		ret_Value |= Integer.class.isAssignableFrom(clazz);
		ret_Value |= Long.class.isAssignableFrom(clazz);
		ret_Value |= Short.class.isAssignableFrom(clazz);

		return ret_Value;
	}

}

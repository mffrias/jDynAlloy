package ar.edu.jdynalloy.xlator;

import static ar.edu.jdynalloy.factory.JDynAlloyFactory.block;
import static ar.edu.jdynalloy.factory.JDynAlloyFactory.ifThenElse;
import static ar.edu.jdynalloy.factory.JPredicateFactory.eq;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import ar.edu.jdynalloy.ast.JDynAlloyMutator;
import ar.edu.jdynalloy.ast.JIfThenElse;
import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.JStatement;
import ar.edu.jdynalloy.ast.JVariableDeclaration;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;

class VAlloyVisitor extends JDynAlloyMutator {

	public VAlloyVisitor(JDynAlloyBinding binding, boolean isJavaArith) {
		super(isJavaArith);
		this.binding = binding;
	}

	@Override
	public Object visit(JProgramDeclaration node) {
		String programId = resolveUniqueProgramId(node);
		JProgramDeclaration pd = (JProgramDeclaration) super.visit(node);
		JStatement body = null;
		if (node.isVirtual()) {
			List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
			for (JVariableDeclaration d : node.getParameters())
				ps.add(new ExprVariable(d.getVariable()));
			body = buildDispatcherBody(node, ps);
		} else {
			body = pd.getBody();
		}
		
		return new JProgramDeclaration(false, node.isConstructor(), node.isPure(), null, programId, pd
				.getParameters(), pd.getSpecCases(), body, node.getVarsResultOfArithmeticOperationsInRequiresAndEnsures(), node.getPredsEncodingValueOfArithmeticOperationsInRequiresAndEnsures());
	}

	private JStatement buildDispatcherBody(JProgramDeclaration node,
			List<AlloyExpression> parameters) {
		Vector<JIfThenElse> ifs = new Vector<JIfThenElse>();
		for (JProgramDeclaration implementor : binding.implementorsOf(node)) {
			String signatureId = implementor.getSignatureId();
			String programId = resolveUniqueProgramId(implementor);
			PredicateFormula eq = eq(JExpressionFactory
					.classOf(JExpressionFactory.THIS_EXPRESSION),
					JExpressionFactory.classSingleton(signatureId));
			JProgramCall call = new JProgramCall(false, programId, parameters);
			JIfThenElse ifThen = ifThenElse(eq, call);
			ifs.add(ifThen);
		}
		return block(ifs.toArray(new JStatement[] {}));
	}

	@Override
	public Object visit(JProgramCall n) {
		JProgramDeclaration d = binding.resolve(n);
		String uniqueProgramId = resolveUniqueProgramId(d);
		JProgramCall call = (JProgramCall) super.visit(n);
		return new JProgramCall(false, uniqueProgramId, call.getArguments());
	}

	private final IdentityHashMap<JProgramDeclaration, String> programs = new IdentityHashMap<JProgramDeclaration, String>();
	private final HashMap<String, Integer> prefixes = new HashMap<String, Integer>();

	private String resolveUniqueProgramId(JProgramDeclaration d) {
		if (!programs.containsKey(d)) {
			String prefix = d.getSignatureId() + "_" + d.getProgramId();
			if (!prefixes.containsKey(prefix))
				prefixes.put(prefix, -1);
			else {
				if (d.isPure()){
					List<JVariableDeclaration> pars = d.getParameters();
					boolean isThrowAParam = false;
					for (JVariableDeclaration j : pars){
						if (j.getType().toString().contains("java_lang_Throwable")){
							isThrowAParam = true;
						}
					}
					if (!isThrowAParam){
						String id = prefix + "_" + 0;
						prefixes.put(prefix, 0);
						programs.put(d, id);
						return programs.get(d);
					}
				}
			}
			int index = prefixes.get(prefix);
			index++;
//			String id = prefix + "_" + index;
			String id = prefix; //removed the _0 at the end of the method name
			prefixes.put(prefix, index);
			programs.put(d, id);
		}
		return programs.get(d);
	}

	private final JDynAlloyBinding binding;

}

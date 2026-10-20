package ar.edu.jdynalloy.ast;

import java.util.Vector;

/**
 * Collects the variables that statements declare. Its only use is
 * {@link JAlloyProgramBuffer#collectNewlyDefinedVars}, which runs it on each statement of the
 * body of a {@code do} loop and flattens the results, to rename those variables in the copy of
 * the body that goes inside the loop ({@link VariableNameChangerVisitor}).
 *
 * <p>A variable declaration returns a {@code Vector} with its {@code AlloyVariable}; blocks,
 * {@code if} and {@code while} return the nested {@code Vector}s of {@link JDynAlloyVisitor},
 * so the declarations inside them are found too; every other statement returns null. No static
 * state.
 */
public class JDynAlloyVarDeclarationCollector extends JDynAlloyVisitor {

	public JDynAlloyVarDeclarationCollector(boolean isJavaArithmetic){
		super(isJavaArithmetic);
	}


	public Object visit(JVariableDeclaration n) {
		Vector<Object> result = new Vector<Object>();
		result.add(n.getVariable());
		return result;
	}


}

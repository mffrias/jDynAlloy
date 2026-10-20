package ar.edu.jdynalloy.xlator;

import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JAssert;

/**
 * Tells whether the nodes it visits contain an {@code assert} statement: after a visit,
 * {@link #isAssertFound()} is true if one was met. The flag is never reset, so an instance
 * answers for everything it has visited. Unclear: nothing in jDynAlloy or TACO uses this class;
 * whether it is still needed is an open question. No static state.
 */
public class JDynAlloyAssertionFinderVisitor extends JDynAlloyVisitor {

	private boolean assertFound = false;
	
	public JDynAlloyAssertionFinderVisitor(boolean isJavaArithmetic) {
		super(isJavaArithmetic);
	}
	
		
	@Override
	public Object visit(JAssert n) {
		assertFound = true;
		return super.visit(n);
	}
	
	public boolean isAssertFound() {
		return assertFound;
	}
	
}

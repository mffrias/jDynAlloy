package ar.edu.jdynalloy.ast;


/**
 * A statement of a program's body: {@link JAssert}, {@link JAssume}, {@link JAssignment},
 * {@link JBlock}, {@link JCreateObject}, {@link JHavoc}, {@link JIfThenElse},
 * {@link JProgramCall}, {@link JSkip}, {@link JVariableDeclaration} or {@link JWhile} (their
 * syntax is in docs/LANGUAGE.md, "Statements"). TACO builds them from the Java bodies and the
 * parser from {@code .djals} files; {@code JDynAlloyXlatorVisitor} translates each into a
 * DynAlloy program (docs/ARCHITECTURE.md, "JDynAlloyXlatorVisitor: modules, programs,
 * statements").
 *
 * <p>Every subclass is final, with final fields, but the lists and expressions it holds are the
 * caller's, not copies. The visitors that change a body ({@link JDynAlloyMutator} and its
 * subclasses) build new statements; {@code clone()} always throws
 * {@code CloneNotSupportedException}. Every subclass defines {@code equals} and {@code hashCode}
 * by value, with the exceptions its own comment gives.
 */
public abstract class JStatement {
    public abstract Object accept(IJDynAlloyVisitor v);

	@Override
	protected final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	
    
    
}

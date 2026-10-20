package ar.edu.jdynalloy.ast;

/**
 * The visitor of the JDynAlloy AST: one {@code visit} per node class, and each node's
 * {@code accept(IJDynAlloyVisitor)} calls the one for its class and returns its result. The
 * formulas and expressions inside the nodes are dynalloy4's ({@code AlloyFormula},
 * {@code AlloyExpression}) and are visited with dynalloy4's visitors, not with this one. Its only
 * implementation is {@link JDynAlloyVisitor}, which every visitor of jDynAlloy and TACO extends;
 * see there for the traversal and the results. See docs/LANGUAGE.md for the nodes.
 */
public interface IJDynAlloyVisitor {

	Object visit(JAssertionDeclaration assertion);

	Object visit(JAlloyCondition condition);
	
	Object visit(JProgramDeclaration node);
	
	Object visit(JSignature node);

	Object visit(JField node);
	
	Object visit(JDynAlloyModule node);

	Object visit(JClassInvariant n);

	Object visit(JClassConstraint n);
	
	Object visit(JObjectInvariant node);
	
	Object visit(JObjectConstraint node);

	Object visit(JRepresents node);

	Object visit(JPrecondition node);
	
	Object visit(JPostcondition node);

	Object visit(JModifies node);
	
	Object visit(JSpecCase node);
	
	Object visit(JAssert n);
	Object visit(JAssignment n);
	Object visit(JBlock n);
	Object visit(JIfThenElse n);
	Object visit(JSkip n);
	Object visit(JWhile n);
	Object visit(JCreateObject n);
	Object visit(JVariableDeclaration n);
	Object visit(JProgramCall n);
	Object visit(JAssume n);
	Object visit(JLoopInvariant n);
	Object visit(JHavoc n);
	
	
}

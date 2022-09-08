package ar.edu.jdynalloy.ast;

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

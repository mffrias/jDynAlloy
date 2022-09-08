package ar.edu.jdynalloy.ast;


public abstract class JStatement {
    public abstract Object accept(IJDynAlloyVisitor v);

	@Override
	protected final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	
    
    
}

package ar.edu.jdynalloy.ast;

public final class JSkip extends JStatement {

	private static final int HASHCODE = 56;

	public final Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(JSkip.class)) {
			return true;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return HASHCODE;
	}

	@Override
	public String toString() {
		return "skip";
	}

}

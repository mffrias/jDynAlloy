package ar.edu.jdynalloy.ast;

/**
 * {@code skip;}: does nothing, and is translated into DynAlloy's {@code skip}. All instances are
 * equal. {@code JAlloyProgramBuffer} uses it for an empty branch or body, so an {@code if}
 * without {@code else} has {@code skip} there.
 */
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

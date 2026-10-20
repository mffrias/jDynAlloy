package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * {@code while F [loop_invariant I] { ... };}: a loop, with an optional loop invariant and branch
 * id. {@code JDynAlloyTransformationVisitor} replaces a loop that has an invariant before the
 * translation proper ({@link JLoopInvariant}). {@code JDynAlloyXlatorVisitor} translates the
 * others into DynAlloy's {@code while} with {@code nestedLoopUnroll}, and into
 * {@code (test F; body)*; test !F} otherwise; dynalloy4 unrolls both (docs/ARCHITECTURE.md,
 * "JDynAlloyXlatorVisitor: modules, programs, statements"). The branch id is printed as
 * {@code lblpos} and given by the transformation to the statements it adds, but the translation
 * of the loop itself does not use it.
 *
 * <p>{@code equals} and {@code hashCode} use the branch id, the condition and the body, not the
 * loop invariant.
 */
public final class JWhile extends JStatement {

	private final AlloyFormula condition;

	private final JStatement body;

	private final JLoopInvariant loopInvariant;

	private final String branchId;

	/**
	 * Creates a loop.
	 *
	 * @param condition the loop condition
	 * @param body the body
	 * @param loopInvariant the loop invariant, or null for none
	 * @param branchId the label of the loop, or null for none
	 */
	public JWhile(AlloyFormula condition, JStatement body,
			JLoopInvariant loopInvariant, String branchId) {
		super();
		this.condition = condition;
		this.body = body;
		this.loopInvariant = loopInvariant;
		this.branchId = branchId;
	}

	public final Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	public JStatement getBody() {
		return body;
	}

	public AlloyFormula getCondition() {
		return condition;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(JWhile.class)) {
			JWhile w = (JWhile) arg0;
			return (getBranchId() == null ? w.getBranchId() == null
					: getBranchId().equals(w.getBranchId()))
					&& getCondition().equals(w.getCondition())
					&& getBody().equals(w.getBody());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return getCondition().hashCode() + getBody().hashCode()
				+ (getBranchId() != null ? getBranchId().hashCode() : 0);
	}

	@Override
	public String toString() {
		return "while " + getCondition().toString() + " do "
				+ getBody().toString() + " endwhile";
	}

	public JLoopInvariant getLoopInvariant() {
		return loopInvariant;
	}

	public String getBranchId() {
		return branchId;
	}
}

package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public final class JWhile extends JStatement {

	private final AlloyFormula condition;

	private final JStatement body;

	private final JLoopInvariant loopInvariant;

	private final String branchId;

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

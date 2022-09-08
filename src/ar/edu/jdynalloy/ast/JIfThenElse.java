package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public final class JIfThenElse extends JStatement {

	private final AlloyFormula condition;

	private final JStatement trueStmt;

	private final JStatement falseStmt;

	private final String branchId;

	public JIfThenElse(AlloyFormula condition, JStatement trueStmt,
			JStatement falseStmt, String branchId) {
		super();
		this.condition = condition;
		this.trueStmt = trueStmt;
		this.falseStmt = falseStmt;
		this.branchId = branchId;
	}

	public final Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	public AlloyFormula getCondition() {
		return condition;
	}

	public JStatement getElse() {
		return falseStmt;
	}

	public JStatement getThen() {
		return trueStmt;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(JIfThenElse.class)) {
			JIfThenElse i = (JIfThenElse) arg0;
			return (getBranchId() != null ? getBranchId().equals(
					i.getBranchId()) : i.getBranchId() == null)
					&& getCondition().equals(i.getCondition())
					&& getThen().equals(i.getThen())
					&& getElse().equals(i.getElse());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return getCondition().hashCode() + getThen().hashCode()
				+ getElse().hashCode()
				+ (getBranchId() != null ? getBranchId().hashCode() : 0);
	}

	@Override
	public String toString() {
		return "if " + getCondition() + " then " + getThen() + " else "
				+ getElse() + " endif";
	}

	public String getBranchId() {
		return branchId;
	}

}

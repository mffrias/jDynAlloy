package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * {@code if F { ... } else { ... };}: a conditional, with an optional branch id.
 * {@code JDynAlloyXlatorVisitor} translates it into a choice between {@code test F} followed by
 * the then branch and {@code test !F} followed by the else branch, and, with a branch id, labels
 * the two tests with it (positive and negative). {@code JDynAlloyPrinter} prints the id as
 * {@code lblpos <id>}, which the parser cannot read back (docs/LANGUAGE.md, "The parser"). In
 * practice an id is null or a constant: TACO's {@code LabelUtils} and the parser's give
 * {@code if_1} to every {@code if} (docs/KNOWN-LIMITATIONS.md), and the {@code if}s that
 * {@code JDynAlloyTransformationVisitor} adds get the id of their loop. Equal when the branch
 * ids, the conditions and both branches are equal.
 */
public final class JIfThenElse extends JStatement {

	private final AlloyFormula condition;

	private final JStatement trueStmt;

	private final JStatement falseStmt;

	private final String branchId;

	/**
	 * Creates a conditional.
	 *
	 * @param condition the condition
	 * @param trueStmt the then branch
	 * @param falseStmt the else branch; {@code skip} when there is none
	 * @param branchId the label of the branch, or null for none
	 */
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

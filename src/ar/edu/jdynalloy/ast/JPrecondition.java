package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * A {@code requires {F}} of a {@link JSpecCase}: a formula over the state before the program.
 * With a fixed input, no {@code requires} is translated, and the method under analysis gets a
 * formula that fixes its input instead (docs/ARCHITECTURE.md, "Fixed input").
 *
 * <p>{@code setFormula} replaces the formula in place; only {@link JDynAlloyModule#prefixArgs()},
 * which has no caller, calls it. No {@code equals} or {@code hashCode}: compared by identity,
 * also in {@link JSpecCase#equals}.
 */
public class JPrecondition {

	public JPrecondition(AlloyFormula formula) {
		super();
		this.formula = formula;
	}

	private AlloyFormula formula;

	public AlloyFormula getFormula() {
		return formula;
	}

	public void setFormula(AlloyFormula f){
		this.formula = f;
	}
	public Object accept(IJDynAlloyVisitor visitor) {
		return visitor.visit(this);
	}
	
}

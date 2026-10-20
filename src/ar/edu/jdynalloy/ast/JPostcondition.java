package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * An {@code ensures {F}} of a {@link JSpecCase}: a formula over the state before the program
 * (plain names) and after it (primed names, {@code throw'}, {@code return'}).
 *
 * <p>{@code setFormula} replaces the formula in place; only {@link JDynAlloyModule#prefixArgs()},
 * which has no caller, calls it. No {@code equals} or {@code hashCode}: compared by identity,
 * also in {@link JSpecCase#equals}.
 */
public class JPostcondition {

	public JPostcondition(AlloyFormula formula) {
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

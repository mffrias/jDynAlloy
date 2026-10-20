package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * {@code assert F;}: a check in a program's body. {@code JDynAlloyXlatorVisitor} translates it
 * into a choice between {@code F} followed by {@code skip}, and {@code !F} followed by
 * {@code throw := java_lang_AssertionErrorLit}: a failed assert does not stop the execution, it
 * sets {@code throw} (docs/ARCHITECTURE.md, "JDynAlloyXlatorVisitor: modules, programs,
 * statements"). TACO emits it for Java and JML {@code assert} statements, and
 * {@code JDynAlloyTransformationVisitor} for loop invariants. {@code BindingManager} does not
 * bind the {@code callSpec} formulas in it, unlike those in {@link JAssume}. Equal when the
 * formulas are equal.
 */
public final class JAssert extends JStatement {

	private final AlloyFormula assertion;

	public JAssert(AlloyFormula assertion) {
		super();
		this.assertion = assertion;
	}

	public final Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	public AlloyFormula getCondition() {
		return assertion;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(JAssert.class)) {
			JAssert a = (JAssert) arg0;
			return getCondition().equals(a.getCondition());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return getCondition().hashCode();
	}

	@Override
	public String toString() {
		return "assertion " + getCondition().toString();
	}
}

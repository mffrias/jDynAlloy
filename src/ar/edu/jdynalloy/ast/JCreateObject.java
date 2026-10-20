package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.AlloyVariable;

/**
 * {@code createObject<C>[x];}: makes the variable {@code x} a new object of class {@code C}. TACO
 * translates {@code x = new C(...)} into this statement followed by
 * {@code call Constructor[x, ...]}. {@code JDynAlloyXlatorVisitor} translates it into
 * {@code getUnusedObject[x]} (an object not in {@code usedObjects}, which is then added to it)
 * followed by a test that the class of {@code x} is {@code C}: {@code classOf} with
 * {@code useClassSingletons}, {@code instanceOf} otherwise.
 *
 * <p>{@code equals} and {@code hashCode} compare only the variable, not the class.
 */
public final class JCreateObject extends JStatement {

	private final String signatureId;

	private final AlloyVariable lvalue;

	public JCreateObject(String signatureId, AlloyVariable lvalue) {
		super();
		this.signatureId = signatureId;
		this.lvalue = lvalue;
	}

	@Override
	public Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(JCreateObject.class)) {
			JCreateObject that = (JCreateObject) arg0;
			return this.getLvalue().equals(that.getLvalue());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return getLvalue().hashCode();
	}

	@Override
	public String toString() {
		return "createObject<" + this.getSignatureId() + ">["
				+ getLvalue().toString() + "]";
	}

	public AlloyVariable getLvalue() {
		return lvalue;
	}

	public String getSignatureId() {
		return signatureId;
	}

}

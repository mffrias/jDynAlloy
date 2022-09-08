package ar.edu.jdynalloy.ast;

import ar.uba.dc.rfm.alloy.AlloyVariable;

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

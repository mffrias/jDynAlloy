package ar.edu.jdynalloy.xlator;

import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaPrinter;

class PredicateDeclaration {
	private final String predicateId;

	private final List<AlloyVariable> formalParams;

	private final AlloyTyping typing;

	private final AlloyFormula formula;


	public PredicateDeclaration(String predicateId,
			List<AlloyVariable> signature, AlloyTyping t, AlloyFormula body) {
		super();
		this.predicateId = predicateId;
		this.formalParams = signature;
		this.typing = t;
		this.formula = body;
	}

	public AlloyFormula getFormula() {
		return formula;
	}

	public String getPredicateId() {
		return predicateId;
	}

	public List<AlloyVariable> getFormalParams() {
		return formalParams;
	}

	public AlloyTyping getTyping() {
		return typing;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null
				&& arg0.getClass().equals(PredicateDeclaration.class)) {
			PredicateDeclaration that = (PredicateDeclaration) arg0;
			return this.getPredicateId().equals(that.getPredicateId())
					&& getFormalParams().equals(that.getFormalParams())
					&& getTyping().equals(that.getTyping())
					&& getFormula().equals(that.getFormula());

		} else
			return false;
	}

	@Override
	public int hashCode() {
		return getPredicateId().hashCode() + getFormalParams().hashCode()
				+ getTyping().hashCode() + getFormula().hashCode();
	}

	public String toString(boolean prettyPrint) {
		StringBuffer buff = new StringBuffer();
		buff.append("pred " + this.getPredicateId());
		buff.append("[");

		if (prettyPrint)
			buff.append("\n");

		for (int i = 0; i < this.getFormalParams().size(); i++) {
			AlloyVariable v = this.getFormalParams().get(i);

			if (i != 0) {
				buff.append(",");
				if (prettyPrint)
					buff.append("\n");
			}

			String parameterDecl = v.toString() + ":"
					+ this.getTyping().get(v);

			if (prettyPrint)
				parameterDecl = "  " + parameterDecl;

			buff.append(parameterDecl);
		}

		if (prettyPrint && !this.getFormalParams().isEmpty())
			buff.append("\n");

		buff.append("]{");

		if (prettyPrint)
			buff.append("\n");

		JFormulaPrinter printer = new JFormulaPrinter();
		printer.setPrettyPrinting(prettyPrint);

		String formulaString = (String) this.getFormula().accept(printer);

		if (prettyPrint) {
			formulaString = increaseIndentation(formulaString);
		}

		buff.append(formulaString);

		if (prettyPrint)
			buff.append("\n");

		buff.append("}");
		buff.append("\n");

		return buff.toString();

	}

	@Override
	public String toString() {
		return toString(false);
	}
	
	public static String increaseIndentation(String string) {
		StringBuffer buffer = new StringBuffer();
		String[] lines = string.split("\n");
		for (String line : lines) {
			buffer.append("   " + line + "\n");
		}
		return buffer.toString();
	}

}
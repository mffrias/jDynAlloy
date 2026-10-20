package ar.edu.jdynalloy.xlator;

import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaPrinter;

/**
 * An Alloy predicate that {@link FormulaWrapper} declares when it wraps a formula: its id, its formal
 * parameters in order, their types and its body. {@code JDynAlloyXlatorVisitor.visit(JDynAlloyModule)}
 * prints every declaration of the module's wrapper ({@link #toString(boolean)}, pretty-printed) into
 * the Alloy part of the DynAlloy module, so the predicates of the conditions, invariants and
 * contracts, and the pre- and postcondition of the method under analysis, are declared in the module
 * that wrapped them (docs/NAMING.md, "What it names").
 *
 * <p>Equality is by value: the id, the parameters, the typing and the body. The visitor collects the
 * declarations in a {@code HashSet}, so they are printed in no particular order. No static state.
 */
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

	/**
	 * Returns the Alloy text of the predicate, {@code pred <id>[<p>:<type>,...]{<body>}} and a line
	 * break: each parameter with its type in {@code getTyping()} ({@code null} if it has none there),
	 * and the body printed by dynalloy4's {@code JFormulaPrinter}.
	 *
	 * @param prettyPrint true to put each parameter on its own line, indented by two spaces, and to
	 *        indent the body by three ({@link #increaseIndentation(String)})
	 * @return the text
	 */
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
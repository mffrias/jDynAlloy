/**
 * 
 */
package ar.edu.jdynalloy.ast;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import ar.uba.dc.rfm.alloy.AlloyTypingPrinter;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.JFormulaPrinter;
import ar.uba.dc.rfm.alloy.util.ExpressionPrinter;

/**
 * @author jgaleotti
 * 
 */
public final class JDynAlloyPrinter extends JDynAlloyVisitor {

	@Override
	public Object visit(JClassConstraint n) {
		StringBuffer sb = new StringBuffer();
		sb.append("class_constraint\n");
		String invariantStr = (String) n.getFormula().accept(formulaPrinter);
		sb.append(increaseIdentation(invariantStr));
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public Object visit(JClassInvariant n) {
		StringBuffer sb = new StringBuffer();
		sb.append("class_invariant\n");
		String invariantStr = (String) n.getFormula().accept(formulaPrinter);
		sb.append(increaseIdentation(invariantStr));
		sb.append("\n");
		return sb.toString();
	}

	private enum PrinterStatus {
		OUT_OF_PROGRAM_BODY, IN_PROGRAM_BODY
	};

	PrinterStatus status = PrinterStatus.OUT_OF_PROGRAM_BODY;

	@Override
	public Object visit(JModifies node) {
		StringBuffer sb = new StringBuffer();
		sb.append("modifies {\n");
		if (node.isModifiesEverything()) {
			sb.append("EVERYTHING");
		} else {
			AlloyExpression location = node.getLocation();
			String locationStr = (String) location.accept(expressionPrinter);
			sb.append("  " + locationStr + "\n");
		}
		sb.append("}\n");
		return sb.toString();
	}

	@Override
	public Object visit(JSpecCase node) {
		Vector<Object> v = (Vector<Object>) super.visit(node);

		Vector<String> requiresVec = (Vector<String>) v.get(0);

		Vector<String> ensuresVec = (Vector<String>) v.get(1);

		Vector<String> modifiesVec = (Vector<String>) v.get(2);

		StringBuffer sb = new StringBuffer();

		for (String requires : requiresVec) {
			sb.append(requires);
			sb.append("\n");
		}
		for (String ensures : ensuresVec) {
			sb.append(ensures);
			sb.append("\n");
		}
		for (String modifies : modifiesVec) {
			sb.append(modifies);
			sb.append("\n");
		}
		return sb.toString();
	}

	private final JFormulaPrinter formulaPrinter = new JFormulaPrinter();

	private static final String ABSTRACT_KEYWORD = "abstract";
	private static final Character BLANK_CHR = new Character(' ');
	private static final String EXTENDS_KEYWORD = "extends";
	private static final String IN_KEYWORD = "in";
	private static final String NEXT_LINE_CHR = "\n";
	private static final String ONE_KEYWORD = "one";
	private static final String SIG_KEYWORD = "sig";
	private String signatureId;

	private final ExpressionPrinter expressionPrinter;

	/**
	 * @param output
	 */
	public JDynAlloyPrinter(boolean isJavaArithmetic) {
		super(isJavaArithmetic);		
		formulaPrinter.setPrettyPrinting(true);
		expressionPrinter = (ExpressionPrinter) formulaPrinter
				.getDfsExprVisitor();
	}

	public Object visit(JBlock s) {
		StringBuffer buffer = new StringBuffer();
		Vector<String> v = (Vector<String>) super.visit(s);
		buffer.append("{\n");
		for (String line : v) {
			// if (buffer.length() != 0)
			// buffer.append("\n");
			buffer.append(increaseIdentation(line));
		}
		buffer.append("\n}");
		return buffer.toString();
	}

	public Object visit(JIfThenElse s) {
		boolean isThenABlock = s.getThen() instanceof JBlock;
		boolean isElseABlock = s.getElse() instanceof JBlock;

		Vector<String> v = (Vector<String>) super.visit(s);

		StringBuffer buff = new StringBuffer();
		buff.append("if ");
		buff.append((String) s.getCondition().accept(formulaPrinter));

		if (s.getBranchId() != null) {
			buff.append(String.format(" lblpos %s ", s.getBranchId()));
		}

		if (!isThenABlock) {
			buff.append("{\n");
		}
		// buff.append(" \n");
		buff.append(increaseIdentation(v.get(0)));
		if (!isThenABlock) {
			buff.append("}");
		}
		buff.append(" else ");
		if (!isElseABlock) {
			buff.append("{\n");
		}
		buff.append(increaseIdentation(v.get(1)));
		if (!isElseABlock) {
			buff.append("}");
		}
		buff.append(";");
		return buff.toString();
	}

	public Object visit(JAssert s) {
		StringBuffer buff = new StringBuffer();
		buff.append("assert ");
		buff.append((String) s.getCondition().accept(formulaPrinter));
		buff.append(";");
		return buff.toString();
	}

	public Object visit(JWhile s) {
		boolean isBodyABlock = s.getBody() instanceof JBlock;

		Vector<String> v = (Vector<String>) super.visit(s);

		StringBuffer buff = new StringBuffer();
		buff.append("while ");
		buff.append((String) s.getCondition().accept(formulaPrinter) + "\n");

		if (s.getBranchId() != null) {
			buff.append(String.format(" lblpos %s ", s.getBranchId()));
		}

		if (s.getLoopInvariant() != null) {
			Object invariantStr = s.getLoopInvariant().getFormula().accept(
					formulaPrinter);
			buff.append("loop_invariant " + invariantStr + "\n");
		}
		if (!isBodyABlock) {
			buff.append("{\n");
		}
		buff.append("" + "\n");
		if (!isBodyABlock) {
			buff.append("}");
		}
		buff.append(increaseIdentation(v.get(1)));
		buff.append("");
		buff.append(";");
		return buff.toString();
	}

	private String increaseIdentation(String string) {
		StringBuffer buffer = new StringBuffer();
		String[] lines = string.split("\n");
		for (String line : lines) {
			buffer.append("   " + line + "\n");
		}
		return buffer.toString();
	}

	public Object visit(JSkip s) {

		return "skip;";
	}

	@Override
	public Object visit(JAssignment n) {
		StringBuffer buff = new StringBuffer();
		buff.append(n.getLvalue().accept(expressionPrinter));
		buff.append(":=");
		buff.append(n.getRvalue().accept(expressionPrinter));
		buff.append(";");
		return buff.toString();
	}

	@Override
	public Object visit(JCreateObject n) {
		StringBuffer buff = new StringBuffer();
		buff.append("createObject");
		buff.append("<" + n.getSignatureId() + ">");
		buff.append("[" + n.getLvalue().toString() + "]");
		buff.append(";");
		return buff.toString();
	}

	@Override
	public Object visit(JVariableDeclaration n) {
		StringBuffer buff = new StringBuffer();
		buff.append("var " + n.getVariable() + ":" + n.getType());
		if (this.status == PrinterStatus.IN_PROGRAM_BODY) {
			buff.append(";");
		}
		return buff.toString();
	}

	@Override
	public Object visit(JProgramCall n) {
		StringBuffer buffer = new StringBuffer();
		for (AlloyExpression e : n.getArguments()) {
			if (buffer.length() != 0)
				buffer.append(",");
			buffer.append(e.accept(expressionPrinter));
		}
		return "call " + n.getProgramId() + "[" + buffer.toString() + "]" + ";";
	}

	@Override
	public Object visit(JProgramDeclaration node) {
		// taken from super
		Vector<Object> children = new Vector<Object>();

		Vector<Object> varResultsSuper = new Vector<Object>();
		for (JVariableDeclaration child : node.getParameters()) {
			varResultsSuper.add(child.accept(this));
		}
		children.add(varResultsSuper);

		Vector<Object> specCasesResult = new Vector<Object>();
		for (JSpecCase child : node.getSpecCases()) {
			specCasesResult.add(child.accept(this));
		}
		children.add(specCasesResult);

		this.status = PrinterStatus.IN_PROGRAM_BODY;
		Object bodyResult = node.getBody().accept(this);
		children.add(bodyResult);
		this.status = PrinterStatus.OUT_OF_PROGRAM_BODY;
		// end of taken from super

		StringBuffer buffer = new StringBuffer();
		if (node.isVirtual())
			buffer.append("virtual ");
		buffer.append("program " + node.getSignatureId() + "::"
				+ node.getProgramId() + "[");
		buffer.append("\n");

		Vector<String> varResults = (Vector<String>) children.get(0);
		for (int i = 0; i < varResults.size(); i++) {
			if (i != 0) {
				buffer.append(",");
				buffer.append("\n");
			}
			String varDeclStr = varResults.get(i);
			buffer.append("  " + varDeclStr);
		}
		buffer.append("] \n");

		Vector<String> specCaseResults = (Vector<String>) children.get(1);
		if (!specCaseResults.isEmpty()) {
			buffer.append("Specification \n");
			buffer.append("{\n");
			for (int i = 0; i < specCaseResults.size(); i++) {
				String specCaseStr = specCaseResults.get(i);
				buffer.append(String.format("  SpecCase #%s {\n", i));
				buffer.append(increaseIdentation(specCaseStr));
				buffer.append("  }\n");
			}
			buffer.append("}\n");
		}

		String bodyProgram = (String) children.get(2);

		buffer.append("Implementation \n");
		buffer.append("\n");
		buffer.append(increaseIdentation(bodyProgram));
		buffer.append("\n");
		buffer.append("");
		buffer.append("\n");

		return buffer.toString();
	}

	@Override
	public Object visit(JDynAlloyModule node) {

		JDynAlloyModuleVisitResult v = (JDynAlloyModuleVisitResult) super
				.visit(node);
		StringBuffer sb = new StringBuffer();
		sb.append("module " + node.getModuleId());
		sb.append("\n");

		String signature = (String) v.signature_result;
		sb.append(signature);

		if (v.class_singleton_result != null) {
			String classSingletonStr = (String) v.class_singleton_result;
			sb.append(classSingletonStr);
			sb.append("\n");
		}

		if (v.literal_singleton_result != null) {
			String literalSingletonStr = (String) v.literal_singleton_result;
			sb.append(literalSingletonStr);
			sb.append("\n");
		}

		Vector<String> fieldResults = (Vector<String>) v.fields_result;
		for (String s : fieldResults) {
			sb.append(s);
		}
		if (!fieldResults.isEmpty())
			sb.append("\n");

		Vector<String> class_invariants = (Vector<String>) v.class_invariants_result;
		for (String s : class_invariants) {
			sb.append(s);
		}
		if (!class_invariants.isEmpty())
			sb.append("\n");

		Vector<String> class_constraints = (Vector<String>) v.class_constraints_result;
		for (String s : class_constraints) {
			sb.append(s);
		}
		if (!class_invariants.isEmpty())
			sb.append("\n");

		Vector<String> invariantResults = (Vector<String>) v.object_invariants_result;
		for (String s : invariantResults) {
			sb.append(s);
		}
		if (!invariantResults.isEmpty())
			sb.append("\n");

		Vector<String> constraintResults = (Vector<String>) v.object_constraints_result;
		for (String s : constraintResults) {
			sb.append(s);
		}
		if (!invariantResults.isEmpty())
			sb.append("\n");

		Vector<String> representsResults = (Vector<String>) v.represents_result;
		for (String s : representsResults) {
			sb.append(s);
		}
		if (!representsResults.isEmpty())
			sb.append("\n");

		Vector<String> programResults = (Vector<String>) v.programs_result;
		for (String s : programResults) {
			sb.append(s);
		}
		if (!programResults.isEmpty())
			sb.append("\n");

		return sb.toString();
	}

	@Override
	public Object visit(JSignature node) {
		signatureId = node.getSignatureId();

		StringBuffer buff = new StringBuffer();
		if (node.isAbstract())
			buff.append("abstract ");

		if (node.isOne())
			buff.append("one ");

		buff.append(String.format("sig %s ", node.getSignatureId()));

		if (node.isExtension())
			buff.append(String.format("extends %s ", node.getExtSigId()));

		if (node.inSignatureId() != null)
			buff.append(String.format("in %s ", node.inSignatureId()));

		buff.append("{");
		String typing_str = new AlloyTypingPrinter().print(node.getFields())
				.replace(",", ",\n");
		buff.append(typing_str);
		buff.append("}");
		buff.append("\n");
		buff.append("{");

		for (String inSingature : node.superInterfaces()) {
			if (buff.charAt(buff.length() - 1) != '{')
				buff.append("\n");

			final String axiom = String.format("%s in %s ", node
					.getSignatureId(), inSingature);
			buff.append(axiom);
		}

		SortedSet<String> sorted_facts = new TreeSet<String>();
		for (AlloyFormula aFact : node.getFacts()) {
			JFormulaPrinter printer = new JFormulaPrinter();
			sorted_facts.add((String) aFact.accept(printer));
		}

		for (String sorted_fact : sorted_facts) {
			buff.append(sorted_fact);
			buff.append("\n");
		}

		buff.append("}");
		buff.append("\n");

		for (String alloy_predicate : node.getAlloyPredicates()) {
			buff.append(alloy_predicate + "\n");
		}

		for (String alloy_function : node.getAlloyFunctions()) {
			buff.append(alloy_function + "\n");
		}

		return buff.toString();
	}

	@Override
	public Object visit(JField node) {
		StringBuffer sb = new StringBuffer();
		sb.append("field ");
		sb.append(node.getFieldVariable().toString() + ":"
				+ node.getFieldType().toString());
		sb.append(" {}");
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public Object visit(JAssume n) {
		StringBuffer sb = new StringBuffer();
		sb.append("assume ");
		sb.append((String) n.getCondition().accept(formulaPrinter));
		sb.append(";");
		return sb.toString();
	}

	@Override
	public Object visit(JObjectInvariant n) {
		StringBuffer sb = new StringBuffer();
		sb.append("object_invariant\n");
		String invariantStr = (String) n.getFormula().accept(formulaPrinter);
		sb.append(increaseIdentation(invariantStr));
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public Object visit(JObjectConstraint n) {
		StringBuffer sb = new StringBuffer();
		sb.append("object_constraint\n");
		String invariantStr = (String) n.getFormula().accept(formulaPrinter);
		sb.append(increaseIdentation(invariantStr));
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public Object visit(JRepresents n) {
		StringBuffer sb = new StringBuffer();
		sb.append("represents ");
		sb.append(n.getExpression().toString());
		sb.append(" such that ");
		sb.append((String) n.getFormula().accept(formulaPrinter));
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public Object visit(JPostcondition n) {
		StringBuffer sb = new StringBuffer();
		sb.append("ensures {\n");
		String postStr = (String) n.getFormula().accept(formulaPrinter);
		sb.append(increaseIdentation(postStr));
		sb.append("}\n");
		return sb.toString();
	}

	@Override
	public Object visit(JPrecondition n) {
		StringBuffer sb = new StringBuffer();
		sb.append("requires {\n");
		String preconditionStr = (String) n.getFormula().accept(formulaPrinter);
		sb.append(increaseIdentation(preconditionStr));
		sb.append("}\n");
		return sb.toString();
	}

	@Override
	public Object visit(JHavoc n) {
		StringBuffer buff = new StringBuffer();
		buff.append("havoc ");
		buff.append(n.getExpression().accept(expressionPrinter));
		buff.append(";");
		return buff.toString();
	}

}

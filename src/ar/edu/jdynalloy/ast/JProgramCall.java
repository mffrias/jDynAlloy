package ar.edu.jdynalloy.ast;

import java.util.List;

import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.IProgramCall;

/**
 * {@code [super] call p[args];}: a call to a program. The arguments match the program's
 * parameters by position: the receiver (for the callee's {@code thiz}), {@code throw},
 * {@code return} if the method returns something, then the method's arguments; a call to a static
 * program has {@code throw} first ({@link #isStatic()}). The program id is the name TACO gives
 * the call (docs/NAMING.md, "What TACO gives it"): {@code <receiver type>_<method>} for static
 * calls and for calls into the classes TACO translates, the bare method name for instance calls
 * into the built-in modules, and {@code Constructor} for constructors. {@code BindingManager}
 * resolves it to a program; {@code VAlloyVisitor} replaces it with a call that has that program's
 * id and no {@code super} flag. {@code JDynAlloyXlatorVisitor} translates it into a DynAlloy call
 * with the arguments, then every field, then {@code usedObjects}.
 *
 * <p>The {@code super} flag takes part in {@code equals}, {@code hashCode} and
 * {@code toString()}, but neither in the binding nor in the translation, and
 * {@code JDynAlloyPrinter} does not print it.
 *
 * <p>Equality is by value (the program id, the arguments and the flag), and the binding relies on
 * it: {@code JDynAlloyBinding} copies the identity map of {@code CallBindingResolver} into a
 * {@code HashMap}, so that a call built afresh, as {@code RelevancyAnalysisUtils} does for the
 * function calls in specifications, finds the program of an equal call statement. Two equal calls
 * in different programs therefore share one entry there.
 */
public final class JProgramCall extends JStatement implements IProgramCall {

	private final String programId;
	private final List<AlloyExpression> arguments;
	private final boolean isSuperCall;

	/**
	 * Creates a call; the list of arguments is kept, not copied.
	 *
	 * @param isSuper true for a call through {@code super} (see the class comment)
	 * @param programId the name of the called program, as TACO gives it (docs/NAMING.md)
	 * @param arguments the receiver, or {@code throw} for a static call, first; then as in the
	 *        class comment
	 */
	public JProgramCall(boolean isSuper, String programId,
			List<AlloyExpression> arguments) {
		super();
		this.isSuperCall = isSuper;
		this.programId = programId;
		this.arguments = arguments;
	}

	@Override
	public Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ar.edu.dynjml4alloy.ast.ICallProgram#getProgramId()
	 */
	public String getProgramId() {
		return programId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ar.edu.dynjml4alloy.ast.ICallProgram#getArguments()
	 */
	public List<AlloyExpression> getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (AlloyExpression e : getArguments()) {
			if (buffer.length() > 0)
				buffer.append(",");
			buffer.append(e.toString());
		}
		return (this.isSuperCall ? "super " : "") + "call " + getProgramId()
				+ "[" + buffer.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((arguments == null) ? 0 : arguments.hashCode());
		result = prime * result + (isSuperCall ? 1231 : 1237);
		result = prime * result
				+ ((programId == null) ? 0 : programId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final JProgramCall other = (JProgramCall) obj;
		if (arguments == null) {
			if (other.arguments != null)
				return false;
		} else if (!arguments.equals(other.arguments))
			return false;
		if (isSuperCall != other.isSuperCall)
			return false;
		if (programId == null) {
			if (other.programId != null)
				return false;
		} else if (!programId.equals(other.programId))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ar.edu.dynjml4alloy.ast.ICallProgram#isSuperCall()
	 */
	public boolean isSuperCall() {
		return isSuperCall;
	}

	/**
	 * Returns if a call is static or not If first arguments is "throw" then is
	 * static *EXCEPT* when the second argument is "throw", then is an
	 * non-static call to a member method of an "exception" object
	 */
	public boolean isStatic() {
		if (this.arguments.get(0) instanceof ExprVariable) {
			ExprVariable first_argument = (ExprVariable) this.arguments.get(0);
			if (first_argument.equals(
					JExpressionFactory.THROW_EXPRESSION)) {

				if (this.arguments.size() >= 2
						&& this.arguments.get(1) instanceof ExprVariable) {
					ExprVariable second_argument = (ExprVariable) this.arguments
							.get(1);
					if (second_argument.equals(
							JExpressionFactory.THROW_EXPRESSION)) {
						return false;
					} else {
						return true;
					}
				} else {
					if (this.arguments.size() == 1)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the receiver: the first argument of a call that is not static.
	 *
	 * @return the first argument
	 * @throws IllegalStateException if {@link #isStatic()} is true ("cannot return receiver in a
	 *        call to a static program")
	 */
	public AlloyExpression getReceiver() {
		if (this.isStatic())
			throw new IllegalStateException("cannot return receiver in a call to a static program");
		return this.arguments.get(0);
	}

}

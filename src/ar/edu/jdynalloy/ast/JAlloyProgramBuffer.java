package ar.edu.jdynalloy.ast;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import ar.edu.jdynalloy.xlator.JType;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * Builds a JDynAlloy statement one statement at a time, for code that walks a structured
 * program. Its users: TACO's statement translator ({@code JmlAstTransverseStatementVisitor} holds
 * one, and {@code JDynAlloyASTVisitor} creates a {@code BlockStatementsVisitor}, so a buffer, for
 * each method body), TACO's models of {@code Integer} and {@code Boolean}, and jDynAlloy's
 * {@code ModularMutator}, for the specification programs.
 *
 * <p>It keeps a stack of open constructs. {@link #openIf}, {@link #openWhile} and
 * {@link #openDo} push one; statements go to the block of the innermost one, or to the top level
 * when none is open; {@link #switchToElseIf()} moves an {@code if} from its then branch to its
 * else branch; {@link #closeIf()}, {@link #closeWhile()} and {@link #closeDo()} pop it and add
 * the statement they build to the enclosing block. A list of statements becomes {@code skip} when
 * it is empty, the statement itself when it has one, and a {@code JBlock} otherwise.
 * {@link #toJAlloyProgram()} returns the top level once every construct is closed. Calls out of
 * order throw an {@code IllegalStateException}.
 *
 * <p>The other methods add one statement to the current block: the {@code assign} overloads (an
 * assignment {@code lvalue := rvalue}), {@code declare} (a variable declaration),
 * {@code assertion}, {@code assumption}, {@code havoc}, {@code skip}, and {@code appendProgram}
 * (any statement). One buffer per program; no static state.
 */
final public class JAlloyProgramBuffer {


	private boolean isJavaArithmetic;

	public boolean isJavaArithmetic() {
		return isJavaArithmetic;
	}

	/**
	 * Sets the flag passed to the visitors that {@link #closeDo()} creates, where it has no effect
	 * (they have no input to fix; see {@link JDynAlloyVisitor}). TACO's
	 * {@code JDynAlloyASTVisitor} sets it from {@code useJavaArithmetic}. The default is false.
	 *
	 * @param isJavaArithmetic true with Java arithmetic
	 */
	public void setJavaArithmetic(boolean isJavaArithmetic) {
		this.isJavaArithmetic = isJavaArithmetic;
	}


	private static class PartialAST {

		private abstract static class Context {

			public AlloyFormula condition;

			public List<JStatement> currBlock = new LinkedList<JStatement>();

			public Context(AlloyFormula condition) {
				super();
				this.condition = condition;
			}

			private String branchId = null;

			public void setBranchId(String branchId) {
				this.branchId = branchId;

			}

		}

		private static final class IfThenElseContext extends Context {
			public List<JStatement> prevBlock = null;

			public IfThenElseContext(AlloyFormula condition) {
				super(condition);
			}

		}

		private static final class WhileContext extends Context {
			private JLoopInvariant annotation = null;

			public WhileContext(AlloyFormula condition,
					JLoopInvariant annotation) {
				super(condition);
				this.annotation = annotation;
			}
		}


		private static final class DoContext extends Context {
			private JLoopInvariant annotation = null;

			public DoContext(AlloyFormula condition,
					JLoopInvariant annotation) {
				super(condition);
				this.annotation = annotation;
			}
		}


		private Stack<Context> context = new Stack<Context>();

		private List<JStatement> initialBody = new LinkedList<JStatement>();

		public void newIfThenElseState(AlloyFormula condition) {
			newIfThenElseState(condition, null);
		}

		public void newIfThenElseState(AlloyFormula condition, String branchId) {
			IfThenElseContext ifThenElseContext = new IfThenElseContext(
					condition);
			if (branchId != null)
				ifThenElseContext.setBranchId(branchId);

			context.push(ifThenElseContext);
		}

		public void newWhileState(AlloyFormula condition,
				JLoopInvariant annotation, String branchId) {
			WhileContext whileContext = new WhileContext(condition, annotation);
			whileContext.setBranchId(branchId);
			context.push(whileContext);
		}


		public void newDoState(AlloyFormula condition,
				JLoopInvariant annotation, String branchId) {
			DoContext doContext = new DoContext(condition, annotation);
			doContext.setBranchId(branchId);
			context.push(doContext);
		}


		public List<JStatement> getPrevBlockIfAvailable() {
			if (onIfElseState())
				return ((IfThenElseContext) context.peek()).prevBlock;
			else
				return null;
		}

		public List<JStatement> getCurrBlock() {
			if (onInitialState())
				return initialBody;
			else
				return context.peek().currBlock;
		}

		public JLoopInvariant getWhileAnnotation() {
			Context ctx = context.peek();
			WhileContext whileContex = (WhileContext) ctx;
			return whileContex.annotation;
		}

		public JLoopInvariant getDoAnnotation() {
			Context ctx = context.peek();
			DoContext whileContex = (DoContext) ctx;
			return whileContex.annotation;
		}


		public AlloyFormula getCurrCondition() {
			if (!onInitialState())
				return context.peek().condition;
			else
				throw new IllegalStateException();
		}

		public String getCurrBranchId() {
			if (!onInitialState())
				return context.peek().branchId;
			else
				throw new IllegalStateException();

		}

		public boolean onIfElseState() {
			return !context.isEmpty()
					&& (context.peek().getClass()
							.equals(IfThenElseContext.class))
					&& ((IfThenElseContext) context.peek()).prevBlock != null;
		}

		public boolean onIfThenState() {
			return !context.isEmpty()
					&& (context.peek().getClass()
							.equals(IfThenElseContext.class))
					&& ((IfThenElseContext) context.peek()).prevBlock == null;
		}

		public boolean onInitialState() {
			return context.isEmpty();
		}

		public boolean onWhileState() {
			return !context.isEmpty()
					&& context.peek().getClass().equals(WhileContext.class);
		}

		public boolean onDoState() {
			return !context.isEmpty()
					&& context.peek().getClass().equals(DoContext.class);
		}


		public void switchToElseState() {
			if (onIfThenState()) {
				IfThenElseContext c = (IfThenElseContext) context.peek();
				c.prevBlock = c.currBlock;
				c.currBlock = new LinkedList<JStatement>();
			} else
				throw new IllegalStateException();
		}

		public void leaveState() {
			if (!context.isEmpty())
				context.pop();
			else
				throw new IllegalStateException();
		}

	}

	private PartialAST ast = new PartialAST();

	/**
	 * Adds the assignment of a constant, {@code lvalue := constantId}, to the current block.
	 *
	 * @param lvalue the variable assigned
	 * @param constantId the constant, as it is printed (an {@code ExprConstant} without module
	 *        alias)
	 */
	public void assign(AlloyVariable lvalue, String constantId) {
		addAssignment(lvalue, new ExprConstant(null, constantId));
	}

	/**
	 * Adds the assignment of a constant, {@code lvalue := constantId}, to the current block.
	 *
	 * @param lvalue the location assigned (a variable, a field of an object, ...)
	 * @param constantId the constant, as it is printed (an {@code ExprConstant} without module
	 *        alias)
	 */
	public void assign(AlloyExpression lvalue, String constantId) {
		addAssignment(lvalue, new ExprConstant(null, constantId));
	}

	/**
	 * Closes the innermost construct, an {@code if}, and adds it to the enclosing block: its then
	 * branch is what was added before {@link #switchToElseIf()}, and its else branch what was
	 * added after, or {@code skip} if the branch was never switched. The branch id is the one
	 * given to {@link #openIf(AlloyFormula, String)}.
	 *
	 * @throws IllegalStateException if the innermost construct is not an {@code if}
	 */
	public void closeIf() {
		JIfThenElse i = null;

		if (ast.onIfThenState()) {
			i = buildIfThen(ast.getCurrCondition(), ast.getCurrBlock(), ast
					.getCurrBranchId());

		} else if (ast.onIfElseState()) {
			i = buildIfThenElse(ast.getCurrCondition(), ast
					.getPrevBlockIfAvailable(), ast.getCurrBlock(), ast
					.getCurrBranchId());

		} else
			throw new IllegalStateException();

		ast.leaveState();
		ast.getCurrBlock().add(i);
	}

	private JIfThenElse buildIfThen(AlloyFormula c, List<JStatement> b,
			String branchId) {
		return buildIfThenElse(c, b, new LinkedList<JStatement>(), branchId);
	}

	private JIfThenElse buildIfThenElse(AlloyFormula c,
			List<JStatement> thenList, List<JStatement> elseList,
			String branchId) {
		return new JIfThenElse(c, buildStmt(thenList), buildStmt(elseList),
				branchId);
	}

	public void assign(AlloyVariable lval, AlloyVariable rval) {
		addAssignment(lval, new ExprVariable(rval));
	}

	public void assign(AlloyVariable lvalue, AlloyExpression rvalue) {
		addAssignment(lvalue, rvalue);

	}

	public void assign(AlloyExpression lvalue, AlloyExpression rvalue) {
		addAssignment(lvalue, rvalue);
	}

	public void assign(AlloyExpression lvalue, AlloyVariable rvalue) {
		addAssignment(lvalue, new ExprVariable(rvalue));

	}

	private void addAssignment(AlloyVariable lvalue, AlloyExpression rvalue) {
		addAssignment(new ExprVariable(lvalue), rvalue);
	}

	private void addAssignment(AlloyExpression lvalue, AlloyExpression rvalue) {
		ast.getCurrBlock().add(new JAssignment(lvalue, rvalue));
	}

	/**
	 * Closes the innermost construct, a {@code while}, and adds it to the enclosing block, with
	 * the condition, loop invariant and branch id given to
	 * {@link #openWhile(AlloyFormula, JLoopInvariant, String)}.
	 *
	 * @throws IllegalStateException if the innermost construct is not a {@code while}
	 */
	public void closeWhile() {
		if (ast.onWhileState()) {
			JWhile w = buildWhile(ast.getCurrCondition(), ast.getWhileAnnotation(),
					ast.getCurrBlock(), ast.getCurrBranchId());
			ast.leaveState();
			ast.getCurrBlock().add(w);
		} else
			throw new IllegalStateException();
	}


	/**
	 * Closes the innermost construct, a {@code do} loop, and adds to the enclosing block the block
	 * {@code { body; while condition' { body' } }}: the body as it was added, then a
	 * {@code while} with the loop invariant and branch id given to
	 * {@link #openDo(AlloyFormula, JLoopInvariant, String)}, whose body is a copy of the body and
	 * whose condition a copy of the condition, in both of which the variables that the body
	 * declares, at any depth ({@link #collectNewlyDefinedVars}), are renamed to
	 * {@code do_body_rename_<name>} ({@link VariableNameChangerVisitor}), so that they are not
	 * declared twice.
	 *
	 * @throws IllegalStateException if the innermost construct is not a {@code do}
	 * @throws NullPointerException as {@link #collectNewlyDefinedVars} does, if a statement at the
	 *         top of the body is not a variable declaration, a block, an {@code if} or a
	 *         {@code while}
	 */
	public void closeDo() {
		if (ast.onDoState()) {
			JWhile w = buildWhile(ast.getCurrCondition(), ast.getDoAnnotation(),
					ast.getCurrBlock(), ast.getCurrBranchId());
			JBlock b = buildDoBlock(ast.getCurrBlock(), w);
			ast.leaveState();
			ast.getCurrBlock().add(b);
		} else
			throw new IllegalStateException();
	}



	private JBlock buildDoBlock(List<JStatement> currBlock, JWhile w) {
		List<Object> objvars = collectNewlyDefinedVars(currBlock, isJavaArithmetic);
		List<AlloyVariable> vars = new LinkedList<AlloyVariable>();
		for (int idx = 0; idx < objvars.size(); idx++)
			vars.add((AlloyVariable)objvars.get(idx));
		JWhile newW = avoidCollisions(w, vars);
		JStatement[] sts = new JStatement[currBlock.size()+1];
		for (int idx = 0; idx < currBlock.size(); idx++){
			sts[idx] = currBlock.get(idx);
		}
		sts[currBlock.size()] = newW;
		JBlock b = new JBlock(sts);
		return b;
	}



	private JWhile avoidCollisions(JWhile w, List<AlloyVariable> vars) {
		JStatement body = w.getBody();
		VariableNameChangerVisitor visitor = new VariableNameChangerVisitor(vars, isJavaArithmetic);
		JStatement newBody = (JStatement) body.accept(visitor);
		AlloyFormula cond = w.getCondition();
		VariableNameChangerFormulaMutator condVisitor = new VariableNameChangerFormulaMutator(vars);
		AlloyFormula newCond = (AlloyFormula) cond.accept(condVisitor);
		return new JWhile(newCond, newBody, w.getLoopInvariant(), w.getBranchId());
	}

	/**
	 * Returns the variables that a list of statements declares, at any depth (inside blocks,
	 * branches and loops), in order, with a {@link JDynAlloyVarDeclarationCollector}.
	 *
	 * @param currBlock the statements
	 * @param isJavaArithmetic passed to the collector, where it has no effect
	 * @return the declared {@code AlloyVariable}s
	 * @throws NullPointerException if a statement of the list is not a variable declaration, a
	 *         block, an {@code if} or a {@code while}: the collector returns null for it, and the
	 *         result is flattened without a check
	 */
	public static List<Object> collectNewlyDefinedVars(List<JStatement> currBlock, boolean isJavaArithmetic){
		Vector<Object> vars = new Vector<Object>();
		JDynAlloyVarDeclarationCollector newDecsVisitor = new JDynAlloyVarDeclarationCollector(isJavaArithmetic);
		for (int idx = 0; idx < currBlock.size(); idx++){
			Vector<Object> v = (Vector<Object>)currBlock.get(idx).accept(newDecsVisitor);
			Vector<Object> input = flatten(v);
			addAllNonNull(vars, input);
		}
		return vars;
	}


	private static Vector<Object> flatten(Vector<Object> tree){
		Vector<Object> result = new Vector<Object>();
		for (int idx = 0; idx < tree.size(); idx++){
			if (!(tree.get(idx) instanceof Vector)){
				result.add(tree.get(idx));
			} else {
				result.addAll(flatten((Vector<Object>)tree.get(idx)));
			}
		}
		return result;
	}

	private static void addAllNonNull(Vector<Object> vars, Vector<Object> vector){
		for (int idx = 0; idx < vector.size(); idx++){
			if (vector.get(idx) != null){
				vars.add(vector.get(idx));
			}
		}
	}

	/**
	 * Closes the JWhile in the current state and returns it.
	 * This method is used when translating loop invariants.
	 * @return
	 */
	public JWhile closeAndDiscardWhile() {
		if (ast.onWhileState()) {
			JWhile w = buildWhile(ast.getCurrCondition(), ast.getWhileAnnotation(),
					ast.getCurrBlock(), null);
			ast.leaveState();
			return w;
		} else
			throw new IllegalStateException();
	}

	private JWhile buildWhile(AlloyFormula condition,
			JLoopInvariant annotation, List<JStatement> body, String branchId) {
		return new JWhile(condition, buildStmt(body), annotation, branchId);
	}



	private JStatement buildStmt(List<JStatement> b) {
		if (b.size() == 0)
			return new JSkip();
		if (b.size() == 1) {
			return b.get(0);
		} else
			return new JBlock(b);
	}

	/**
	 * Returns the statements added at the top level: {@code skip} if there are none, the
	 * statement if there is one, and a {@code JBlock} otherwise. The buffer is not reset, and the
	 * block shares its list with the buffer: statements added later appear in it too.
	 *
	 * @return the program's statement
	 * @throws IllegalStateException if a construct is still open ("ProgramBuffer haven't return
	 *         to initial state")
	 */
	public JStatement toJAlloyProgram() {
		if (ast.onInitialState()) {
			return buildStmt(ast.getCurrBlock());
		} else
			throw new IllegalStateException(
					"ProgramBuffer haven't return to initial state");
	}

	/**
	 * Opens an {@code if}: the statements added next go to its then branch, until
	 * {@link #switchToElseIf()} or {@link #closeIf()}.
	 *
	 * @param condition the condition
	 * @param branchId the id of the branch (printed as {@code lblpos}), or null for none
	 */
	public void openIf(AlloyFormula condition, String branchId) {
		ast.newIfThenElseState(condition, branchId);
	}

	/**
	 * Opens an {@code if} without a branch id; see {@link #openIf(AlloyFormula, String)}.
	 *
	 * @param condition the condition
	 */
	public void openIf(AlloyFormula condition) {
		openIf(condition, null);
	}

	/**
	 * Makes the statements added next go to the else branch of the innermost {@code if}.
	 *
	 * @throws IllegalStateException if the innermost construct is not an {@code if} in its then
	 *         branch (once only per {@code if})
	 */
	public void switchToElseIf() {
		if (ast.onIfThenState())
			ast.switchToElseState();
		else
			throw new IllegalStateException();
	}

	public void skip() {
		ast.getCurrBlock().add(new JSkip());
	}

	/**
	 * Opens a {@code while} without a branch id; see
	 * {@link #openWhile(AlloyFormula, JLoopInvariant, String)}.
	 *
	 * @param condition the condition
	 * @param annotation the loop invariant, or null
	 */
	public void openWhile(AlloyFormula condition, JLoopInvariant annotation) {
		openWhile(condition, annotation, null);
	}

	/**
	 * Opens a {@code do} loop without a branch id; see
	 * {@link #openDo(AlloyFormula, JLoopInvariant, String)}.
	 *
	 * @param condition the condition
	 * @param annotation the loop invariant, or null
	 */
	public void openDo(AlloyFormula condition, JLoopInvariant annotation) {
		openDo(condition, annotation, null);
	}

	/**
	 * Opens a {@code while}: the statements added next are its body, until
	 * {@link #closeWhile()}.
	 *
	 * @param condition the condition
	 * @param annotation the loop invariant, or null
	 * @param branchId the id of the branch (printed as {@code lblpos}), or null for none
	 */
	public void openWhile(AlloyFormula condition, JLoopInvariant annotation,
			String branchId) {
		ast.newWhileState(condition, annotation, branchId);
	}

	/**
	 * Opens a {@code do} loop: the statements added next are its body, until {@link #closeDo()},
	 * which makes the loop.
	 *
	 * @param condition the condition, checked after each pass through the body
	 * @param annotation the loop invariant, or null
	 * @param branchId the id of the branch, or null for none
	 */
	public void openDo(AlloyFormula condition, JLoopInvariant annotation,
			String branchId) {
		ast.newDoState(condition, annotation, branchId);
	}


	public void declare(AlloyVariable variable, JType type) {
		ast.getCurrBlock().add(new JVariableDeclaration(variable, type));

	}

	public void assertion(AlloyFormula f) {
		ast.getCurrBlock().add(new JAssert(f));

	}

	public void assumption(AlloyFormula f) {
		ast.getCurrBlock().add(new JAssume(f));

	}

	public void havoc(AlloyVariable v) {
		ast.getCurrBlock().add(new JHavoc(new ExprVariable(v)));
	}

	public void havoc(AlloyExpression expr) {
		ast.getCurrBlock().add(new JHavoc(expr));
	}

	public void appendProgram(JStatement program) {
		ast.getCurrBlock().add(program);
	}

}

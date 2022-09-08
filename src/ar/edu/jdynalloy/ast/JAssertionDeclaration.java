package ar.edu.jdynalloy.ast;

import java.util.Collections;
import java.util.Set;

import ar.edu.jdynalloy.ast.IJDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JAlloyCondition;
import ar.edu.jdynalloy.ast.JAssertionDeclaration;
import ar.edu.jdynalloy.xlator.JDynAlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyCheckCommand;

public final class JAssertionDeclaration {
	
	private final JAlloyCondition precondition;

	private final JAlloyCondition postcondition;

	private final JDynAlloyTyping typing;

	private final String assertionId;

	private final JStatement program;
	
	private final Set<AlloyCheckCommand> chkCmds;
	
	
	public JAssertionDeclaration(String assertionId, JDynAlloyTyping typing,
			JAlloyCondition pre, JStatement program, JAlloyCondition post) {
		this(assertionId,typing,pre,program,post,Collections.<AlloyCheckCommand>emptySet());
	}

	public JStatement getProgram() {
		return program;
	}

	public JDynAlloyTyping getTyping() {
		return typing;
	}

	public JAlloyCondition getPost() {
		return postcondition;
	}

	public JAlloyCondition getPre() {
		return precondition;
	}

	public String getAssertionId() {
		return assertionId;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null
				&& arg0.getClass().equals(JAssertionDeclaration.class)) {
			JAssertionDeclaration that = (JAssertionDeclaration) arg0;
			return this.getAssertionId().equals(that.getAssertionId())
					&& this.getTyping().equals(that.getTyping())
					&& this.getPre().equals(that.getPre())
					&& this.getProgram().equals(that.getProgram())
					&& this.getPost().equals(that.getPost());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return this.getAssertionId().hashCode()
				+ this.getTyping().hashCode()
				+ this.getPre().hashCode()
				+ this.getProgram().hashCode()
				+ this.getPost().hashCode();
	}

	@Override
	public String toString() {
		return "[" + this.getAssertionId() + ","
				+ this.getTyping().toString() + ","
				+ this.getPre().toString() + ","
				+ this.getProgram().toString() + ","
				+ this.getPost().toString() + "]";
	}

	public JAssertionDeclaration(String assertionId, JDynAlloyTyping typing,
			JAlloyCondition pre, JStatement program, JAlloyCondition post, Set<AlloyCheckCommand> chkCmds) {
		super();
		this.assertionId = assertionId;
		this.typing = typing;
		this.precondition = pre;
		this.program = program;
		this.postcondition = post;
		this.chkCmds = chkCmds;
	}

	public Set<AlloyCheckCommand> geChkCmds() {
		return chkCmds;
	}
	
	public Object accept(IJDynAlloyVisitor visitor) {
		return visitor.visit(this);
	}
	
	
}

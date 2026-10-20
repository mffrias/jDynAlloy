package ar.edu.jdynalloy.ast;

import java.util.LinkedList;
import java.util.List;

/**
 * A sequence of statements, translated into a DynAlloy composition; {@code JDynAlloyPrinter}
 * prints it between braces. The constructor that takes an array copies it into a new list; the
 * one that takes a list keeps that list, and {@link #getBlock()} returns it, so a change to the
 * list changes the block (the blocks of {@code JAlloyProgramBuffer} share their lists with the
 * buffer). A block must not be empty
 * when it is translated: {@code JDynAlloyXlatorVisitor.visit(JBlock)} throws
 * {@code IllegalArgumentException} for an empty one. Equal when the lists are equal.
 */
public final class JBlock extends JStatement {

	private final List<JStatement> block;

	public JBlock(JStatement[] b) {
		super();
		this.block = new LinkedList<JStatement>();
		for (int i = 0; i < b.length; i++) {
			this.block.add(b[i]);
		}
	}
	
	public JBlock(List<JStatement> block) {
		super();
		this.block = block;
	}

	public final Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	public List<JStatement> getBlock() {
		return block;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(JBlock.class)) {
			JBlock b = (JBlock) arg0;
			return getBlock().equals(b.getBlock());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return getBlock().hashCode();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer(); 
		for (JStatement stmt : getBlock()) {
			if (buff.length()>0)
				buff.append(";");
			
			buff.append(stmt.toString());
		}
		return buff.toString();
	}

}

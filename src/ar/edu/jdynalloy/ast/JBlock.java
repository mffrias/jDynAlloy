package ar.edu.jdynalloy.ast;

import java.util.LinkedList;
import java.util.List;

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

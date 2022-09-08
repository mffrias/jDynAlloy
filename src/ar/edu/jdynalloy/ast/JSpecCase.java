package ar.edu.jdynalloy.ast;

import java.util.List;

public final class JSpecCase {

	public List<JPrecondition> getRequires() {
		return requires;
	}

	public List<JPostcondition> getEnsures() {
		return ensures;
	}

	public List<JModifies> getModifies() {
		return modifies;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((modifies == null) ? 0 : modifies.hashCode());
		result = prime * result
				+ ((ensures == null) ? 0 : ensures.hashCode());
		result = prime * result
				+ ((requires == null) ? 0 : requires.hashCode());
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
		JSpecCase other = (JSpecCase) obj;
		if (modifies == null) {
			if (other.modifies != null)
				return false;
		} else if (!modifies.equals(other.modifies))
			return false;
		if (ensures == null) {
			if (other.ensures != null)
				return false;
		} else if (!ensures.equals(other.ensures))
			return false;
		if (requires == null) {
			if (other.requires != null)
				return false;
		} else if (!requires.equals(other.requires))
			return false;
		return true;
	}

	public final Object accept(IJDynAlloyVisitor v) {
		return v.visit(this);
	}

	public JSpecCase(List<JPrecondition> requires,
			List<JPostcondition> ensures, List<JModifies> modifies) {
		super();
		this.requires = requires;
		this.ensures = ensures;
		this.modifies = modifies;
	}

	private final List<JPrecondition> requires;
	
	private final List<JPostcondition> ensures;
	
	private final List<JModifies> modifies;
	
	
}

package ar.edu.jdynalloy.xlator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;

public final class JDynAlloyTyping extends AlloyTyping {

	public static final class Entry {

		private final AlloyVariable variable;
		private final JType type;

		public Entry(AlloyVariable variable, JType type) {
			super();
			this.variable = variable;
			this.type = type;
		}

		public AlloyVariable getVariable() {
			return variable;
		}

		public JType getType() {
			return type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result
					+ ((variable == null) ? 0 : variable.hashCode());
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
			Entry other = (Entry) obj;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			if (variable == null) {
				if (other.variable != null)
					return false;
			} else if (!variable.equals(other.variable))
				return false;
			return true;
		}

	}

	public JDynAlloyTyping() {
		super();
	}

	public JDynAlloyTyping(JDynAlloyTyping t) {
		this();
		m.addAll(t.m);
	}

	private final LinkedList<Entry> m = new LinkedList<Entry>();

	public JType getJAlloyType(AlloyVariable v) {
		for (Entry entry: m) {
			if (entry.variable.equals(v)) {
				return entry.type;
			}
		}
		return null;
	}

	public void put(AlloyVariable v, JType t) {
		super.put(v, t.toString());
		//m.put(v, t);
		for (Entry entry: m) {
			if (entry.variable.equals(v)) {
				int index = m.indexOf(entry);
				m.remove(index);
				m.add(index, new Entry(v,t));
				return;
			}
		}
		m.add(new Entry(v,t));
	}

	public String toString() {
		return m.toString();
	}

	public Iterator<Entry> entry_iterator() {
		return m.iterator();
	}
	
	public Iterator<AlloyVariable> iterator() {
		List<AlloyVariable> iter_list = new LinkedList<AlloyVariable>();
		for (Entry entry: m) {
			iter_list.add(entry.variable);
		}
		return iter_list.iterator();
	}
}

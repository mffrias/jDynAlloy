package ar.edu.jdynalloy.xlator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;

/**
 * The types of a set of variables, in two views: dynalloy4's {@code AlloyTyping}, which this
 * class extends, maps each variable to its type as text, and this class also keeps the
 * {@link JType}s, as a list of {@link Entry}s in the order the variables were first put. TACO
 * and jDynAlloy use it for the fields of a signature ({@code JSignature}), the fields of all
 * modules ({@code JDynAlloyContext.allFields()}) and the parameters and local variables of a
 * program being translated ({@code JDynAlloyXlatorVisitor}), whose text view goes into the
 * DynAlloy program.
 *
 * <p>Only {@link #put(AlloyVariable, JType)} keeps both views in step. It stores
 * {@code t.toString()} as the text, so a later change to the type ({@code JType.setAsNonNull()})
 * does not reach the text. The inherited {@code put} with a string and {@code remove} change
 * only the text view: {@link #getJAlloyType} and {@link #iterator()} do not see them.
 * {@code equals} is {@code AlloyTyping}'s, which is false when the other object's class is not
 * exactly {@code AlloyTyping}, so no {@code JDynAlloyTyping} equals a {@code JDynAlloyTyping},
 * itself included; {@code hashCode} is that of the text view. No static state.
 */
public final class JDynAlloyTyping extends AlloyTyping {

	/**
	 * A variable and its {@link JType}. {@code equals} and {@code hashCode} use both.
	 */
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

	/**
	 * Creates a typing with the same {@code JType} entries as {@code t}. Only the entries are
	 * copied, not the text view, so the copy's inherited methods ({@code varSet()}, {@code get},
	 * {@code contains}, {@code toString} of {@code AlloyTyping}) see no variable. Nothing in
	 * jDynAlloy or TACO calls this constructor.
	 *
	 * @param t the typing to copy
	 */
	public JDynAlloyTyping(JDynAlloyTyping t) {
		this();
		m.addAll(t.m);
	}

	private final LinkedList<Entry> m = new LinkedList<Entry>();

	/**
	 * Returns the {@code JType} of a variable, searching the entries in order.
	 *
	 * @param v the variable (compared with {@code equals}, so its prime counts)
	 * @return the type, or null if the variable was not put with {@link #put(AlloyVariable, JType)}
	 */
	public JType getJAlloyType(AlloyVariable v) {
		for (Entry entry: m) {
			if (entry.variable.equals(v)) {
				return entry.type;
			}
		}
		return null;
	}

	/**
	 * Sets the type of a variable in both views: the text view gets {@code t.toString()}, and the
	 * entry of the variable is replaced in place, or a new one is added at the end.
	 *
	 * @param v the variable
	 * @param t its type
	 */
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

	/**
	 * Returns the list of entries as text. {@link Entry} has no {@code toString}, so each entry
	 * prints as its class name and hash code; {@code AlloyTyping}'s {@code x:type} form is not
	 * used.
	 *
	 * @return the text
	 */
	public String toString() {
		return m.toString();
	}

	/**
	 * Returns an iterator over the entries themselves: its {@code remove} removes the entry but
	 * not the variable's text.
	 *
	 * @return the iterator
	 */
	public Iterator<Entry> entry_iterator() {
		return m.iterator();
	}
	
	/**
	 * Returns the variables of the entries, in the order they were first put. The iterator is
	 * over a copy. It replaces {@code AlloyTyping}'s, which {@code AlloyTyping.merge} and every
	 * for-each loop over a typing use.
	 *
	 * @return the iterator
	 */
	public Iterator<AlloyVariable> iterator() {
		List<AlloyVariable> iter_list = new LinkedList<AlloyVariable>();
		for (Entry entry: m) {
			iter_list.add(entry.variable);
		}
		return iter_list.iterator();
	}
}

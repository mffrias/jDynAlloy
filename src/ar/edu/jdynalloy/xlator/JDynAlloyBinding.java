package ar.edu.jdynalloy.xlator;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.uba.dc.rfm.alloy.ast.formulas.IProgramCall;

/**
 * The result of {@code BindingManager.execute()}: the program each call invokes, and the
 * implementors of each {@code virtual} program (docs/ARCHITECTURE.md, "How TACO calls it", step 1).
 * TACO's {@code JDynAlloyStage} builds one before the relevancy analysis and one on the sliced
 * modules, sets {@link #unfoldScopeForRecursiveCode} on each, and passes them to the relevancy
 * analysis and to {@link JDynAlloyTranslator}. Calls are looked up by the relevancy analysis and by
 * {@code VAlloyVisitor}, which renames each call after the program it resolves to.
 *
 * <p>The constructor copies the {@code IdentityHashMap}s of {@code BindingManager} into
 * {@code HashMap}s, so {@link #resolve} finds a call by {@code equals}, that is by its program id,
 * arguments and {@code super} flag ({@code JProgramCall}), not by identity. Two equal calls, in
 * different programs for example, are one key, which keeps the program of the one copied last (in
 * the identity map's iteration order). The implementors are keyed by
 * {@code JProgramDeclaration.equals}.
 *
 * <p>No static state. The bindings refer to the modules given to {@code BindingManager}; a call
 * that a later pass builds anew is found only if it equals one of them.
 */
public class JDynAlloyBinding {

	private final HashMap<IProgramCall, JProgramDeclaration> binding = new HashMap<IProgramCall, JProgramDeclaration>();
	private final HashMap<JProgramDeclaration, List<JProgramDeclaration>> implementors = new HashMap<JProgramDeclaration, List<JProgramDeclaration>>();

	public JDynAlloyBinding(
			IdentityHashMap<IProgramCall, JProgramDeclaration> _binding,
			IdentityHashMap<JProgramDeclaration, List<JProgramDeclaration>> _implementors) {
		super();
		binding.putAll(_binding);
		implementors.putAll(_implementors);
	}

	/**
	 * How many times recursion is unfolded: {@link JDynAlloyTranslator} gives it to
	 * {@code RecursionUnfolderVisitor} (docs/ARCHITECTURE.md, "The translation", step 6). Not set by
	 * {@code BindingManager}: TACO sets it to its loop-unrolling bound
	 * ({@code dynalloy.toAlloy.loopUnroll}). The default is 0.
	 */
	public int unfoldScopeForRecursiveCode;
	
	/**
	 * Returns the program a call invokes.
	 *
	 * @param call a call statement, or a call in a specification
	 * @return the program bound to a call equal to this one (see the class comment), or null if
	 *         there is none
	 */
	public JProgramDeclaration resolve(IProgramCall call) {
		return binding.get(call);
	}

	/**
	 * Returns the implementors of a {@code virtual} program, which {@code VAlloyVisitor}'s
	 * dispatcher calls according to the receiver's class.
	 *
	 * @param n a virtual program
	 * @return the list that {@code SearchImplementors} found, or null if the program has no entry:
	 *         today always null, since {@code SearchImplementors} returns an empty map
	 *         (docs/KNOWN-LIMITATIONS.md, "Dynamic dispatch does not work")
	 * @throws IllegalArgumentException if the program is not virtual
	 */
	public List<JProgramDeclaration> implementorsOf(JProgramDeclaration n) {
		if (!n.isVirtual())
			throw new IllegalArgumentException(
					"Only virtual programs have implementors.");
		return implementors.get(n);
	}

	@Override
	public String toString() {
		return binding.toString();
	}

	/**
	 * Moves a binding to another call: if a call equal to {@code oldProgramCall} is bound, removes it
	 * and binds {@code newProgramCall} to the same program. Nothing in jDynAlloy or TACO calls it.
	 *
	 * @param oldProgramCall the call whose binding moves
	 * @param newProgramCall the call that takes it
	 * @return true if there was a binding to move
	 */
	public boolean updateBinding(JProgramCall oldProgramCall,
			JProgramCall newProgramCall) {
		if (binding.containsKey(oldProgramCall)) {
			JProgramDeclaration value = binding.get(oldProgramCall);
			binding.remove(oldProgramCall);
			binding.put(newProgramCall, value);
			return true;
		} else
			return false;
	}

}

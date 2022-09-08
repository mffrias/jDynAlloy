package ar.edu.jdynalloy.xlator;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

import ar.edu.jdynalloy.ast.JProgramCall;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.uba.dc.rfm.alloy.ast.formulas.IProgramCall;

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

	public int unfoldScopeForRecursiveCode;
	
	public JProgramDeclaration resolve(IProgramCall call) {
		return binding.get(call);
	}

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

package ar.edu.jdynalloy.xlator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Allocation counts by type, filled by {@link ObjectCreationCollector}: the key is the type as
 * printed (the second argument of {@code instanceOf}, a signature id), the value the number of
 * allocations counted. TACO's {@code ScopeInference} takes each count as the program scope of
 * its type, and then {@code DynAlloyScopeInferencePlugin} overwrites the counts with the
 * inferred scopes ({@link #setAllocations(String, int)}). TACO has a copy of this class,
 * without {@code getAllocations} and {@code setAllocations}, in {@code ar.edu.taco.dynalloy}.
 * No static state; nothing resets the counts.
 */
public class ObjectCreationCounter {

	private Map<String, Integer> allocations = new HashMap<String, Integer>();

	public void inc_alloc_type(String type_str) {
		if (!this.allocations.containsKey(type_str)) {
			this.allocations.put(type_str, 0);
		}
		int alloc_counter = this.allocations.get(type_str);
		alloc_counter++;
		this.allocations.put(type_str, alloc_counter);
	}

	/**
	 * Returns the counts themselves, not a copy: changes to the map change the counter.
	 *
	 * @return the map from type to count
	 */
	public Map<String, Integer> getAllocations(){
		return this.allocations;
	}

	/**
	 * Replaces the count of a type, or adds it. TACO's {@code DynAlloyScopeInferencePlugin}
	 * writes the inferred scope of each counted type here, after the inference has read the
	 * counts.
	 *
	 * @param typeName the type
	 * @param theAmount the new count
	 */
	public void setAllocations(String typeName, int theAmount) {
		this.allocations.put(typeName, theAmount);
	}
	
	public Set<String> signatureSet() {
		return allocations.keySet();
	}
	
	/**
	 * Returns the count of a type.
	 *
	 * @param signature_id the type, one of {@link #signatureSet()}
	 * @return its count
	 * @throws NullPointerException for a type with no allocation (the map has no entry to unbox)
	 */
	public int getAllocationCount(String signature_id) {
		return allocations.get(signature_id);
	}
}

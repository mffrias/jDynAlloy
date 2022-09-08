/*
 * dynjalloy  Translator
 * Copyright (c) 2007 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */

package ar.edu.jdynalloy.xlator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph<E> {
	private Map<E, Set<E>> edges = new HashMap<E, Set<E>>();

	private Set<E> nodes = new HashSet<E>();

	private boolean allowCycles;

	public Graph(boolean _allowCycles) {
		super();
		this.allowCycles = _allowCycles;
	}

	public boolean containsNode(E nodeId) {
		return this.nodes.contains(nodeId);
	}

	public void addNode(E nodeId) {
		nodes.add(nodeId);
		edges.put(nodeId, new HashSet<E>());
	}

	public void addEdge(E from, E to) {
		if (!allowCycles && ascendentsOf(from).contains(to))
			throw new IllegalArgumentException(from + "->" + to
					+ "produces a cycle. Cycles are not allowd in this graph.");

		Set<E> childrenOf = edges.get(from);
		childrenOf.add(to);
	}

	private Set<E> parentOf(E nodeId) {
		Set<E> parentOf = new HashSet<E>();
		for (E from : edges.keySet()) {
			Set<E> to = edges.get(from);
			if (to.contains(nodeId))
				parentOf.add(from);
		}
		return parentOf;
	}

	public Set<E> ascendentsOf(E nodeId) {
		Set<E> ascendentsOf = new HashSet<E>();
		for (E ascendent : parentOf(nodeId)) {
			ascendentsOf.add(ascendent);
			ascendentsOf.addAll(ascendentsOf(ascendent));
		}
		return ascendentsOf;
	}

	public Set<E> childrenOf(E nodeId) {
		if (edges.get(nodeId)==null)
			System.out.println("Unknown node : " + nodeId);
		Set<E> childrenOf = edges.get(nodeId);
		return new HashSet<E>(childrenOf);
	}

	public Set<E> descendentsOf(E nodeId) {
		Set<E> descendentsOf = new HashSet<E>();
		Set<E> to_visit = new HashSet<E>();
		to_visit.add(nodeId);
		while (!to_visit.isEmpty()) {
			E visiting = to_visit.iterator().next();
			to_visit.remove(visiting);
			
			for (E child : childrenOf(visiting)) {
				if (!descendentsOf.contains(child)) {
					to_visit.add(child);
				}
			}
			descendentsOf.add(visiting);
		}
		return descendentsOf;
	}

	public boolean equals(Object other) {
		if (other instanceof Graph) {
			Graph<E> other_graph = (Graph<E>) other;
			return this.nodes.equals(other_graph.nodes) &&
			       this.edges.equals(other_graph.edges) ;
		} else
			return false;
	}

	public int hashCode() {
		return nodes.hashCode() + edges.hashCode();
	}

	public String toString() {
		return edges.toString();
	}
}

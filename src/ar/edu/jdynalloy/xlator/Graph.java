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

import ar.uba.dc.rfm.alloy.AlloyCheckCommand;

import java.util.*;

/**
 * A directed graph: a set of nodes, each with the set of its children (the targets of its
 * edges). The translator's call graphs use it ({@link CallGraphVisitor},
 * {@code RecursionUnfolderVisitor}), with cycles allowed, and so does
 * {@code JDynAlloyContext}, for the {@code extends} edges between signatures, without cycles.
 *
 * <p>An edge may point to a value that is not a node; asking for the children of such a value
 * fails (see {@link #childrenOf}). {@link #descendentsOf} includes the node itself;
 * {@link #strictDescendentsOf} only what is reached through at least one edge.
 * {@link #setExtraData(Object)} attaches any object to the graph. {@code equals} and
 * {@code hashCode} compare the nodes and the edges, not the extra data or whether cycles are
 * allowed. No static state.
 */
public class Graph<E> {
	private Map<E, Set<E>> edges = new HashMap<E, Set<E>>();

	private Set<E> nodes = new HashSet<E>();

	private boolean allowCycles;

	private Object extraData = null;

	public Graph(boolean _allowCycles) {
		super();
		this.allowCycles = _allowCycles;
	}

	public boolean containsNode(E nodeId) {
		return this.nodes.contains(nodeId);
	}

	/**
	 * Adds a node with no children. Adding a node that is already there removes its edges, so
	 * callers test {@link #containsNode} first.
	 *
	 * @param nodeId the node
	 */
	public void addNode(E nodeId) {
		nodes.add(nodeId);
		edges.put(nodeId, new HashSet<E>());
	}

	/**
	 * Adds an edge. {@code from} must be a node; {@code to} need not be. In a graph without
	 * cycles, an edge to one of {@code from}'s ascendants is rejected; an edge from a node to
	 * itself is not detected.
	 *
	 * @param from the source, a node
	 * @param to the target
	 * @throws IllegalArgumentException if cycles are not allowed and the edge closes one
	 * @throws NullPointerException if {@code from} is not a node
	 */
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

	/**
	 * Returns the nodes from which {@code nodeId} can be reached through at least one edge. In
	 * {@code JDynAlloyContext}'s graph, whose edges go from a signature to the one it extends,
	 * these are the subclasses. Recursive: if a cycle reaches {@code nodeId} it does not end
	 * ({@link StackOverflowError}); it is used only on graphs without cycles.
	 *
	 * @param nodeId any value
	 * @return a new set
	 */
	public Set<E> ascendentsOf(E nodeId) {
		Set<E> ascendentsOf = new HashSet<E>();
		for (E ascendent : parentOf(nodeId)) {
			ascendentsOf.add(ascendent);
			ascendentsOf.addAll(ascendentsOf(ascendent));
		}
		return ascendentsOf;
	}

	/**
	 * Returns the targets of the node's edges.
	 *
	 * @param nodeId a node
	 * @return a new set
	 * @throws NullPointerException if {@code nodeId} is not a node, after printing
	 *         "Unknown node : " and the value to standard output
	 */
	public Set<E> childrenOf(E nodeId) {
		if (edges.get(nodeId)==null)
			System.out.println("Unknown node : " + nodeId);
		Set<E> childrenOf = edges.get(nodeId);
		return new HashSet<E>(childrenOf);
	}

	/**
	 * Returns the nodes reachable from {@code nodeId}, {@code nodeId} itself included even if
	 * it is on no cycle. Ends on graphs with cycles.
	 *
	 * @param nodeId a node
	 * @return a new set, in no particular order
	 * @throws NullPointerException if {@code nodeId}, or a value reached from it, is not a node
	 *         (see {@link #childrenOf})
	 */
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

	/**
	 * Attaches an object to the graph. {@code JDynAlloyTranslator.translateAll} attaches the
	 * modules the first call graph was built from, a {@code Vector<JDynAlloyModule>}, and
	 * {@code RecursionUnfolderVisitor}'s constructor reads them back with a cast.
	 *
	 * @param o the object, or null
	 */
	public void setExtraData(Object o){
		this.extraData = o;
	}

	public Object getExtraData(){
		return this.extraData;
	}
	public int hashCode() {
		return nodes.hashCode() + edges.hashCode();
	}

	public String toString() {
		return edges.toString();
	}

	/**
	 * Returns the nodes reachable from {@code node}, each once, in breadth-first order starting
	 * with {@code node}. Despite the name this is not a topological order. Nothing in jDynAlloy
	 * or TACO calls it.
	 *
	 * @param node a node
	 * @return a new queue
	 * @throws NullPointerException if a value reached is not a node
	 */
	public Queue<E> topologicalSortFromNode(E node){
		Queue<E> result = new LinkedList<>();
		Queue<E> pending = new LinkedList<>();
		pending.offer(node);
		while (!pending.isEmpty()){
			E front = pending.poll();
			result.offer(front);
			Set<E> descendents = this.edges.get(front);
			for (E d : descendents){
				if (!result.contains(d) && !pending.contains(d)){
					pending.offer(d);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the nodes reachable from {@code programId} through at least one edge: it contains
	 * {@code programId} only if {@code programId} is on a cycle, which is how
	 * {@code RecursionUnfolderVisitor} tells whether a program is recursive (e8c6b06,
	 * docs/NAMING.md).
	 *
	 * @param programId a node
	 * @return a new set
	 * @throws NullPointerException if {@code programId}, or a value reached from it, is not a
	 *         node (see {@link #childrenOf})
	 */
	public Set<E> strictDescendentsOf(E programId) {
		Set<E> children = this.childrenOf(programId);
		Set<E> strict = new HashSet<E>();
		for (E e : children){
			strict.addAll(this.descendentsOf(e));
		}
		return strict;
	}
}

package ar.edu.jdynalloy.ast;

/**
 * Marks the nodes that annotate a statement instead of being one. Its only implementation is
 * {@link JLoopInvariant}, the annotation of a {@link JWhile}; nothing uses the interface as a
 * type.
 */
public interface IJDynAlloyAnnotation {

}

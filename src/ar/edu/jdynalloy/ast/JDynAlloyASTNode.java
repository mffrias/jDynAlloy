package ar.edu.jdynalloy.ast;

/**
 * Marks the two top-level nodes of the JDynAlloy AST, {@link JDynAlloyModule} and
 * {@link JProgramDeclaration}; statements, specifications and the other nodes do not implement
 * it. TACO uses it as the type of what a JML declaration became: its
 * {@code SimpleJmlToJDynAlloyContext} and {@code JfslToJDynAlloyEnv} map each class or interface
 * to its module and each method or constructor to its program.
 */
public interface JDynAlloyASTNode {

}

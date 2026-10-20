package ar.edu.jdynalloy.xlator;

import static ar.edu.jdynalloy.ast.AlloyIntArrayFactory.isArrayAccess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.edu.jdynalloy.ast.JAssignment;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.edu.jdynalloy.ast.ListAccess;
import ar.edu.jdynalloy.ast.MapAccess;
import ar.edu.jdynalloy.factory.DynalloyFactory;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

/**
 * Builds the modifies table: for each program, the fields it may assign, itself or through the
 * programs it calls. This is the last preparation step of {@code JDynAlloyTranslator.translateAll},
 * after the recursion is unfolded and on a new call graph of the result (docs/ARCHITECTURE.md,
 * "The translation", step 7). The translator stores the table in the {@code JDynAlloyContext},
 * and {@code JDynAlloyXlatorVisitor} looks up the method under analysis in it to decide which
 * invariants the postcondition re-checks and which {@code represents} are re-established
 * (docs/KNOWN-LIMITATIONS.md, "Analysis"). No state of its own.
 */
class ModifiesTableBuilder {

	/**
	 * Collects, for each program, the fields its own body assigns. The key is the program id, so
	 * overloads, and programs of the same name in different modules, share one entry: each
	 * program visited replaces the entry with a new, empty set, and only the last one visited
	 * with that id counts. An assignment {@code e.f := ...} adds the field {@code f}; an
	 * assignment to an array element adds {@code java_lang_IntArray_contents}, whatever the
	 * array's type; one to {@code Map_entries} or {@code List_contains} adds those; an
	 * assignment to a variable adds nothing. {@code havoc} and {@code createObject} are not
	 * counted, nor are calls (see {@link ModifiesTableBuilder#buildTable}). No static state.
	 */
	class DirectModifiesVisitor extends JDynAlloyVisitor {

		public DirectModifiesVisitor(boolean isJavaArithmetic) {
			super(isJavaArithmetic);
			// TODO Auto-generated constructor stub
		}

		
		public Map<String, Set<AlloyVariable>> getDirectModifiesTable() {
			return modifiesTable;
		}

		private final Map<String, Set<AlloyVariable>> modifiesTable = new HashMap<String, Set<AlloyVariable>>();
		private String currentProgramId;

		@Override
		public Object visit(JAssignment n) {
			AlloyExpression lvalue = n.getLvalue();
			Set<AlloyVariable> modifiesFields = modifiesTable
					.get(currentProgramId);

			if (isArrayAccess(lvalue)) {
				AlloyVariable objectArray = DynalloyFactory.INT_ARRAY_CONTENTS_FIELD_EXPRESSION
						.getVariable();
				modifiesFields.add(objectArray);

			} else if (MapAccess.isMapAccess(lvalue)) {
				AlloyVariable mapEntries = DynalloyFactory.MAP_ENTRIES
						.getVariable();
				modifiesFields.add(mapEntries);

			} else if (ListAccess.isListAccess(lvalue)) {
				AlloyVariable listContains = DynalloyFactory.LIST_CONTAINS
						.getVariable();
				modifiesFields.add(listContains);

			} else if (lvalue.getClass().equals(ExprJoin.class)) {
				ExprVariable exprVariable = (ExprVariable) ((ExprJoin) lvalue)
						.getRight();
				modifiesFields.add(exprVariable.getVariable());
			}

			return super.visit(n);
		}

		@Override
		public Object visit(JProgramDeclaration node) {
			currentProgramId = node.getProgramId();
			modifiesTable.put(currentProgramId, new HashSet<AlloyVariable>());
			return super.visit(node);
		}

	}

	/**
	 * Builds the table: for each program id, the fields that the programs with that id assign
	 * ({@link DirectModifiesVisitor}) together with those that the programs reachable from it in
	 * the call graph assign. On master the reachable part is commented out ("once the naming
	 * convention is fixed"); the {@code stryker} branch restored it (docs/NAMING.md, 0f920f1).
	 *
	 * @param modules the modules, as the recursion unfolder left them
	 * @param callGraph a call graph of the same modules ({@link CallGraphVisitor}), whose nodes
	 *        are program ids
	 * @param isJavaArith true with Java arithmetic; passed to the visitor
	 * @return the table, keyed by program id, with an entry for every program id of
	 *         {@code modules}
	 * @throws NullPointerException if a node reachable from a program has no program in
	 *         {@code modules} (a call to a program that is not declared), or if a program of
	 *         {@code modules} is not a node of the graph ({@link Graph#childrenOf} prints
	 *         "Unknown node : ..." first)
	 */
	public Map<String, Set<AlloyVariable>> buildTable(Vector<JDynAlloyModule> modules,
			Graph<String> callGraph, boolean isJavaArith) {

		// fill modifies tables
		DirectModifiesVisitor directModifiesVisitor = new DirectModifiesVisitor(isJavaArith);
		for (JDynAlloyModule prunedModule : modules) {
			prunedModule.accept(directModifiesVisitor);
		}
		Map<String, Set<AlloyVariable>> directModifiesTable = directModifiesVisitor
				.getDirectModifiesTable();

		Map<String, Set<AlloyVariable>> modifiesTable = directModifiesVisitor
		.getDirectModifiesTable();
		
		for (String programId : directModifiesTable.keySet()) {
			Set<AlloyVariable> modifiesFields = new HashSet<AlloyVariable>();

			Set<AlloyVariable> directFields = directModifiesTable
					.get(programId);
			modifiesFields.addAll(directFields);

			for (String descendentId : callGraph.descendentsOf(programId)) {
				Set<AlloyVariable> descendentFields = directModifiesTable.get(descendentId);
				modifiesFields.addAll(descendentFields);
			}

			modifiesTable.put(programId, modifiesFields);
		}

		return modifiesTable;
	}

}

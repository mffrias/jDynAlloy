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

class ModifiesTableBuilder {

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

// This has to be added back once the naming convention is fixed		
//			
//			
//			for (String descendentId : callGraph.descendentsOf(programId)) {
//				Set<AlloyVariable> descendentFields = directModifiesTable.get(descendentId);
//				modifiesFields.addAll(descendentFields);
//			}
//			
//			modifiesTable.put(programId, modifiesFields);
		}

		return modifiesTable;
	}

}

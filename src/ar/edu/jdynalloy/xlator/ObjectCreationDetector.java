package ar.edu.jdynalloy.xlator;

/**
 * This class is to be used in order to determine whether objects are created in a method or methods called from it.
 */

import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.util.DfsProgramVisitor;

public class ObjectCreationDetector extends DfsProgramVisitor {

	private static final String GET_UNUSED_OBJECT = "getUnusedObject";


	public ObjectCreationDetector() {
		super(new FormulaVisitor());
	}

	private boolean getUnusedObject_was_found = false;


	@Override
	public Object visit(InvokeAction u) {
		if (u.getActionId().equals(GET_UNUSED_OBJECT)) {
			getUnusedObject_was_found = true;
		}
		return super.visit(u);
	}
	
	
	public boolean getGetUnusedObject_was_found(){
		return getUnusedObject_was_found;
	}


}

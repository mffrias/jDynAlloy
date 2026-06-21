package ar.edu.jdynalloy.xlator;

/**
 * This class is to be used in order to determine whether objects are created in a method or methods called from it.
 */

import ar.edu.jdynalloy.ast.JDynAlloyModule;
import ar.edu.jdynalloy.ast.JProgramDeclaration;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.*;
import ar.uba.dc.rfm.dynalloy.util.DfsProgramVisitor;

import java.util.LinkedList;

public class ObjectCreationDetector extends DfsProgramVisitor {

	private static final String GET_UNUSED_OBJECT = "getUnusedObject";

	private JDynAlloyXlatorVisitor theVisitor;
	private String theProgramId;


	public ObjectCreationDetector(String programId, JDynAlloyXlatorVisitor visitor) {
		super(new FormulaVisitor());
		this.theProgramId = programId;
		this.theVisitor = visitor;
	}

	private boolean getUnusedObject_was_found = false;


	@Override
	public Object visit(InvokeAction u) {
		if (u.getActionId().equals(GET_UNUSED_OBJECT)) {
			getUnusedObject_was_found = true;
		}
		return super.visit(u);
	}

//	@Override
//	public Object visit(InvokeProgram p) {
//		String programName = p.getProgramId();
//		//possible bug if there is a cycle in the recursive calls graph
//		if (!programName.equals(this.theProgramId)){
//			for (JProgramDeclaration pDecl : theVisitor.getCurrentModule().getPrograms()){
//				if (pDecl.getProgramId().equals(p.getProgramId())){
//					JDynAlloyXlatorVisitor.ProgramTranslationResult translation = (JDynAlloyXlatorVisitor.ProgramTranslationResult) pDecl.accept(theVisitor);
//					DynalloyProgram theDynalloyProgram = translation.getProgram().getBody();
//					LinkedList<DynalloyProgram> list = new LinkedList<DynalloyProgram>();
//					list.add(new Skip());
//					list.add(theDynalloyProgram);
//					Composition compo = new Composition(list);
//					this.visit(compo);
//				}
//			}
//		}
//		return super.visit(p);
//	}




	
	
	public boolean getGetUnusedObject_was_found(){
		return getUnusedObject_was_found;
	}


}

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

/**
 * Tells whether a translated DynAlloy program allocates objects: after a visit,
 * {@link #getGetUnusedObject_was_found()} is true if it met the action {@code getUnusedObject},
 * the translation of {@code createObject}. Only the program itself is searched: calls are not
 * followed (the override that would have inlined them is commented out), and the program id and
 * visitor given to the constructor are not used. {@code JDynAlloyXlatorVisitor.visit} of a
 * program runs it on the translated body, but then sets its own {@code hasObjectCreation} to
 * true just before the test that guards the constraint on {@code usedObjects} in the
 * precondition, so that constraint is always added and the result has no effect. Unclear:
 * whether the constraint was meant to depend on the detection. The flag is never reset. No
 * static state.
 */
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

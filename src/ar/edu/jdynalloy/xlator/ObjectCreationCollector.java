package ar.edu.jdynalloy.xlator;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.alloy.util.ExpressionPrinter;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.util.DfsProgramVisitor;

/**
 * Counts the objects a DynAlloy program allocates, by type. It works on the translated program,
 * not on JDynAlloy: {@code JDynAlloyXlatorVisitor} translates {@code createObject<C>[x]} into
 * the action {@code getUnusedObject[x]} followed by the test {@code instanceOf[x, C]}, and the
 * collector counts one allocation of {@code C} (the test's second argument, as printed) for the
 * first {@code instanceOf} test after each {@code getUnusedObject}, in traversal order. Calls are
 * not followed. With {@code useClassSingletons} the test is {@code classOf[x] = CClass} instead,
 * and nothing is counted (TACO leaves {@code useClassSingletons} off).
 *
 * <p>TACO's scope inference uses it: {@code DynAlloyProgramScopeInferencePlugin} runs it on the
 * method under analysis, inlined and with its loops unrolled, in the parsed
 * {@code output.dals}. The counts accumulate in one {@link ObjectCreationCounter}
 * ({@link #getObjectCreationCounter()}) and are never reset, so an instance serves one program.
 * TACO has a copy of this class in {@code ar.edu.taco.dynalloy}, which only an unused plugin
 * uses. No static state.
 */
public class ObjectCreationCollector extends DfsProgramVisitor {

	private static final String GET_UNUSED_OBJECT = "getUnusedObject";

	private static final String INSTANCE_OF = "instanceOf";

	public ObjectCreationCollector() {
		super(new FormulaVisitor());
	}

	private boolean getUnusedObject_was_found = false;

	private ObjectCreationCounter object_creation_counter = new ObjectCreationCounter();

	@Override
	public Object visit(InvokeAction u) {
		if (u.getActionId().equals(GET_UNUSED_OBJECT)) {
			getUnusedObject_was_found = true;
		}
		return super.visit(u);
	}

	@Override
	public Object visit(TestPredicate t) {
		if (getUnusedObject_was_found) {
			if (t.getPredicateFormula().getPredicateId().equals(INSTANCE_OF)) {
				AlloyExpression type_set = t.getPredicateFormula().getParameters().get(1);
				allocate(type_set);
				getUnusedObject_was_found = false;
			}
		}
		return super.visit(t);
	}
	

	private void allocate(AlloyExpression type_set) {
		String type_str = (String) type_set.accept(new ExpressionPrinter());
		object_creation_counter.inc_alloc_type(type_str);
	}

	public ObjectCreationCounter getObjectCreationCounter() {
		return object_creation_counter;
	}

}

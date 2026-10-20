/**
 * 
 */
package ar.edu.jdynalloy.xlator;

import java.util.ArrayList;
import java.util.List;

import ar.edu.jdynalloy.ast.JAssignment;
import ar.edu.jdynalloy.ast.JCreateObject;
import ar.edu.jdynalloy.ast.JDynAlloyVisitor;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

/**
 * @author elgaby
 *
 */
public class DynJAlloyLocationFinderVisitor extends JDynAlloyVisitor {
	
	
	public DynJAlloyLocationFinderVisitor(boolean isJavaArith) {
		super(isJavaArith);
	}
		
		
	private List<AlloyExpression> locations = new ArrayList<AlloyExpression>();
	
	/**
	 * Adds the variable of a {@code createObject} to the locations (see
	 * {@link #visit(JAssignment)}).
	 *
	 * @param n the statement
	 * @return {@code JDynAlloyVisitor}'s result
	 */
	@Override
	public Object visit(JCreateObject n) {
		ExprVariable leftVariable = ExprVariable.buildExprVariable(n.getLvalue());
		
		AlloyExpression location = extractLocation(leftVariable);

		if (!this.locations.contains(location)) {
			this.locations.add(location);			
		}
		
		return super.visit(n);
	}

	/**
	 * Adds the location an assignment writes to the locations, which {@link #getLocations()}
	 * returns in the order they were met, each once. The class collects what a loop modifies:
	 * {@link JDynAlloyTransformationVisitor} runs one instance on each {@code while} that has a
	 * loop invariant and {@code havoc}s every location found (docs/ARCHITECTURE.md, "The
	 * translation", step 2). Only {@code createObject} and assignments count, in the loop's
	 * body and in loops nested in it: what the programs called in the loop assign is not found.
	 * From TACO no loop with an invariant reaches jDynAlloy (TACO's
	 * {@code WhileRemoverSimplifier} unrolls them all), so this class is not used there.
	 *
	 * <p>The location of {@code x := ...} is {@code x}, and that of an array or other function
	 * left side ({@code arrayAccess[...]}) the left side itself. That of {@code e.f := ...} is the
	 * whole field {@code f} if {@code e} is already a location, that is, if an earlier statement
	 * of the loop assigned it, and {@code e.f} otherwise; so the result depends on the order of
	 * the statements. No static state.
	 *
	 * @param n the assignment
	 * @return {@code JDynAlloyVisitor}'s result
	 * @throws IllegalArgumentException if the left side is not a variable, a join or a function
	 *         ("Unexpected left side of assignment. Received: ...")
	 */
	@Override
	public Object visit(JAssignment n) {
		AlloyExpression location = extractLocation(n.getLvalue());
		
		if (!this.locations.contains(location)) {
			this.locations.add(location);			
		}
		
		return super.visit(n);
	}
	
	/**
	 * 
	 * @return
	 */
	public List<AlloyExpression> getLocations() {
		return locations;
	}
	
	/**
	 * @param leftValue
	 */
	private AlloyExpression extractLocation(AlloyExpression leftValue) {
		if (leftValue instanceof ExprVariable) {
			return leftValue;			
		} else if (leftValue instanceof ExprJoin){
			if (this.locations.contains(((ExprJoin) leftValue).getLeft())) {
				return ((ExprJoin) leftValue).getRight();
			} else {
				return leftValue;
			}
		} else if (leftValue instanceof ExprFunction) {
			return leftValue;
		} else {
			throw new IllegalArgumentException("Unexpected left side of assignment. Received: " + leftValue.getClass());
		}
	};	
}

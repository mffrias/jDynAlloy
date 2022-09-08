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
	
	@Override
	public Object visit(JCreateObject n) {
		ExprVariable leftVariable = ExprVariable.buildExprVariable(n.getLvalue());
		
		AlloyExpression location = extractLocation(leftVariable);

		if (!this.locations.contains(location)) {
			this.locations.add(location);			
		}
		
		return super.visit(n);
	}

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

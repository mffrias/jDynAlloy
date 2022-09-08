/**
 * 
 */
package ar.edu.jdynalloy.xlator;

import java.util.ArrayList;
import java.util.List;

import ar.edu.jdynalloy.ast.JAssert;
import ar.edu.jdynalloy.ast.JAssume;
import ar.edu.jdynalloy.ast.JBlock;
import ar.edu.jdynalloy.ast.JDynAlloyMutator;
import ar.edu.jdynalloy.ast.JHavoc;
import ar.edu.jdynalloy.ast.JIfThenElse;
import ar.edu.jdynalloy.ast.JSkip;
import ar.edu.jdynalloy.ast.JStatement;
import ar.edu.jdynalloy.ast.JWhile;
import ar.edu.jdynalloy.factory.JExpressionFactory;
import ar.edu.jdynalloy.factory.JPredicateFactory;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;

/**
 * @author elgaby
 *
 */
public class JDynAlloyTransformationVisitor extends JDynAlloyMutator {
	
	
	public JDynAlloyTransformationVisitor(boolean isJavaArithmetic) {
		super(isJavaArithmetic);
	}

	
	@Override
	public Object visit(JWhile n) {
		JWhile module = (JWhile) super.visit(n);

		// Loop invariant
		if (module.getLoopInvariant() != null) {
			
			/**
			 * In this case we need to transform the code to match the following code:
			 * 
			 *		assert [invariant_condition] ;
			 *		havoc [updated_location] ;
			 *		assume [invariant_condition] ;
			 *		if [loop_condition] {
			 *			Loop_Body;
			 *			assert [invariant_condition] ;
			 *			assume false ;
			 *		}
			 * 
			 */
			
			// Generate an assertion using the loop invariant condition as its condition.
			JAssert assertionInvariant = new JAssert(module.getLoopInvariant().getFormula());
			JStatement surroundedAssertionInvariant = getSurroundedStatement(assertionInvariant, module.getBranchId());
			
			// Generate the assumption using the loop invariant condition
			JAssume assumeInvariant = new JAssume(module.getLoopInvariant().getFormula());
			
			// Generate conditional part of the code.
			List<JStatement> ifStatements = new ArrayList<JStatement>();
			ifStatements.add(module.getBody());
			ifStatements.add(surroundedAssertionInvariant);
			
			JStatement surroundedAssumeFalse = getSurroundedStatement(new JAssume(JPredicateFactory.liftExpression(JExpressionFactory.FALSE_EXPRESSION)), module.getBranchId()); 
			ifStatements.add(surroundedAssumeFalse);
			
			JBlock trueStmt = new JBlock(ifStatements);
			JIfThenElse conditional = new JIfThenElse(module.getCondition(), trueStmt, new JSkip(), module.getBranchId());
			
			// Look for updated locations into the loop body
			DynJAlloyLocationFinderVisitor locationFinderVisitor = new DynJAlloyLocationFinderVisitor(this.isJavaArithmetic);
			module.accept(locationFinderVisitor);
			List<AlloyExpression> updatedLocations = locationFinderVisitor.getLocations();
			
			// generate the havoc and assumption code surrounded.
			List<JStatement> updatesStatementList = new ArrayList<JStatement>();
			for (AlloyExpression alloyExpression : updatedLocations) {
				updatesStatementList.add(new JHavoc(alloyExpression));
			}
			updatesStatementList.add(assumeInvariant);
			
			JStatement updatesStatementCode = getSurroundedStatement(new JBlock(updatesStatementList), module.getBranchId());

			List<JStatement> transformedCodeStatements = new ArrayList<JStatement>();
			transformedCodeStatements.add(surroundedAssertionInvariant);
			transformedCodeStatements.add(updatesStatementCode);
			transformedCodeStatements.add(conditional);
			
			JBlock transformedCodeBlock = new JBlock(transformedCodeStatements);
			return transformedCodeBlock;
		} else {
			return module;
		}
		
	}
		
	/**
	 * Return the jStatement surrounded with a conditional
	 * 
	 * @param jStatement
	 * @param branchId
	 * @return
	 */
	private JStatement getSurroundedStatement(JStatement jStatement, String branchId) {
		JStatement returnStatement = jStatement;
		
		AlloyFormula nullThrowFormula = new EqualsFormula(JExpressionFactory.THROW_EXPRESSION, JExpressionFactory.NULL_EXPRESSION);
		
		AlloyFormula exitReachedFormula = new EqualsFormula(JExpressionFactory.EXIT_REACHED_EXPRESSION, JExpressionFactory.FALSE_EXPRESSION);
		
		exitReachedFormula = new AndFormula(nullThrowFormula, exitReachedFormula);
		
		returnStatement = new JIfThenElse(exitReachedFormula, jStatement, new JSkip(), branchId);
		
		return returnStatement;
	}
	
}

package visitor;


import logicaloperators.*;
import util.Catalog;


/**
 * @author Yixin Cui
 * @author Haodong Ping
 * build the physical plan using visitor pattern
 *
 */
public class PlanEvaluater {
	V_Calculater v_cal = new V_Calculater();
	/**
	 * Get the physical scan operator
	 */
	public void visit(LogicScanOperator scanop) {

	}
	
	/**
	 * Get the physical selection operator
	 */
	public void visit(LogicSelectOperator selop) {

		
	}
	
	/**
	 * Get the physical project operator
	 */
	public void visit(LogicProjectOperator lpo) {
		
	}
	
	/**
	 * Get the physical sort operator
	 */
	public void visit(LogicSortOperator sortop) {

	}
	
	/**
	 * Get the physical distinct operator
	 */
	public void visit(LogicDuplicateEliminationOperator ldeo) {

	}
	
	/**
	 * Get the physical join operator
	 */
	public void visit(LogicJoinOperator ljo) {
		ljo.getLeft().accept(this);
		ljo.getRight().accept(this);
		v_cal.set(ljo.getLeft().alias, ljo.getLeft().stats, ljo.getLeft().Schema, ljo.getRight().alias, ljo.getRight().stats, ljo.getRight().Schema);
		ljo.getExpr().accept(v_cal);
		ljo.cost = ljo.getLeft().cost + ljo.getRight().cost + ljo.getLeft().stats.n*ljo.getRight().stats.n/v_cal.reduction;
		ljo.stats = Catalog.getInstance().new statsInfo(v_cal.s1);
		ljo.stats.add(v_cal.s2);
		ljo.stats.n = ljo.getLeft().stats.n*ljo.getRight().stats.n/v_cal.reduction;
		ljo.Schema = ljo.getLeft().Schema;
		ljo.Schema.addAll(ljo.getRight().Schema);
		ljo.alias = "???";
	}
	/**
	 * Get the final operator
	 */

}
/**
 * @author Yixin Cui
 * @author Haodong Ping
 * create a pair the class for storing the left operator and right operator for binary operators
 *
 */

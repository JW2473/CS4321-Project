package visitor;


import java.util.ArrayList;
import java.util.List;

import logicaloperators.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectItem;
import util.Catalog;
import util.Tools;


/**
 * @author Yixin Cui
 * @author Haodong Ping
 * build the physical plan using visitor pattern
 *
 */
public class PlanEvaluater {
	StatsCalculater v_cal = new StatsCalculater();
	/**
	 * Get the physical scan operator
	 */
	public void visit(LogicScanOperator scanop) {
		String tableFullName = scanop.mt.getFullTableName();
		scanop.Schema = Tools.InitilaizeWholeColumnName(Catalog.getUniqueName(tableFullName), tableFullName);
		scanop.cost = 0;
		scanop.stats = Catalog.getStats(tableFullName);
	}
	
	/**
	 * Get the physical selection operator
	 */
	public void visit(LogicSelectOperator selop) {
		selop.Schema = selop.getChild().Schema;
		selop.cost = selop.getChild().cost;
		v_cal.set(selop.getChild().stats, selop.Schema);
		selop.getExpr().accept(v_cal);
		selop.stats = Catalog.getInstance().new statsInfo(v_cal.s1);
		for(int i = 0; i < selop.Schema.size(); i++)
			selop.stats.n = selop.stats.n*(selop.stats.ma[i] - selop.stats.mi[i] + 1)/(selop.getChild().stats.ma[i] - selop.getChild().stats.mi[i] + 1);
	}
	
	/**
	 * Get the physical project operator
	 */
	public void visit(LogicProjectOperator lpo) {
		if(lpo.Schema != null) {
			return;
		}
		List<SelectItem> select = lpo.getSi();
		lpo.Schema = new ArrayList<String>();
		int[] ma = new int[select.size()];
		int[] mi = new int[select.size()];
		LogicOperator child = lpo.getChild();
		lpo.cost = child.cost;
		for(int i = 0; i < select.size(); i++) {
			Column col = (Column) select.get(i);
			String name = Tools.rebuildWholeColumnName(col);
			lpo.Schema.add(name);
			ma[i] = child.stats.ma[child.Schema.indexOf(name)];
			mi[i] = child.stats.mi[child.Schema.indexOf(name)];
		}
		lpo.stats = Catalog.getInstance().new statsInfo(child.stats.n, ma, mi);
	}
	
	/**
	 * Get the physical sort operator
	 */
	public void visit(LogicSortOperator sortop) {
		LogicOperator child = sortop.getChild();
		sortop.Schema = child.Schema;
		sortop.cost = child.cost;
		sortop.stats = child.stats;
	}
	
	/**
	 * Get the physical distinct operator
	 */
	public void visit(LogicDuplicateEliminationOperator ldeo) {
		LogicOperator child = ldeo.getChild();
		ldeo.Schema = child.Schema;
		ldeo.cost = child.cost;
		ldeo.stats = child.stats;
	}
	
	/**
	 * Get the physical join operator
	 */
	public void visit(LogicJoinOperator ljo) {
		ljo.getLeft().accept(this);
		ljo.getRight().accept(this);
		v_cal.set(ljo.getLeft().stats, ljo.getLeft().Schema, ljo.getRight().stats, ljo.getRight().Schema);
		ljo.getExpr().accept(v_cal);
		ljo.cost = ljo.getLeft().cost + ljo.getRight().cost + ljo.getLeft().stats.n*ljo.getRight().stats.n/v_cal.reduction;
		ljo.stats = Catalog.getInstance().new statsInfo(v_cal.s1);
		ljo.stats.add(v_cal.s2);
		ljo.stats.n = ljo.getLeft().stats.n*ljo.getRight().stats.n/v_cal.reduction;
		ljo.Schema = ljo.getLeft().Schema;
		ljo.Schema.addAll(ljo.getRight().Schema);
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

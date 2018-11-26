package visitor;

import java.util.*;

import logicaloperators.*;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import physicaloperators.BlockNestedJoinOperator;
import physicaloperators.DuplicateEliminationOperator;
import physicaloperators.ExternalSortOperator;
import physicaloperators.InMemorySortOperator;
import physicaloperators.IndexScanOperator;
import physicaloperators.Operator;
import physicaloperators.ProjectOperator;
import physicaloperators.ScanOperator;
import physicaloperators.SelectOperator;
import physicaloperators.SortMergeJoinOperator;
import physicaloperators.TupleNestedLoopJoinOperator;
import util.Catalog;
import util.MyColumn;
import util.ParseWhere;
import util.Tools;
import util.UnionFindElement;;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * build the physical plan using visitor pattern
 *
 */
public class PhysicalPlanBuilder {
	
	Operator op;
	int layer = 0;
	boolean logic_join_status = false;
	/**
	 * Get the physical scan operator
	 */
	public void visit(LogicScanOperator scanop) {
		scanop.print();
		op = new ScanOperator(scanop.mt);
	}
	
	/**
	 * Get the physical selection operator
	 */
	public void visit(LogicSelectOperator selop) {
		selop.print();
		LogicScanOperator child  = (LogicScanOperator)(selop.getChild());
		child.setLayer(selop.layer + 1);
		ScanOperator so = null;
		Set<MyColumn> colSet = new HashSet<>();
		List<Expression> exps = ParseWhere.splitWhere(selop.getExpr());
		float minCost = Integer.MAX_VALUE;
		String scanColumn;
		for(Expression exp : exps) {
			BinaryExpression be = (BinaryExpression) exp;
			Expression l = be.getLeftExpression();
			Expression r = be.getRightExpression();
			Column c = (Column)(l instanceof Column ? (Column)l : (Column)r);
			String tableName = Catalog.getTableFullName(c.getTable().getName());
			String colName = c.getColumnName();
			if (colSet.add(new MyColumn(c))) {
				UnionFindElement ufe = ParseWhere.ufv.getUnionFind().find(c);
				int range = (ufe.getUpperBound() == null ? Tools.getUpperBound(c) : ufe.getUpperBound())
						  - (ufe.getLowerBound() == null ? Tools.getLowerBound(c) : ufe.getLowerBound()) + 1;
				int totalRange = Tools.getUpperBound(c) - Tools.getLowerBound(c) + 1;
				int totalCount = Tools.getTupleNumbers(tableName);
				int tupleSize = Catalog.schema_map.get(tableName).size();
				int pageNum = (int) Math.ceil((float) totalCount * tupleSize / Catalog.pageSize);
				int leafPageNum = Catalog.indexInfo.get(tableName).leafPageNum(colName);
				int root2Leaf = 3;
				boolean isClustered = Catalog.indexInfo.get(tableName).isClustered();
				float reductionFactor = (float) range / totalRange;
				float cost = isClustered ? root2Leaf + pageNum * reductionFactor
						   				 : root2Leaf + (leafPageNum + totalCount) * reductionFactor;
//				System.out.println(cost);
				if (cost < pageNum || cost < minCost) {
					scanColumn = colName;
					so = new IndexScanOperator(child.mt, colName, ufe.getLowerBound(), ufe.getUpperBound());
				}else {
					child.accept(this);
					so = (ScanOperator) op;
				}
			}
		}	
		op = new SelectOperator(so, selop.getExpr());
	}
	
	/**
	 * Get the physical project operator
	 */
	public void visit(LogicProjectOperator lpo) {
		op = null;
		lpo.print();
		lpo.getChild().setLayer(lpo.layer+1);
		lpo.getChild().accept(this);
		op = new ProjectOperator(lpo.getSi(), op);
	}
	
	/**
	 * Get the physical sort operator
	 */
	public void visit(LogicSortOperator sortop) {
		op = null;
		sortop.print();
		sortop.getChild().setLayer(sortop.layer+1);
		sortop.getChild().accept(this);
		switch (Catalog.sortConfig) {
			case Catalog.IMS:
				op = new InMemorySortOperator(op,sortop.getObe());
				break;
			case Catalog.EMS:
				op = new ExternalSortOperator(op,sortop.getObe());
				break;
			default:
				throw new UnsupportedException();
		}
	}
	
	/**
	 * Get the physical distinct operator
	 */
	public void visit(LogicDuplicateEliminationOperator ldeo) {
		op = null;
		ldeo.print();
		ldeo.getChild().setLayer(ldeo.layer+1);
		ldeo.getChild().accept(this);
		op = new DuplicateEliminationOperator(op);
	}
	
	/**
	 * Get the physical join operator
	 */
	public void visit(LogicJoinOperator ljo) {
		int next_layer = ljo.layer;
		if(!logic_join_status) {
			logic_join_status = true;
			ljo.print();
			next_layer++;
		}
		
		pair p = new pair();
		op = null;
		ljo.getLeft().setLayer(next_layer);
		ljo.getLeft().accept(this);
		p.left = op;
		op = null;
		ljo.getRight().setLayer(next_layer);
		ljo.getRight().accept(this);
		p.right = op;
		Expression e = ljo.getExpr();
		int Method = Catalog.BNLJ;
		List<Expression> exps = ParseWhere.splitWhere(e);
		for(Expression exp : exps) {
			if(exp instanceof EqualsTo) {
				Method = Catalog.SMJ;
				break;
			}
		}

		switch (Method) {
			case Catalog.TNLJ:
				op = new TupleNestedLoopJoinOperator(p.left,p.right,ljo.getExpr());
				break;
			case Catalog.BNLJ:
				op = new BlockNestedJoinOperator(p.left, p.right, ljo.getExpr());
				break;
			case Catalog.SMJ:
				Map<String,List<Column>> m = ParseWhere.parseJoin(p.left, p.right, ljo.getExpr());
				if(Catalog.sortConfig == Catalog.IMS ) {
					p.left = new InMemorySortOperator(p.left,m.get("left"));
					p.right = new InMemorySortOperator(p.right,m.get("right"));
				} else {
					p.left = new ExternalSortOperator(p.left,m.get("left"));
					p.right = new ExternalSortOperator(p.right,m.get("right"));
				}
				op = new SortMergeJoinOperator(p.left, p.right, ljo.getExpr(), m.get("left"), m.get("right"));
				break;
			default:
				throw new UnsupportedException();			
		}
	}
	/**
	 * Get the final operator
	 */
	public Operator getOp() {
		return op;
	}


}
/**
 * @author Yixin Cui
 * @author Haodong Ping
 * create a pair the class for storing the left operator and right operator for binary operators
 *
 */
class pair {
	Operator left;
	Operator right;

}

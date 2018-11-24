package visitor;

import java.util.*;

import logicaloperators.*;
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
import util.ParseWhere;;

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
		/*
		if(Catalog.useIndex) {
			String tabName = child.mt.getFullTableName();
			String[] idInfo = null;//Catalog.indexInfo.get(tabName);
			if(idInfo != null && idInfo.length >= 4) {
				String attr = idInfo[1];
				String[] range = ParseWhere.parseSel(attr, selop.getExpr());
				if( (!range[0].equals("x") ) || ( !range[1].equals("x") ) ) {
					Integer lowkey = null;
					Integer highkey = null;
					if(!range[0].equals("x")) 
						lowkey = Integer.getInteger(range[0]);
					if(!range[1].equals("x")) 
						highkey = Integer.getInteger(range[1]);
					so = new IndexScanOperator(child.mt, lowkey, highkey);
				}
			}
		}
		*/
		if(so == null) {
			child.accept(this);
			so = (ScanOperator)op;
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

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
import physicaloperators.Operator;
import physicaloperators.ProjectOperator;
import physicaloperators.ScanOperator;
import physicaloperators.SelectOperator;
import physicaloperators.TupleNestedLoopJoinOperator;
import util.Catalog;
import util.ParseWhere;;

public class PhysicalPlanBuilder {
	
	Operator op;
	public void visit(LogicScanOperator scanop) {
		op = new ScanOperator(scanop.mt);
	}
	
	public void visit(LogicSelectOperator selop) {
		op = null;
		selop.getChild().accept(this);
		op = new SelectOperator(op, selop.getExpr());
	}
	
	public void visit(LogicProjectOperator lpo) {
		op = null;
		lpo.getChild().accept(this);
		op = new ProjectOperator(lpo.getSi(), op);
	}
	
	public void visit(LogicSortOperator sortop) {
		op = null;
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
	
	public void visit(LogicDuplicateEliminationOperator ldeo) {
		op = null;
		ldeo.getChild().accept(this);
		op = new DuplicateEliminationOperator(op);
	}
	
	public void visit(LogicJoinOperator ljo) {
		pair p = new pair();
		op = null;
		ljo.getLeft().accept(this);
		p.left = op;
		op = null;
		ljo.getRight().accept(this);
		p.right = op;
		switch (Catalog.joinConfig) {
			case Catalog.TNLJ:
				op = new TupleNestedLoopJoinOperator(p.left,p.right,ljo.getExpr());
//				System.out.println(ljo.getExpr().toString());
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
				op = new TupleNestedLoopJoinOperator(p.left,p.right,ljo.getExpr());
//				EqualsTo exp = (EqualsTo)(ljo.getExpr());
//				Column left = (Column)(exp.getLeftExpression());
//				
//				EqualsTo exp1 = (EqualsTo)(ljo.getExpr());
//				Column right = (Column)(exp.getLeftExpression());
				break;
			default:
				throw new UnsupportedException();			
		}
	}

	public Operator getOp() {
		return op;
	}


}

class pair {
	Operator left;
	Operator right;

}

package visitor;

import logicaloperators.*;
import physicaloperators.DuplicateEliminationOperator;
import physicaloperators.InMemorySortOperator;
import physicaloperators.JoinOperator;
import physicaloperators.Operator;
import physicaloperators.ProjectOperator;
import physicaloperators.ScanOperator;
import physicaloperators.SelectOperator;
import util.Catalog;;

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
				op = new JoinOperator(p.left,p.right,ljo.getExpr());
				break;
			case Catalog.BNLJ:
				break;
			case Catalog.SMJ:
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

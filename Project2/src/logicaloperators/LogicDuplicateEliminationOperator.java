package logicaloperators;

import visitor.PhysicalPlanBuilder;

public class LogicDuplicateEliminationOperator extends LogicOperator {
	
	LogicOperator child;
	
	public LogicDuplicateEliminationOperator(LogicOperator child) {
		// TODO Auto-generated constructor stub
		this.child = child;
	}
	@Override
	public void accept(PhysicalPlanBuilder ppb) {
		// TODO Auto-generated method stub
		ppb.visit(this);
	}
	public LogicOperator getChild() {
		return child;
	}
}

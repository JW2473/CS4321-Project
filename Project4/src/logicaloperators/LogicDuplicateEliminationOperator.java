package logicaloperators;

import visitor.PhysicalPlanBuilder;
/**
 * @author Yixin Cui
 * @author Haodong Ping
 *  Logic Distinct Operator class
 *
 */
public class LogicDuplicateEliminationOperator extends LogicOperator {
	
	LogicOperator child;
	
	public LogicDuplicateEliminationOperator(LogicOperator child) {
		this.child = child;
	}
	@Override
	public void accept(PhysicalPlanBuilder ppb) {
		ppb.visit(this);
	}
	public LogicOperator getChild() {
		return child;
	}
}

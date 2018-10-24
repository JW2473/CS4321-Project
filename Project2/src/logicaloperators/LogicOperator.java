package logicaloperators;

import visitor.PhysicalPlanBuilder;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * This is the abstract class of logic operators that determines the methods for all logic operators
 */
public abstract class LogicOperator {
	
	public abstract void accept(PhysicalPlanBuilder ppb);
}

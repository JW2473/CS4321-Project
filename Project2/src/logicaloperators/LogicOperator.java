package logicaloperators;

import visitor.PhysicalPlanBuilder;

public abstract class LogicOperator {
	
	public abstract void accept(PhysicalPlanBuilder ppb);
}

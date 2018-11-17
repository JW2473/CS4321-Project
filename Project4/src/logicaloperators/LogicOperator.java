package logicaloperators;

import java.util.List;

import util.Catalog.statsInfo;
import visitor.PhysicalPlanBuilder;
import visitor.PlanEvaluater;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * This is the abstract class of logic operators that determines the methods for all logic operators
 */
public abstract class LogicOperator {
	
	public String alias;
	public statsInfo stats;
	public int cost = 0;
	public List<String> Schema;
	
	public abstract void accept(PhysicalPlanBuilder ppb);

	public abstract void accept(PlanEvaluater planEvaluater);

}

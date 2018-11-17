package logicaloperators;

import util.MyTable;
import visitor.PhysicalPlanBuilder;
import visitor.PlanEvaluater;
/**
 * @author Yixin Cui
 * @author Haodong Ping
 *  Logic Scan Operator class
 *
 */
public class LogicScanOperator extends LogicOperator {
	
	public MyTable mt;
	public LogicScanOperator(MyTable mt) {		
		this.mt = mt;
	}
	
	@Override
	public void accept(PhysicalPlanBuilder ppb) {
		ppb.visit(this);
	}

	@Override
	public void accept(PlanEvaluater planEvaluater) {
		planEvaluater.visit(this);		
	}

}

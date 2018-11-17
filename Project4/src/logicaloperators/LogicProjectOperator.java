package logicaloperators;

import java.util.List;

import net.sf.jsqlparser.statement.select.SelectItem;
import visitor.PhysicalPlanBuilder;
import visitor.PlanEvaluater;
/**
 * @author Yixin Cui
 * @author Haodong Ping
 *  Logic project Operator class
 *
 */
public class LogicProjectOperator extends LogicOperator {
	
	LogicOperator child;
	List<SelectItem> si;
	
	public LogicProjectOperator(LogicOperator child, List<SelectItem> si) {
		this.child = child;
		this.si = si;
	}
	@Override
	public void accept(PhysicalPlanBuilder ppb) {

		ppb.visit(this);
	}
	public LogicOperator getChild() {
		return child;
	}
	
	public List<SelectItem> getSi() {
		return si;
	}
	@Override
	public void accept(PlanEvaluater planEvaluater) {
		// TODO Auto-generated method stub
		
	}

}

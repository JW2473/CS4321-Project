package logicaloperators;

import java.util.List;

import net.sf.jsqlparser.statement.select.OrderByElement;
import visitor.PhysicalPlanBuilder;

public class LogicSortOperator extends LogicOperator {
	
	LogicOperator child;
	List<OrderByElement> obe;
	
	public LogicSortOperator(LogicOperator child, List<OrderByElement> obe) {
		// TODO Auto-generated constructor stub
		this.child = child;
		this.obe = obe;
	}
	
	@Override
	public void accept(PhysicalPlanBuilder ppb) {
		// TODO Auto-generated method stub
		ppb.visit(this);
	}

	public LogicOperator getChild() {
		return child;
	}

	public List<OrderByElement> getObe() {
		return obe;
	}

}

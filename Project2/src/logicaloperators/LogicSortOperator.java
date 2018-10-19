package logicaloperators;

import java.util.List;

import visitor.PhysicalPlanBuilder;

public class LogicSortOperator extends LogicOperator {
	
	LogicOperator child;
	List<?> obe;

	public LogicSortOperator(LogicOperator child, List<?> obe) {
		// TODO Auto-generated constructor stub
		this.child = child;
		this.obe = obe;
	}
	
	public LogicSortOperator(LogicOperator op) {
		this.child = op;
	}
	
	@Override
	public void accept(PhysicalPlanBuilder ppb) {
		// TODO Auto-generated method stub
		ppb.visit(this);
	}

	public LogicOperator getChild() {
		return child;
	}

	public List<?> getObe() {
		return obe;
	}
}

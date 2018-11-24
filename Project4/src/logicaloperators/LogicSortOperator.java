package logicaloperators;

import java.util.List;

import visitor.PhysicalPlanBuilder;
/**
 * @author Yixin Cui
 * @author Haodong Ping
 *  Logic Sort Operator class
 *
 */
public class LogicSortOperator extends LogicOperator {
	
	LogicOperator child;
	List<?> obe;

	public LogicSortOperator(LogicOperator child, List<?> obe) {
		this.child = child;
		this.obe = obe;
	}
	
	public void setOrderBy(List<?> obe) {
		this.obe = obe;
  }
	
	@Override
	public void accept(PhysicalPlanBuilder ppb) {
		ppb.visit(this);
	}

	public LogicOperator getChild() {
		return child;
	}

	public List<?> getObe() {
		return obe;
	}

	@Override
	public void print() {
		for(int i = 0; i < this.layer; i++)
			System.out.print("-");
		System.out.print("Sort");
		System.out.print(obe);
		
	}
	
}

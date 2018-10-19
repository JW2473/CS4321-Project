package logicaloperators;

import java.util.List;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import physicaloperators.Operator;
import visitor.PhysicalPlanBuilder;

public class LogicSortOperator extends LogicOperator {
	
	LogicOperator child;
	List<OrderByElement> obe;
	List<Column> orderBy;
	boolean join;

	public LogicSortOperator(LogicOperator child, List<OrderByElement> obe) {
		// TODO Auto-generated constructor stub
		this.child = child;
		this.obe = obe;
	}
	
	public LogicSortOperator(LogicOperator op, List<Column> orderBy, boolean join) {
		this.child = op;
		this.orderBy = orderBy;
		this.join = join;
	}
	
	public void setOrderBy(List<Column> orderBy) {
		this.orderBy = orderBy;
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

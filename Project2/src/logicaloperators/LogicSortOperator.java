package logicaloperators;

import java.util.List;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import physicaloperators.Operator;
import visitor.PhysicalPlanBuilder;

public class LogicSortOperator extends LogicOperator {
	
	LogicOperator child;
	List<OrderByElement> obe;
	private Column col;

	public LogicSortOperator(LogicOperator child, List<OrderByElement> obe) {
		// TODO Auto-generated constructor stub
		this.child = child;
		this.obe = obe;
	}
	
	public LogicSortOperator(LogicOperator op) {
		this.child = op;
	}

	public LogicSortOperator(LogicOperator op, Column col) {
		this.child = op;
		this.col = col;
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

	public Column getColumn() {
		return col;
	}
}

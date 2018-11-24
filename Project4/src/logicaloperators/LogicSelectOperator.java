package logicaloperators;

import net.sf.jsqlparser.expression.Expression;
import visitor.PhysicalPlanBuilder;
/**
 * @author Yixin Cui
 * @author Haodong Ping
 *  Logic Select Operator class
 *
 */
public class LogicSelectOperator extends LogicOperator{
	LogicOperator child;
	Expression expr;
	
	public LogicSelectOperator(LogicOperator child, Expression expr) {

		this.child = child;
		this.expr = expr;
	}
	
	public LogicOperator getChild() {
		return child;
	}
	
	public Expression getExpr() {
		return expr;
	}
	
	@Override
	public void accept(PhysicalPlanBuilder ppb) {

		ppb.visit(this);
	}

	@Override
	public void print() {
		// TODO Auto-generated method stub
		for(int i = 0; i < this.layer; i++)
			System.out.print("-");
		System.out.print("Select");
		System.out.println("["+expr+"]");
	}

}

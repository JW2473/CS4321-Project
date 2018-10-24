package logicaloperators;

import net.sf.jsqlparser.expression.Expression;
import visitor.PhysicalPlanBuilder;
/**
 * @author Yixin Cui
 * @author Haodong Ping
 *  Logic Join Operator class
 *
 */
public class LogicJoinOperator extends LogicOperator {
	
	LogicOperator left;
	LogicOperator right;
	Expression expr;
	
	public LogicJoinOperator(LogicOperator left, LogicOperator right, Expression expr) {
		// TODO Auto-generated constructor stub
		this.left = left;
		this.right = right;
		this.expr = expr;
	}
	@Override
	public void accept(PhysicalPlanBuilder ppb) {
		// TODO Auto-generated method stub
		ppb.visit(this);
	}
	
	public LogicOperator getLeft() {
		return left;
	}

	public LogicOperator getRight() {
		return right;
	}

	public Expression getExpr() {
		return expr;
	}

}

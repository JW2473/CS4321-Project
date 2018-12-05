package logicaloperators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;
import util.MyColumn;
import util.ParseWhere;
import util.UnionFindElement;
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
		System.out.print(this.toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < this.layer; i++)
			sb.append("-");
		sb.append("Select");
		sb.append("["+expr+"]");
		sb.append("\n");
		return sb.toString();
	}
	
	

}

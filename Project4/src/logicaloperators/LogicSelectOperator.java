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
		// TODO Auto-generated method stub
		for(int i = 0; i < this.layer; i++)
			System.out.print("-");
		System.out.print("Select");
		List<Expression> exps = ParseWhere.splitWhere(expr);
		List<Expression> rebuild_exps = new ArrayList<>();
		Set<MyColumn> hs = new HashSet<>();
		for(Expression exp : exps) {
			BinaryExpression be = (BinaryExpression)exp;
			Expression l = be.getLeftExpression();
			Expression r = be.getRightExpression();
			Column c = (Column)(l instanceof Column ? (Column)l : (Column)r);
			UnionFindElement ufe = ParseWhere.ufv.getUnionFind().find(c);	
			if(!hs.add(new MyColumn(c))) continue;
			if(ufe.getEqualityConstraint() != null) {
				int val = ufe.getEqualityConstraint();
				rebuild_exps.add(new EqualsTo(c,new LongValue((long)val)));
			} else {
				if(ufe.getLowerBound() != null) {
					int val = ufe.getLowerBound();
					rebuild_exps.add(new GreaterThanEquals(c,new LongValue((long)val)));
				}
				if(ufe.getUpperBound() != null) {
					int val = ufe.getUpperBound();
					rebuild_exps.add(new MinorThanEquals(c,new LongValue((long)val)));
				}
			}			
		}
		System.out.println(rebuild_exps.toString().replaceAll(",", " AND"));
	}

}

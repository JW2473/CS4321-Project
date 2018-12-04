package logicaloperators;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import util.ParseWhere;
import util.UnionFindElement;
import visitor.PhysicalPlanBuilder;
import java.util.*;
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
		this.left = left;
		this.right = right;
		this.expr = expr;
	}
	@Override
	public void accept(PhysicalPlanBuilder ppb) {
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
	@Override
	public void print() {
		
		for(int i = 0; i < this.layer; i++)
			System.out.print("-");
		System.out.print("Join");
		List<Expression> exps = ParseWhere.splitWhere(expr);
		System.out.println(ParseWhere.ufv.getUnusableComp().toString().replaceAll(",", " AND"));
		System.out.print(ParseWhere.ufv.getUnionFind());
//		for(Expression e : exps) {
//			if(e instanceof EqualsTo) {
//				Column c = (Column)(((BinaryExpression)e).getLeftExpression());
//				UnionFindElement ufe = ParseWhere.ufv.getUnionFind().find(c);
//				System.out.println(ufe);
//			} else {
//				Column c = (Column)(((BinaryExpression)e).getLeftExpression());
//				UnionFindElement ufe = ParseWhere.ufv.getUnionFind().find(c);
//				System.out.println(ufe);
//				Column c_r = (Column)(((BinaryExpression)e).getRightExpression());
//				UnionFindElement ufe_r = ParseWhere.ufv.getUnionFind().find(c_r);
//				System.out.println(ufe_r);
//			}
//				
//		}
		
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < this.layer; i++)
			sb.append("-");
		sb.append("Join");
		List<Expression> exps = ParseWhere.splitWhere(expr);
		sb.append(ParseWhere.ufv.getUnusableComp().toString().replaceAll(",", " AND") + "\n");
		sb.append(ParseWhere.ufv.getUnionFind());
		return sb.toString();
	}
	

}

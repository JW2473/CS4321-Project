package visitor;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import util.UnionFind;

/**
 * UnionFindVisitor goes through where clause to create union-find data structure
 * @author Yixin Cui
 * @author Haodong Ping
 */
public class UnionFindVisitor implements ExpressionVisitor{

	private final String msg = "Unusable comparison!";
	private UnionFind uf = new UnionFind();
	private int intValue;
	private List<Expression> unusableComp = new ArrayList<>();
	
	/**
	 * @return the union-find data structure
	 */
	public UnionFind getUnionFind() {
		return uf;
	}
	
	/**
	 * @return the list contains all unusable comparison expression
	 */
	public List<Expression> getUnusableComp() {
		return unusableComp;
	}
	
	/**
	 * Set the value according to the value in the expression
	 * @param arg0 the long value
	 */
	@Override
	public void visit(LongValue arg0) {
		this.intValue = (int) arg0.getValue();
	}

	/**
	 * Visit and expression and process left and right expressing using
	 * this visitor
	 */
	@Override
	public void visit(AndExpression arg0) {
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
	}

	/**
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the left or right expression is a column and then process it
	 * using a union-find data structure
	 * @param arg0 the expression of =
	 */
	@Override
	public void visit(EqualsTo arg0) {
		boolean isLeftCol = arg0.getLeftExpression() instanceof Column;
		boolean isRightCol = arg0.getRightExpression() instanceof Column;
		if (isLeftCol && isRightCol) {
			Column colLeft = (Column) arg0.getLeftExpression();
			Column colRight = (Column) arg0.getRightExpression();
			uf.join(uf.find(colLeft), uf.find(colRight));
		}else if (isLeftCol) {
			Column colLeft = (Column) arg0.getLeftExpression();
			arg0.getRightExpression().accept(this);
			uf.setEqualityConstraint(uf.find(colLeft), intValue);
		}else if (isRightCol) {
			Column colRight = (Column) arg0.getRightExpression();
			arg0.getLeftExpression().accept(this);
			uf.setEqualityConstraint(uf.find(colRight), intValue);
		}else {
			unusableComp.add(arg0);
		}
	}

	/**
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the left or right expression is a column and then process it
	 * using a union-find data structure
	 * @param arg0 the expression of >
	 */
	@Override
	public void visit(GreaterThan arg0) {
		boolean isLeftCol = arg0.getLeftExpression() instanceof Column;
		boolean isRightCol = arg0.getRightExpression() instanceof Column;
		if (isLeftCol && !isRightCol) {
			Column colLeft = (Column) arg0.getLeftExpression();
			arg0.getRightExpression().accept(this);
			uf.setLowerBound(uf.find(colLeft), intValue + 1);
		}else if (isRightCol && !isLeftCol) {
			Column colRight = (Column) arg0.getRightExpression();
			arg0.getLeftExpression().accept(this);
			uf.setUpperBound(uf.find(colRight), intValue - 1);
		}else {
			unusableComp.add(arg0);
		}
	}

	/**
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the left or right expression is a column and then process it
	 * using a union-find data structure
	 * @param arg0 the expression of >=
	 */
	@Override
	public void visit(GreaterThanEquals arg0) {
		boolean isLeftCol = arg0.getLeftExpression() instanceof Column;
		boolean isRightCol = arg0.getRightExpression() instanceof Column;
		if (isLeftCol && !isRightCol) {
			Column colLeft = (Column) arg0.getLeftExpression();
			arg0.getRightExpression().accept(this);
			uf.setLowerBound(uf.find(colLeft), intValue);
		}else if (isRightCol && !isLeftCol) {
			Column colRight = (Column) arg0.getRightExpression();
			arg0.getLeftExpression().accept(this);
			uf.setUpperBound(uf.find(colRight), intValue);
		}else {
			unusableComp.add(arg0);
		}
	}

	/**
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the left or right expression is a column and then process it
	 * using a union-find data structure
	 * @param arg0 the expression of <
	 */
	@Override
	public void visit(MinorThan arg0) {
		boolean isLeftCol = arg0.getLeftExpression() instanceof Column;
		boolean isRightCol = arg0.getRightExpression() instanceof Column;
		if (isLeftCol && !isRightCol) {
			Column colLeft = (Column) arg0.getLeftExpression();
			arg0.getRightExpression().accept(this);
			uf.setUpperBound(uf.find(colLeft), intValue - 1);
		}else if (isRightCol && !isLeftCol) {
			Column colRight = (Column) arg0.getRightExpression();
			arg0.getLeftExpression().accept(this);
			uf.setLowerBound(uf.find(colRight), intValue + 1);
		}else {
			unusableComp.add(arg0);
		}
	}

	/**
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the left or right expression is a column and then process it
	 * using a union-find data structure
	 * @param arg0 the expression of <=
	 */
	@Override
	public void visit(MinorThanEquals arg0) {
		boolean isLeftCol = arg0.getLeftExpression() instanceof Column;
		boolean isRightCol = arg0.getRightExpression() instanceof Column;
		if (isLeftCol && !isRightCol) {
			Column colLeft = (Column) arg0.getLeftExpression();
			arg0.getRightExpression().accept(this);
			uf.setUpperBound(uf.find(colLeft), intValue);
		}else if (isRightCol && !isLeftCol) {
			Column colRight = (Column) arg0.getRightExpression();
			arg0.getLeftExpression().accept(this);
			uf.setLowerBound(uf.find(colRight), intValue);
		}else {
			unusableComp.add(arg0);
		}
	}

	/**
	 * Visit the not equals to expression add it to unusable comparasion list
	 * @param arg0 the expression of <>
	 */
	@Override
	public void visit(NotEqualsTo arg0) {
		unusableComp.add(arg0);
	}

	@Override
	public void visit(Column arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(SubSelect arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(CaseExpression arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(WhenClause arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(ExistsExpression arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(AllComparisonExpression arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(Concat arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(Matches arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(BitwiseOr arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(BitwiseXor arg0) {
		throw new UnsupportedException(msg);
	}
	
	@Override
	public void visit(NullValue arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(Function arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(InverseExpression arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(JdbcParameter arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(DoubleValue arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(DateValue arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(TimeValue arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(TimestampValue arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(Parenthesis arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(StringValue arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(Addition arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(Division arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(Multiplication arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(Subtraction arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(InExpression arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(IsNullExpression arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(LikeExpression arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(OrExpression arg0) {
		throw new UnsupportedException(msg);
	}

	@Override
	public void visit(Between arg0) {
		throw new UnsupportedException(msg);
	}

}

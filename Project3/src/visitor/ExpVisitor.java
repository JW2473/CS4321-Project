package visitor;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
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
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * ExpVisitor implements the expressions we have to support
 *
 */
public abstract class ExpVisitor implements ExpressionVisitor{
	
	private final String msg = "This operation is not supported";
	
	long curValue = 0;
	boolean curStatus = true;
	
	/*
	 * Get current value from the specified column or constant in the query
	 * @return a long value 
	 */
	public long getCurValue() {
		return curValue;
	}
	
	/*
	 * Get current statues of the expression
	 * @return a boolean indicates a true or false status of the expression
	 */
	public boolean getCurStatus() {
		return curStatus;
	}

	/*
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the current variable in this object (curStatus or curValue)
	 * meets the specified condition, in this case left >= right, and update current value in the variable
	 * @param arg0 the expression of AND
	 */
	@Override
	public void visit(AndExpression arg0) {
		arg0.getLeftExpression().accept(this);
		boolean left  = curStatus;
		arg0.getRightExpression().accept(this);
		boolean right  = curStatus;
		curStatus = (left && right);
	}

	/*
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the current variable in this object (curStatus or curValue)
	 * meets the specified condition, in this case left == right, and update current value in the variable
	 * @param arg0 the expression of =
	 */
	@Override
	public void visit(EqualsTo arg0) {
		arg0.getLeftExpression().accept(this);
		long left = curValue;
		arg0.getRightExpression().accept(this);
		long right = curValue;
		curStatus = (left == right);
	}

	/*
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the current variable in this object (curStatus or curValue)
	 * meets the specified condition, in this case left == right, and update current value in the variable
	 * @param arg0 the expression of =
	 */
	@Override
	public void visit(GreaterThan arg0) {
		arg0.getLeftExpression().accept(this);
		long left = curValue;
		arg0.getRightExpression().accept(this);
		long right = curValue;
		curStatus = (left > right);
	}

	/*
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the current variable in this object (curStatus or curValue)
	 * meets the specified condition, in this case left >= right, and update current value in the variable
	 * @param arg0 the expression of >=
	 */
	@Override
	public void visit(GreaterThanEquals arg0) {
		arg0.getLeftExpression().accept(this);
		long left = curValue;
		arg0.getRightExpression().accept(this);
		long right = curValue;
		curStatus = (left >= right);
	}

	/*
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the current variable in this object (curStatus or curValue)
	 * meets the specified condition, in this case left != right, and update current value in the variable
	 * @param arg0 the expression of !=
	 */
	@Override
	public void visit(NotEqualsTo arg0) {
		arg0.getLeftExpression().accept(this);
		long left = curValue;
		arg0.getRightExpression().accept(this);
		long right = curValue;
		curStatus = (left != right);
	}

	/*
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the current variable in this object (curStatus or curValue)
	 * meets the specified condition, in this case left < right, and update current value in the variable
	 * @param arg0 the expression of <
	 */
	@Override
	public void visit(MinorThan arg0) {
		arg0.getLeftExpression().accept(this);
		long left = curValue;
		arg0.getRightExpression().accept(this);
		long right = curValue;
		curStatus = (left < right);
	}

	/*
	 * Visit the expression and evaluating its left expression and right expression
	 * to see whether the current variable in this object (curStatus or curValue)
	 * meets the specified condition, in this case left <= right, and update current value in the variable
	 * @param arg0 the expression of <=
	 */
	@Override
	public void visit(MinorThanEquals arg0) {
		arg0.getLeftExpression().accept(this);
		long left = curValue;
		arg0.getRightExpression().accept(this);
		long right = curValue;
		curStatus = (left <= right);
	}
	
	/*
	 * Visit the constant in the expression and update curValue
	 * @param arg0 the constant long value in the expression
	 */
	@Override
	public void visit(LongValue arg0) {
		this.curValue = arg0.getValue();
	}
	
	/*
	 * All the following expressions are unsupported expressions
	 */
	@Override
	public void visit(Addition arg0) {

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
	public void visit(Between arg0) {

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
	public void visit(CaseExpression arg0) {

			throw new UnsupportedException(msg);

	}



	@Override
	public void visit(Concat arg0) {

			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(DateValue arg0) {

			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(Division arg0) {

			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(DoubleValue arg0) {

			throw new UnsupportedException(msg);

	}



	@Override
	public void visit(ExistsExpression arg0) {

			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(Function arg0) {

			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(InExpression arg0) {

			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(InverseExpression arg0) {
		
			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(IsNullExpression arg0) {
		
			throw new UnsupportedException(msg);


	}

	@Override
	public void visit(JdbcParameter arg0) {

			throw new UnsupportedException(msg);
		
	}

	@Override
	public void visit(LikeExpression arg0) {

			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(Matches arg0) {

			throw new UnsupportedException(msg);
		
	}

	@Override
	public void visit(Multiplication arg0) {

			throw new UnsupportedException(msg);
 
	}

	@Override
	public void visit(NullValue arg0) {

			throw new UnsupportedException(msg);
		
	}

	@Override
	public void visit(OrExpression arg0) {

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
	public void visit(SubSelect arg0) {

			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(Subtraction arg0) {

			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(TimestampValue arg0) {

			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(TimeValue arg0) {

			throw new UnsupportedException(msg);

	}

	@Override
	public void visit(WhenClause arg0) {

			throw new UnsupportedException(msg);

	}

}

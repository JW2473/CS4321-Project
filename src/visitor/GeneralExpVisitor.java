package visitor;

import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

public abstract class GeneralExpVisitor extends ExpVisitor{
	
	int curValue = 0;
	boolean curStatus = true;
	public int getCurValue() {
		return curValue;
	}

	public boolean getCurStatus() {
		return curStatus;
	}


	@Override
	public void visit(AndExpression arg0) {
		// TODO Auto-generated method stub
		arg0.getLeftExpression().accept(this);
		boolean left  = curStatus;
		arg0.getRightExpression().accept(this);
		boolean right  = curStatus;
		curStatus = left&&right;
	}

	@Override
	public void visit(EqualsTo arg0) {
		// TODO Auto-generated method stub
		arg0.getLeftExpression().accept(this);
		int left = curValue;
		arg0.getRightExpression().accept(this);
		int right = curValue;
		curStatus = (left == right);
	}

	@Override
	public void visit(GreaterThan arg0) {
		// TODO Auto-generated method stub
		arg0.getLeftExpression().accept(this);
		int left = curValue;
		arg0.getRightExpression().accept(this);
		int right = curValue;
		curStatus = (left > right);
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		// TODO Auto-generated method stub
		arg0.getLeftExpression().accept(this);
		int left = curValue;
		arg0.getRightExpression().accept(this);
		int right = curValue;
		curStatus = (left >= right);
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		// TODO Auto-generated method stub
		arg0.getLeftExpression().accept(this);
		int left = curValue;
		arg0.getRightExpression().accept(this);
		int right = curValue;
		curStatus = (left != right);
	}

	@Override
	public void visit(MinorThan arg0) {
		// TODO Auto-generated method stub
		arg0.getLeftExpression().accept(this);
		int left = curValue;
		arg0.getRightExpression().accept(this);
		int right = curValue;
		curStatus = (left < right);
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		// TODO Auto-generated method stub
		arg0.getLeftExpression().accept(this);
		int left = curValue;
		arg0.getRightExpression().accept(this);
		int right = curValue;
		curStatus = (left <= right);
	}
	
}

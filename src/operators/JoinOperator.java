package operators;

import net.sf.jsqlparser.expression.Expression;
import util.Tuple;

public class JoinOperator extends Operator{

	Operator left;
	Operator right;
	Expression expr;
	
	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	public JoinOperator (Operator left, Operator right, Expression expr) {
		this.left = left;
		this.right = right;
		this.expr = expr;
	}

}

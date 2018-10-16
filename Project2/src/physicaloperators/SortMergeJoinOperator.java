package physicaloperators;

import net.sf.jsqlparser.expression.Expression;
import util.Tuple;

public class SortMergeJoinOperator extends JoinOperator{

	
	
	public SortMergeJoinOperator(Operator left, Operator right, Expression expr) {
		super(left, right, expr);
	}

	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void nextPair() {
		// TODO Auto-generated method stub
		
	}

}

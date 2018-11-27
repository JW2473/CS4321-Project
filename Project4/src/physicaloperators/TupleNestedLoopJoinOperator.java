package physicaloperators;

import net.sf.jsqlparser.expression.Expression;
import util.Tuple;

public class TupleNestedLoopJoinOperator extends JoinOperator{
	
	/**
	 * Create the TNLJ operator
	 * @param left the left child operator
	 * @param right the right child operator
	 * @param expr the join condition
	 */
	public TupleNestedLoopJoinOperator(Operator left, Operator right, Expression expr) {
		super(left, right, expr);
		t1 = left.getNextTuple();
		t2 = right.getNextTuple();
	}

	/**
	 * Set the tuples to next pair from the two relations
	 */
	@Override
	public void nextPair() {
		if (t1 == null) return;
		if (t2 != null) t2 = right.getNextTuple();
		if (t2 == null) {
			t1 = left.getNextTuple();
			right.reset();
			t2 = right.getNextTuple();
		}
	}

	/**
	 * Get next tuple after join
	 * @return the next tuple
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple t = null;
		while (t1 != null && t2 != null) {
			if (expr == null) 
				t = combineTuples(t1, t2);
			else {
				jv.readTuple(t1, t2);
				expr.accept(jv);
				if (jv.getCurStatus()) {
					t = combineTuples(t1, t2);
				}
			}
			this.nextPair();
			if (t != null) return t;
		}
		return null;
	}

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < this.layer; i++)
			sb.append("-");
		sb.append("TNLJ");
		String exp = "";
		if(expr != null) {
			exp = expr.toString();
		}
		sb.append("["+exp+"]");
		sb.append("\n");
		
		return sb.toString();
	}
	
	
}

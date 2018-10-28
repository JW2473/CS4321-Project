package physicaloperators;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import util.Tuple;
import visitor.JoinExpVisitor;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * JoinOperator class joins two tuples on specified condition
 */
public abstract class JoinOperator extends Operator{

	protected Operator left;
	protected Operator right;
	protected Expression expr;
	protected Tuple t1, t2;
	protected JoinExpVisitor jv = new JoinExpVisitor();
	
	/*
	 * Check whether two tuple meets the join condition and get the result after join
	 * @return the tuple after join
	 */
	@Override
	public abstract Tuple getNextTuple();

	/*
	 * Combine two tuples that meet the join condition
	 * @param t1 the first tuple
	 * @param t2 the second tuple
	 * @return the combined tuple
	 */
	protected Tuple combineTuples(Tuple t1, Tuple t2) {
		List<Long> value = new ArrayList<>();
		value.addAll(t1.getAllColumn());
		value.addAll(t2.getAllColumn());
		List<String> schemas = new ArrayList<String>();
		schemas.addAll(t1.getAllSchemas());
		schemas.addAll(t2.getAllSchemas());
		return new Tuple(value, schemas);
	}

	/*
	 * Look for next pair of tuples 
	 */
	public abstract void nextPair();
	
	/*
	 * Rest the child operators
	 */
	@Override
	public void reset() {
		left.reset();
		right.reset();
	}
	
	/*
	 * Create a JoinOperator object
	 * @param left the child operator
	 * @param right the child operator
	 * @param expr the join condition
	 */
	public JoinOperator (Operator left, Operator right, Expression expr) {
		this.left = left;
		this.right = right;
		this.expr = expr;

		this.uniqueSchema = new ArrayList<>();
		uniqueSchema.addAll(left.uniqueSchema);
		uniqueSchema.addAll(right.uniqueSchema);
	}

}

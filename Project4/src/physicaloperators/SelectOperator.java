package physicaloperators;

import net.sf.jsqlparser.expression.Expression;
import util.Tuple;
import visitor.SelectExpVisitor;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * SelectOperator class selects specific row in a table
 *
 */
public class SelectOperator extends Operator{

	Operator child;
	Expression expr;
	SelectExpVisitor sv = new SelectExpVisitor();
	
	/**
	 * Read the tuple from its child and check whether it meets WHERE condition
	 * It uses visitor pattern to visit every expression in the condition
	 * @return the selected tuple
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple t;
		while ((t = child.getNextTuple()) != null) {
			if (expr == null) return t;
			sv.readTuple(t);
			expr.accept(sv);
			if (sv.getCurStatus()) {
				return t;
			}
		}
		return null;
	}
	
	/**
	 * Reset its child
	 */
	@Override
	public void reset() {
		child.reset();
		
	}
	
	/**
	 * Create a SelectOperator object
	 * @param scanOp the child operator of select operator
	 * @param expr the Expression object from the WHERE clause
	 */
	public SelectOperator(Operator op, Expression expr) {
		child = op;
		this.expr = expr;
		this.uniqueSchema = child.uniqueSchema;
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

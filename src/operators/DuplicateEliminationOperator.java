package operators;

import util.Tuple;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * DuplicateEliminationOperator class eliminates the duplicate tuples from the sort operator
 */
public class DuplicateEliminationOperator extends Operator{

	Operator child;
	Tuple returned;
	
	/*
	 * Return the next distinct tuple
	 * @return the next tuple
	 */
	@Override
	public Tuple getNextTuple() {
		if (returned == null) {
			returned = child.getNextTuple();
			return returned;
		}else {
			Tuple t = null;
			while ((t = child.getNextTuple()) != null) {
				if (t.equals(returned)) break;
			}
			returned = t;
			return t;
		}
	}

	/*
	 * Reset the child operator
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		child.reset();
		
	}
	
	/*
	 * Create the DuplicateEliminationOperator object from a sort operator, if the child is not a sort operator
	 * create a new sort operator to sort the tuples first
	 * @param op the child operator
	 */
	public DuplicateEliminationOperator(Operator op) {
		if (child instanceof SortOperator) {
			child = op;
		}else{
			SortOperator sortOp = new SortOperator(op);
			child = sortOp;
		}
	}
	
}

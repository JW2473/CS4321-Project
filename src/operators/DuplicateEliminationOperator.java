package operators;

import util.Tuple;

public class DuplicateEliminationOperator extends Operator{

	SortOperator child;
	
	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
//		Tuple t = child.getNextTuple();
		Tuple t;
		while ((t = child.getNextTuple()) != null) {
			
		}
		
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		child.reset();
		
	}
	
	public DuplicateEliminationOperator(SortOperator sortOp) {
		child = sortOp;
	}
	
}

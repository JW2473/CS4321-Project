package operators;

import util.Tuple;

public class DuplicateEliminationOperator extends Operator{

	Operator child;
	Tuple returned;
	
	@Override
	public Tuple getNextTuple() {
		if (returned == null) {
			returned = child.getNextTuple();
			return returned;
		}else {
			Tuple t = null;
			while ((t = child.getNextTuple()) != null) {
				if ( !t.equals(returned) ) break;
			}
			returned = t;
			return t;
		}
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		child.reset();
		
	}
	
	public DuplicateEliminationOperator(Operator op) {
		if (child instanceof SortOperator) {
			child = op;
		}else{
			SortOperator sortOp = new SortOperator(op);
			child = sortOp;
		}
	}
	
}

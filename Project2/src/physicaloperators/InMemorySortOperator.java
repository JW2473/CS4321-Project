package physicaloperators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import util.Tuple;

public class InMemorySortOperator extends SortOperator{

	int index = 0;
	List<Tuple> tps = new ArrayList<>();
	
	public InMemorySortOperator(Operator op) {
		super(op);
		Tuple t = child.getNextTuple();
		this.uniqueSchema = op.uniqueSchema;
		while (t != null) {
			tps.add(t);
			t = child.getNextTuple();
		}
		Collections.sort(tps, new tupleComp());
	}

	public InMemorySortOperator(Operator op, List<?> obe) {
		super(op, obe);
		Tuple t = null;
		this.uniqueSchema = op.uniqueSchema;
		while ((t = child.getNextTuple()) != null) {
			tps.add(t);
		}
		Collections.sort(tps, new tupleComp(orderBy));
	}
  
	@Override
	public void reset() {
		
		index = 0;
	}

	@Override
	public Tuple getNextTuple() {
		if (index < tps.size()) return tps.get(index++);
		return null;
	}

	@Override
	public void reset(int index) {
		this.index = index;
	}
	
	
}

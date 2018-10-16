package physicaloperators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import util.Tuple;

public class InMemorySortOperator extends SortOperator{

	int index = 0;
	List<Tuple> tps = new ArrayList<>();
	
	public InMemorySortOperator(Operator op) {
		super(op);
		Tuple t = child.getNextTuple();
		while (t != null) {
			tps.add(t);
			t = child.getNextTuple();
		}
		Collections.sort(tps, new tupleComp());
	}

	public InMemorySortOperator(Operator op, List<OrderByElement> obe) {
		super(op, obe);
		Tuple t = null;
		while ((t = child.getNextTuple()) != null) {
			tps.add(t);
		}
		Collections.sort(tps, new tupleComp(orderBy));
	}
	
	public InMemorySortOperator(Operator op, Column col) {
		super(op, col);
		Collections.sort(tps, new tupleComp(orderBy));
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		index = 0;
	}

	@Override
	public Tuple getNextTuple() {
		if (index < tps.size()) return tps.get(index++);
		return null;
	}
	
	
}

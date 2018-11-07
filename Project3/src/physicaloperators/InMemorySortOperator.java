package physicaloperators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import util.Tuple;


/**
 * @author Yixin Cui
 * @author Haodong Ping
 * InMemorySortOperator sorts the table in the memory
 */
public class InMemorySortOperator extends SortOperator{

	int index = 0;
	List<Tuple> tps = new ArrayList<>();
	
	/**
	 * Create the operator that sorts all the columns
	 * @param the child operator
	 */
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

	/**
	 * Create the operator with a list of orders
	 * @param op the child operator
	 * @param obe the column list contains orders
	 */
	public InMemorySortOperator(Operator op, List<?> obe) {
		super(op, obe);
		Tuple t = null;
		this.uniqueSchema = op.uniqueSchema;
		while ((t = child.getNextTuple()) != null) {
			tps.add(t);
		}
		Collections.sort(tps, new tupleComp(orderBy));
	}
  
	/**
	 * Reset the operator
	 */
	@Override
	public void reset() {
		index = 0;
	}

	/**
	 * return the next tuple in the operator
	 * @return the next tuple in the operator
	 */
	@Override
	public Tuple getNextTuple() {
		if (index < tps.size()) return tps.get(index++);
		return null;
	}

	/**
	 * Reset the operator to a specified index
	 * @param index the index we want to go
	 */
	@Override
	public void reset(int index) {
		this.index = index;
	}
	
	
}

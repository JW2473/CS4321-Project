package operators;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import util.Tuple;

public class SortOperator extends Operator{

	Column col;
	Operator child;
	
	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		child.reset();
	}
	
	public SortOperator(Operator op, OrderByElement obe) {
		child = op;
		try {
			col = ((Column) obe.getExpression());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

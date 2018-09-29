package operators;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import util.Tuple;

public class ProjectOperator extends Operator{

	Operator child;
	List<SelectItem> si;
	
	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		Tuple t = child.getNextTuple();
		if (t == null) return null;
		List<Long> projection = new ArrayList<>();
		for (SelectItem item : si) {
			if (item instanceof AllColumns) return t;
			if (item instanceof SelectExpressionItem) {
				Expression expr = ((SelectExpressionItem) item).getExpression();
				Column col = (Column) expr;
				System.out.println(col.toString());
				projection.add(t.getValue(col));
			}
		}
		return new Tuple(projection);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		child.reset();
	}
	
	public ProjectOperator(List<SelectItem> si, Operator op) {
		this.si = si;
		child = op;
	}

}

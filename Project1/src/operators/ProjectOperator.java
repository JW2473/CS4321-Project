package operators;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import util.Tools;
import util.Tuple;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * ProjectOperator class select the specific column of the tuple
 *
 */
public class ProjectOperator extends Operator{

	Operator child;
	List<SelectItem> si;
	List<String> schemas = new ArrayList<>();
	
	/*
	 * Create a new tuple from the Column object and child operator's tuple
	 * It can determine whether the SelectItem is * or a certain column in a tuple
	 * @return the projected tuple
	 */
	@Override
	public Tuple getNextTuple() {
		schemas.clear();
		Tuple t = child.getNextTuple();
		if (t == null) return null;
		List<Long> projection = new ArrayList<>();
		for (SelectItem item : si) {
			if (item instanceof AllColumns) return t;
			if (item instanceof SelectExpressionItem) {
				Expression expr = ((SelectExpressionItem) item).getExpression();
				Column col = (Column) expr;
				projection.add(t.getValue(col));
				schemas.add(Tools.rebuildWholeColumnName(col));
			}
		}
		return new Tuple(projection, schemas);
	}
	
	/*
	 * Reset the child's operator
	 */
	@Override
	public void reset() {
		child.reset();
	}
	
	/*
	 * Create a ProjectOperator object
	 * @param si the SelectItem object from the query
	 * @param op the child operator
	 */
	public ProjectOperator(List<SelectItem> si, Operator op) {
		this.si = si;
		child = op;
	}

}

package physicaloperators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import util.Tuple;

public class SortMergeJoinOperator extends JoinOperator{

	private List<Column> rightColumns;
	private List<Column> leftColumns;
	private tupleComp tc = null;
	private int index = 0;
	private int startIndex;
	
	public SortMergeJoinOperator(Operator left, Operator right, Expression expr, List<Column> leftColumns, List<Column> rightColumns) {
		super(left, right, expr);
		this.leftColumns = leftColumns;
		this.rightColumns = rightColumns;
		tc = new tupleComp(this.leftColumns, this.rightColumns);
	}

	@Override
	public Tuple getNextTuple() {
		Tuple t = null;
		while (t1 != null && t2 != null) {
			if (tc.compare(t1, t2) < 0) {
				t1 = left.getNextTuple();
				continue;
			}
			if (tc.compare(t1, t2) > 0) {
				t2 = right.getNextTuple();
				index++;
				startIndex = index;
				continue;
			}
			if (expr == null) 
				t = combineTuples(t1, t2);
			else {
				jv.readTuple(t1, t2);
				expr.accept(jv);
				if (jv.getCurStatus()) {
					t = combineTuples(t1, t2);
				}
			}
			index++;
			nextPair();
			if (t != null) return t;
		}
		return null;
	}

	@Override
	public void nextPair() {
		t2 = right.getNextTuple();
		if (t2 != null && tc.compare(t1, t2) == 0) {
			return;
		}else {
			((SortOperator)right).reset(startIndex);
			t1 = left.getNextTuple();
			t2 = right.getNextTuple();
			index = startIndex;
		}
	}
	
public class tupleComp implements Comparator<Tuple> {
		
		List<Column> leftColumns = new ArrayList<>();
		List<Column> rightColumns = new ArrayList<>();

		@Override
		public int compare(Tuple o1, Tuple o2) {
			for (int i = 0; i < leftColumns.size(); i++) {
				int cmp = Long.compare(o1.getValue(leftColumns.get(i)), o2.getValue(rightColumns.get(i)));
				if (cmp != 0) return cmp;
			}
			
			return 0;
		}
		
		public tupleComp(List<Column> leftColumns, List<Column> rightColumns) {
			this.leftColumns = leftColumns;
			this.rightColumns = rightColumns;
		}
	}

}

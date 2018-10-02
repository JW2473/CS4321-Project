package operators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import util.Tools;
import util.Tuple;

public class SortOperator extends Operator{

	Column col;
	Operator child;
	List<Tuple> tps = new ArrayList<>();
	List<Column> orderBy = new ArrayList<>();
	int index = 0;
	
	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		if (index < tps.size()) return tps.get(index++);
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		child.reset();
		index = 0;
	}
	
	public SortOperator(Operator op, List<OrderByElement> obe) {
		child = op;
		try {
			for (OrderByElement obelement : obe) {
				Column col = (Column) obelement.getExpression();
				orderBy.add(col);
			}
			Tuple t = null;
			while ((t = child.getNextTuple()) != null) {
				tps.add(t);
			}
			Collections.sort(tps, new tupleComp(orderBy));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public SortOperator(Operator op) {
		child = op;
		Tuple t = child.getNextTuple();
		while (t != null) {
			tps.add(t);
			t = child.getNextTuple();
		}
		Collections.sort(tps, new tupleComp());
	}
	
	public class tupleComp implements Comparator<Tuple> {
		
		List<Column> cols = new ArrayList<>();
		HashSet<String> orderByElements = new HashSet<>();
		
		@Override
		public int compare(Tuple o1, Tuple o2) {
			// TODO Auto-generated method stub
			if (o1.getSize() != o2.getSize()) {
				try{
					throw new Exception("Tuples' lengths don't match!");
				}catch (Exception e) {
					e.printStackTrace();
					return 0;
				}
			}
			
			if (cols != null) {
				for (Column col : cols) {
					int cmp = Long.compare(o1.getValue(col), o2.getValue(col));
					if (cmp != 0) return cmp;
				}
			}
			
			for (int i = 0; i < o1.getSize(); i++) {
				String schemaName = o1.getAllSchemas().get(i);
				if (orderByElements.contains(schemaName)) continue;
				int cmp = Long.compare(o1.getAllColumn().get(i), o2.getAllColumn().get(i));
				if (cmp != 0) return cmp;
			}
			
			return 0;
		}
		
		public tupleComp(List<Column> cols) {
			this.cols = cols;
			for (Column col : cols) {
				orderByElements.add(Tools.rebuildWholeColumnName(col));
			}
		}
		
		public tupleComp() {
			this.cols = null;
			this.orderByElements = new HashSet<>();
		}
	}

}

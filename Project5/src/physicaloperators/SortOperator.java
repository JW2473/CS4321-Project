package physicaloperators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import util.Tools;
import util.Tuple;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 *SortOperator class creates a list of tuple its child operator returns
 *and sort the tuple according to the OrderByElement
 */
public abstract class SortOperator extends Operator{
	
	Operator child;
	List<Column> orderBy = new ArrayList<>();
	
	
	/**
	 * Get the next tuple in the tuple list
	 * @return next tuple
	 */
	@Override
	public abstract Tuple getNextTuple();

	/**
	 * Set the index to 0 so that it can return tuple from the beginning
	 */
	@Override
	public abstract void reset();
	
	/**
	 * Set the index to a specified value so that it can return tuple from the specified location
	 */
	public abstract void reset(int index);
	
	/**
	 * Create a SortOperator object with OrderByElements
	 * @param obe the list of OrderByElement
	 * @param op the child operator
	 */
	public SortOperator(Operator op, List<?> obe) {
		child = op;
		this.uniqueSchema = child.uniqueSchema;
		try {
			for (Object obelement : obe) {
				Column col = obelement instanceof OrderByElement ? (Column) ((OrderByElement) obelement).getExpression() : (Column) obelement;
				orderBy.add(col);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a ProjectOperator object without OrderByElements means sort the tuple by all schemas
	 * @param op the child operator
	 */	
	public SortOperator(Operator op) {
		child = op;
	}

	/**
	 * tupleComp class implements Comparator interface and compare two tuples according to
	 * OrderByElement and schemas
	 * 
	 */
	public static class tupleComp implements Comparator<Tuple> {
		List<String> uniqueSchema = null;
		List<Column> cols = new ArrayList<>();
		HashSet<String> orderByElements = new HashSet<>();
		
		/**
		 * compare two tuples according to the order
		 * @param o1 tuple 1
		 * @param o2 tuple 2
		 * @return cmp the result after comparison
		 */
		@Override
		public int compare(Tuple o1, Tuple o2) {
			if (o1.getSize() != o2.getSize()) {
				try{
					throw new Exception("Tuples' lengths don't match!");
				}catch (Exception e) {
					e.printStackTrace();
					return 0;
				}
			}
			
			if (cols != null) {
				///---------------------------------------comment----------------------------------
			//	System.out.println(cols.size());
				for (Column col : cols) {
					int cmp = Long.compare(o1.getValue(uniqueSchema, col), o2.getValue(uniqueSchema, col));
					if (cmp != 0) return cmp;
				}
			}
			
			for (int i = 0; i < o1.getSize(); i++) {
				String schemaName = uniqueSchema.get(i);
				if (orderByElements.contains(schemaName)) continue;
				int cmp = Long.compare(o1.getAllColumn().get(i), o2.getAllColumn().get(i));
				if (cmp != 0) return cmp;
			}
			
			return 0;
		}
		
		/**
		 * Create a compare object with order
		 * @param cols the list of schema of order
		 */
		public tupleComp(List<Column> cols, List<String> uniqueSchema) {
			this.uniqueSchema = uniqueSchema;
			this.cols = cols;
			for (Column col : cols) {
				orderByElements.add(Tools.rebuildWholeColumnName(col));
			}
		}
		
		/**
		 * Create a compare object
		 * 
		 */
		public tupleComp(List<String> uniqueSchema) {
			this.uniqueSchema = uniqueSchema;
			this.cols = null;
			this.orderByElements = new HashSet<>();
		}
	}

}

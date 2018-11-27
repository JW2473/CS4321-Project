package physicaloperators;

import util.MyTable;
import util.Tools;

import java.util.List;

import util.Tuple;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * ScanOperator class scan the whole table
 *
 */
public class ScanOperator extends Operator{
	
	MyTable table;
	List<String> schema;
	
	/**
	 * Create a ScanOperator 
	 * @param table the table to be scanned
	 */
	public ScanOperator(MyTable table) {
		this.table = table;
		schema = table.getSchemaName();
		this.uniqueSchema = Tools.InitilaizeWholeColumnName(table.getUniqueName(), table.getFullTableName());
	}
	
	/**
	 * Call nextTuple in the table object to get next tuple in that table
	 * @return next tuple
	 */
	@Override
	public Tuple getNextTuple() {
		return table.nextTuple();
	}

	/**
	 * reset the table
	 */
	@Override
	public void reset() {
		table.reset();
	}

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < this.layer; i++)
			sb.append("-");
		sb.append("TableScan");
		sb.append("["+table.getFullTableName()+"]\n");
		
		return sb.toString();
	}
	
	
}

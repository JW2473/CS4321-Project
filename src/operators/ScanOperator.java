package operators;

import util.MyTable;

import java.io.PrintStream;
import java.util.List;

import util.Tuple;

public class ScanOperator extends Operator{
	
	MyTable table;
	List<String> schema;
	
	public ScanOperator(MyTable table) {
		this.table = table;
		schema = table.getSchemaName();
	}
	
	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		return table.nextTuple();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		table.reset();
	}
	
	public void dump(PrintStream ps) {
		super.dump(ps);
	}
	
}

package operators;

import util.myTable;

import java.io.PrintStream;
import java.util.List;

import util.Tuple;

public class ScanOperater extends Operator{
	
	myTable table;
	List<String> schema;
	
	public ScanOperater(myTable table) {
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
		this.reset();
//		System.out.println(schema);
		ps.println(schema);
		Tuple curr = table.nextTuple();
		while (curr != null) {
//			System.out.println(curr);
			ps.println(curr);
			curr = table.nextTuple();
		}
	}
	
}

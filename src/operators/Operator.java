package operators;

import java.io.PrintStream;

import util.Tuple;

public abstract class Operator {
	
	public abstract Tuple getNextTuple();
	
	public abstract void reset();
	
	public void dump(PrintStream ps) {
		Tuple curr = getNextTuple();
		while (curr != null) {
//			System.out.println(curr);
			ps.println(curr);
			curr = getNextTuple();
		}
	}
}

package physicaloperators;

import java.io.IOException;
import java.io.PrintStream;

import util.Tuple;
import util.TupleWriter;
import java.util.*;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * This is the abstract class of operators that determines the methods for all concrete operators
 */
public abstract class Operator {
	
	public abstract Tuple getNextTuple();
	
	public abstract void reset();
	/*
	 * @param an output stream
	 * Call getNextTuple until the next tuple is null and then print the result to the output stream
	 */
	public void dump(PrintStream ps) {
		Tuple curr = getNextTuple();
		while (curr != null) {
			ps.println(curr);
			curr = getNextTuple();
		}
		ps.close();
	}
	
	public void dump(String filePath, String fileName) {
		String out = filePath + fileName;
		TupleWriter tw = new TupleWriter(out);
		try {
			Tuple curr = getNextTuple();
			while (curr != null) {
				tw.writeTuple(curr);
				curr = getNextTuple();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		tw.close();
	}
}

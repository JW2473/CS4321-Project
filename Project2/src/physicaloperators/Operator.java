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
	
	List<String> uniqueSchema;
	
	public abstract Tuple getNextTuple();
	
	public abstract void reset();
	
	/*
	 * Call getNextTuple until the next tuple is null and then print the result to the output stream
	 * @param an output stream
	 */
	public void dump(PrintStream ps) {
		Tuple curr = getNextTuple();
		while (curr != null) {
			ps.println(curr);
			curr = getNextTuple();
		}
		ps.close();
	}
	
	/*
	 * Return the unique schema of the tuple in the operator
	 * @return the list of the columns of the tuple
	 */
	public List<String> getUniqueSchema() {
		return this.uniqueSchema;
	}
	
	/*
	 * Dump all the tuples to a file in binary format
	 * @param filePath the path of the file
	 * @param filename the name of the file
	 */
	public void dump(String filePath, String fileName) {
		long startTime = System.currentTimeMillis();
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
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Elapsed Time is: " + elapsedTime);
	}
}

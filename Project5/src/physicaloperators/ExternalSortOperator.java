package physicaloperators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.sf.jsqlparser.schema.Column;
import util.Catalog;
import util.Tuple;
import util.TupleReader;
import util.TupleWriter;


/**
 * @author Yixin Cui
 * @author Haodong Ping
 * ExternalSortOperator sort tables by creating intermediate files to keep 
 * the partial sorted runs which are pending a merge in the next pass
 * 
 */
public class ExternalSortOperator extends SortOperator{

	private int bufferSize = Catalog.sortBuffer;
	private int fanIn = bufferSize - 1;
	private int numRun = 0;
	private int inputPass = 0;
	private int outputPass = 0;
	private int inputRun = 0;
	private int outputRun = 0;
	private int tuplePerPage = 0;
	private int tupleCount = 0;
	private String tempDir = Catalog.tempDir;
	private int ID;
	private List<String> allSchema;
	private int tupleSize;
	private int tuplePerFile;
	private List<Tuple> tps = new LinkedList<>();
	private List<TupleReader> trs = new ArrayList<>();
	private List<Tuple> buff = new LinkedList<>();
	private TupleReader tr = null;
	private TupleWriter tw = null;
	
	/**
	 * Create the externalsortoperator with a list of orders
	 * @param op the child operator
	 * @param obe the column list contains orders
	 */
	public ExternalSortOperator(Operator op, List<?> obe) {
		super(op, obe);
		this.uniqueSchema = op.uniqueSchema;
//		System.out.println("Unique Schema: " + this.uniqueSchema.toString());
//		System.out.println("Columns: " + obe.toString());
		sort();
	}
	
	/**
	 * Create the externalsortoperator that sorts by every column
	 * @param op the child operator
	 */
	public ExternalSortOperator(Operator op) {
		super(op);
		this.uniqueSchema = op.uniqueSchema;
		sort();
	}
	
	/**
	 * sort method sort the table
	 */
	private void sort() {
		ID = Catalog.sortID();
		initialPass(orderBy);
//		totalPass = (int) Math.ceil((Math.log(Math.ceil((double) Math.ceil(totalCount / tuplePerPage) / bufferSize)) / Math.log(fanIn)));
		while (outputRun != 0) {
			inputRun = 0;
			outputRun = 0;
			tupleCount = 0;
			outputPass++;
			mergePass(orderBy);
			inputPass++;
		}
	}
	
	/**
	 * The initial pass takes all buffers to sort table into partial sorted runs
	 * @param orderBy the column list contains orders
	 */
	private void initialPass(List<Column> orderBy) {
		tempDir = Catalog.tempDir + "ExSort" + ID;
		File tempPath = new File(tempDir);
		tempPath.mkdirs();
		Tuple t = child.getNextTuple();
		if (t == null) return;
		allSchema = t.getAllSchemas();
		tupleSize = t.getSize();
		tuplePerPage = (Catalog.pageSize - 8) / tupleSize / 4;
		tuplePerFile = tuplePerPage * bufferSize;
		while (t != null) {
			int i = 0;
			while (i < tuplePerFile && t != null) {
				buff.add(t);
				i++;
				t = child.getNextTuple();
			}
			if (!orderBy.isEmpty()) Collections.sort(buff, new tupleComp(orderBy));
			else Collections.sort(buff, new tupleComp());
			tupleCount += buff.size();
			buff.size();
			writeTuples(buff);
			buff.clear();
			if (t == null && outputRun == 0) {
				break;
			}else if (t == null) break;
		}
		if (tw != null) {
			tw.close();
			tw = null;
		}
		numRun = outputRun;
	}
	
	/**
	 * The merge pass merge partial sorted runs
	 * @param orderBy the column list contains orders
	 */
	private void mergePass(List<Column> orderBy) {
		tuplePerFile *= fanIn;
		while (inputRun <= numRun) {
			for (int i = 0; i < fanIn; i++) {
				File f = new File(tempDir + getFileName(inputPass, inputRun));
//				System.out.println(numRun + getFileName(inputPass, inputRun));
				try {
					trs.add(new TupleReader(f));
					tps.add(new Tuple(trs.get(i).nextTuple(), allSchema));
					inputRun++;
				} catch (FileNotFoundException e) {
					inputRun++;
					continue;
				}
			}
			while (!trs.isEmpty() && !tps.isEmpty()) {
				fillBuffer(popMin());
			}
			if (buff.size() != 0) {
				writeTuples(buff);
				buff.clear();
			}
		}
		numRun = outputRun;
		trs.clear();
		tps.clear();
		if (tw != null) {
			tw.close();
			tw = null;
		}
	}
	
	/**
	 * Get the minimum tuple from current buffers
	 * @return the minimum tuple
	 */
	private Tuple popMin() {
		Tuple min = tps.get(0);
		int minTr = 0;
		Comparator<Tuple> tc = orderBy == null ? new tupleComp() : new tupleComp(orderBy);
		for (int i = 0; i < tps.size(); i++) {
			if (tc.compare(min, tps.get(i)) > 0) {
				min = tps.get(i);
				minTr = i;
			}
		}
		Tuple ret = tps.get(minTr);
//		System.out.println(tps.size());
		long[] val = trs.get(minTr).nextTuple();
		if (val == null) {
			trs.get(minTr).close();
			tps.remove(minTr);
			trs.remove(minTr);
		}else {
			Tuple next = new Tuple(val, allSchema);
			tps.set(minTr, next);
		}
		return ret;
	}
	
	/**
	 * Put the minimum tuple in the output buffer
	 * @param the minimum tuple that need to be stored in the output buffer
	 */
	private void fillBuffer(Tuple currMin) {
		buff.add(currMin);
		tupleCount++;
		if (buff.size() >= tuplePerPage) {
			writeTuples(buff);
			buff.clear();
		}
	}
	
	/**
	 * Write the tuples in the buffer into output file
	 * @param buff the output buffer
	 */
	private void writeTuples(List<Tuple> buff) {
		tw = (tw == null) ? new TupleWriter(tempDir + getFileName(outputPass, outputRun)) : tw;
		for (Tuple tp : buff) {
			try {
				tw.writeTuple(tp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (tupleCount >= tuplePerFile) {
			tw.close();
			tw = null;
			tupleCount = 0;
			outputRun++;
		}
	}

	/**
	 * Return the next tuple in the sorted file
	 * @return the next tuple
	 */
	@Override
	public Tuple getNextTuple() {
		try {
			tr = (tr == null) ? new TupleReader(new File(tempDir + getFileName(inputPass, numRun))) : tr;
			long[] vals = tr.nextTuple();
			return new Tuple(vals, allSchema);
		} catch (NullPointerException e) {
			return null;
		} catch (FileNotFoundException e) {
			return null;
		}
		
	}
	
	/**
	 * Reset the operator
	 */
	@Override
	public void reset() {
		tr.reset();
	}
	
	/**
	 * Reset the operator to a specified index
	 * @param index the index we want to go
	 */
	public void reset(int index) {
		tr.reset(index);
	}
	
	/**
	 * Generate the name of the output file
	 * @param pass the pass number
	 * @param run the run number
	 */
	private String getFileName(int pass, int run) {
		return File.separator + "Pass" + pass + "_" + (run);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < layer; i++) {
			sb.append("-");
		}
		sb.append("ExternalSort");
		sb.append(this.orderBy);
		sb.append("\n");
		return sb.toString();
	}
	
	
}

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
import net.sf.jsqlparser.statement.select.OrderByElement;
import util.Catalog;
import util.Tuple;
import util.TupleReader;
import util.TupleWriter;

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
	private int totalCount = 0;
	private int totalPass = 0;
	private String tempDir = Catalog.tempDir;
	private int ID;
	private List<String> allSchema;
	private int tupleSize;
	private int tuplePerFile;
	private List<Tuple> tps = new LinkedList<>();
	List<TupleReader> trs = new ArrayList<>();
	List<Tuple> buff = new LinkedList<>();
	private TupleReader tr = null;
	private TupleWriter tw = null;
	
	public ExternalSortOperator(Operator op, List<OrderByElement> obe) {
		super(op, obe);
		sort();
	}
	
	public ExternalSortOperator(Operator op) {
		super(op);
		sort();
	}

	public ExternalSortOperator(Operator op, Column col) {
		super(op, col);
		sort();
	}
	
	public void sort() {
		ID = Catalog.sortID();
		initialPass(orderBy);
		totalPass = (int) Math.ceil((Math.log(totalCount / bufferSize / tuplePerPage) / Math.log(fanIn)));
		while (inputPass < totalPass) {
			inputRun = 0;
			outputRun = 0;
			tupleCount = 0;
			outputPass++;
			mergePass(orderBy);
			inputPass++;
		}
	}
	
	public void initialPass(List<Column> orderBy) {
		tempDir = Catalog.tempDir + "ExSort" + ID;
		File tempPath = new File(tempDir);
		tempPath.mkdirs();
		Tuple t = child.getNextTuple();
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
			totalCount += buff.size();
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
	
	private void mergePass(List<Column> orderBy) {
		tuplePerFile *= fanIn;
		while (inputRun <= numRun) {
			for (int i = 0; i < fanIn; i++) {
				File f = new File(tempDir + getFileName(inputPass, inputRun));
				try {
					trs.add(new TupleReader(f));
					tps.add(new Tuple(trs.get(i).nextTuple(), allSchema));
					inputRun++;
				} catch (FileNotFoundException e) {
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
	
	private void fillBuffer(Tuple currMin) {
		buff.add(currMin);
		tupleCount++;
		if (buff.size() >= tuplePerPage) {
			writeTuples(buff);
			buff.clear();
		}
	}
	
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

	@Override
	public Tuple getNextTuple() {
		try {
			tr = (tr == null) ? new TupleReader(new File(tempDir + getFileName(inputPass, numRun))) : tr;
			long[] vals = tr.nextTuple();
			return new Tuple(vals, allSchema);
		} catch (NullPointerException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	private String getFileName(int pass, int run) {
		return File.separator + "Pass" + pass + "_" + (run);
	}
	
}

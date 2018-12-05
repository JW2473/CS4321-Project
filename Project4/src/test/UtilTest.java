package test;

import org.junit.Test;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import util.Catalog;
import util.SelectParserTree;
import util.Tools;
import util.TreeReader;
import util.TupleReader;
import util.UnionFind;
import visitor.UnionFindVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;

public class UtilTest {
	
	@Test
	public void CatalogTest() {
		Catalog.initialize("samples2/interpreter_config_file.txt");
		Catalog.getInstance();
		System.out.println(Catalog.buildIndex);
		System.out.println(Catalog.executeQuery);
		System.out.println(Catalog.useIndex);
	}
	
	@Test
	public void ConvertTest() {
		Catalog.initialize("samples2/interpreter_config_file.txt");
		Catalog.getInstance();
//		String filePath = Catalog.tempDir + "ExSort1/Pass0_0";
//		String filePath = Catalog.output + "query1";
		String filePath = Catalog.input + File.separator + "db" + File.separator + "data" + File.separator + "Sailors";
		File inputFile = new File(filePath);
		TupleReader tr;
		try {
			tr = new TupleReader(inputFile);
			tr.convertToReadableFile(filePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void newExeTest() {
//		Catalog.initialize("samples/interpreter_config_file.txt");
//		Catalog.initialize("samples/test_input", "samples/output", "samples/temp");
		Catalog.initialize("/Users/cuiyixin/Desktop/Submission/benchmark/interpreter_config_file.txt");
		Catalog.getInstance();
		CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
		Statement statement;
		int count = 1;
		//System.out.println(Catalog.joinConfig);
		try {
			while ( (statement = parser.Statement()) != null ) {	
				long startTime = System.currentTimeMillis();
				Catalog.resetAlias();
				try {
					Select select = (Select) statement;
					System.out.println(select.toString());
					SelectParserTree spt = new SelectParserTree(select);
					
//					System.out.println("Dumping binary file...");
//					String filePath = Catalog.output;
//					String fileName = "query" + String.valueOf(count);
//					spt.root.dump(filePath, fileName);
					
					String logicFileName = "query" + String.valueOf(count)+"_logicalplan";
					String physicFileName = "query" + String.valueOf(count)+"_physicalplan";
					spt.ppb.dumpLog_Plan(logicFileName);
					spt.ppb.dumpPhy_Plan(physicFileName);
					System.out.println("Dumping readiable file...");
					PrintStream ps = null;
					ps = new PrintStream(new File(Catalog.output + "query" + String.valueOf(count)) + ".txt");
					spt.root.dump(ps);
					
				} catch (Exception e) {	
					e.printStackTrace();
					System.err.println("Exception occurred during parsing");
					continue;
				}finally {
					count++;
					Catalog.resetAlias();
				}
				long elapsedTime = System.currentTimeMillis() - startTime;
				System.out.println("Elapsed Time is: " + elapsedTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//long elapsedTime = System.currentTimeMillis() - startTime;
		//System.out.println("Elapsed Time is: " + elapsedTime);
	}

	@Test
	public void IndexSortTest() {
		Catalog.initialize("samples2/interpreter_config_file.txt");
		Catalog.getInstance();
		Tools.sortByIndex("Sailors");
	}
	
	@Test
	public void TupleReaderTest() {
		Catalog.initialize("samples2/interpreter_config_file.txt");
		Catalog.getInstance();
		TupleReader tr = new TupleReader("Sailors");
		long[] t = null;
		int i = 0;
		System.out.println(tr.getAttrNum());
		System.out.println(tr.getSize());
		while (i < Integer.MAX_VALUE && (t = tr.nextTuple()) != null) {
			System.out.println(Arrays.toString(t));
			System.out.println(tr.pageNum() + ", " + tr.tupleNum());
			i++;
		}
	}
	
	@Test
	public void TreeReaderTest() {
		Catalog.initialize("samples2/interpreter_config_file.txt");
		Catalog.getInstance();
		TreeReader tr = new TreeReader("Sailors", "B", null, 399);
		int[] rid = null;
		while ((rid = tr.nextRid()) != null) {
			System.out.println(Arrays.toString(rid));
		}
	}
	
	@Test
	public void TreeBuilerTest() {
		Catalog.initialize("samples2/interpreter_config_file.txt");
		Catalog.getInstance();
//		IndexBuilder ib = new IndexBuilder(Catalog.getTableFiles("Boats"), 1, 10);
//		ib.leafNodes();
//		ib.IndexNodes();
	}
	
	@Test
	public void UnionFindTest() {
		// ufe1: 1,3,6  100,100,100
		// ufe2: 2,4,5,7  20,90,null
		Column col1 = generateColumn("Sailors", "A");
		Column col2 = generateColumn("Boats", "E");
		Column col3 = generateColumn("Reserves", "G");
		Column col4 = generateColumn("Reserves", "H");
		Column col5 = generateColumn("Sailors", "B");
		Column col6 = generateColumn("Sailors", "C");
		Column col7 = generateColumn("Boats", "D");
		UnionFind uf = new UnionFind();
		uf.setLowerBound(uf.find(col1), 50);
		uf.setLowerBound(uf.find(col5), 20);
		uf.setEqualityConstraint(uf.find(col3), 100);
		uf.setUpperBound(uf.find(col7), 600);
		uf.join(uf.find(col1), uf.find(col3));
		uf.join(uf.find(col2), uf.find(col4));
		uf.join(uf.find(col2), uf.find(col5));
		uf.join(uf.find(col7), uf.find(col4));
		uf.join(uf.find(col1), uf.find(col6));
		uf.setUpperBound(uf.find(col2), 90);
		System.out.println(uf.find(col1).toString());
		System.out.println(uf.find(col2).toString());
		System.out.println(uf.find(col3).toString());
		System.out.println(uf.find(col4).toString());
		System.out.println(uf.find(col5).toString());
		System.out.println(uf.find(col6).toString());
		System.out.println(uf.find(col7).toString());
	}
	
	private Column generateColumn(String tName, String AttrName) {
		Column col = new Column();
		Table t = new Table();
		t.setName(tName);
		col.setColumnName(AttrName);
		col.setTable(t);
		return col;
	}
	
	@Test
	public void UnionFindVisitorTest() {
		Catalog.initialize("samples2"+File.separator+"interpreter_config_file.txt");
		Catalog.getInstance();
		CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
		Statement statement;
		try {
			while ( (statement = parser.Statement()) != null ) {	
				Catalog.resetAlias();
				try {
					Select select = (Select) statement;
					System.out.println(select.toString());
					PlainSelect ps = (PlainSelect)select.getSelectBody();
					UnionFindVisitor ufv = new UnionFindVisitor();
					Expression exp = ps.getWhere();
					exp.accept(ufv);
					System.out.println(ufv.getUnionFind().toString());
				} catch (Exception e) {	
					e.printStackTrace();
					System.err.println("Exception occurred during parsing");
					continue;
				}finally {
					Catalog.resetAlias();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

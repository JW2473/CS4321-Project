package test;

import org.junit.Test;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import physicaloperators.ScanOperator;
import util.Catalog;
import util.MyTable;
import util.SelectParserTree;
import util.TupleReader;
import visitor.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class UtilTest {
	
	@Test
	public void SelExprTest() throws Exception{

		Catalog.getInstance();
	
		// TODO Auto-generated catch block
	try {
		CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
		Statement statement;
		
		while ((statement = parser.Statement()) != null) {
			System.out.println("Read statement: " + statement);
			Select select = (Select) statement;
			PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
			SelectExpVisitor sv = new SelectExpVisitor();
			ScanOperator s = new ScanOperator(new MyTable(plainSelect.getFromItem()));
			if (plainSelect.getWhere() != null) {
				sv.readTuple(s.getNextTuple());
				plainSelect.getWhere().accept(sv);
				System.out.println(sv.getCurStatus());
			}
			
		}
	} catch (Exception e) {
		System.err.println("Exception occurred during parsing");
		e.printStackTrace();
		}

	}
	
	@Test
	public void JoinExprTest() throws Exception{

		Catalog.getInstance();
	
		// TODO Auto-generated catch block
	try {
		CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
		Statement statement;
		
		while ((statement = parser.Statement()) != null) {
			System.out.println("Read statement: " + statement);
			Select select = (Select) statement;
			PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
			//JoinExpVisitor jv = new JoinExpVisitor();
			//ScanOperator s1 = new ScanOperator(new MyTable(plainSelect.getFromItem()));
			List<Join> joins = plainSelect.getJoins();
			for(Join j : joins)
				System.out.println(j.toString());
			/*
			if (plainSelect.getJoins() != null) {
				ScanOperator s2 = new ScanOperator(new MyTable(((Join) plainSelect.getJoins().get(0)).getRightItem()));
				if (plainSelect.getWhere() != null) {
					jv.readTuple(s1.getNextTuple(), s2.getNextTuple());
					plainSelect.getWhere().accept(jv);
					System.out.println(jv.getCurStatus());
				}
			}*/
		}
	} catch (Exception e) {
		System.err.println("Exception occurred during parsing");
		e.printStackTrace();
		}
	}
	
	@Test
	public void SelTreeTest() throws Exception{

		Catalog.getInstance();
	
		// TODO Auto-generated catch block
		try {
			CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
			Statement statement;
			int count = 1;
			while ((statement = parser.Statement()) != null) {
				System.out.println("Read statement: " + statement);
				Select select = (Select) statement;
				SelectParserTree spt = new SelectParserTree(select);
				PrintStream ps = new PrintStream(new File(Catalog.output + "query" + String.valueOf(count) + ".txt"));
				spt.root.dump(ps);
				count++;
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
			}
	}
	
	@Test
	public void ExeTest() {
		Catalog.initialize("samples/test_input", "samples/output2", "samples/temp");
		Catalog.getInstance();
		CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
		Statement statement;
		int count = 1;
		try {
			while ( (statement = parser.Statement()) != null ) {	
				PrintStream ps = null;
				Catalog.resetAlias();
				try {
					Select select = (Select) statement;
					System.out.println(select.toString());
					SelectParserTree spt = new SelectParserTree(select);
					ps = new PrintStream(new File(Catalog.output + "query" + String.valueOf(count)) + ".txt");
					spt.root.dump(ps);
				} catch (Exception e) {	
					e.printStackTrace();
					System.err.println("Exception occurred during parsing");
					continue;
				}finally {
					if ( ps != null ) ps.close();
					count++;
					Catalog.resetAlias();
					
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void ParseJoinTest() {
		Catalog.getInstance();
	}
	@Test
	public void ReaderTest() {
		Catalog.getInstance();
		String filePath = Catalog.tempDir + "ExSort1/Pass0_0";
//		String filePath = Catalog.output + "query1";
//		String filePath = Catalog.input + File.separator + "db" + File.separator + "data" + File.separator + "Reserves";
		File inputFile = new File(filePath);
		TupleReader tr;
		try {
			tr = new TupleReader(inputFile);
			System.out.println(tr.getAttrNum());
			System.out.println(tr.getSize());
			tr.convertToReadableFile(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void newExeTest() {
//		Catalog.initialize("samples/test_input", "samples/output", "samples/temp");
		Catalog.initialize("/Users/cuiyixin/Desktop/Submission/p2/input", "/Users/cuiyixin/Desktop/Submission/p2/output", "/Users/cuiyixin/Desktop/Submission/p2/temp");
		Catalog.getInstance();
		CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
		Statement statement;
		int count = 1;
		long startTime = System.currentTimeMillis();
		try {
			while ( (statement = parser.Statement()) != null ) {	
				Catalog.resetAlias();
				try {
					Select select = (Select) statement;
					System.out.println(select.toString());
					SelectParserTree spt = new SelectParserTree(select);
					
					System.out.println("Dumping binary file...");
					String filePath = Catalog.output;
					String fileName = "query" + String.valueOf(count);
					spt.root.dump(filePath, fileName);
					
//					System.out.println("Dumping readiable file...");
//					PrintStream ps = null;
//					ps = new PrintStream(new File(Catalog.output + "query" + String.valueOf(count)) + ".txt");
//					spt.root.dump(ps);
					
				} catch (Exception e) {	
					e.printStackTrace();
					System.err.println("Exception occurred during parsing");
					continue;
				}finally {
					count++;
					Catalog.resetAlias();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Elapsed Time is: " + elapsedTime);
	}
}

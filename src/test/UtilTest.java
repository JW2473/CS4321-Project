package test;

import org.junit.Test;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operators.ScanOperator;
import util.Catalog;
import util.MyTable;
import util.SelectParserTree;
import visitor.*;

import java.io.File;
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
		//Catalog.initialize(input, output);
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
					SelectParserTree spt = new SelectParserTree(select);
					ps = new PrintStream(new File(Catalog.output + "query" + String.valueOf(count)) + ".txt");
					spt.root.dump(ps);
				} catch (Exception e) {					
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
}

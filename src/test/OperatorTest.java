package test;

import java.io.File;
import java.io.PrintStream;

import org.junit.Test;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operators.JoinOperator;
import operators.ProjectOperator;
import operators.ScanOperator;
import operators.SelectOperator;
import util.Catalog;
import util.MyTable;

public class OperatorTest {
	
	@Test
	public void ScanTest() throws Exception{

			Catalog.getInstance();
		
			// TODO Auto-generated catch block
		try {
			CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
			Statement statement;
			int count = 1;
			while ((statement = parser.Statement()) != null) {
				System.out.println("\nRead statement: " + statement);
				Select select = (Select) statement;
				PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
				ScanOperator s = new ScanOperator(new MyTable(plainSelect.getFromItem()));				
				
				System.out.println("\nStart dumping...");
				PrintStream ps = new PrintStream(new File(Catalog.output + "query" + String.valueOf(count) + ".txt"));
				s.dump(ps);
				System.out.println("Dumping finished !");
				count++;
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
	
	@Test
	public void SelectTest() throws Exception{

			Catalog.getInstance();
		
			// TODO Auto-generated catch block
		try {
			CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
			Statement statement;
			int count = 1;
			while ((statement = parser.Statement()) != null) {
				System.out.println("\nRead statement: " + statement);
				Select select = (Select) statement;
				PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
				ScanOperator s = new ScanOperator(new MyTable(plainSelect.getFromItem()));
				SelectOperator selOp = new SelectOperator(s, plainSelect.getWhere());
				System.out.println("\nStart dumping...");
				PrintStream ps = new PrintStream(new File(Catalog.output + "query" + String.valueOf(count) + ".txt"));
				selOp.dump(ps);
				System.out.println("Dumping finished !");
				count++;
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
	
	@Test
	public void ProjectTest() throws Exception{

			Catalog.getInstance();
		
			// TODO Auto-generated catch block
		try {
			CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
			Statement statement;
			int count = 1;
			while ((statement = parser.Statement()) != null) {
				System.out.println("\nRead statement: " + statement);
				Select select = (Select) statement;
				PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
				ScanOperator s = new ScanOperator(new MyTable(plainSelect.getFromItem()));
				SelectOperator selOp = new SelectOperator(s, plainSelect.getWhere());
				ProjectOperator proOp = new ProjectOperator(plainSelect.getSelectItems(), selOp);
				System.out.println("\nStart dumping...");
				PrintStream ps = new PrintStream(new File(Catalog.output + "query" + String.valueOf(count) + ".txt"));
				proOp.dump(ps);
				System.out.println("Dumping finished !");
				count++;
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
	
	@Test
	public void JoinTest() throws Exception{

			Catalog.getInstance();
		
			// TODO Auto-generated catch block
		try {
			CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
			Statement statement;
			int count = 1;
			while ((statement = parser.Statement()) != null) {
				System.out.println("\nRead statement: " + statement);
				Select select = (Select) statement;
				PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
				PrintStream ps = new PrintStream(new File(Catalog.output + "query" + String.valueOf(count) + ".txt"));
				ScanOperator s1 = new ScanOperator(new MyTable(plainSelect.getFromItem()));
				if (plainSelect.getJoins() != null) {
					FromItem fi = ((Join) plainSelect.getJoins().get(0)).getRightItem();
					System.out.println(fi.toString());
					MyTable mt = new MyTable(fi);
					System.out.println(mt.getAlias());
					ScanOperator s2 = new ScanOperator(mt);
					if (plainSelect.getWhere() != null) {
						JoinOperator j = new JoinOperator(s1, s2, plainSelect.getWhere());
						ProjectOperator proOp = new ProjectOperator(plainSelect.getSelectItems(), j);
						proOp.dump(ps);
					}
				}
				count++;
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}

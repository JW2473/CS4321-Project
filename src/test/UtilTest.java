package test;

import org.junit.Test;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operators.ScanOperator;
import util.Catalog;
import util.MyTable;
import visitor.*;

public class UtilTest {
	
	@Test
	public void ExprTest() throws Exception{

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
}

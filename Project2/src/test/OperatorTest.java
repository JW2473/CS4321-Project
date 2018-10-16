package test;

import java.io.File;
import java.io.PrintStream;

import org.junit.Test;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import physicaloperators.ExternalSortOperator;
import physicaloperators.InMemorySortOperator;
import physicaloperators.ScanOperator;
import physicaloperators.SortOperator;
import util.Catalog;
import util.MyTable;

public class OperatorTest {
	
	@Test
	public void ExSortTest() throws Exception{

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
				if (plainSelect.getOrderByElements() != null) {
//					SortOperator sortOp = new InMemorySortOperator(s, plainSelect.getOrderByElements());
					SortOperator sortOp = new ExternalSortOperator(s, plainSelect.getOrderByElements());
					
					System.out.println("\nStart dumping...");
					PrintStream ps = new PrintStream(new File(Catalog.output + "query" + String.valueOf(count) + ".txt"));
					sortOp.dump(ps);
					System.out.println("Dumping finished !");
				}
				count++;
				Catalog.resetAlias();
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}
}

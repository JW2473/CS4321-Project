package client;

import java.io.FileNotFoundException;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import util.Catalog;


/**
 * @author Yixin Cui
 * @author Haodong Ping
 * 
 * This is the interpreter class that takes in the input/output directory and 
 * calls other methods to perform parsing
 */
public class Interpreter {

	public static void main(String[] args) throws FileNotFoundException {
		if (args.length != 2) {
			throw new IllegalArgumentException("Wrong arguments!");
		}
		Catalog.initialize(args[0], args[1]);
		Catalog.getInstance();
		try {
			CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
			Statement statement;
			while ((statement = parser.Statement()) != null) {
				System.out.println("Read statement: " + statement);
				Select select = (Select) statement;
				PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
				System.out.println(plainSelect.getFromItem());
			}
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}
	}

}

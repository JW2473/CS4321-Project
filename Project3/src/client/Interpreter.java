package client;

import java.io.PrintStream;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import util.Catalog;
import util.SelectParserTree;


/**
 * @author Yixin Cui
 * @author Haodong Ping
 * 
 * This is the interpreter class that takes in the input/output directory and 
 * calls other methods to perform parsing
 */

public class Interpreter {

	public static void main(String[] args) {
		if (args.length != 3) {
			throw new IllegalArgumentException("Wrong arguments!");
		}
		Interpreter interpreter = new Interpreter();
		interpreter.ExecuteSel( args[0], args[1], args[2] );
	}
	/**
	 * @author Yixin Cui
	 * @author Haodong Ping
	 * 
	 * This is the method that execute the select querys from input
	 * path and output the result to output path
	 */
	public void ExecuteSel( String input, String output, String temp ) {
		Catalog.initialize(input, output, temp);
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
					String filePath = Catalog.output;
					String fileName = "query" + String.valueOf(count);
					spt.root.dump(filePath, fileName);
				} catch (Exception e) {					
					System.err.println("Exception occurred during parsing");
					continue;
				}finally {
					if ( ps != null ) ps.close();
					count++;	
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Catalog.resetAlias();
			Catalog.schema_map.clear();
			
		}
	}

}

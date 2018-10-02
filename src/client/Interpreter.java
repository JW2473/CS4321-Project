package client;

import java.io.File;
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
		if (args.length != 2) {
			throw new IllegalArgumentException("Wrong arguments!");
		}
		Interpreter interpreter = new Interpreter();
		interpreter.ExecuteSel( args[0], args[1] );
	}
	
	public void ExecuteSel( String input, String output ) {
		Catalog.initialize(input, output);
		Catalog.getInstance();
		CCJSqlParser parser = new CCJSqlParser(Catalog.getQueryFiles());
		Statement statement;
		int count = 1;
		try {
			while ((statement = parser.Statement()) != null) {
				System.out.println("Processing statement: " + statement);
				
				try {
					Select select = (Select) statement;
					SelectParserTree spt = new SelectParserTree(select);
					PrintStream ps = new PrintStream(new File(Catalog.output + "query" + String.valueOf(count)));
					spt.root.dump(ps);
					System.out.println("Processing finished!\n");
					count++;
				} catch (Exception e) {
					
					System.err.println("Exception occurred during parsing");
					continue;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

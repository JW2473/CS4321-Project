package test;

import org.junit.Test;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import util.Catalog;
import util.SelectParserTree;
import util.Tools;
import util.TupleReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

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
	public void ReaderTest() {
		Catalog.initialize("samples2/interpreter_config_file.txt");
		Catalog.getInstance();
//		String filePath = Catalog.tempDir + "ExSort1/Pass0_0";
//		String filePath = Catalog.output + "query1";
		String filePath = Catalog.input + File.separator + "db" + File.separator + "data" + File.separator + "Sailors";
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
		Catalog.initialize("samples2/interpreter_config_file.txt");
//		Catalog.initialize("samples/test_input", "samples/output", "samples/temp");
//		Catalog.initialize("/Users/cuiyixin/Desktop/Submission/p2/input", "/Users/cuiyixin/Desktop/Submission/p2/output", "/Users/cuiyixin/Desktop/Submission/p2/temp");
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
					
//					System.out.println("Dumping binary file...");
//					String filePath = Catalog.output;
//					String fileName = "query" + String.valueOf(count);
//					spt.root.dump(filePath, fileName);
					
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
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Elapsed Time is: " + elapsedTime);
	}

	@Test
	public void IndexSortTest() {
		Catalog.initialize("samples2/interpreter_config_file.txt");
		Catalog.getInstance();
		Tools.sortByIndex("Sailors");
		
	}
}

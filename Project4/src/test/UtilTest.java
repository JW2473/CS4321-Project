package test;

import org.junit.Test;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import util.Catalog;
import util.SelectParserTree;
import util.Tools;
import util.TreeReader;
import util.TupleReader;
import util.UnionFind;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class UtilTest {
	
	@Test
	public void CatalogTest() {
		Catalog.initialize("samples/interpreter_config_file.txt");
		Catalog.getInstance();
		Catalog.findStats();
		System.out.println(Catalog.buildIndex);
		System.out.println(Catalog.executeQuery);
		System.out.println(Catalog.useIndex);
	}
	

}

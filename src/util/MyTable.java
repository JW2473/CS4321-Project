package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * MyTable class connects the table file and the table object extracted from the query
 *
 */
public class MyTable{
	String tname;
	String alias;
	List<String> schemaName;
	BufferedReader br = null;
	
	/*
	 * Create a MyTable object from the query
	 * @param fromItem FromItem in the query
	 */
	public MyTable(FromItem fromItem) {
		Table t = (Table) fromItem;
		alias = t.getAlias();
		tname = t.getWholeTableName();
		schemaName = Catalog.getSchema(tname);
		br = Catalog.getTableFiles(tname);
	}
	
	/*
	 * Get table name
	 * @return table name
	 */
	public String getTname() {
		return tname;
	}
	/*
	 * Get the alias of the table
	 * @return the alias of the table
	 */
	public String getAlias() {
		return alias;
	}

	/*
	 * Get the schema list of the table
	 * @return the list of all schemas in the table
	 */
	public List<String> getSchemaName() {
		return schemaName;
	}

	/*
	 * Read next line from the table until last line
	 * Convert every text line to an array of string
	 * Create the tuple from that array
	 * @return the next tuple in the file
	 */
	public Tuple nextTuple() {
		try {
			String[] val = br.readLine().split(",");
			return new Tuple(val, this.tname);
		} catch (NullPointerException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Close the table file and reopen it through the Catalog object
	 */
	public void reset() {
		try {
			br.close();
			br = Catalog.getTableFiles(tname);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

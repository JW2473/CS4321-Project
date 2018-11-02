package util;

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
	String tFullName;
	String alias;
	List<String> schemaName;
	TupleReader tr = null;
	
	/**
	 * Create a MyTable object from the query
	 * @param fromItem FromItem in the query
	 */
	public MyTable(FromItem fromItem) {
		Table t = (Table) fromItem;
		alias = t.getAlias();
		tFullName = t.getWholeTableName();
		schemaName = Catalog.getSchema(tFullName);
		tr = Catalog.getTableFiles(tFullName);
		if (alias != null) {
			Catalog.setAlias(alias, tFullName);
		}else {
			Catalog.setAlias(tFullName, tFullName);
		}
	}
	
	/**
	 * Get table name
	 * @return table name
	 */
	public String getFullTableName() {
		return tFullName;
	}
	
	/**
	 * Get the alias of the table
	 * @return the alias of the table
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Get the unique identity for the table
	 * @return the unique identity name for the table
	 */
	public String getUniqueName() {
		return alias == null ? tFullName : alias;
	}

	/**
	 * Get the schema list of the table
	 * @return the list of all schemas in the table
	 */
	public List<String> getSchemaName() {
		return schemaName;
	}

	/**
	 * Read next line from the table until last line
	 * Convert every text line to an array of string
	 * Create the tuple from that array
	 * @return the next tuple in the file
	 */
	public Tuple nextTuple() {
		try {
			long[] value = tr.nextTuple();
			return new Tuple(value, getUniqueName(), tFullName);
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public Tuple nextTuple(int pageNum, int tupleNum) {
		// TODO Return tuple according to the Rid
		try {
			long[] value = tr.nextTuple(pageNum, tupleNum);
			return new Tuple(value, getUniqueName(), tFullName);
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	/**
	 * Close the table file and reopen it through the Catalog object
	 */
	public void reset() {
		tr.close();
		tr = Catalog.getTableFiles(tFullName);
	}
}

package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;

public class myTable{
	String tname;
	String alias;
	List<String> schemaName;
	BufferedReader br = null;
	
	public myTable(FromItem fromItem) {
		Table t = (Table) fromItem;
		alias = t.getAlias();
		tname = t.getWholeTableName();
		schemaName = Catalog.getSchema(tname);
		br = Catalog.getTableFiles(tname);
	}
	
	public String getTname() {
		return tname;
	}

	public String getAlias() {
		return alias;
	}

	public List<String> getSchemaName() {
		return schemaName;
	}

	public Tuple nextTuple() {
		try {
			String[] val = br.readLine().split(",");
			return new Tuple(val);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void reset() {
		try {
			br.close();
			br = Catalog.getTableFiles(tname);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

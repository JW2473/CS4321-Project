package util;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Column;

public class Tools {

	public static String rebuildWholeColumnName(Column col) {
		String colName = col.getColumnName();
		String tName = col.getWholeColumnName().split("\\.")[0];
		String fullTableName = Catalog.getTableFullName(tName);
		StringBuilder sb = new StringBuilder();
		sb.append(fullTableName);
		sb.append(".");
		sb.append(colName);
		return sb.toString();	
	}
	
	public static List<String> InitilaizeWholeColumnName(String tableName) {
		List<String> ret = new ArrayList<>();
		List<String> schemas = Catalog.getSchema(tableName);
		String fullTableName = Catalog.getTableFullName(tableName);
		StringBuilder sb = new StringBuilder();
		for (String colName : schemas) {
			sb.append(fullTableName);
			sb.append(".");
			sb.append(colName);
			ret.add(sb.toString());
			sb = new StringBuilder();
		}
		return ret;	
	}
	
}

package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import physicaloperators.SortOperator.tupleComp;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * Tools class provides some helpful tools for other classes to call
 *
 */
public class Tools {

	/*
	 * Rebuild the whole column Name from the column object with the unique identity name of the table
	 * @param col the column
	 * @return the whole column name
	 */
	public static String rebuildWholeColumnName(Column col) {
		String colName = col.getColumnName();
		String tName = col.getTable().getName();
		//String tName = col.getWholeColumnName().split("\\.")[0];
		String uniqueTableName = Catalog.getUniqueName(tName);
		StringBuilder sb = new StringBuilder();
		sb.append(uniqueTableName);
		sb.append(".");
		sb.append(colName);
		return sb.toString();	
	}
	
	/*
	 * Initialize the whole column Name from the the table full name and unique identity name
	 * @param uniqueName the unique identity name of the table
	 * @param tableFullName the full name of the table
	 * @return the whole column name
	 */
	public static List<String> InitilaizeWholeColumnName(String uniqueName, String tableFullName) {
		List<String> ret = new ArrayList<>();
		List<String> schemas = Catalog.getSchema(tableFullName);
		StringBuilder sb = new StringBuilder();
		for (String colName : schemas) {
			sb.append(uniqueName);
			sb.append(".");
			sb.append(colName);
			ret.add(sb.toString());
			sb = new StringBuilder();
		}
		return ret;	
	}
	/*
	 * Get the Related TableName from the Expression
	 * @param exp: the binary expression
	 * @return the list of related tablename
	 */
	public static List<String> getRelativeTabAlias(Expression exp) {
		List<String> res = new ArrayList<>();
		if( !(exp instanceof BinaryExpression)) return res;
		Expression left = ((BinaryExpression)exp).getLeftExpression();
		Expression right = ((BinaryExpression)exp).getRightExpression();
		Column c = null;
		if( left instanceof Column ) {
			c = (Column)left;
			String s = c.getTable().getName();
			if( s != null)
				res.add(s);
		}
		if( right instanceof Column ) {
			c = (Column)right;
			String s = c.getTable().getName();
			if( s != null)
				if( res == null || !res.get(0).equals(s))
					res.add(s);
		}
		return res;
	}
	/*
	 * Get table name from Join
	 * @param j: Join 
	 * @return the table name from join class
	 */
	public static String Join2Tabname(Join j) {
		FromItem fi = j.getRightItem();
		String s = fi.getAlias();
		if ( s != null ) {
			Table t = (Table) fi;
			Catalog.setAlias(s, t.getWholeTableName());
			return s;
		}
		return  fi.toString();
	}
	
	private static List<Column> indexBy(String tableName) {
		List<Column> ret = new ArrayList<>();
		Column col = new Column();
		Table t = new Table();
		t.setName(tableName);
		String[] indexInfo = Catalog.indexInfo.get(tableName);
		col.setColumnName(indexInfo[1]);
		col.setTable(t);
		ret.add(col);
		return ret;
	}
	
	public static void sortByIndex(String tableName) {
		List<Tuple> tps = new ArrayList<>();
		TupleReader tr = Catalog.getTableFiles(tableName);
		long[] t = null;
		while ((t = tr.nextTuple()) != null) {
			tps.add(new Tuple(t, tableName, tableName));
		}
		tr.close();
		Collections.sort(tps, new tupleComp(indexBy(tableName)));
		
		TupleWriter tw = new TupleWriter(tr.getFile());
		try {
			for (Tuple tp : tps) {
				tw.writeTuple(tp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		tw.close();
	}
	
}

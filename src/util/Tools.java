package util;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;

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
	
	public static List<String> getRelativeTabAlias(Expression exp) {
		List<String> res = new ArrayList();
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
	
}

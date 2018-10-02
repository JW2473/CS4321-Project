package util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import java.util.*;
public class SelectParserTree {
	Select sel;
	PlainSelect ps;
	List<String> froms;
	Map<String, Expression> selcon;
	Map<String, Expression> joincon;
	
	private List<Expression> splitAnds(Expression exp) {
		List<Expression> res = new ArrayList<>();
		
		while(exp instanceof AndExpression) {
			AndExpression ae = (AndExpression)exp;
			res.add(ae.getRightExpression());
			exp = ae.getLeftExpression();
		}
		res.add(exp);
		return res;
	}
	
	private Expression rebuildExpression(List<Expression> exps) {
		if( exps.size() == 0 ) return null;
		for ( Expression exp : exps ) {
			AndExpression ae = new AndExpression();
		}
	}
	private int getRightTableId(List<String> relateTable) {
		int id = 0;
		if( relateTable.size()==0 )
			return froms.size()-1;
		for(String table:relateTable) {
			id = Math.max(id, froms.indexOf(table));
		}
		return id;
	}
	public SelectParserTree(Select select) {
		this.sel = select;
		this.ps = (PlainSelect)sel.getSelectBody();
		FromItem fi = ps.getFromItem();
		if(fi != null)
			if ( fi.getAlias() != null) {
				froms.add(fi.getAlias());
				Table t = (Table) fi;
				Catalog.setAlias(t.getAlias(), t.getWholeTableName());
			}				
			else
				froms.add(fi.toString());
		List<Join> joins = ps.getJoins();
		for( Join j : joins ) {
			String name = Tools.Join2Tabname(j);
			froms.add(name);
		}
		Expression e = ps.getWhere();
		if ( e != null ) {
			List<Expression> exps = splitAnds(e);
			Map<String,List<Expression>> tempselcon = new HashMap();
			Map<String,List<Expression>> tempjoincon = new HashMap();
			for(String name : froms) {
				tempselcon.put(name,new ArrayList<>());
				tempjoincon.put(name,new ArrayList<>());
			}
			for(Expression exp : exps) {
				List<String> relateTable = Tools.getRelativeTabAlias(exp);
				if( relateTable.size() == 0 ) {
					tempjoincon.get(froms.get(getRightTableId(relateTable))).add(exp);
				}else if( relateTable.size() == 1 ) {
					tempselcon.get(froms.get(getRightTableId(relateTable))).add(exp);
				}else {
					tempjoincon.get(froms.get(getRightTableId(relateTable))).add(exp);
				}
			}
			
			
		}
		
		
	}
	
}

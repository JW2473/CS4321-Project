package util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import operators.JoinOperator;
import operators.Operator;
import operators.ProjectOperator;
import operators.ScanOperator;
import operators.SelectOperator;

import java.util.*;
public class SelectParserTree {
	Select sel;
	PlainSelect ps;
	List<String> froms;
	List<SelectItem> selItems;
	Map<String, Expression> selcon;
	Map<String, Expression> joincon;
	Map<String, FromItem> from_map;
	public Operator root;
	
	private List<Expression> splitAnds(Expression exp) {
		List<Expression> res = new ArrayList<>();
		if(exp == null)
			return res;
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
		Expression res = exps.get(0);
		for ( int i = 1; i< exps.size(); i++ ) {
			res = new AndExpression(res,exps.get(i));
		}
		return res;
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
	private void buildTree() {
		Operator op = new ScanOperator(new MyTable(from_map.get(froms.get(0))));
		if(selcon.get(froms.get(0))!=null)
			op = new SelectOperator(op,selcon.get(froms.get(0)));
		for ( int i = 1; i < froms.size(); i++ ) {
			String name = froms.get(i);
			FromItem temp = from_map.get(name);
			Operator temp_op = new ScanOperator(new MyTable(temp));
			if(selcon.get(name)!=null)
				temp_op = new SelectOperator(temp_op, selcon.get(name));
			Expression joinExp = joincon.get(name);
			op = new JoinOperator(op, temp_op, joinExp);
		}
		op = new ProjectOperator(selItems, op);
		this.root = op;
		
	}
	public SelectParserTree(Select select) {
		this.sel = select;
		this.ps = (PlainSelect)sel.getSelectBody();
		this.from_map = new HashMap<>();
		this.froms = new ArrayList<>();
		this.selItems = ps.getSelectItems();
		FromItem fi = ps.getFromItem();
		if(fi != null) {
			if ( fi.getAlias() != null) {
				froms.add(fi.getAlias());
				Table t = (Table) fi;
				Catalog.setAlias(t.getAlias(), t.getWholeTableName());
				from_map.put(fi.getAlias(), fi);
			}				
			else {
				froms.add(fi.toString());
				from_map.put(fi.toString(),fi);
			}
		}
		List<Join> joins = ps.getJoins();
		if ( joins != null )
			for( Join j : joins ) {
				String name = Tools.Join2Tabname(j);
				froms.add(name);
				from_map.put(name, j.getRightItem());
			}
		Expression e = ps.getWhere();

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
		this.selcon = new HashMap<>();
		this.joincon = new HashMap<>();
		for(String from:froms) {
			this.selcon.put(from, rebuildExpression(tempselcon.get(from)));
			this.joincon.put(from, rebuildExpression(tempjoincon.get(from)));
		}
			

		buildTree();
		Catalog.resetAlias();
		//this.selcon.clear();
		//this.joincon.clear();
	}
	
}

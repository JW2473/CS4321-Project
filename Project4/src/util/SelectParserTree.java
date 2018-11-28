package util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import physicaloperators.Operator;
import visitor.PhysicalPlanBuilder;
import java.util.*;

import logicaloperators.*;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * SelectParserTree class builds an operator tree from the select query to get the final result.
 *
 */
public class SelectParserTree {

	private Select sel;
	private List<String> froms;
	private List<SelectItem> selItems;
	private List<OrderByElement> orders;
	private Distinct delDup;
	private Map<String, MyTable> from_map;
	private ParseWhere pw;
	public Operator root;
	public PhysicalPlanBuilder ppb;
	
	/**
	 * build the operator tree
	 */
	private void buildTree() {
		
		LogicOperator op = new LogicScanOperator(from_map.get(froms.get(0)));
		if((pw.getSelExp(froms.get(0)))!=null)
			op = new LogicSelectOperator(op,pw.getSelExp(froms.get(0)));
		for ( int i = 1; i < froms.size(); i++ ) {
			String name = froms.get(i);
			MyTable temp = from_map.get(name);
			LogicOperator temp_op = new LogicScanOperator(temp);
			Expression selExp = pw.getSelExp(name);
			if(selExp!=null)
				temp_op = new LogicSelectOperator(temp_op, selExp);
			Expression joinExp = pw.getJoinExp(name);
	
			op = new LogicJoinOperator(op, temp_op, joinExp);
		}

		op = new LogicProjectOperator(op,selItems);
		
		if( orders != null )
			op = new LogicSortOperator(op, orders);
		if( delDup != null )
			op = new LogicDuplicateEliminationOperator(op);
		
		//System.out.println("\nlogical query plan:");
		this.ppb = new PhysicalPlanBuilder();
		op.accept(ppb);
		root = ppb.getOp();
		//System.out.println("\nphysical query plan:");
	//	System.out.println(ppb.toString());
	}
	
	/**
	 * Create the Select Parser Tree from the select query.
	 * @param select the select query.
	 */
	public SelectParserTree(Select select) {
		this.sel = select;
		PlainSelect ps = (PlainSelect)sel.getSelectBody();
		this.from_map = new HashMap<>();
		this.froms = new ArrayList<>();
		this.selItems = ps.getSelectItems();
		this.orders = ps.getOrderByElements();
		this.delDup = ps.getDistinct();
		ps.getOrderByElements();
		FromItem fi = ps.getFromItem();
		if(fi != null) {
			if ( fi.getAlias() != null) {
				froms.add(fi.getAlias());
				from_map.put(fi.getAlias(), new MyTable(fi));
			}				
			else {
				froms.add(fi.toString());
				from_map.put(fi.toString(), new MyTable(fi));
			}
		}
		List<Join> joins = ps.getJoins();
		if ( joins != null )
			for( Join j : joins ) {
				String name = Tools.Join2Tabname(j);
				froms.add(name);
				from_map.put(name, new MyTable(j.getRightItem()));
			}
		//Collections.reverse(froms);
		this.pw = new ParseWhere(froms, from_map, ps.getWhere());
		this.froms = new ArrayList<>(pw.getOptimizeFroms());
		
		//this.froms = 
		buildTree();
		pw.ufv = null;
	}
	
}

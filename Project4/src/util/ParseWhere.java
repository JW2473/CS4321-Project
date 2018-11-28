package util;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;
import physicaloperators.Operator;
import visitor.UnionFindVisitor;
import visitor.UnsupportedException;

import java.util.*;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * ParserWhere class get the expression for each table by parsing where expression.
 *
 */
public class ParseWhere {
	private List<String> froms;
	private List<String> optimizedFroms;
	
	private Map<String, MyTable> from_map;
	private Map<String, Expression> selcon;
	private Map<String, Expression> joincon;
	
	private Map<TablePairs, List<Expression>> joinState;
	private Map<String, Integer> selSize;
	
	private long min_size = Long.MAX_VALUE;
	
	public static UnionFindVisitor ufv;
	/**
	 * split Expression from 'Where' to non and Expressions.
	 * @param exp the Expression get from where
	 * @return the list of expression from where
	 */
	public static List<Expression> splitWhere(Expression exp) {
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
	
	/**
	 *  pares the select condition and get the lowkey and high key for the attribute
	 * @param attr
	 * @param exp
	 * @return
	 */
	public static String[] parseSel(String attr, Expression exp){
		List<Expression> selExp = splitWhere(exp);
		String[] res = {"x","x"};
		if(selExp.size() == 0) return res;
		for(Expression expr:selExp) {
			if(!res[0].equals("x")&&!res[1].equals("x")) {
				long small = Long.parseLong(res[0]);
				long large = Long.parseLong(res[1]);
				if(small > large)
					return new String[] {"x","x"};
			}
			Expression left = ((BinaryExpression)expr).getLeftExpression();
			Expression right = ((BinaryExpression)expr).getRightExpression();
			if( (left instanceof Column && right instanceof LongValue) || (right instanceof Column && left instanceof LongValue) ) {
				String col = left instanceof Column ? Tools.rebuildWholeColumnName((Column)left):Tools.rebuildWholeColumnName((Column)right);
				String colname = col.split("\\.")[1];
				if(colname.equals(attr)) {
					long value = left instanceof LongValue ? ((LongValue)left).getValue():((LongValue)right).getValue();
					
					// attr < value
					if( ( left instanceof LongValue && expr instanceof GreaterThan ) || ( right instanceof LongValue && expr instanceof MinorThan ) ) {
						if(res[1].equals("x")) {
							res[1] = Long.toString(value - 1);
						} else {
							long temp = Long.parseLong(res[1]);
							if(value < temp)
								res[1] = Long.toString(value - 1);
						}
					}
					// attr <= value
					else if ( ( left instanceof LongValue && expr instanceof GreaterThanEquals ) || ( right instanceof LongValue && expr instanceof MinorThanEquals ) ) {
						if(res[1].equals("x")) {
							res[1] = Long.toString(value);
						} else {
							long temp = Long.parseLong(res[1]);
							if(value < temp)
								res[1] = Long.toString(value);
						}
					}
					// attr > value
					else if( ( right instanceof LongValue && expr instanceof GreaterThan ) || ( left instanceof LongValue && expr instanceof MinorThan ) ) {
						if(res[0].equals("x")) {
							res[0] = Long.toString(value + 1);
						} else {
							long temp = Long.parseLong(res[0]);
							if(value > temp)
								res[0] = Long.toString(value + 1);
						}
					}
					// attr >= value
					else if ( ( right instanceof LongValue && expr instanceof GreaterThanEquals ) || ( left instanceof LongValue && expr instanceof MinorThanEquals ) ) {
						if(res[0].equals("x")) {
							res[0] = Long.toString(value);
						} else {
							long temp = Long.parseLong(res[0]);
							if(value > temp)
								res[0] = Long.toString(value);
						}
					}
					// attr == value
					else if(expr instanceof EqualsTo) {
						if(res[0].equals("x")&&res[1].equals("x")) {
							res[0] = Long.toString(value);
							res[1] = Long.toString(value);
							continue;
						} else if (res[0].equals("x")&&!res[1].equals("x")) {
							long large = Long.parseLong(res[1]);
							res[0] = Long.toString(value);
							if(value < large)
								res[1] = Long.toString(value);
						} else if (!res[0].equals("x")&& res[1].equals("x")) {
							long small = Long.parseLong(res[0]);
							res[1] = Long.toString(value);
							if(value > small)
								res[0] = Long.toString(value);
						} else {
							long small = Long.parseLong(res[0]);
							long large = Long.parseLong(res[1]);
							if(value > small )
								res[0] = Long.toString(value);
							if(value < large)
								res[1] = Long.toString(value);
						}
						
					}
				}
			}
		}
		if(!res[0].equals("x")&&!res[1].equals("x")) {
			long small = Long.parseLong(res[0]);
			long large = Long.parseLong(res[1]);
			if(small > large)
				return new String[] {"x","x"};
		}
		return res;
	}
	/**
	 * pares the join condition allocate the expression to the left operator and right operator
	 * @param left left operator
	 * @param right right operator
	 * @param joinCon
	 * @return the map contains the list of columns for each operator.
	 */
	public static Map<String,List<Column>> parseJoin(Operator left, Operator right, Expression joinCon) {
		Map<String,List<Column>> res = new HashMap<>();
		res.put("left", new ArrayList<Column>());
		res.put("right", new ArrayList<Column>());
		List<String> schema_left = left.getUniqueSchema();
		List<String> schema_right = right.getUniqueSchema();
		List<Expression> RemoveAnds = splitWhere(joinCon);
		for(Expression exp : RemoveAnds) {
			Column col_left = (Column) ( ((BinaryExpression)exp).getLeftExpression() );
			Column col_right = (Column) ( ((BinaryExpression)exp).getRightExpression() );
			
			String leftColumnName = Tools.rebuildWholeColumnName(col_left);
			String rightColumnName = Tools.rebuildWholeColumnName(col_right);
			
			//just a simple implementation, may have bugs
			
			if( schema_left.indexOf(leftColumnName) != -1 ) {
				res.get("left").add(col_left);
				res.get("right").add(col_right);
			} else {
				res.get("right").add(col_left);
				res.get("left").add(col_right);
			}
			
		}
		return res;
	}
	/**
	 * get the final Expression for a table.
	 * @param exps the expression list related to a table
	 * @return the final Expression for a table.
	 */
	private Expression rebuildExpression(List<Expression> exps) {
		if( exps.size() == 0 ) return null;
		Expression res = exps.get(0);
		for ( int i = 1; i< exps.size(); i++ ) {
			res = new AndExpression(res,exps.get(i));
		}
		return res;
	}
	
	/**
	 * get the most right table ID from the from list.
	 * @param relateTable the list of table we need to distinguish
	 * @return the final Expression for a table.
	 */
	private int getRightTableId(List<String> relateTable) {

		int id = 0;
		if( relateTable.size()==0 )
			return this.froms.size()-1;
		for(String table:relateTable) {
			id = Math.max(id, this.froms.indexOf(table));
		}
		return id;
	}
	
	/**
	 * get the related select expression of a table.
	 * @param tabName the table name
	 * @return the select Expression for a table.
	 */
	public Expression getSelExp(String tabName) {
		return selcon.get(tabName);
	}
	
	/**
	 * get the related join expression of a table.
	 * @param tabName the table name
	 * @return the join Expression for a table.
	 */	
	public Expression getJoinExp(String tabName) {
		return joincon.get(tabName);
	}
	
	/**
	 * Create ParseWhere class from the list of table names and the where expression.
	 * @param froms the list of table names
	 * @param whereExpression expression from where
	 */
	public ParseWhere( List<String> froms, Map<String, MyTable> from_map, Expression whereExpression) {
		this.froms = froms;
		this.from_map = from_map;
		this.optimizedFroms = new ArrayList<>();
		this.joinState = new HashMap<>();
		List<Expression> exps = splitWhere(whereExpression);
		Map<String,List<Expression>> tempselcon = new HashMap<>();
		Map<String,List<Expression>> tempjoincon = new HashMap<>();
		//UnionFind to parse the join
		this.ufv = new UnionFindVisitor();
		if (whereExpression != null)
			whereExpression.accept(ufv);
		//
		for(String name : froms) {
			tempselcon.put(name,new ArrayList<>());
			tempjoincon.put(name,new ArrayList<>());
		}
		for(Expression exp : exps) {
			List<String> relateTable = Tools.getRelativeTabAlias(exp);

			if( relateTable.size() == 1 ) {
				tempselcon.get(froms.get(getRightTableId(relateTable))).add(exp);
			}else if( relateTable.size() == 2 ){
				
				 //---------------- add TablePairs -------------------------------------------------//
				
				TablePairs tp = new TablePairs(relateTable.get(0),relateTable.get(1));
				if(joinState.containsKey(tp)) {
					joinState.get(tp).add(exp);
				}else {
					List<Expression> temp = new ArrayList<>();
					temp.add(exp);
					joinState.put(tp, temp);
				}
				//--------------------------------------------------------------------------------//
				
				//tempjoincon.get(froms.get(getRightTableId(relateTable))).add(exp);
				Column left_c = (Column)(((BinaryExpression)exp).getLeftExpression());
				Column right_c = (Column)(((BinaryExpression)exp).getRightExpression());
				UnionFindElement left = ufv.getUnionFind().find(left_c);
				UnionFindElement right = ufv.getUnionFind().find(right_c);
				if(left != null) {
					int id = froms.indexOf(relateTable.get(0));
					if(left.getEqualityConstraint() != null) {
						EqualsTo et = new EqualsTo(left_c, new LongValue(left.getLowerBound().toString()));
						tempselcon.get(froms.get(id)).add(et);
					}else {						
						if(left.getLowerBound() != null) {
							GreaterThanEquals gte = new GreaterThanEquals(left_c, new LongValue(left.getLowerBound().toString()));
							tempselcon.get(froms.get(id)).add(gte);
						}
						if(left.getUpperBound() != null) {
							MinorThanEquals mte = new MinorThanEquals(left_c, new LongValue(left.getUpperBound().toString()));
							tempselcon.get(froms.get(id)).add(mte);
						}
					}
				}
				if (right != null) {
					int id = froms.indexOf(relateTable.get(1));
					if(right.getEqualityConstraint() != null) {
						EqualsTo et = new EqualsTo(right_c, new LongValue(right.getLowerBound().toString()));
						tempselcon.get(froms.get(id)).add(et);
					}else {						
						if(right.getLowerBound() != null) {
							GreaterThanEquals gte = new GreaterThanEquals(right_c, new LongValue(right.getLowerBound().toString()));
							tempselcon.get(froms.get(id)).add(gte);
						}
						if(right.getUpperBound() != null) {
							MinorThanEquals mte = new MinorThanEquals(right_c, new LongValue(right.getUpperBound().toString()));
							tempselcon.get(froms.get(id)).add(mte);
						}
					}
				}
			}
		}
		
		/******
		 * implement optimization
		 *****************/
		this.optimizedFroms = new ArrayList<>(this.froms);
		tableStat[] initial = initialSize(tempselcon);
		evalTable et = new evalTable();
		backtrack(initial, et);
		this.froms = new ArrayList(optimizedFroms);
		for(Expression exp : exps) {
			List<String> relateTable = Tools.getRelativeTabAlias(exp);
			if( relateTable.size() == 0 ) {
				tempjoincon.get(this.froms.get(getRightTableId(relateTable))).add(exp);
			}else if( relateTable.size() == 2 ){
				tempjoincon.get(this.froms.get(getRightTableId(relateTable))).add(exp);
			}
		}
		
		
		//--------------------------------------------------------------------//
		this.selcon = new HashMap<>();
		this.joincon = new HashMap<>();
		for(String from:this.froms) {
			
			//rebuild sel expression
			List<Expression> sel_exps = tempselcon.get(from);
			List<Expression> rebuild_exps = new ArrayList<>();
			Set<MyColumn> hs = new HashSet<>();
			for(Expression exp : sel_exps) {
				BinaryExpression be = (BinaryExpression)exp;
				Expression l = be.getLeftExpression();
				Expression r = be.getRightExpression();
				if( l instanceof Column && r instanceof Column) {
					rebuild_exps.add(exp);
					continue;
				}
				Column c = (Column)(l instanceof Column ? (Column)l : (Column)r);
				UnionFindElement ufe = ParseWhere.ufv.getUnionFind().find(c);	
				if(!hs.add(new MyColumn(c))) continue;
				if(ufe.getEqualityConstraint() != null) {
					int val = ufe.getEqualityConstraint();
					rebuild_exps.add(new EqualsTo(c,new LongValue((long)val)));
				} else {
					if(ufe.getLowerBound() != null) {
						int val = ufe.getLowerBound();
						rebuild_exps.add(new GreaterThanEquals(c,new LongValue((long)val)));
					}
					if(ufe.getUpperBound() != null) {
						int val = ufe.getUpperBound();
						rebuild_exps.add(new MinorThanEquals(c,new LongValue((long)val)));
					}
				}			
			}
			//this.selcon.put(from, rebuildExpression(tempselcon.get(from)));
			this.selcon.put(from, rebuildExpression(rebuild_exps));
			this.joincon.put(from, rebuildExpression(tempjoincon.get(from)));
		}
	}
	
	/**
	 * return the optimized order
	 */
	private void backtrack(tableStat[] ts,evalTable et) {
		for(int i = 0; i < ts.length; i++) {
			if(et.tables.add(ts[i].name)) {
				int insertPos = et.optimizeOrder.size();
				if(et.optimizeOrder.size() == 0) {
					
					et.size.add(ts[i].tuple_numbers);
					et.optimizeOrder.add(ts[i]);
					
					backtrack(ts,et);
												
					et.optimizeOrder.remove(insertPos);
					et.size.remove(insertPos);
					et.tables.remove(ts[i].name);

				}
				else if(et.optimizeOrder.size()== froms.size()-1) {
					et.tables.remove(ts[i].name);
					long size = 0;
					for(Long num : et.size)
						size += num;

					if(size < min_size) {

						this.optimizedFroms.clear();
						List<String> temp = new ArrayList<>();
						for(tableStat t: et.optimizeOrder) {
							temp.add(t.name);
						}
						temp.add(ts[i].name);
						this.optimizedFroms = new ArrayList(temp);
						min_size = size;
						
						return;
					}
				} else {					
						long addsize = et.size.get(insertPos-1) * ts[i].tuple_numbers;
						//------------calculate the intermediate size----------------
						for(tableStat t: et.optimizeOrder) {
							TablePairs tp = new TablePairs(t.name, ts[i].name);
							List<Expression> exps = joinState.get(tp);							
							if(exps != null)
								for(Expression exp : exps) {
									if(exp instanceof EqualsTo) {
										EqualsTo eqt = (EqualsTo)exp;
										Column left = (Column)(eqt.getLeftExpression());
										Column right = (Column)(eqt.getRightExpression());
										String left_name = Tools.rebuildWholeColumnName(left);
										String right_name = Tools.rebuildWholeColumnName(right);
										if(t.stat.containsKey(left_name)) {
											pair p_left = t.stat.get(left_name);
											long range_left = p_left.high - p_left.low + 1;
											pair p_right = ts[i].stat.get(right_name);
											long rang_right = p_right.high - p_right.low + 1;
											long Max_range = Math.max(range_left, rang_right);
											addsize = (int)(addsize /(double)Max_range);
											//et.size = temp_size;
										}else {
											pair p_left = t.stat.get(right_name);
											long range_left = p_left.high - p_left.low + 1;
											pair p_right = ts[i].stat.get(left_name);
											long rang_right = p_right.high - p_right.low + 1;
											long Max_range = Math.max(range_left, rang_right);
											addsize = (int)(addsize /(double)Max_range);
											//et.size = temp_size;
										}
									}
								}
						}
						//--------------------------------------------------
						et.size.add(addsize);
						et.optimizeOrder.add(ts[i]);
						backtrack(ts, et);
						et.optimizeOrder.remove(insertPos);
						et.size.remove(insertPos);
						
					et.tables.remove(ts[i].name);
				}
			}
		}
	}
	public List<String> getOptimizeFroms(){
		return this.froms;
	}
	/**
	 * initialize the size of tables in froms
	 */
	tableStat[] initialSize(Map<String,List<Expression>> tempselcon) {
		tableStat[] initial = new tableStat[froms.size()];
		for(int i = 0; i < froms.size(); i++) {
			//------------------------------------
			//initialize value for each table
			//------------------------------------
			statsInfo si = Catalog.stats.get(from_map.get(froms.get(i)).getFullTableName());
			initial[i] = new tableStat();
			initial[i].name = froms.get(i);
			initial[i].tuple_numbers = si.n;
			List<String> schema = Catalog.schema_map.get(from_map.get(froms.get(i)).getFullTableName());
			for(int j = 0; j < schema.size(); j++) {
				String colName = initial[i].name+"."+schema.get(j);
				initial[i].stat.put(colName, new pair(si.mi[j],si.ma[j]));
			}
			
			//change the table value according to select condition
			List<Expression> exps = tempselcon.get(froms.get(i));
			for(Expression exp : exps) {
				if(!(exp instanceof BinaryExpression)) continue;
				BinaryExpression be = (BinaryExpression)exp;
				long tuple_number = initial[i].tuple_numbers;
				Expression left = be.getLeftExpression();
				Expression right = be.getRightExpression();
				if((left instanceof Column && right instanceof LongValue) || (right instanceof Column && left instanceof LongValue)) {
					String colName = left instanceof Column ? Tools.rebuildWholeColumnName((Column)left):Tools.rebuildWholeColumnName((Column)right);
					long value = left instanceof LongValue ? ((LongValue)left).getValue():((LongValue)right).getValue();
					pair temp = initial[i].stat.get(colName);			
					if(temp == null) {
						throw new RuntimeException("cannot find pairs");
					}
					
					long origin_range = temp.high - temp.low + 1;
					
					if(value > temp.high || value < temp.low)
						continue;
					//col < value
					if( (left instanceof LongValue && exp instanceof GreaterThan) || (right instanceof LongValue && exp instanceof MinorThan) ) {
						temp.high = value - 1;
					}
					//col <= value
					else if( (left instanceof LongValue && exp instanceof GreaterThanEquals ) || (right instanceof LongValue && exp instanceof MinorThanEquals) ) {
						temp.high = value;
					}
					//col > value
					else if( (right instanceof LongValue && exp instanceof GreaterThan ) || (left instanceof LongValue && exp instanceof MinorThan) ) {
						temp.low = value + 1;
					}
					// col >= value
					else if( (right instanceof LongValue && exp instanceof GreaterThanEquals ) || (left instanceof LongValue && exp instanceof MinorThanEquals) ) {
						temp.low = value;
					}
					//col == value
					else if( exp instanceof EqualsTo ) {
						temp.low = value;
						temp.high = value;
					}
					else {
						throw new UnsupportedException();
					}
					
					//update tuple numbers
					double radio = (temp.high - temp.low+1)/(double)origin_range;
					initial[i].tuple_numbers = (int)(tuple_number*radio);
					initial[i].stat.put(colName, temp);
				}				
			}
		}
		return initial;
	}
	
}

class TablePairs { 
	String tableName1;
	String tableName2;
	
	public TablePairs(String tableName1, String tableName2) {
		// TODO Auto-generated constructor stub
		this.tableName1 = tableName1;
		this.tableName2 = tableName2;
	}
	@Override
	public boolean equals(Object arg0) {
		if(!(arg0 instanceof TablePairs)) return false;
		TablePairs tp = (TablePairs)arg0;
		return (this.tableName1.equals(tp.tableName2)&&this.tableName2.equals(tp.tableName1))
				||
				(this.tableName2.equals(tp.tableName2)&&this.tableName1.equals(tp.tableName1));
	}
	@Override
	public int hashCode() {
		int hash1 = tableName1.hashCode();
		int hash2 = tableName2.hashCode();
		return hash1^hash2;
	}
	
}


class evalTable {
	List<tableStat> optimizeOrder;
	Set<String> tables;
	List<Long> size;
	public evalTable() {
		this.size = new LinkedList<>();
		this.optimizeOrder = new ArrayList<>();
		this.tables = new HashSet<>();
	}
	public int getTableNumbers() {
		return optimizeOrder.size();
	}
}

class tableStat {
	String name;
	long tuple_numbers;
	Map<String,pair> stat = new HashMap<>(); 	
}

class pair{
	long low;
	long high;
	public pair(long low,long high) {
		this.low = low;
		this.high = high;
	}
}
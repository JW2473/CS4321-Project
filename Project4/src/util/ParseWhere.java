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

import java.util.*;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * ParserWhere class get the expression for each table by parsing where expression.
 *
 */
public class ParseWhere {
	private List<String> froms;
	private Map<String, Expression> selcon;
	private Map<String, Expression> joincon;
	/**
	 * split Expression from 'Where' to non and Expressions.
	 * @param exp the Expression get from where
	 * @return the list of expression from where
	 */
	private static List<Expression> splitWhere(Expression exp) {
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
			return froms.size()-1;
		for(String table:relateTable) {
			id = Math.max(id, froms.indexOf(table));
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
	public ParseWhere( List<String> froms, Expression whereExpression) {
		this.froms = froms;
		List<Expression> exps = splitWhere(whereExpression);
		Map<String,List<Expression>> tempselcon = new HashMap<>();
		Map<String,List<Expression>> tempjoincon = new HashMap<>();

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
	}
}
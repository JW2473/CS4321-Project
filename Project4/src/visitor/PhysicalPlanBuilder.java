package visitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import logicaloperators.*;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import physicaloperators.BlockNestedJoinOperator;
import physicaloperators.DuplicateEliminationOperator;
import physicaloperators.ExternalSortOperator;
import physicaloperators.InMemorySortOperator;
import physicaloperators.IndexScanOperator;
import physicaloperators.Operator;
import physicaloperators.ProjectOperator;
import physicaloperators.ScanOperator;
import physicaloperators.SelectOperator;
import physicaloperators.SortMergeJoinOperator;
import physicaloperators.TupleNestedLoopJoinOperator;
import util.Catalog;
import util.MyColumn;
import util.ParseWhere;
import util.Tools;
import util.UnionFindElement;;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * build the physical plan using visitor pattern
 *
 */
public class PhysicalPlanBuilder {
	
	Operator op;
	int layer = 0;
	StringBuilder sb = new StringBuilder();
	public String logic_plan = "";
	boolean logic_join_status = false;
	/**
	 * Get the physical scan operator
	 */
	public void visit(LogicScanOperator scanop) {
		//scanop.print();
		logic_plan += scanop;
		op = new ScanOperator(scanop.mt);
		op.setLayer(scanop.real_layer);
		sb.insert(0, op.toString());
	}
	
	/**
	 * Get the physical selection operator
	 */
	public void visit(LogicSelectOperator selop) {
		//selop.print();
		logic_plan += selop;
		int size = sb.toString().split("\n").length;
		String scan = "";
		LogicScanOperator child  = (LogicScanOperator)(selop.getChild());
		child.setLayer(selop.layer + 1);
		child.setRealLayer(selop.real_layer + 1);
		ScanOperator so = null;
		Set<MyColumn> colSet = new HashSet<>();
		List<Expression> exps = ParseWhere.splitWhere(selop.getExpr());
		float minCost = Float.MAX_VALUE;
		for(Expression exp : exps) {
			BinaryExpression be = (BinaryExpression) exp;
			Expression l = be.getLeftExpression();
			Expression r = be.getRightExpression();
			Column c = (Column)(l instanceof Column ? (Column)l : (Column)r);
			String tableName = Catalog.getTableFullName(c.getTable().getName());
			String colName = c.getColumnName();
			if (Catalog.indexInfo.get(tableName).allIndice().indexOf(colName) == -1) continue;
			if (colSet.add(new MyColumn(c))) {
				UnionFindElement ufe = ParseWhere.ufv.getUnionFind().find(c);
				int range = (ufe.getUpperBound() == null ? Tools.getUpperBound(c) : ufe.getUpperBound())
						  - (ufe.getLowerBound() == null ? Tools.getLowerBound(c) : ufe.getLowerBound()) + 1;
				int totalRange = Tools.getUpperBound(c) - Tools.getLowerBound(c) + 1;
				int totalCount = Tools.getTupleNumbers(tableName);
				int tupleSize = Catalog.schema_map.get(tableName).size() * 4;
				int pageNum = (int) Math.ceil((float) totalCount * tupleSize / Catalog.pageSize);
				int leafPageNum = Catalog.indexInfo.get(tableName).leafPageNum(colName);
				int root2Leaf = 3;
				boolean isClustered = Catalog.indexInfo.get(tableName).isClustered();
				float reductionFactor = (float) range / totalRange;
				float cost = isClustered ? root2Leaf + pageNum * reductionFactor
						   				 : root2Leaf + (leafPageNum + totalCount) * reductionFactor;
//				System.out.println(cost);
				if (pageNum < minCost) {
					minCost = pageNum;
					so = null;
				}
				if (cost < minCost) {
//					System.out.println(colName);
					minCost = cost;
					so = new IndexScanOperator(child.mt, colName, ufe.getLowerBound(), ufe.getUpperBound());
					so.setLayer(selop.real_layer + 1);
					scan = so.toString();
				}
			}
		}
		if (so == null) {
			child.accept(this);
			so = (ScanOperator) op;
			so.setLayer(selop.real_layer + 1);
			//scan = so.toString();
		}
		int cur_size = sb.toString().split("\n").length;
		op = new SelectOperator(so, selop.getExpr());
		op.setLayer(selop.real_layer);
		String sel = op.toString();
		if(cur_size == size) {
			sb.insert(0, scan);
		}
		
		sb.insert(0, sel);


		
	}
	
	/**
	 * Get the physical project operator
	 */
	public void visit(LogicProjectOperator lpo) {
		op = null;
	//	lpo.print();
		logic_plan += lpo;
		lpo.getChild().setLayer(lpo.layer+1);
		lpo.getChild().setRealLayer(lpo.real_layer + 1);
		lpo.getChild().accept(this);
		op = new ProjectOperator(lpo.getSi(), op);
		op.setLayer(lpo.real_layer);
		sb.insert(0, op.toString());
	}
	
	/**
	 * Get the physical sort operator
	 */
	public void visit(LogicSortOperator sortop) {
		op = null;
		//sortop.print();
		logic_plan += sortop;
		sortop.getChild().setLayer(sortop.layer+1);
		sortop.getChild().setRealLayer(sortop.real_layer + 1);
		sortop.getChild().accept(this);
		switch (Catalog.sortConfig) {
			case Catalog.IMS:
				op = new InMemorySortOperator(op,sortop.getObe());
				op.setLayer(sortop.real_layer);
				sb.insert(0, op.toString());
				break;
			case Catalog.EMS:
				op = new ExternalSortOperator(op,sortop.getObe());
				op.setLayer(sortop.real_layer);
				sb.insert(0, op.toString());
				break;
			default:
				throw new UnsupportedException();
		}
	}
	
	/**
	 * Get the physical distinct operator
	 */
	public void visit(LogicDuplicateEliminationOperator ldeo) {
		op = null;
	//	ldeo.print();
		logic_plan += ldeo;
		ldeo.getChild().setLayer(ldeo.layer+1);
		ldeo.getChild().setRealLayer(ldeo.real_layer+1);
		ldeo.getChild().accept(this);
		op = new DuplicateEliminationOperator(op);
		sb.insert(0,op.toString());
	}
	
	/**
	 * Get the physical join operator
	 */
	public void visit(LogicJoinOperator ljo) {
		int next_layer = ljo.layer;
		if(!logic_join_status) {
			logic_join_status = true;
			//ljo.print();
			logic_plan += ljo;
			next_layer++;
		}
		
		Expression e = ljo.getExpr();
		int Method = Catalog.SMJ;
		List<Expression> exps = ParseWhere.splitWhere(e);
		for(Expression exp : exps) {
			if (!(exp instanceof EqualsTo)) {
				Method = Catalog.BNLJ;
				break;
			}
		}
		if( exps.size() == 0 )
			Method = Catalog.BNLJ;
		
		int plus = 1;
		if(Method == Catalog.SMJ )
			plus = 2;
		pair p = new pair();
		
//		op = null;
//		ljo.getRight().setLayer(next_layer);
//		ljo.getRight().setRealLayer(ljo.real_layer + plus);
//		ljo.getRight().accept(this);
//		p.right = op;
		
		op = null;
		ljo.getRight().setLayer(next_layer);
		ljo.getRight().setRealLayer(ljo.real_layer + plus);
		ljo.getRight().accept(this);
		p.right = op;
		
		op = null;
		ljo.getLeft().setLayer(next_layer);
		ljo.getLeft().setRealLayer(ljo.real_layer + plus);
		ljo.getLeft().accept(this);
		p.left = op;
		
		


		switch (Method) {
			case Catalog.TNLJ:
				op = new TupleNestedLoopJoinOperator(p.left,p.right,ljo.getExpr());
				op.setLayer(ljo.real_layer);
				sb.insert(0, op.toString());
				break;
			case Catalog.BNLJ:
				op = new BlockNestedJoinOperator(p.left, p.right, ljo.getExpr());
				op.setLayer(ljo.real_layer);
				sb.insert(0, op.toString());
				break;
			case Catalog.SMJ:
				Map<String,List<Column>> m = ParseWhere.parseJoin(p.left, p.right, ljo.getExpr());
				if(Catalog.sortConfig == Catalog.IMS ) {
					p.left = new InMemorySortOperator(p.left,m.get("left"));
					p.left.setLayer(ljo.real_layer + 1);
					
					p.right = new InMemorySortOperator(p.right,m.get("right"));
					p.right.setLayer(ljo.real_layer + 1);
					sb.insert(0, p.right.toString());
					
					sb.insert(0, p.left.toString());
				} else {
					p.left = new ExternalSortOperator(p.left,m.get("left"));
					p.left.setLayer(ljo.real_layer + 1);
					
					
					p.right = new ExternalSortOperator(p.right,m.get("right"));
					p.right.setLayer(ljo.real_layer + 1);
					sb.insert(0, p.right.toString());
					
					sb.insert(0, p.left.toString());
				}
				op = new SortMergeJoinOperator(p.left, p.right, ljo.getExpr(), m.get("left"), m.get("right"));
				op.setLayer(ljo.real_layer);
				sb.insert(0, op.toString());
				break;
			default:
				throw new UnsupportedException();			
		}
	}
	/**
	 * Get the final operator
	 */
	public Operator getOp() {
		return op;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return sb.toString();
	}
	
	public void dumpPhy_Plan(String fileName) {
		PrintStream ps = null;
		try {
			ps = new PrintStream(new File(Catalog.output + fileName + ".txt"));
			ps.println(this.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(ps != null)
			ps.close();
	}
	
	public void dumpLog_Plan(String fileName) {
		PrintStream ps = null;
		try {
			ps = new PrintStream(new File(Catalog.output + fileName + ".txt"));
			ps.println(logic_plan);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(ps != null)
			ps.close();
	}

}
/**
 * @author Yixin Cui
 * @author Haodong Ping
 * create a pair the class for storing the left operator and right operator for binary operators
 *
 */
class pair {
	Operator left;
	Operator right;

}

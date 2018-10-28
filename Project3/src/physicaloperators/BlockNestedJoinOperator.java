package physicaloperators;

import java.util.*;

import net.sf.jsqlparser.expression.Expression;
import util.Catalog;
import util.Tuple;
/**
 * @author Yixin Cui
 * @author Haodong Ping
 * BlockedNestedJoinOperator join two relations by using BNLJ
 *
 */
public class BlockNestedJoinOperator extends JoinOperator{
	
	List<Tuple> block = null;
	int blocksize = -1;
	int id = 0;
	Tuple t1,t2;
	
	/**
	 * read the tuples from the left operator into the block
	 */
	private void readBlock() {
		block.clear();
		int count = 0;
		Tuple tp = null;
		while(count < blocksize && (tp = left.getNextTuple()) != null) {
			block.add(tp);
			count++;
		}
		id = 0;
		t1 = getTuple();
		id++;
	}
	
	/**
	 * get the Tuple from the block
	 * @return the No.id Tuple in the block
	 */
	private Tuple getTuple() {
		return id < block.size() ? block.get(id) : null;
	}
	
	/**
	 * Get next tuple after join
	 * @return the next tuple
	 */
	@Override
	public Tuple getNextTuple() {
		Tuple t = null;
		while (t1 != null && t2 != null) {
			if (expr == null) 
				t = combineTuples(t1, t2);
			else {
				jv.readTuple(t1, t2);
				expr.accept(jv);
				if (jv.getCurStatus()) {
					t = combineTuples(t1, t2);
				}
			}
			this.nextPair();
			if (t != null) return t;
		}
		return null;
		
	}
	
	/**
	 * Set the tuples to next pair from the two relations
	 */
	@Override
	public void nextPair() {
		// TODO Auto-generated method stub
		if( t2 != null ) {
			t1 = getTuple();
			id++;
			if( t1 != null ) return;
			
			t2 = right.getNextTuple();
			if( t2  != null ) {
				id = 0;
				t1 = getTuple();
				id++;
				return;
			}
		}
		readBlock();
		right.reset();
		t2 = right.getNextTuple();				
	}
	
	/**
	 * Create the BNLJ operator
	 * @param left the left child operator
	 * @param right the right child operator
	 * @param expr the join condition
	 */
	public BlockNestedJoinOperator(Operator left, Operator right, Expression expr) {
		super(left, right, expr);
		int tuplesize = left.getUniqueSchema().size() * 4;
		blocksize = Catalog.joinBuffer * ( (Catalog.pageSize)/tuplesize );
//		System.out.println("blocksize:"+blocksize);
		block = new ArrayList<>();
		nextPair();
	}
}

package physicaloperators;

import java.util.*;

import net.sf.jsqlparser.expression.Expression;
import util.Catalog;
import util.Tuple;

public class BlockNestedJoinOperator extends JoinOperator{
	
	List<Tuple> block = null;
	int blocksize = -1;
	int id = 0;
	Tuple t1,t2;
	
	private void readBlock() {
		block.clear();
		int count = 0;
		Tuple tp = null;
		while(count < blocksize && (tp = left.getNextTuple()) != null) {
			block.add(tp);
			count++;
		}
		System.out.println("tuple number:"+block.size());
		id = 0;
		t1 = getTuple();
		id++;
	}
	
	private Tuple getTuple() {
		return id < block.size() ? block.get(id) : null;
	}
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
	
	public BlockNestedJoinOperator(Operator left, Operator right, Expression expr) {
		super(left, right, expr);
		int tuplesize = left.getUniqueSchema().size() * 4;
		blocksize = Catalog.joinBuffer * ( (Catalog.pageSize)/tuplesize );
		System.out.println("blocksize:"+blocksize);
		block = new ArrayList<>();
		nextPair();
	}
}

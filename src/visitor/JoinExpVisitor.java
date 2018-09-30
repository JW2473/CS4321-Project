package visitor;

import net.sf.jsqlparser.schema.Column;
import util.Tuple;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * JoinExpVisitor class contains two tuples
 *
 */
public class JoinExpVisitor extends ExpVisitor{
	
	Tuple t1, t2;
	
	/*
	 * Create a JoinExpVisitor object
	 */
	public JoinExpVisitor() {
		t1 = null;
		t2 = null;
	}
	
	/*
	 * Read in two tuples
	 * @param t1 the first tuple
	 * @param t2 the second tuple
	 */
	public void readTuple(Tuple t1, Tuple t2) {
		this.t1 = t1;
		this.t2 = t2;
	}
	
	/*
	 * Visit a column and send back the value of the column from the tuple
	 * @param arg0 the column
	 */
	@Override
	public void visit(Column arg0) {
		Long value = t1.getValue(arg0);
		if (value == null) {
			value = t2.getValue(arg0);
		}
		
		this.curValue = value;
	}

	
}

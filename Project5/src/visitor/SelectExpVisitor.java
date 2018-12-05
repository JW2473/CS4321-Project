package visitor;

import net.sf.jsqlparser.schema.Column;
import physicaloperators.Operator;
import util.Tuple;
import java.util.*;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * SelectExpVisitor class contains one tuple
 *
 */
public class SelectExpVisitor extends ExpVisitor {
	Tuple t;
	Operator op;
	List<String> schema;
	
	/**
	 * Create a SelectExpVisitor object
	 */
	public SelectExpVisitor(Operator op) {
		this.op = op;
		t = null;
	}
	
	/**
	 * Read in a tuple
	 * @param t the tuple to be read
	 */
	public void readTuple(Tuple t) {
		this.t = t;
	}
	
	/**
	 * Visit a column and send back the value of the column from the tuple
	 * @param arg0 the column
	 */
	@Override
	public void visit(Column arg0) {
		this.curValue = t.getValue(op.getUniqueSchema(), arg0);
	}

}

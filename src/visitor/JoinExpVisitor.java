package visitor;

import net.sf.jsqlparser.schema.Column;
import util.Tuple;

public class JoinExpVisitor extends ExpVisitor{
	
	Tuple t1, t2;
	
	public JoinExpVisitor() {
		t1 = null;
		t2 = null;
	}
	
	public void readTuple(Tuple t1, Tuple t2) {
		this.t1 = t1;
		this.t2 = t2;
	}
	
	@Override
	public void visit(Column arg0) {
		// TODO Auto-generated method stub
		Long value = t1.getValue(arg0);
		if (value == null) {
			value = t2.getValue(arg0);
		}
		
		this.curValue = value;
	}

	
}

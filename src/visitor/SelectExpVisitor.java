package visitor;

import net.sf.jsqlparser.schema.Column;
import util.Tuple;
import java.util.*;
public class SelectExpVisitor extends ExpVisitor {
	Tuple t;
	List<String> schema;
	
	public SelectExpVisitor() {
		t = null;
	}
	
	public void readTuple(Tuple t) {
		this.t = t;
	}
	@Override
	public void visit(Column arg0) {
		// TODO Auto-generated method stub
		this.curValue = t.getValue(arg0);
	}

}

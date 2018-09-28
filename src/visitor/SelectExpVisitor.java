package visitor;

import net.sf.jsqlparser.schema.Column;
import util.Tool;
import util.Tuple;
import java.util.*;
public class SelectExpVisitor extends GeneralExpVisitor {
	Tuple t;
	List<String> schema;
	
	public SelectExpVisitor(List<String> schema) {
		// TODO Auto-generated constructor stub
		this.schema = schema;
	}
	
	public void resetTuple(Tuple t) {
		this.t = t;
	}
	@Override
	public void visit(Column arg0) {
		// TODO Auto-generated method stub
		this.curValue = Tool.getValByColName(t, schema, arg0.getColumnName());
	}

}

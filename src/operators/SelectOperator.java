package operators;

import java.io.PrintStream;

import net.sf.jsqlparser.expression.Expression;
import util.Tuple;
import visitor.SelectExpVisitor;

public class SelectOperator extends Operator{

	ScanOperator child;
	Expression expr;
	SelectExpVisitor sv = new SelectExpVisitor();
	
	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		Tuple t;
		while ((t = child.getNextTuple()) != null) {
			if (expr == null) return t;
			sv.readTuple(t);
			expr.accept(sv);
			if (sv.getCurStatus()) {
				return t;
			}
		}
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		child.reset();
		
	}
	
	public SelectOperator(ScanOperator scanOp, Expression expr) {
		child = scanOp;
		this.expr = expr;
	}

	@Override
	public void dump(PrintStream ps) {
		// TODO Auto-generated method stub
		super.dump(ps);
	}
	
	

}

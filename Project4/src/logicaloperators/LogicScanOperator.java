package logicaloperators;

import util.MyTable;
import visitor.PhysicalPlanBuilder;
/**
 * @author Yixin Cui
 * @author Haodong Ping
 *  Logic Scan Operator class
 *
 */
public class LogicScanOperator extends LogicOperator {
	
	public MyTable mt;
	public LogicScanOperator(MyTable mt) {		
		this.mt = mt;
	}
	
	@Override
	public void accept(PhysicalPlanBuilder ppb) {
		ppb.visit(this);
	}

	@Override
	public void print() {

		for(int i = 0; i < this.layer; i++)
			System.out.print("-");
		System.out.print("Leaf");
		System.out.println("["+mt.getFullTableName()+"]");
	}

}

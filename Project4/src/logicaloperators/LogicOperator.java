package logicaloperators;

import visitor.PhysicalPlanBuilder;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * This is the abstract class of logic operators that determines the methods for all logic operators
 */
public abstract class LogicOperator {
	public int layer = 0;
	public int real_layer = 0;
	public void setLayer(int layer) {
		this.layer = layer;
	}
	
	public void setRealLayer(int real_layer) {
		this.real_layer = real_layer;
	}
	public abstract void accept(PhysicalPlanBuilder ppb);
	public abstract void print();
}

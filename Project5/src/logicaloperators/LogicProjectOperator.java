package logicaloperators;

import java.util.List;

import net.sf.jsqlparser.statement.select.SelectItem;
import visitor.PhysicalPlanBuilder;
/**
 * @author Yixin Cui
 * @author Haodong Ping
 *  Logic project Operator class
 *
 */
public class LogicProjectOperator extends LogicOperator {
	
	LogicOperator child;
	List<SelectItem> si;
	
	public LogicProjectOperator(LogicOperator child, List<SelectItem> si) {
		this.child = child;
		this.si = si;
	}
	@Override
	public void accept(PhysicalPlanBuilder ppb) {

		ppb.visit(this);
	}
	public LogicOperator getChild() {
		return child;
	}
	
	public List<SelectItem> getSi() {
		return si;
	}
	@Override
	public void print() {
		
		for(int i = 0; i < this.layer; i++)
			System.out.print("-");
		System.out.print("Project");

		System.out.println(si);
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < this.layer; i++)
			sb.append("-");
		sb.append("Project");

		sb.append(si+"\n");
		return sb.toString();
	}
	
}

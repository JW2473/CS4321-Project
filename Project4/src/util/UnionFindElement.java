package util;

import java.util.HashSet;

import net.sf.jsqlparser.schema.Column;

public class UnionFindElement {
	private HashSet<Column> ufe;
	private Integer lowerBound = null;
	private Integer upperBound = null;
	private Integer equalityConstraint = null;
	
	public UnionFindElement(Column col) {
		ufe = new HashSet<>();
		ufe.add(col);
	}
	
	public void addUfe(UnionFindElement ufe) {
		this.ufe.addAll(ufe.getUfe());
	}
	
	public HashSet<Column> getUfe() {
		return ufe;
	}

	public Integer getLowerBound() {
		return lowerBound;
	}
	
	public void setLowerBound(Integer lowerBound) {
		if (equalityConstraint == null)
			this.lowerBound = lowerBound;
	}
	
	public Integer getUpperBound() {
		return upperBound;
	}
	
	public void setUpperBound(Integer upperBound) {
		if (equalityConstraint == null)
			this.upperBound = upperBound;
	}
	
	public Integer getEqualityConstraint() {
		return equalityConstraint;
	}
	
	public void setEqualityConstraint(Integer equalityConstraint) {
		this.equalityConstraint = equalityConstraint;
		if (equalityConstraint != null) {
			this.lowerBound = equalityConstraint;
			this.upperBound = equalityConstraint;
		}
	}

	@Override
	public String toString() {
		return "UnionFindElement [ufe=" + ufe + ", lowerBound=" + lowerBound + ", upperBound=" + upperBound
				+ ", equalityConstraint=" + equalityConstraint + "]";
	}
	
}

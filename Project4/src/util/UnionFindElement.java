package util;

import java.util.HashSet;

/**
 * Union-find element class store columns and numeric constraints
 * @author Yixin Cui
 *
 */
public class UnionFindElement {
	private HashSet<MyColumn> ufe;
	private Integer lowerBound = null;
	private Integer upperBound = null;
	private Integer equalityConstraint = null;
	
	public UnionFindElement(MyColumn col) {
		ufe = new HashSet<>();
		ufe.add(col);
	}
	
	/**
	 * Merge two union-find elements
	 * @param ufe the union-find element needs to be merged
	 */
	public void addUfe(UnionFindElement ufe) {
		this.ufe.addAll(ufe.getUfe());
	}
	
	/**
	 * @return all the columns in the union-find element
	 */
	public HashSet<MyColumn> getUfe() {
		return ufe;
	}

	/**
	 * @return lower bound of the union-find element
	 */
	public Integer getLowerBound() {
		return lowerBound;
	}
	
	/**
	 * Set the lower bound of the union-find element
	 * @param lowerBound the lower bound value
	 */
	public void setLowerBound(Integer lowerBound) {
		if (equalityConstraint == null)
			this.lowerBound = lowerBound;
	}
	
	/**
	 * @return upper bound of the union-find element
	 */
	public Integer getUpperBound() {
		return upperBound;
	}
	
	/**
	 * Set the upper bound of the union-find element
	 * @param upperBound the upper bound value
	 */
	public void setUpperBound(Integer upperBound) {
		if (equalityConstraint == null)
			this.upperBound = upperBound;
	}
	
	/**
	 * @return equality constraint of the union-find element
	 */
	public Integer getEqualityConstraint() {
		return equalityConstraint;
	}
	
	/**
	 * Set the equality constraint of the union-find element
	 * @param equalityConstraint the equality constraint value
	 */
	public void setEqualityConstraint(Integer equalityConstraint) {
		this.equalityConstraint = equalityConstraint;
		if (equalityConstraint != null) {
			this.lowerBound = equalityConstraint;
			this.upperBound = equalityConstraint;
		}
	}

	/**
	 * @return the string representation of the union-find element
	 * which contains columns and numeric constraints
	 */
	@Override
	public String toString() {
		return "[" + ufe + ", equals " + equalityConstraint + ", min " + lowerBound + ", max " + upperBound
				+"]";
	}
	
}

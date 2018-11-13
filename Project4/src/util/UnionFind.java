package util;

import java.util.HashMap;
import java.util.Map;

import net.sf.jsqlparser.schema.Column;

public class UnionFind {
	private Map<Column, Column> parentMap;
	private Map<Column, UnionFindElement> unionMap; 
	
	
	public UnionFind() {
		parentMap = new HashMap<>();
		unionMap = new HashMap<>();
	}
	
	public UnionFindElement find(Column col) {
		if (unionMap.get(col) == null) {
			parentMap.put(col, col);
			UnionFindElement ufe = new UnionFindElement(col);
			unionMap.put(col, ufe);
			return ufe;
		}else {
			Column root = findParent(col);
			return unionMap.get(root);
		}
	}
	
	public void join(UnionFindElement ufe1, UnionFindElement ufe2) {
		Column root1 = findParent(ufe1.getUfe().iterator().next());
		Column root2 = findParent(ufe2.getUfe().iterator().next());
		if (!root1.equals(root2)) {
			parentMap.put(root1, root2);
			UnionFindElement newUfe1 = unionMap.get(root2);
			UnionFindElement newUfe2 = unionMap.get(root1);
			newUfe1.addUfe(newUfe2);
			newUfe1.setEqualityConstraint(newEqualityConstraint(newUfe1, newUfe2));
			newUfe1.setLowerBound(newLowerBound(newUfe1, newUfe2));
			newUfe1.setUpperBound(newUpperBound(newUfe1, newUfe2));
		}
	}
	
	public void setLowerBound(UnionFindElement ufe, Integer lowerBound) {
		ufe.setLowerBound(lowerBound);
	}
	
	public void setUpperBound(UnionFindElement ufe, Integer upperBound) {
		ufe.setUpperBound(upperBound);
	}
	
	public void setEqualityConstraint(UnionFindElement ufe, Integer equalityConstraint) {
		ufe.setEqualityConstraint(equalityConstraint);
	}
	
	private Column findParent(Column col) {
		if (parentMap.get(col) == col) {
			return col;
		}else {
			return findParent(parentMap.get(col));
		}
	}
	
	private Integer newEqualityConstraint(UnionFindElement ufe1, UnionFindElement ufe2) {
		if (ufe1.getEqualityConstraint() == null | ufe2.getEqualityConstraint() == null) {
			return ufe1.getEqualityConstraint() == null ? ufe2.getEqualityConstraint() : ufe1.getEqualityConstraint();
		}else {
			assert(ufe1.getEqualityConstraint() == ufe2.getEqualityConstraint());
			return ufe1.getEqualityConstraint();
		}
	}
	
	private Integer newLowerBound(UnionFindElement ufe1, UnionFindElement ufe2) {
		if (ufe1.getLowerBound() == null | ufe2.getLowerBound() == null) {
			return ufe1.getLowerBound() == null ? ufe2.getLowerBound() : ufe1.getLowerBound();
		}else {
			return ufe1.getLowerBound() < ufe2.getLowerBound() ? ufe2.getLowerBound() : ufe1.getLowerBound();
		}
	}

	private Integer newUpperBound(UnionFindElement ufe1, UnionFindElement ufe2) {
		if (ufe1.getUpperBound() == null | ufe2.getUpperBound() == null) {
			return ufe1.getUpperBound() == null ? ufe2.getUpperBound() : ufe1.getUpperBound();
		}else {
			return ufe1.getUpperBound() < ufe2.getUpperBound() ? ufe1.getUpperBound() : ufe2.getUpperBound();
		}
	}
}

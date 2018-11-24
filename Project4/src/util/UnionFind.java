package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.sf.jsqlparser.schema.Column;

public class UnionFind {
	private Map<MyColumn, MyColumn> parentMap;
	private Map<MyColumn, UnionFindElement> unionMap; 
	
	
	public UnionFind() {
		parentMap = new HashMap<>();
		unionMap = new HashMap<>();
	}
	
	public UnionFindElement find(Column col) {
		MyColumn mCol = new MyColumn(col);
		if (unionMap.get(mCol) == null) {
			parentMap.put(mCol, mCol);
			UnionFindElement ufe = new UnionFindElement(mCol);
			unionMap.put(mCol, ufe);
			return ufe;
		}else {
			MyColumn root = findParent(mCol);
			return unionMap.get(root);
		}
	}
	
	public void join(UnionFindElement ufe1, UnionFindElement ufe2) {
		MyColumn root1 = findParent(ufe1.getUfe().iterator().next());
		MyColumn root2 = findParent(ufe2.getUfe().iterator().next());
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
	
	@Override
	public String toString() {
		HashSet<UnionFindElement> set = new HashSet<>();
		for (MyColumn col : parentMap.keySet()) {
			
			set.add(unionMap.get(findParent(col)));
		}
		String str = "";
		for (UnionFindElement ufe : set) {
			str = str + ufe.toString() + "\n";
		}
		return str;
	}
	
	private MyColumn findParent(MyColumn col) {
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

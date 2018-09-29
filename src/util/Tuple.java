package util;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Column;

public class Tuple {
	List<Integer> value = new ArrayList<>();
	int size;
	String tableName;

	public Tuple(String[] val, String tableName) {
		this.tableName = tableName;
		size = val.length;
		for (int i = 0; i < val.length; i++) {
			this.value.add(Integer.valueOf(val[i]));
		}
	}
	
	public int getVal(int id) {
		return this.value.get(id);
	}
	
	@Override
	public String toString() {
		return value.toString().replaceAll("\\[", "").replaceAll("\\]", " ");
	}
	
	public long getValue(Column c) {
		if (c.getTable().getWholeTableName().equals(tableName)) {
			return value.get(Catalog.getIndex(tableName, c.getWholeColumnName().split("\\.")[1]));
		}
		return -1;
	}
	
}

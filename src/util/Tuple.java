package util;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.schema.Column;

public class Tuple {
	List<Long> value = new ArrayList<>();
	int size;
	String tableName;

	public Tuple(String[] val, String tableName) {
		this.tableName = tableName;
		size = val.length;
		for (int i = 0; i < val.length; i++) {
			this.value.add(Long.valueOf(val[i]));
		}
	}
	
	public Tuple(List<Long> value) {
		this.tableName = null;
		size = value.size();
		this.value = value;
	}
	
	public String getTableName() {
		return this.tableName;
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

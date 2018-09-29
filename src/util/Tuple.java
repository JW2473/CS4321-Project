package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.schema.Column;

public class Tuple {
	List<Long> value = new ArrayList<>();
	HashMap<String, Integer> schemaIndex = new HashMap<>();
	int size;
	String tableName;

	public Tuple(String[] val, String tableName) {
		this.tableName = tableName;
		size = val.length;
		for (int i = 0; i < val.length; i++) {
			this.value.add(Long.valueOf(val[i]));
		}
	}
	
	public Tuple(List<Long> value, List<String> schemas) {
		this.tableName = null;
		size = value.size();
		this.value = value;
		for (int i = 0; i < schemas.size(); i++) {
			schemaIndex.put(schemas.get(i), i);
		}
	}
	
	public String getTableName() {
		return this.tableName;
	}
	
	@Override
	public String toString() {
		return value.toString().replaceAll("\\[", "").replaceAll("\\]", " ");
	}
	
	public Long getValue(Column c) {
		try {
			if (tableName != null) {
				return value.get(Catalog.getIndex(tableName, c.getWholeColumnName().split("\\.")[1]));
			}else {
				String colName = c.getWholeColumnName().split("\\.")[1];
				return value.get(schemaIndex.get(colName));
			}
		}catch (Exception e) {
			return null;
		}
	}
	
}

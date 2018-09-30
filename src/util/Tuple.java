package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.schema.Column;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * The Tuple class store the data of the tuple of an original table or a temperate tuple during the process
 *
 */
public class Tuple {
	List<Long> value = new ArrayList<>();
	HashMap<String, Integer> schemaIndex = new HashMap<>();
	int size;
	String tableName;

	/*
	 * Create a new tuple from an original table in the db directory
	 * @param val the array of tuple data as string
	 * @param tableName the name of the table
	 * @return the tuple in the table
	 */
	public Tuple(String[] val, String tableName) {
		this.tableName = tableName;
		size = val.length;
		for (int i = 0; i < val.length; i++) {
			this.value.add(Long.valueOf(val[i]));
		}
	}
	
	/*
	 * Create a new tuple from a returned long value list and its corresponding schemas list
	 * It maps from column reference to indexes by using the hashmap
	 * @param value the list of the tuple's data returned by another operator
	 * @param schemas the list of schemas returned by another operator
	 * @return the newly created tuple that has been processed by the operator
	 */
	public Tuple(List<Long> value, List<String> schemas) {
		this.tableName = null;
		size = value.size();
		this.value = value;
		for (int i = 0; i < schemas.size(); i++) {
			schemaIndex.put(schemas.get(i), i);
		}
	}
	
	/*
	 * Get the name of the table
	 * @return the name of the table
	 */
	public String getTableName() {
		return this.tableName;
	}
	
	/*
	 * Override toString() to format the output string
	 * @return the formatted string
	 */
	@Override
	public String toString() {
		return value.toString().replaceAll("\\[", "").replaceAll("\\]", " ");
	}
	
	/*
	 * Get the specified value in the tuple according to the Column object in the query
	 * It checks whether the tuple is in the original table or a temperate tuple and calls corresponding functions
	 * @param c the column object in the query
	 * @return the value in that tuple
	 */
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

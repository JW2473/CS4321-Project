package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
	String uniqueName;

	/**
	 * Create a new tuple from an original table in the db directory
	 * @param val the array of tuple data as string
	 * @param uniqueName the unique identity name of the table
	 * @param tableFullName the full name of the table
	 * @return the tuple in the table
	 */
	public Tuple(long[] val, String uniqueName, String tableFullName) {
		this.uniqueName = uniqueName;
		size = val.length;
		List<String> s = Tools.InitilaizeWholeColumnName(uniqueName, tableFullName);
		for (int i = 0; i < s.size(); i++) {
			schemaIndex.put(s.get(i), i);
		}
		for (int i = 0; i < val.length; i++) {
			this.value.add(val[i]);
		}
	}
	
	/**
	 * Create a new tuple from a returned long value list and its corresponding schemas list
	 * It maps from column reference to indexes by using the hashmap
	 * @param value the list of the tuple's data returned by another operator
	 * @param schemas the list of schemas returned by another operator
	 * @return the newly created tuple that has been processed by the operator
	 */
	public Tuple(List<Long> value, List<String> schemas) {
		this.uniqueName = null;
		size = value.size();
		this.value = value;
		for (int i = 0; i < schemas.size(); i++) {
			schemaIndex.put(schemas.get(i), i);
		}
	}
	
	/**
	 * Create a new tuple from a returned long value list and its corresponding schemas list
	 * It maps from column reference to indexes by using the hashmap
	 * @param value the list of the tuple's data returned by another operator
	 * @param schemas the list of schemas returned by another operator
	 * @return the newly created tuple that has been processed by the operator
	 */
	public Tuple(long[] value, List<String> schemas) {
		this.uniqueName = null;
		size = value.length;
		for (long val : value) {
			this.value.add(val);
		}
		for (int i = 0; i < schemas.size(); i++) {
			schemaIndex.put(schemas.get(i), i);
		}
	}
	
	/**
	 * Return the schema list of the tuple
	 * @return the list of the schema in the tuple
	 */
	public List<String> getAllSchemas() {
		String[] schemas = new String[size];
		for (Entry<String, Integer> entry : schemaIndex.entrySet()) {
			schemas[entry.getValue()] = entry.getKey();
		}
		return new ArrayList<String>(Arrays.asList(schemas));
	}
	
	/**
	 * Return the data list of the tuple
	 * @return the list of the data in the tuple
	 */
	public List<Long> getAllColumn() {
		return value;
	}

	/**
	 * Get the number of columns of the tuple
	 * @return the columns number
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Get the unique identity name of the table
	 * @return the unique identity name of the table
	 */
	public String getUniqueName() {
		return this.uniqueName;
	}
	
	/**
	 * Override toString() to format the output string
	 * @return the formatted string
	 */
	@Override
	public String toString() {
		return value.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "");
	}
	
	/**
	 * Get the specified value in the tuple according to the Column object in the query
	 * It checks whether the tuple is in the original table or a temperate tuple and performs corresponding functions
	 * @param c the column object in the query
	 * @return the value in that tuple
	 */
	public Long getValue(Column c) {
		String wholeColumnName = Tools.rebuildWholeColumnName(c);
		try{
			return value.get(schemaIndex.get(wholeColumnName));	
		}catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * Override equals to check whether two tuples have exactly same value.
	 * @param the tuple need to be compared
	 * @return true if equals, false if not equal
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Tuple))
			return false;
		Tuple t = (Tuple)obj;
		if(t.size != this.size) return false;
		return t.value.equals(this.value);
	}
	
}

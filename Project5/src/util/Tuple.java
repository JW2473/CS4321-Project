package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.jsqlparser.schema.Column;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * The Tuple class store the data of the tuple of an original table or a temperate tuple during the process
 *
 */
public class Tuple {
	int i = 0;
	long[] value = null;

	/**
	 * Create a new tuple from an original table in the db directory
	 * @param val the array of tuple data as string
	 * @param uniqueName the unique identity name of the table
	 * @param tableFullName the full name of the table
	 * @return the tuple in the table
	 */
	public Tuple(long[] val) {
		this.value = val;
	}
	
	/**
	 * Create a new tuple from a returned long value list and its corresponding schemas list
	 * It maps from column reference to indexes by using the hashmap
	 * @param value the list of the tuple's data returned by another operator
	 * @param schemas the list of schemas returned by another operator
	 * @return the newly created tuple that has been processed by the operator
	 */
	public Tuple(List<Long> value) {
		int size = value.size();
		this.value = new long[size];
		for (int i = 0; i < value.size(); i++) {
			this.value[i] = value.get(i);
		}
	}

	/**
	 * Get the number of columns of the tuple
	 * @return the columns number
	 */
	public int getSize() {
		return value == null ? 0 : value.length;
	}
	
	/**
	 * Override toString() to format the output string
	 * @return the formatted string
	 */
	@Override
	public String toString() {
		return Arrays.toString(value).replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "");
	}
	
	/**
	 * Get the specified value in the tuple according to the Column object in the query
	 * It checks whether the tuple is in the original table or a temperate tuple and performs corresponding functions
	 * @param c the column object in the query
	 * @return the value in that tuple
	 */
	public Long getValue(List<String> schemas, Column c) {
		String wholeColumnName = Tools.rebuildWholeColumnName(c);
		try{
			return value[schemas.indexOf(wholeColumnName)];	
		}catch (NullPointerException e) {
			return null;
		}catch (ArrayIndexOutOfBoundsException aioobe) {
//			System.out.println(Arrays.toString(value) + "," + schemas + "," + wholeColumnName);
//			aioobe.printStackTrace();
			return null;
		}
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
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
		Tuple t = (Tuple) obj;
		if(t.getSize() != this.value.length) return false;
		return Arrays.equals(t.value, this.value);
	}

	public List<Long> getAllColumn() {
		List<Long> ret = new ArrayList<>();
		for (long val : value) {
			ret.add(val);
		}
		return ret;
	}
	
}

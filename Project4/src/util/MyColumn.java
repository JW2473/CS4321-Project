package util;

import net.sf.jsqlparser.schema.Column;

/**
 * Helper Class for generating MyColumn object from Column object
 * to enable the use of hashmap
 * @author Yixin Cui
 *
 */
public class MyColumn{

	private Column col;
	
	public MyColumn(Column col) {
		this.col = col;
	}
	
	/**
	 * @return the hash code of the column
	 */
	@Override
	public int hashCode() {
		return col.toString().hashCode();
	}

	/**
	 * Override the equals method to enable the use of hashtable
	 * @return true if two columns are the same
	 */
	@Override
	public boolean equals(Object obj) {
		return obj.toString().equals(this.toString());
	}
	
	/**
	 * @return the string representation of the column
	 */
	@Override
	public String toString() {
		return col.toString();
	}

	/**
	 * @return the column object
	 */
	public Column getCol() {
		return col;
	}
	
}

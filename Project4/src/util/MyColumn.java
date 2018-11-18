package util;

import net.sf.jsqlparser.schema.Column;

public class MyColumn{

	Column col;
	
	public MyColumn(Column col) {
		this.col = col;
	}
	
	@Override
	public int hashCode() {
		return col.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj.toString().equals(this.toString());
	}
	
	@Override
	public String toString() {
		return col.toString();
	}

	public Column getCol() {
		return col;
	}
	
}

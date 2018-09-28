package util;

import java.util.Arrays;

public class Tuple {
	int[] val;
	int size;
	
	public Tuple(String[] val) {
		size = val.length;
		this.val = new int[size];
		for (int i = 0; i < val.length; i++) {
			this.val[i] = Integer.valueOf(val[i]);
		}
	}
	
	public int getVal(int id) {
		return this.val[id];
	}
	@Override
	public String toString() {
		return Arrays.toString(val);
	}
	
}

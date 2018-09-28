package util;
import java.util.*;
public class Tool {
	public static String getRealColName(String name) {
		return name.split("\\.")[1];
	}
	public static int getColId(String name,List<String> schema) {
		int id = schema.indexOf(name);
		if(id !=-1) return id;
		for(int i=0;i<schema.size();i++)
			if(name.equals(getRealColName(schema.get(i))))
				return i;
		return -1;
	}
	public static int getValByColName(Tuple t, List<String> schema, String colName) {
		int id = getColId(colName,schema);
		return id == -1 ? null : t.getVal(id);
	}
}

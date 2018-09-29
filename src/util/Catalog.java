package util;

import java.io.*;
import java.util.*;

public class Catalog {
	private static Catalog instance = null;
	
	public static String input = "samples" + File.separator + "input";
	public static String output = "samples" + File.separator + "output";
	public static String query = "";
	public static String schema = "";
	public static HashMap<String, List<String>> schema_map = new HashMap<>();
	public static HashMap<String, String> aliase_map = new HashMap<>();

	private Catalog() throws FileNotFoundException {
		initialize(input, output);
	}
	
	public static synchronized Catalog getInstance() throws FileNotFoundException {
		if (instance == null) instance = new Catalog();
		return instance;
	}
	
	public static void initialize(String input, String output) throws FileNotFoundException {
		if (!input.isEmpty()) {
			Catalog.input = input;
		}
		if (!output.isEmpty()) {
			Catalog.output = output;
		}
		schema = Catalog.input + File.separator + "db" + File.separator + "schema.txt";
//		System.out.println(input);
//		System.out.println(Catalog.input);
//		System.out.println(schema);
		File file = new File(schema);
		Scanner in = new Scanner(file);
		while(in.hasNextLine()) {
			String[] fi = in.nextLine().split(" ");
			if (fi.length >= 2) {
				String key = fi[0];
				List<String> value = new LinkedList<>();
				for (int i = 1; i < fi.length; i++) {
					value.add(fi[i]);
				}
				schema_map.put(key, value);
			}
		}
		in.close();
	}
	
	public static FileReader getQueryFiles() {
		query = Catalog.input + File.separator + "queries.sql";
		try {
			return new FileReader(query);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static BufferedReader getTableFiles(String tName) {
		String table = Catalog.input + File.separator + "db" + File.separator + "data" + File.separator + tName;
		try {
			return new BufferedReader(new FileReader(table));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<String> getSchema(String tName) {
		return schema_map.get(tName);
	}
	
	public static int getIndex(String tableName, String schemaName) {
		return schema_map.get(tableName).indexOf(schemaName);
	}
	
}

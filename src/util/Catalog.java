package util;

import java.io.*;
import java.util.*;

/**
 * @author Yixin Cui
 * @author Haodong Ping
 * Catalog class uses singleton pattern to access tables and schemas information from the file system
 *
 */
public class Catalog {
	private static Catalog instance = null;
	
	public static String input = "samples" + File.separator + "input";
	public static String output = "samples" + File.separator + "output" + File.separator;
	public static String query = "";
	public static String schema = "";
	public static HashMap<String, List<String>> schema_map = new HashMap<>();
	public static HashMap<String, String> aliases = new HashMap<>();
	public static HashMap<String, String> uniqueAliases = new HashMap<>();

	/*
	 * Create the Catalog object then initialize it
	 */
	private Catalog() {
		
			initialize(input, output);					
	}
	
	/*
	 * getInstance() is used to create the object from other classes
	 * and make sure there is only one Catalog object at the same time
	 * @return the Catalog object that it created
	 */
	public static synchronized Catalog getInstance() {
		if (instance == null) instance = new Catalog();
		return instance;
	}
	
	/*
	 * Initialize the Catalog with the new input address and output address
	 * Read the schema data from file and save data in a map
	 * @param input user specified input address
	 * @param output user specified output address
	 */
	public static void initialize(String input, String output) {
		if (!input.isEmpty()) {
			Catalog.input = input;
		}
		if (!output.isEmpty()) {
			Catalog.output = output + File.separator;
		}
		schema = Catalog.input + File.separator + "db" + File.separator + "schema.txt";
		File file = new File(schema);
		Scanner in = null;
		try {
			in = new Scanner(file);
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if( in != null ) in.close();
		}
		
		
	}
	
	/*
	 * Find the location of queries file and read the query file
	 * @return the FileReader of the query file
	 */
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
	
	/*
	 * Find the specified table in the input directory and read it
	 * @param tName the name of the table
	 * @return the BufferedReader of the table file
	 */
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
	
	/*
	 * Get the schema of specified table
	 * @param tName the name of the table
	 * @return the list that contains all the schema of that table
	 */
	public static List<String> getSchema(String tFullName) {
		return schema_map.get(tFullName);
	}
	
	/*
	 * Get the index of the specified column in the specified table
	 * @param tableName the name of the table
	 * @param schemaName the name of the schema
	 * @return the index of the column
	 */
	public static int getIndex(String tableName, String schemaName) {
		return schema_map.get(tableName).indexOf(schemaName);
	}
	
	/*
	 * Get the original table name from the alias
	 * @param alias the alias of the table
	 * @return the full table name
	 */
	public static String getTableFullName(String tName) {
		if (aliases.containsKey(tName)) {
			return aliases.get(tName);
		}
		return tName;
	}
	
	/*
	 * Get the unique identity name of the input table name
	 * @param tName the table name
	 * @return the unique identity of the table
	 */
	public static String getUniqueName(String tName) {
		if (uniqueAliases.containsKey(tName)) return uniqueAliases.get(tName);
		return tName;
	}
	
	/*
	 * Assign alias to corresponding table name
	 * @param alias the alias of table
	 * @param table the original table name
	 */
	public static void setAlias(String alias, String tableName) {
		aliases.put(alias, tableName);
		uniqueAliases.put(tableName, alias);
	}
	
	/*
	 * Clear aliases map for next query
	 */
	public static void resetAlias() {
		aliases.clear();
		uniqueAliases.clear();
	}
}

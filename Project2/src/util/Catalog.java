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
	public static String tempDir = "samples" + File.separator + "temp" + File.separator;
	public static HashMap<String, List<String>> schema_map = new HashMap<>();
	public static HashMap<String, String> aliases = new HashMap<>();
	public static HashMap<String, String> uniqueAliases = new HashMap<>();
	public static int pageSize = 4096;
	public static int joinConfig = 0;
	public static int joinBuffer = 0;
	public static int sortConfig = 0;
	public static int sortBuffer = 0;
	
	public static final int TNLJ = 0;
	public static final int BNLJ = 1;
	public static final int SMJ = 2;
	
	public static final int IMS = 0;
	public static final int EMS = 1;

	/*
	 * Create the Catalog object then initialize it
	 */
	private Catalog() {
		
			initialize(input, output, tempDir);					
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
	public static void initialize(String input, String output, String tempDir) {
		if (!input.isEmpty()) {
			Catalog.input = input;
		}
		if (!output.isEmpty()) {
			Catalog.output = output + File.separator;
		}
		if (!tempDir.isEmpty()) {
			Catalog.tempDir = tempDir + File.separator;
		}
		Scanner in = null;
		String schema = Catalog.input + File.separator + "db" + File.separator + "schema.txt";
		File file = new File(schema);
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
			e.printStackTrace();
		} finally {
			if( in != null ) in.close();
		}
		String configFile = Catalog.input + File.separator + "plan_builder_config.txt";
		File conf = new File(configFile);
		in  = null;
		try {
			in = new Scanner(conf);
			String[] joinMethod = in.nextLine().split(" ");
			Catalog.joinBuffer = joinMethod.length == 2 ? Catalog.joinBuffer = Integer.valueOf(joinMethod[1]) : 0;
			Catalog.joinConfig = Integer.valueOf(joinMethod[0]);
//			if (joinMethod.length == 2) {
//				Catalog.joinConfig = Integer.valueOf(joinMethod[0]);
//				Catalog.joinBuffer = Integer.valueOf(joinMethod[1]);
//			}else {
//				Catalog.joinConfig = Integer.valueOf(joinMethod[0]);
//			}
			String[] sortMethod = in.nextLine().split(" ");
			if (sortMethod.length == 2) {
				Catalog.sortConfig = 1;
				Catalog.sortBuffer = Integer.valueOf(sortMethod[1]);
			}
		} catch (IOException e) {
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
		String query = Catalog.input + File.separator + "queries.sql";
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
	public static TupleReader getTableFiles(String tName) {
		return new TupleReader(tName);
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
		if (tName == null && uniqueAliases.size() == 1) return uniqueAliases.values().toString().replaceAll("\\[", "").replaceAll("\\]", "");
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

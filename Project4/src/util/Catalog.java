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
	
	public static String interpreterConfig = "samples" + File.separator + "interpreter_config_file.txt";
	public static String input = "samples" + File.separator + "input";
	public static String output = "samples" + File.separator + "output" + File.separator;
	public static String tempDir = "samples" + File.separator + "temp" + File.separator;
	public static String indexDir = "samples" + File.separator + "input" + File.separator + "db" + File.separator + "indexes" + File.separator;
	public static String statsDir = "samples" + File.separator + "input" + File.separator + "db" + File.separator + "stats.txt";	
	public static HashMap<String, List<String>> schema_map = new HashMap<>();
	public static HashMap<String, String> aliases = new HashMap<>();
	public static HashMap<String, String> uniqueAliases = new HashMap<>();
	public static HashMap<String, IndexInfo> indexInfo = new HashMap<>();
	public static HashMap<String, statsInfo> stats = new HashMap<>();
	
	public static int ID = 0;
	public static int pageSize = 4096;
	public static int joinConfig = 2;
	public static int joinBuffer = 5;
	public static int sortConfig = 1;
	public static int sortBuffer = 4;
	
	public static final int TNLJ = 0;
	public static final int BNLJ = 1;
	public static final int SMJ = 2;
	
	public static final int IMS = 0;
	public static final int EMS = 1;
	
	public static boolean buildIndex = false;
	public static boolean executeQuery = false;
	public static boolean useIndex = false;

	/**
	 * Create the Catalog object then initialize it
	 */
	private Catalog() {
		
			initialize(interpreterConfig);					
	}
	
	/**
	 * getInstance() is used to create the object from other classes
	 * and make sure there is only one Catalog object at the same time
	 * @return the Catalog object that it created
	 */
	public static synchronized Catalog getInstance() {
		if (instance == null) instance = new Catalog();
		return instance;
	}
	
	/**
	 * Initialize the Catalog with the new input address and output address
	 * and temp directory address. It also processes the config file
	 * Read the schema data from file and save data in a map
	 * @param the address of the config file
	 * @param tempDir the temp directory address
	 */
	public static void initialize(String interpreterConfig) {
		Scanner in = null;
		if (!interpreterConfig.isEmpty()) {
			Catalog.interpreterConfig = interpreterConfig;
		}
		File interpreterFile = new File(Catalog.interpreterConfig);
		in  = null;
		try {
			in = new Scanner(interpreterFile);
			Catalog.input = in.nextLine();
			Catalog.output = in.nextLine() + File.separator;
			Catalog.tempDir = in.nextLine() + File.separator;
			Catalog.buildIndex = true;
			Catalog.executeQuery = true;
			Catalog.indexDir = Catalog.input + File.separator + "db" + File.separator + "indexes" + File.separator;
			//comment after test
			Catalog.statsDir = Catalog.input + File.separator + "db" + File.separator + "stats.txt";
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if( in != null ) in.close();
		}

		String schema = Catalog.input + File.separator + "db" + File.separator + "schema.txt";
		File file = new File(schema);
		in = null;
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
		
		String indexInfo = Catalog.input + File.separator + "db" + File.separator + "index_info.txt";
		File indexInfoFile = new File(indexInfo);
		in = null;
		try {
			in = new Scanner(indexInfoFile);
			while(in.hasNextLine()) {
				String[] fi = in.nextLine().split(" ");
				if (fi.length >= 4) {
					IndexInfo ii = new IndexInfo(fi[0]);
					ii.setOrder(fi[fi.length - 1]);
					ii.setIsclustered(fi[fi.length - 2]);
					if (ii.isClustered()) {
						ii.setClusteredIndex(fi[1]);
						for (int i = 2; i < fi.length - 2; i++) {
							ii.addUnclusteredIndex(fi[i]);
						}
					}else {
						for (int i = 1; i < fi.length - 2; i++) {
							ii.addUnclusteredIndex(fi[i]);
						}
					}
					Catalog.indexInfo.put(ii.getTableName(), ii);
					ii.buildTree();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if( in != null ) in.close();
		}
		findStats();
	}
	
	/**
	 * Find the location of queries file and read the query file
	 * @return the FileReader of the query file
	 */
	public static FileReader getQueryFiles() {
		String query = Catalog.input + File.separator + "queries.sql";
		try {
			return new FileReader(query);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Find the specified table in the input directory and read it
	 * @param tName the name of the table
	 * @return the BufferedReader of the table file
	 */
	public static TupleReader getTableFiles(String tName) {
		return new TupleReader(tName);
	}
	
	/**
	 * Get the schema of specified table
	 * @param tName the name of the table
	 * @return the list that contains all the schema of that table
	 */
	public static List<String> getSchema(String tFullName) {
		return schema_map.get(tFullName);
	}
	
	/**
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
	
	/**
	 * Get the unique identity name of the input table name
	 * @param tName the table name
	 * @return the unique identity of the table
	 */
	public static String getUniqueName(String tName) {
		if (tName == null && uniqueAliases.size() == 1) return uniqueAliases.values().toString().replaceAll("\\[", "").replaceAll("\\]", "");
		if (uniqueAliases.containsKey(tName)) return uniqueAliases.get(tName);
		return tName;
	}
	
	/**
	 * Assign alias to corresponding table name
	 * @param alias the alias of table
	 * @param table the original table name
	 */
	public static void setAlias(String alias, String tableName) {
		aliases.put(alias, tableName);
		uniqueAliases.put(tableName, alias);
	}
	
	/**
	 * Clear aliases map for next query
	 */
	public static void resetAlias() {
		aliases.clear();
		uniqueAliases.clear();
	}
	
	/**
	 * Get the number of external sort
	 * @return the number of external sort
	 */
	public static int sortID() {
		return ID++;
	}
	/**
	 * Get the number of external sort
	 * @return the number of external sort
	 */
	public static void findStats() {
		File f = new File(statsDir);
		if(!f.exists()) {
			FileWriter fw = null;
			try {
				fw = new FileWriter(statsDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
			BufferedWriter bw = new BufferedWriter(fw);
			for(String Table : schema_map.keySet()) {
				List<String> s = schema_map.get(Table);
				int l = s.size();
				int size = 0;
				int[] ma = new int[l];
				int[] mi = new int[l];
				TupleReader r = new TupleReader(Table);
				long[] t = r.nextTuple();
				while(t != null) {
					size++;
					for(int i = 0; i < l ; i++) {
						ma[i] = (int) (ma[i]>t[i]? ma[i]:t[i]);
						mi[i] = (int) (mi[i]<t[i]? mi[i]:t[i]);
					}
					t = r.nextTuple();
				}
				stats.put(Table, new statsInfo(size, ma, mi));
				String str = Table + ' ' + Integer.toString(size);		
				for(int i = 0; i < l; i++) {
					str += ' ' + s.get(i) + ',' + Integer.toString(mi[i]) + ',' + Integer.toString(ma[i]);
				}
				try {
					bw.write(str + '\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else {
			FileReader fr = null;
			try {
				fr = new FileReader(statsDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
			BufferedReader br = new BufferedReader(fr);
			while(true)
				try {
					String str = br.readLine();
					String[] list = str.split(" ");
					int[] ma = new int[list.length - 2];
					int[] mi = new int[list.length - 2];
					int size = Integer.valueOf(list[1]);
					for(int i = 2; i < list.length; i++) {
						String[] list2 = list[i].split(",");
						mi[i-2] = Integer.valueOf(list2[1]);
						ma[i-2] = Integer.valueOf(list2[2]);
					}
					stats.put(list[0], new statsInfo(size, ma, mi));
				} catch (Exception e) {
					break;
				}
		}
	}

}

class statsInfo {
	public int n;
	public int[] ma;
	public int[] mi;
	public statsInfo(int n, int[] ma, int[] mi) {
		this.n = n;
		this.ma = ma;
		this.mi = mi;
	}
	
	public statsInfo(statsInfo s) {
		this.n = s.n;
		this.ma = Arrays.copyOf(s.ma, s.ma.length);
		this.mi = Arrays.copyOf(s.mi, s.mi.length);
	}
	
	public void add(statsInfo s) {
		int[] ma_new = new int[ma.length + s.ma.length];
	    System.arraycopy(ma, 0, ma_new, 0, ma.length);
	    System.arraycopy(s.ma, 0, ma_new, ma.length, s.ma.length);
	    ma = ma_new;
		int[] mi_new = new int[mi.length + s.mi.length];
	    System.arraycopy(mi, 0, mi_new, 0, mi.length);
	    System.arraycopy(s.mi, 0, mi_new, mi.length, s.mi.length);
	    mi = mi_new;
	}
	
}

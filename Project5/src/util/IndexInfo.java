package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class store index information
 * @author Yixin Cui
 *
 */
public class IndexInfo {
	private String tableName;
	private int order;
	private boolean isClustered;
	private String clusteredIndex = null;
	private List<String> unclusteredIndex = null;
	/**
	 * Construct the class
	 * @param tableName the full table name of the original table
	 */
	public IndexInfo(String tableName) {
		this.tableName = tableName;
		unclusteredIndex = new ArrayList<>();
	}
	
	/**
	 * @return full table name
	 */
	public String getTableName() {
		return tableName;
	}
	
	/**
	 * @return the B+ tree order defined in the index information file
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @return whether the table has clustered index
	 */
	public boolean isClustered() {
		return isClustered;
	}

	/**
	 * @return the clustered index
	 */
	public String getClusteredIndex() {
		return clusteredIndex;
	}

	/**
	 * @return a list contains all unclustered index
	 */
	public List<String> getUnclusteredIndex() {
		return unclusteredIndex;
	}
	
	/**
	 * Add a new unclustered index to the list
	 * @param index the new index
	 */
	public void addUnclusteredIndex(String index) {
		unclusteredIndex.add(index);
	}

	/**
	 * Set the order of the B+ tree
	 * @param the order of the B+ tree
	 */
	public void setOrder(String order) {
		this.order = Integer.valueOf(order);
	}

	/**
	 * Define whether the table contains clustered index
	 * @param isclustered string form of 1 indicates true and 0 indicates false
	 */
	public void setIsclustered(String isclustered) {
		this.isClustered = Integer.valueOf(isclustered) == 1;
	}

	/**
	 * Set the clustered index
	 * @param clusteredIndex the clustered index
	 */
	public void setClusteredIndex(String clusteredIndex) {
		this.clusteredIndex = clusteredIndex;
	}
	/**
	 * Build the B+ tree according to the index information
	 */
	public void buildTree() {
		if (isClustered) {
			Tools.sortByIndex(tableName);
		}
		for (String col : allIndice()) {
			int index = Catalog.schema_map.get(tableName).indexOf(col);
			IndexBuilder ib = new IndexBuilder(Catalog.getTableFiles(tableName), index , order);
			ib.leafNodes();
			ib.IndexNodes();
		}
	}
	
	/**
	 * Get the number of pages in the B+ tree
	 * @param ColumnName the column name of the table
	 * @return the number of pages
	 */
	public int leafPageNum(String ColumnName) {
		String indexName = tableName + "." + ColumnName;
		File indexFile = new File(Catalog.indexDir + indexName);
		try {
			FileInputStream fin = new FileInputStream(indexFile);
			FileChannel fc = fin.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(Catalog.pageSize);
			fc.read(buffer);
			int leafNum = buffer.getInt(4);
			fin.close();
			return leafNum;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * Helper function uses a list to store all the indices 
	 * @return
	 */
	public List<String> allIndice() {
		ArrayList<String> ret = new ArrayList<>();
		if (unclusteredIndex != null) ret.addAll(unclusteredIndex);
		if (clusteredIndex != null) ret.add(clusteredIndex);
		return ret;
	}
}

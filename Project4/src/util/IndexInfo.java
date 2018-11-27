package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class IndexInfo {
	private String tableName;
	private int order;
	private boolean isClustered;
	private String clusteredIndex = null;
	private List<String> unclusteredIndex = null;
	
	public IndexInfo(String tableName) {
		this.tableName = tableName;
		unclusteredIndex = new ArrayList<>();
	}

	public String getTableName() {
		return tableName;
	}

	public int getOrder() {
		return order;
	}

	public boolean isClustered() {
		return isClustered;
	}

	public String getClusteredIndex() {
		return clusteredIndex;
	}

	public List<String> getUnclusteredIndex() {
		return unclusteredIndex;
	}
	
	public void addUnclusteredIndex(String index) {
		unclusteredIndex.add(index);
	}

	public void setOrder(String order) {
		this.order = Integer.valueOf(order);
	}

	public void setIsclustered(String isclustered) {
		this.isClustered = Integer.valueOf(isclustered) == 1;
	}

	public void setClusteredIndex(String clusteredIndex) {
		this.clusteredIndex = clusteredIndex;
	}
	
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
	
	public List<String> allIndice() {
		ArrayList<String> ret = new ArrayList<>();
		if (unclusteredIndex != null) ret.addAll(unclusteredIndex);
		if (clusteredIndex != null) ret.add(clusteredIndex);
		return ret;
	}
}

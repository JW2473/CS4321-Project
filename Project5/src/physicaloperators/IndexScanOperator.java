package physicaloperators;

import util.Catalog;
import util.MyTable;
import util.TreeReader;
import util.Tuple;
/**
 * @author Yixin Cui
 * @author Haodong Ping
 * Index Scan Operator fetches tuples according to the index
 *
 */
public class IndexScanOperator extends ScanOperator{

	private boolean isClustered;
	private String columnName;
	private Integer lowKey;
	private Integer highKey;
	private TreeReader treeReader;
	
	/**
	 * Create the IndexScanOpertor 
	 * @param table the Name of the table
	 * @param the name of the column
	 * @param lowKey the lower bound of the index range
	 * @param highKey the upper bound of the index range
	 */
	public IndexScanOperator(MyTable table, String columnName, Integer lowKey, Integer highKey) {
		super(table);
		isClustered = Catalog.indexInfo.get(table.getFullTableName()).isClustered();
		this.columnName = columnName;
		this.lowKey = lowKey;
		this.highKey = highKey;
		treeReader = new TreeReader(table.getFullTableName(), columnName, this.lowKey, this.highKey);
		if (isClustered && columnName == Catalog.indexInfo.get(table.getFullTableName()).getClusteredIndex()) {
			if (highKey == null) this.highKey = Integer.MAX_VALUE;
			int[] rid = treeReader.firstRid();
			table.reset(rid[0], rid[1]);
		}
	}
	
	/**
	 * Fetch next tuple from the table according to the Rid
	 * @return the tuple
	 */
	@Override
	public Tuple getNextTuple() {
		try {
			if (isClustered && Catalog.indexInfo.get(table.getFullTableName()).getClusteredIndex() == this.columnName) {
				return table.nextTuple(highKey);
			}else {
				int[] rid = treeReader.nextRid();
				return table.nextTuple(rid[0], rid[1]);
			}
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	/**
	 * Reset the operator
	 */
	@Override
	public void reset() {
		treeReader.reset();
	}

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < layer; i++) {
			sb.append("-");
		}
		sb.append("IndexScan");
		sb.append("["+table.getFullTableName()+","+columnName+","+lowKey+","+highKey+"]");
		sb.append("\n");
		return sb.toString();
	}
	
	

}

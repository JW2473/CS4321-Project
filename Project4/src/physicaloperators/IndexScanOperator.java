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
	private Integer lowKey;
	private Integer highKey;
	private TreeReader treeReader;
	
	/**
	 * Create the IndexScanOpertor 
	 * @param table the Name of the table
	 * @param lowKey the lower bound of the index range
	 * @param highKey the upper bound of the index range
	 */
	public IndexScanOperator(MyTable table, Integer lowKey, Integer highKey) {
		super(table);
		isClustered = Integer.valueOf(Catalog.indexInfo.get(table.getFullTableName())[2]) == 1;
		this.lowKey = lowKey;
		this.highKey = highKey;
		treeReader = new TreeReader(table.getFullTableName(), this.lowKey, this.highKey);
		if (isClustered) {
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
			if (isClustered) {
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
		// TODO Reset the IndexScanOperator
		treeReader.reset();
	}

}

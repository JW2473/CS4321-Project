package physicaloperators;

import util.Catalog;
import util.MyTable;
import util.TreeReader;
import util.Tuple;

public class IndexScanOperator extends ScanOperator{

	private boolean isClustered;
	private Integer lowKey;
	private Integer highKey;
	private TreeReader treeReader;
	
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
	
	@Override
	public Tuple getNextTuple() {
		// TODO Get next tuple from the IndexScanOperator
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

	@Override
	public void reset() {
		// TODO Reset the IndexScanOperator
		treeReader.reset();
	}

}

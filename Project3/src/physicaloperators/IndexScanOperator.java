package physicaloperators;

import util.Catalog;
import util.MyTable;
import util.Tuple;

public class IndexScanOperator extends ScanOperator{

	private boolean isClustered;
	private Integer lowKey;
	private Integer highKey;
	
	public IndexScanOperator(MyTable table, Integer lowKey, Integer highKey) {
		super(table);
		isClustered = Integer.valueOf(Catalog.indexInfo.get(table.getFullTableName())[3]) == 1;
		this.lowKey = lowKey;
		this.highKey = highKey;
	}
	
	@Override
	public Tuple getNextTuple() {
		// TODO Get next tuple from the IndexScanOperator
		return table.nextTuple(lowKey, highKey);
		//return null;
	}

	@Override
	public void reset() {
		// TODO Reset the IndexScanOperator 
	}

}

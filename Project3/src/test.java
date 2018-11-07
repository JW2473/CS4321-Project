import util.Catalog;
import util.IndexBuilder;

public class test {
	public static void main(String[] args) {
		IndexBuilder x = new IndexBuilder(Catalog.getTableFiles("Boats"), 1, 10);
		x.leafNodes();
		x.IndexNodes();
	}
}

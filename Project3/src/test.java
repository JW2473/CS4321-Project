import util.Catalog;
import util.IndexBuilder;

public class test {
	public static void main(String[] args) {
		IndexBuilder x = new IndexBuilder(Catalog.getTableFiles("Sailors"), 0, 15);
		x.leafNodes();
		x.IndexNodes();
	}
}

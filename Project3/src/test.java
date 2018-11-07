import util.IndexBuilder;

public class test {
	public static void main(String[] args) {
		IndexBuilder x = new IndexBuilder("input/db/data/Boats", "./test.1", 1, 10);
		x.leafNodes();
		x.IndexNodes();
	}
}

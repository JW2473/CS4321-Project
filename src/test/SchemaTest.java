package test;

import java.io.FileNotFoundException;

import util.Catalog;

public class SchemaTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

			Catalog.initialize("samples/input","");
			Catalog.getInstance();
			System.out.println(Catalog.schema_map.get("Sailors").toString());
		

}
}
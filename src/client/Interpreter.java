package client;

import java.io.FileNotFoundException;

import util.Catalog;

public class Interpreter {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		if (args.length != 2) {
			throw new IllegalArgumentException("Wrong arguments!");
		}
		Catalog.initialize(args[0], args[1]);
		Catalog.getInstance();
		
	}

}

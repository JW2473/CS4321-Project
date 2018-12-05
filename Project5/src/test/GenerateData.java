package test;

import org.junit.Test;

import util.Tuple;
import util.TupleWriter;

public class GenerateData {
	@Test
	public void GenerateSailors() throws Exception{
		int max_A = 2000;
		int max_B = 1500;
		int max_C = 1500;
		
		int size = 3000;
		TupleWriter tw = new TupleWriter("samples2/test_input/Sailors");
		for( int i = 0; i < size; i++) {
			int val_A = (int)(Math.random() * max_A);
			int val_B = (int)(Math.random() * max_B);
			int val_C = (int)(Math.random() * max_C);
			long[] t = {val_A, val_B, val_C};
			tw.writeTuple(new Tuple(t));
		}
		tw.close();
	}
	@Test
	public void GenerateReserves() throws Exception{
		int max_G = 2000;
		int max_H = 1500;
		
		int size = 5000;
		TupleWriter tw = new TupleWriter("samples2/test_input/Reserves");
		for( int i = 0; i < size; i++) {
			int val_G = (int)(Math.random() * max_G);
			int val_H = (int)(Math.random() * max_H);
			long[] t = {val_G, val_H};
			tw.writeTuple(new Tuple(t));
		}
		tw.close();
	}
	@Test
	public void GenerateBoats() throws Exception{
		int max_D = 1500;
		int max_E = 1000;
		int max_F = 1000;
		
		int size = 2000;
		TupleWriter tw = new TupleWriter("samples2/test_input/Boats");
		for( int i = 0; i < size; i++) {
			int val_D = (int)(Math.random() * max_D);
			int val_E = (int)(Math.random() * max_E);
			int val_F = (int)(Math.random() * max_F);
			long[] t = {val_D, val_E, val_F};
			tw.writeTuple(new Tuple(t));
		}
		tw.close();
	}
}

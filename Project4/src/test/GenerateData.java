package test;

import java.util.ArrayList;
import org.junit.Test;

import util.Tuple;
import util.TupleWriter;

public class GenerateData {
	@Test
	public void GenerateSailors() throws Exception{
		int max_A = 800;
		int max_B = 8000;
		int max_C = 8000;
		
		int size = 15000;
		TupleWriter tw = new TupleWriter("samples/test_input/Sailors");
		for( int i = 0; i < size; i++) {
			int val_A = (int)(Math.random() * max_A);
			int val_B = (int)(Math.random() * max_B);
			int val_C = (int)(Math.random() * max_C);
			long[] t = {val_A, val_B, val_C};
			tw.writeTuple(new Tuple(t, new ArrayList<String>()));
		}
		tw.close();
	}
	@Test
	public void GenerateReserves() throws Exception{
		int max_G = 800;
		int max_H = 1000;
		
		int size = 20000;
		TupleWriter tw = new TupleWriter("samples/test_input/Reserves");
		for( int i = 0; i < size; i++) {
			int val_G = (int)(Math.random() * max_G);
			int val_H = (int)(Math.random() * max_H);
			long[] t = {val_G, val_H};
			tw.writeTuple(new Tuple(t, new ArrayList<String>()));
		}
		tw.close();
	}
	@Test
	public void GenerateBoats() throws Exception{
		int max_D = 1000;
		int max_E = 8000;
		int max_F = 8000;
		
		int size = 15000;
		TupleWriter tw = new TupleWriter("samples/test_input/Boats");
		for( int i = 0; i < size; i++) {
			int val_D = (int)(Math.random() * max_D);
			int val_E = (int)(Math.random() * max_E);
			int val_F = (int)(Math.random() * max_F);
			long[] t = {val_D, val_E, val_F};
			tw.writeTuple(new Tuple(t, new ArrayList<String>()));
		}
		tw.close();
	}
}

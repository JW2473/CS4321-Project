package test;

import java.util.ArrayList;
import org.junit.Test;

import util.Tuple;
import util.TupleWriter;

public class GenerateData {
	@Test
	public void GenerateSailors() throws Exception{
		int max_A = 200;
		int max_B = 150;
		int max_C = 150;
		
		int size = 300;
		TupleWriter tw = new TupleWriter("samples2/test_input/Sailors");
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
		int max_G = 200;
		int max_H = 150;
		
		int size = 500;
		TupleWriter tw = new TupleWriter("samples2/test_input/Reserves");
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
		int max_D = 150;
		int max_E = 100;
		int max_F = 100;
		
		int size = 200;
		TupleWriter tw = new TupleWriter("samples2/test_input/Boats");
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

package test;

import org.junit.Test;

import util.Tool;

public class UtilTest {
	@Test
	public void testgetName() {
		String name = Tool.getRealColName("s.a");
		System.out.println(name);
	}
}

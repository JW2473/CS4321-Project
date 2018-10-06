package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TupleReader {
	FileInputStream fin;
	FileChannel fc;
	ByteBuffer buffer;
	String tableFile;
	int bytesRead = 0;
	int numAttr;
	int size;
	int count;
	int index;
	
	public TupleReader(String tableName) {
		tableFile = Catalog.input + File.separator + "db" + File.separator + "data" + File.separator + tableName;
		try {
			fin = new FileInputStream(tableFile);
			fc = fin.getChannel();
			buffer = ByteBuffer.allocate(4096);
			readPage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean readPage() {
		try {
			buffer.clear();
			bytesRead = fc.read(buffer);
			numAttr = buffer.getInt(0);
			size = buffer.getInt(4);
			index = 8;
			if (bytesRead == -1)
				return false;
			else
				return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public int getAttrNum() {
		return numAttr;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public int[] nextTuple() {
		int[] val = new int[numAttr];
		if (index < buffer.capacity() - 8 && count < size) {
			for (int i = 0; i < numAttr; i++) {
				val[i] = buffer.getInt(index);
				index += 4;
			}
			count++;
			return val;
		}else {
			if (readPage()) {
				count = 0;
				return nextTuple();	
			}
		}
		return null;
	}
	
	public void close() {
		try {
			index = 0;
			size = 0;
			numAttr = 0;
			bytesRead = 0;
			buffer.clear();
			fc.close();
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void reset() {
		try {
			close();
			fin = new FileInputStream(tableFile);
			fc = fin.getChannel();
			buffer = ByteBuffer.allocate(4096);
			readPage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void convertToReadableFile() {
		reset();
		int[] val = nextTuple();
		try {
			PrintStream ps = new PrintStream(new File(tableFile + "_humanreadable.txt"));
			while (val != null) {
				ps.println(val[0] + "," + val[1] + "," + val[2]);
				val = nextTuple();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}
}

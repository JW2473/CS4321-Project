package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Yixin Cui
 * @author Haodong Ping
 * Read tuples from binary file using NIO
 *
 */
public class TupleReader {
	private FileInputStream fin;
	private FileChannel fc;
	private ByteBuffer buffer;
	private String tableFile;
	private File file;
	private int bytesRead = 0;
	private int numAttr;
	private int size;
	private int count;
	private int index;
	private int pageSize = Catalog.pageSize;
	private List<Integer> totalCount = new ArrayList<>();
	
	/**
	 * Read tuples from an original table
	 * @param tableName the name of original table
	 */
	public TupleReader(String tableName) {
		tableFile = Catalog.input + File.separator + "db" + File.separator + "data" + File.separator + tableName;
		try {
			file = new File(tableFile);
			fin = new FileInputStream(file);
			fc = fin.getChannel();
			buffer = ByteBuffer.allocate(pageSize);
			totalCount.add(0);
			readPage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read tuples from a file
	 * @param file the file to be read
	 * @throws FileNotFoundException
	 */
	public TupleReader(File file) throws FileNotFoundException {
		this.file = file;
		fin = new FileInputStream(file);
		fc = fin.getChannel();
		buffer = ByteBuffer.allocate(pageSize);
		totalCount.add(0);
		readPage();
	}
	
	/**
	 * Fetch the next page in the table
	 * @return whether the reader reaches the end of the file
	 */
	private boolean readPage() {
		try {
			clearBuffer();
			bytesRead = fc.read(buffer);
			numAttr = buffer.getInt(0);
			size = buffer.getInt(4);
			index = 8;
			if (bytesRead == -1) {
				return false;
			}else {
				totalCount.add(totalCount.get(totalCount.size() - 1) + size);
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Return the number of attributes of the tuple in the page
	 * @return the number of attributes
	 */
	public int getAttrNum() {
		return numAttr;
	}
	
	/**
	 * Return the number of tuples in a page
	 * @return the number of tuples
	 */
	public int getSize() {
		return this.size;
	}
	
	/**
	 * Get the tuple value from the buffer
	 * @return the array contains values of the tuple
	 */
	public long[] nextTuple() {
		long[] val = new long[numAttr];
		if (index < buffer.capacity() && count < size) {
			for (int i = 0; i < numAttr; i++) {
				val[i] = (long) buffer.getInt(index);
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
	
	/**
	 * Close the tuple reader
	 */
	public void close() {
		try {
			index = 0;
			size = 0;
			numAttr = 0;
			bytesRead = 0;
			count = 0;
			totalCount = null;
			clearBuffer();
			fc.close();
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reset the tuple reader to the beginning
	 */
	public void reset() {
		try {
			close();
			fin = new FileInputStream(file);
			fc = fin.getChannel();
			buffer = ByteBuffer.allocate(pageSize);
			totalCount = new ArrayList<>();
			readPage();
			totalCount.add(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reset the tuple reader to a specified index
	 * @param index the index we want to go
	 */
	public void reset(int index) {
		int pageNum = Collections.binarySearch(totalCount, new Integer(index));
		pageNum = pageNum >= 0 ? pageNum : -(pageNum + 1) - 1;
		try {
			fc.position(pageNum * pageSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		totalCount = totalCount.subList(0, pageNum + 1);
		readPage();
		count = index - totalCount.get(pageNum);
		this.index = count * numAttr * 4 + 8;
		buffer.position(this.index);
	}
	
	/**
	 * Fill buffer with 0
	 */
	private void clearBuffer() {
		buffer.clear();
		buffer.put(new byte[pageSize]);
		buffer.clear();
	}
	
	/**
	 * Convert binary file to readable file for debugging
	 * @param outputFile the output file
	 */
	public void convertToReadableFile(String outputFile) {
		reset();
		long[] val = nextTuple();
		try {
			PrintStream ps = new PrintStream(new File(outputFile + "_humanreadable.txt"));
			while (val != null) {
				for( int i = 0; i < val.length; i++ ) {
					ps.print(val[i]);
					if( i != val.length-1 )
					   ps.print( "," );
				}
				ps.println();
				val = nextTuple();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}
}

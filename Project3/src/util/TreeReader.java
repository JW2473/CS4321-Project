package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TreeReader {
	private FileInputStream fin;
	private FileChannel fc;
	private ByteBuffer buffer;
	private String tableName;
	private File indexFile;
	private int pageSize = Catalog.pageSize;
	private int rootAddr;
	private int leafNum;
//	private int treeOrder;
	private Integer lowKey;
	private Integer highKey;
	private int lowPage;
	private int currPage;
	private int dataNum;
	private int dataStart;
	private int entryKeyIndex;
	private int entryNumIndex;
	private int entryStartIndex;
	private int entryStep = 8;
	private int nextKeyIndex;
	private int count;
	
	public TreeReader(String tableName, Integer lowKey, Integer highKey) {
		this.tableName = tableName;
		if (lowKey == null) this.lowKey = Integer.MIN_VALUE;
		else this.lowKey = lowKey;
		if (highKey == null) this.highKey = Integer.MAX_VALUE;
		else this.highKey = highKey;
		String indexName = this.tableName + "." + Catalog.indexInfo.get(this.tableName)[1];
		indexFile = new File(Catalog.indexDir + indexName);
		try {
			fin = new FileInputStream(indexFile);
			fc = fin.getChannel();
			buffer = ByteBuffer.allocate(pageSize);
			fc.read(buffer);
			rootAddr = buffer.getInt(0);
			leafNum = buffer.getInt(4);
//			treeOrder = buffer.getInt(8);
			lowPage = readPage(rootAddr, this.lowKey);
			currPage = lowPage;
			readLeafPage(currPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int readPage(int pageAddr, Integer key) {
		int nextPage = readIndexPage(pageAddr, key);
		while (nextPage > leafNum) {
			nextPage = readIndexPage(nextPage, key);
		}
		return nextPage;
	}
	
	private int readIndexPage(int pageAddr, Integer key) {
		clearBuffer();
		try {
			fc.position(pageAddr * pageSize);
			fc.read(buffer);
			assert buffer.getInt(0) == 1;
			int keyNum = buffer.getInt(4);
			int keyStart = 8;
			int childAddr = 4 * keyNum + 8;
			if (key < buffer.getInt(keyStart)) {
				return buffer.getInt(childAddr);
			}
			for (int i = 0; i < keyNum - 1; i++) {
				int prevKey = buffer.getInt(4 * i + keyStart);
				int nextKey = buffer.getInt(4 * (i + 1) + keyStart);
				if (key >= prevKey && key < nextKey) {
					return buffer.getInt(childAddr + 4 * (i + 1));
				}
			}
			if (key >= buffer.getInt(keyStart + (keyNum - 1) * 4)) {
				return buffer.getInt(childAddr + 4 * keyNum);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return -1;
	}
	
	private boolean readLeafPage(int pageAddr) {
		clearBuffer();
		try {
			fc.position(pageAddr * pageSize);
			int bytesRead = fc.read(buffer);
			int pageType = buffer.getInt(0);
			dataNum = buffer.getInt(4);
			dataStart = 8;
			entryKeyIndex = dataStart;
			entryNumIndex = entryKeyIndex + 4;
			entryStartIndex = entryNumIndex + 4;
			nextKeyIndex = entryKeyIndex + 8 * (buffer.getInt(entryNumIndex) + 1);
			count = 1;
			if (bytesRead < 0 || pageType == 1) {
				return false;
			}else {
				currPage++;
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public int[] nextRid() {
		int[] rid = new int[2];
		while (buffer.getInt(entryKeyIndex) < lowKey) {
//			System.out.println(buffer.getInt(entryKeyIndex));
			updateIndex();
		}
		if (entryStartIndex < buffer.capacity() && count <= dataNum 
				&& buffer.getInt(entryKeyIndex) >= lowKey 
				&& buffer.getInt(entryKeyIndex) <= highKey 
				&& entryStartIndex < nextKeyIndex) {
			rid[0] = buffer.getInt(entryStartIndex);
			rid[1] = buffer.getInt(entryStartIndex + 4);
			entryStartIndex += entryStep;
			return rid;
		}else if (entryStartIndex < buffer.capacity() && count < dataNum 
				&& entryStartIndex == nextKeyIndex) {
			updateIndex();
			return nextRid();
		}else if ((entryStartIndex >= buffer.capacity() || count >= dataNum) 
				&& buffer.getInt(entryKeyIndex) <= highKey) {
			if (readLeafPage(currPage)) {
				return nextRid();	
			}
		}
		return null;
	}
	
	public int[] firstRid() {
		int[] rid = new int[2];
		while (buffer.getInt(entryKeyIndex) < lowKey) {
			updateIndex();
		}
		rid[0] = buffer.getInt(entryStartIndex);
		rid[1] = buffer.getInt(entryStartIndex + 4);
		entryStartIndex += entryStep;
		return rid;
	}
	
	private void updateIndex() {
		entryNumIndex = entryKeyIndex + 4;
		entryStartIndex = entryNumIndex + 4;
		nextKeyIndex = entryKeyIndex + 8 * (buffer.getInt(entryNumIndex) + 1);
		entryKeyIndex = nextKeyIndex;
		entryNumIndex = entryKeyIndex + 4;
		entryStartIndex = entryNumIndex + 4;
		nextKeyIndex = entryKeyIndex + 8 * (buffer.getInt(entryNumIndex) + 1);
		count++;
	}
	
	private void clearBuffer() {
		buffer.clear();
		buffer.put(new byte[pageSize]);
		buffer.clear();
	}
	
	public void close() {
		try {
			rootAddr = 0;
			leafNum = 0;
//			treeOrder = 0;
			lowPage = 0;
			clearBuffer();
			fc.close();
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reset the index tree reader to the beginning
	 */
	public void reset() {
		try {
			close();
			fin = new FileInputStream(indexFile);
			fc = fin.getChannel();
			buffer = ByteBuffer.allocate(pageSize);
			fc.read(buffer);
			rootAddr = buffer.getInt(0);
			leafNum = buffer.getInt(4);
//			treeOrder = buffer.getInt(8);
			lowPage = readPage(rootAddr, this.lowKey);
			currPage = lowPage;
			readLeafPage(currPage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

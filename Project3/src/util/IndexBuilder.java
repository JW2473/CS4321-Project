package util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IndexBuilder {
	List<Rid> ridList = new ArrayList<>();
	int ind = 0;
	int key = -1;
	int position_n = 12;
	int position = 8;
	int n1 = 0;
	int n2 = 0;
	
	private class Rid {
		int key;
		int pageNum;
		int rNum;	
		public Rid(int key, int pageNum, int rNum) {
			this.key = key;
			this.pageNum = pageNum;
			this.rNum = rNum;
		}
	}
	
	private class RidComp implements Comparator<Rid>{
		public int compare(Rid o1, Rid o2) {
			int result;
			result = Integer.compare(o1.key, o2.key);
			if(result != 0)
				return result;
			result = Integer.compare(o1.pageNum, o2.pageNum);
			if(result != 0)
				return result;
			result = Integer.compare(o1.rNum, o2.rNum);
			return result;	
		}
	}

	public IndexBuilder(String path_name, int keyInd) {
		Path path = Paths.get(path_name);
		SeekableByteChannel sbc = null;
		ByteBuffer bf = null;
		int tupleLength;
		int maxPos;
		int page_position = 0;
		keyInd = keyInd*4;
		try {
			sbc = Files.newByteChannel(path, StandardOpenOption.READ);
			bf = ByteBuffer.allocate(4096);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		while(true) {
			try {
				sbc.read(bf);
				tupleLength = bf.getInt(0);
				maxPos = bf.getInt(4)*bf.getInt(0)*4+8;
				if(page_position == sbc.position())
					break;
				else
					page_position = (int) sbc.position();
				int position = 8;
				while(position < maxPos) {
					ridList.add(new Rid(bf.getInt(position+keyInd), page_position/4096-1, (position-8)/4));
					position += tupleLength;
				}				
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		Collections.sort(ridList, new RidComp());
	}
	
	
	public void putNextRid(ByteBuffer bf) {
		n1++;
		Rid entry = ridList.get(ind);
		ind++;
		if(entry.key == key) {
			n2++;
			bf.putInt(position, entry.pageNum);
			bf.putInt(position+4, entry.rNum);
			position += 8;
		}
		else {
			bf.putInt(position_n, n2);
			n2 = 0;
			key = entry.key;
			bf.putInt(position, entry.key);
			position_n = position + 4;
			bf.putInt(position+8, entry.pageNum);
			bf.putInt(position+12, entry.rNum);
			position += 16;
		}
	}
	
	
	public void toLeaves(String path_name) {
		if (!Files.notExists(Paths.get(path_name))) {
			File file = new File(path_name); 
			file.delete();
		}
		File file = new File(path_name);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Path path = Paths.get(path_name);	
		SeekableByteChannel sbc = null;
		ByteBuffer bf = null;
		try {
			sbc = Files.newByteChannel(path, StandardOpenOption.WRITE);
			bf = ByteBuffer.allocate(4096);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		while(true) {
			putNextRid(bf);
			if(position >= 4080) {
				bf.putInt(4, n1);
				try {
					sbc.write(bf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				bf = ByteBuffer.allocate(4096);
				n1 = 0;
				n2 = 0;
				key = -1;
				position = 8;
				position_n = 12;
				if(ind < ridList.size() - 384) {
					break;
				}
			}			
		}
		if(ind >= ridList.size() - 256) {
			int k = (ind + ridList.size())/2;
			while(ind < k) {
				putNextRid(bf);
			}
			bf.putInt(4, n1);
			try {
				sbc.write(bf);
			} catch (IOException e) {
				e.printStackTrace();
			}
			bf = ByteBuffer.allocate(4096);
			n1 = 0;
			n2 = 0;
			key = -1;
			position = 8;
			position_n = 12;
			while(ind < ridList.size()) {
				putNextRid(bf);
			}
			bf.putInt(4, n1);
		}
	}
}

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
	int order = 0;
	int key = -1;
	int position_n = 12;
	int position = 8;
	int n1 = 0;
	int n2 = 0;
	int nLeaves = 0;
	Path indexPath;
	List<Integer> keyHead;
	List<Integer> keyTail;
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

	public IndexBuilder(String input_dir, String output_dir, int keyInd, int order) {
		this.order = order;
		if (!Files.notExists(Paths.get(output_dir))) {
			File file = new File(output_dir); 
			file.delete();
		}
		File file = new File(output_dir);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		this.indexPath = Paths.get(output_dir);	
		Path path = Paths.get(input_dir);
		SeekableByteChannel sbc = null;
		ByteBuffer bf = null;
		int tupleLength;
		int maxPos;
		int page_position = 0;
		keyInd = keyInd*4;
		try {
			sbc = Files.newByteChannel(path, StandardOpenOption.READ);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		while(true) {
			try {
				bf = ByteBuffer.allocate(4096);
				sbc.read(bf);
				tupleLength = bf.getInt(0);
				maxPos = bf.getInt(4)*bf.getInt(0)*4+8;
				if(page_position == sbc.position()) {
					break;
				}
				else
					page_position = (int) sbc.position();
				int position = 8;
				while(position < maxPos) {
					ridList.add(new Rid(bf.getInt(position+keyInd), page_position/4096-1, (position-8)/4));
					position += tupleLength*4;
				}				
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		Collections.sort(ridList, new RidComp());
	}
	
	
	public void putNextRid(ByteBuffer bf, int ind) {
		n1++;
		Rid entry = ridList.get(ind);
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
	
	
	public void leafNodes() {
		nLeaves = 0;
		int ind = 0;
		keyHead = new ArrayList<>();
		keyTail = new ArrayList<>();
		SeekableByteChannel sbc = null;
		ByteBuffer bf = null;
		try {
			sbc = Files.newByteChannel(indexPath, StandardOpenOption.WRITE);
			bf = ByteBuffer.allocate(4096);
			sbc.write(bf);			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		bf = ByteBuffer.allocate(4096);
		while(true) {
			putNextRid(bf, ind);
			ind++;
			if(n1==order*2) {
				bf.putInt(4, n1);
				keyHead.add(bf.getInt(8));
				keyTail.add(bf.getInt(position_n-4));
				try {
					sbc.write(bf);
					nLeaves++;
				} catch (IOException e) {
					e.printStackTrace();
				}
				bf = ByteBuffer.allocate(4096);
				n1 = 0;
				n2 = 0;
				key = -1;
				position = 8;
				position_n = 12;
				if(ind >= ridList.size() - order*3) {
					break;
				}
			}			
		}
		if(ind < ridList.size() - order*2) {
			nLeaves+=2;
			int k = (ind + ridList.size())/2;
			while(ind < k) {
				putNextRid(bf, ind);
				ind++;
			}
			bf.putInt(4, n1);
			keyHead.add(bf.getInt(8));
			keyTail.add(bf.getInt(position_n-4));
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
				putNextRid(bf, ind);
				ind++;
			}
			bf.putInt(4, n1);
			keyHead.add(bf.getInt(8));
			keyTail.add(bf.getInt(position_n-4));
		}
		else {
			nLeaves++;
			while(ind < ridList.size()) {
				putNextRid(bf, ind);
				ind++;
			}
			bf.putInt(4, n1);
			keyHead.add(bf.getInt(8));
			keyTail.add(bf.getInt(position_n-4));
			try {
				sbc.write(bf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(nLeaves);
	}
	
	public void IndexNodes() {
		SeekableByteChannel sbc = null;
		ByteBuffer bf = null;
		try {
			sbc = Files.newByteChannel(indexPath, StandardOpenOption.WRITE);	
			sbc.position((1+nLeaves)*4096);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		while(keyHead.size() > 1) {
			List<Integer> keyHead_new = new ArrayList<>();
			List<Integer> keyTail_new = new ArrayList<>();
			bf = ByteBuffer.allocate(4096);
			bf.putInt(0, 1);
			n1 = 0;
			position = 8;
			int ind = 0;		
			while(true) {
				if(n1 == 0) {
					keyHead_new.add(keyHead.get(ind));
					bf.putInt(position, ind+1);
					position+=4;
					ind++;
				}				
				bf.putInt(position, keyHead.get(ind));
				bf.putInt(position, ind+1);
				position+=8;
				ind++;
				n1++;
				if(ind == keyHead.size() | n1==order*2) {
					bf.putInt(4, n1);
					try {
						sbc.write(bf);
						bf = ByteBuffer.allocate(4096);
						bf.putInt(0, 1);
						position = 8;
					} catch (IOException e) {
						e.printStackTrace();
					}
					keyTail_new.add(keyTail.get(ind-1));
					n1 = 0;
					if(ind >= keyHead.size() - order*3 - 1) {
						break;
					}
				}
			}
			if(ind < keyHead.size() - order*2 - 1) {
				int k = (ind + keyHead.size())/2;
				keyHead_new.add(keyHead.get(ind));
				bf.putInt(0, 1);
				bf.putInt(position, ind+1);
				position+=4;
				ind++;
				while(ind < k) {
					bf.putInt(position, keyHead.get(ind));
					bf.putInt(position, ind+1);
					position+=8;
					ind++;
					n1++;
				}
				bf.putInt(1, n1);
				try {
					sbc.write(bf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				keyTail_new.add(keyTail.get(ind-1));
				n1 = 0;
				position = 8;
				bf = ByteBuffer.allocate(4096);
				
				keyHead_new.add(keyHead.get(ind));
				bf.putInt(0, 1);
				bf.putInt(position, ind+1);
				position+=4;
				ind++;
				while(ind < keyHead.size()) {
					bf.putInt(position, keyHead.get(ind));
					bf.putInt(position, ind+1);
					position+=8;
					ind++;
					n1++;
				}
				bf.putInt(1, n1);
				try {
					sbc.write(bf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				keyTail_new.add(keyTail.get(ind-1));
			}
			else if(ind != keyHead.size()) {				
				keyHead_new.add(keyHead.get(ind));
				bf.putInt(0, 1);
				bf.putInt(position, ind+1);
				position+=4;
				ind++;
				while(ind < keyHead.size()) {
					bf.putInt(position, keyHead.get(ind));
					bf.putInt(position, ind+1);
					position+=8;
					ind++;
					n1++;
				}
				bf.putInt(1, n1);
				try {
					sbc.write(bf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				keyTail_new.add(keyTail.get(ind-1));
			}
			keyHead = keyHead_new;
			keyTail = keyTail_new;
		}
		int k = 0;
		try {
			k = (int) (sbc.position()/4096) - 1;
			sbc.position(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bf = ByteBuffer.allocate(4096);
		bf.putInt(0, k);
		bf.putInt(4, nLeaves);
		bf.putInt(8, order);
		try {
			sbc.write(bf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

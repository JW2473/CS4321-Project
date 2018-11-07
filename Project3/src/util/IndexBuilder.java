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
	Path path;
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

	public IndexBuilder(TupleReader reader, int keyInd, int order) {
		this.order = order;
		String[] tokens = reader.getFile().split(File.separator);
		String tName = tokens[tokens.length-1];
		String output_dir = Catalog.indexDir + tName + '.' + Catalog.getSchema(tName).get(keyInd);	
		File file = new File(output_dir);
		file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.path = Paths.get(output_dir);	
		long[] tuple = null;
		while((tuple = reader.nextTuple()) != null) {
			ridList.add(new Rid((int) tuple[keyInd], reader.pageNum(), reader.tupleNum()));
			tuple = reader.nextTuple();
		}
		Collections.sort(ridList, new RidComp());
	}
	
	
	public boolean putNextRid(ByteBuffer bf, int ind, int order) {
		Rid entry = ridList.get(ind);
		if(entry.key == key) {
			n2++;
			bf.putInt(position, entry.pageNum);
			bf.putInt(position+4, entry.rNum);
			position += 8;
		}
		else {
			bf.putInt(position_n, n2);
			if(n1 == order*2) {
				return false;
			}
			n1++;
			n2 = 1;
			key = entry.key;
			bf.putInt(position, entry.key);
			position_n = position + 4;
			bf.putInt(position+8, entry.pageNum);
			bf.putInt(position+12, entry.rNum);
			position += 16;
		}
		return true;
	}
	
	
	public void leafNodes() {
		nLeaves = 0;
		int ind = 0;
		keyHead = new ArrayList<>();
		keyTail = new ArrayList<>();
		SeekableByteChannel sbc = null;
		ByteBuffer bf = null;
		try {
			sbc = Files.newByteChannel(path, StandardOpenOption.WRITE);
			bf = ByteBuffer.allocate(4096);
			sbc.write(bf);			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		bf = ByteBuffer.allocate(4096);
		while(true) {
			boolean a = putNextRid(bf, ind, order);
			if(a)
				ind++;
			if(!a | ind == ridList.size()) {
				//System.out.println("!!!");
				bf.putInt(position_n, n2);
				bf.putInt(4, n1);
				keyHead.add(bf.getInt(8));
				keyTail.add(bf.getInt(position_n-4));
				try {
					sbc.write(bf);
					nLeaves++;
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(ind == ridList.size()) {
					break;
				}
				bf = ByteBuffer.allocate(4096);
				n1 = 0;
				n2 = 0;
				key = -1;
				position = 8;
				position_n = 12;
			}			
		}
		if(n1 <  order) {
			int k = (n1 + order*2)/2;
			ByteBuffer bf_read = null;
			SeekableByteChannel sbc_read = null;
			try {
				bf_read = ByteBuffer.allocate(4096);
				bf = ByteBuffer.allocate(4096);
				sbc_read = Files.newByteChannel(path, StandardOpenOption.READ);;
				sbc_read.position(sbc.position()-4096*2);
				sbc_read.read(bf_read);
			} catch (IOException e) {
				e.printStackTrace();
			}
			int position_read = 8;
			position = 8;
			int key, n;
			for(int i = 0; i < order*2; i++) {			
				key = bf_read.getInt(position_read);
				n = bf_read.getInt(position_read+4);		
				if(i >= k) {
					bf.putInt(position, key);
					bf.putInt(position+4, n);
					for(int j = 0; j < n; j++) {
						bf.putInt(position+8+j*8, bf_read.getInt(position_read+8+j*8));
						bf.putInt(position+8+j*8+4, bf_read.getInt(position_read+8+j*8+4));
					}
					position += 8 + n*8;
				}
				position_read += 8 + n*8;
				if(i == k-1) {
					keyTail.set(keyTail.size()-2, key);
					keyHead.set(keyHead.size()-1, bf_read.getInt(position_read));
				}
			}
			bf_read.putInt(4, k);
			try {
				sbc.position(sbc.position()-4096*2);
				bf_read.rewind();
				sbc.write(bf_read);
				sbc_read.position(sbc.position());
				bf_read = ByteBuffer.allocate(4096);
				sbc_read.read(bf_read);
			} catch (IOException e) {
				e.printStackTrace();
			}
			position_read = 8;
			for(int i = 0; i < bf_read.getInt(4); i++) {
				key = bf_read.getInt(position_read);
				n = bf_read.getInt(position_read+4);
				bf.putInt(position, key);
				bf.putInt(position+4, n);
				for(int j = 0; j < n; j++) {
					bf.putInt(position+8+j*8, bf_read.getInt(position_read+8+j*8));
					bf.putInt(position+8+j*8+4, bf_read.getInt(position_read+8+j*8+4));
				}
				position += 8 + n*8;
				position_read += 8 + n*8;
			}
			bf.putInt(4, 2*order + n1 - k);
			try {
				sbc.write(bf);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
/*
			bf = ByteBuffer.allocate(4096);
			try {
				sbc_read.position(sbc.position()-4096*5);
				System.out.println(sbc_read.position());
				sbc_read.read(bf);
			} catch (IOException e) {
				e.printStackTrace();
			}
			position = 8;
			for(int i = 0; i < bf.getInt(4); i++) {
				System.out.println(bf.getInt(position));
				System.out.println(bf.getInt(position+4));
				position += 8 + bf.getInt(position+4)*8;
			}
		*/
		}
		//System.out.println(nLeaves);		
	}
	
	public void IndexNodes() {
		SeekableByteChannel sbc = null;
		ByteBuffer bf = null;
		try {
			sbc = Files.newByteChannel(path, StandardOpenOption.WRITE);	
			sbc.position((1+nLeaves)*4096);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		int offset = 1;
		while(keyHead.size() > 1) {
			List<Integer> keyHead_new = new ArrayList<>();
			List<Integer> keyTail_new = new ArrayList<>();
			bf = ByteBuffer.allocate(4096);
			bf.putInt(0, 1);
			n1 = 0;
			position = 8;
			int ind = 0;
			int offset_new = offset;
			int k;
			if(2*order < keyHead.size())
				k = 2*order;
			else
				k = keyHead.size()-1;
			while(true) {
				if(n1 == 0) {
					keyHead_new.add(keyHead.get(ind));
					bf.putInt(position+k*4, ind+offset);
					offset_new++;
					//position+=4;
					ind++;
				}				
				bf.putInt(position, keyHead.get(ind));
				bf.putInt(position+k*4+4, ind+offset);
				position+=4;
				ind++;
				offset_new++;
				n1++;
				if(ind == keyHead.size() | n1==order*2) {
					bf.putInt(4, n1);
					/*
					for(int i = 0; i < n1*2+1; i++) {
						System.out.print(bf.getInt(8+i*4)+" ");
					}
					System.out.print('\n');
					*/
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
				k = (ind + keyHead.size())/2 - ind -1;
				int m = keyHead.size() - ind - 2 - k;
				keyHead_new.add(keyHead.get(ind));
				bf.putInt(0, 1);
				bf.putInt(position+k*4, ind+offset);
				//position+=4;
				ind++;
				offset_new++;
				while(n1 < k) {
					bf.putInt(position, keyHead.get(ind));
					bf.putInt(position+k*4+4, ind+offset);
					position+=4;
					ind++;
					n1++;
					offset_new++;
				}
				bf.putInt(4, n1);
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
				bf.putInt(position+4*m, ind+1);
				//position+=4;
				ind++;
				offset_new++;
				while(ind < keyHead.size()) {
					bf.putInt(position, keyHead.get(ind));
					bf.putInt(position+4*m+4, ind+offset);
					position+=4;
					ind++;
					n1++;
					offset_new++;
				}
				bf.putInt(4, n1);
				bf.putInt(4, n1);
				try {
					sbc.write(bf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				keyTail_new.add(keyTail.get(ind-1));
			}
			else if(ind != keyHead.size()) {
				k = keyHead.size() - 1 - ind;
				keyHead_new.add(keyHead.get(ind));
				bf.putInt(0, 1);
				bf.putInt(position+4*k, ind+offset);
				//position+=4;
				ind++;
				offset_new++;
				while(ind < keyHead.size()) {
					bf.putInt(position, keyHead.get(ind));
					bf.putInt(position+4*k+4, ind+offset);
					position+=4;
					ind++;
					n1++;
					offset_new++;
				}
				bf.putInt(4, n1);
				try {
					sbc.write(bf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				keyTail_new.add(keyTail.get(ind-1));
			}
			offset = offset_new;
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

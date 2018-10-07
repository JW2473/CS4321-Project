package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TupleWriter {
	FileOutputStream fout = null;
	FileChannel fc;
	ByteBuffer buffer;
	int bytesRead = 0;
	int numAttr;
	int size;
	int count;
	int id;
	int maxsize;
	STATE state;
	public enum STATE {
		START, CHGPAGE, INSERT;
	}
	
	public TupleWriter(String filePath) {
		File f = new File(filePath);
		try {
			fout = new FileOutputStream(f);
			fc = fout.getChannel();
			buffer = ByteBuffer.allocate(4096);
			numAttr = 0;
			size = 0;
			maxsize = 0;
			count = 0; 
			state = STATE.START;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void writeTuple(Tuple t) throws IOException {
		if( t == null ) buffer.putInt(4, size);
		boolean insertFlag = true;
		while(insertFlag)
			switch (state) {
				case START:
					id = 0;
					size = t.getSize();
					buffer.putInt(0,size);
					buffer.putInt(4,0);
					count = 0;
					maxsize = (buffer.capacity() - 8)/(4 * size);
					if (count < maxsize ) state = STATE.INSERT;
					else state = STATE.CHGPAGE;
					id = 8;
					break;
				case INSERT:
					insertFlag = false;
					for(Long val:t.value) {		
						int value = Integer.parseInt(val.toString());
						buffer.putInt(id, value);
						//System.out.println(id);
						id += 4;
					}
					count++;
					if ( count == maxsize || id == 4096) state = STATE.CHGPAGE;
						break;
				case CHGPAGE:
					fillRemainingPage();
					buffer.putInt(4,count);
					fc.write(buffer);
					buffer.clear();
					buffer.put(new byte[4096]);
					buffer.clear();
					state = STATE.START;
					count = 0;
		}			 
	}
	
	public void fillRemainingPage() {
		while ( id < 4096) {
			buffer.putInt(id, 0);
			id += 4;
		}
	}
	
	public void close() {
		fillRemainingPage();
		buffer.putInt(4,count);
		buffer.clear();
		try {
			if(fc != null )
				fc.write(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				if( fc != null ) fc.close();
				if( fout != null ) fout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

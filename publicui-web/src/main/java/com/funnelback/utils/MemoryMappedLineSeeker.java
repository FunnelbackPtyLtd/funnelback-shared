package com.funnelback.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MemoryMappedLineSeeker {
	private static final long PAGE_SIZE = Integer.MAX_VALUE;
	private static final byte finalLineSep = System.getProperty("line.separator").getBytes()[ System.getProperty("line.separator").getBytes().length -1];
	private static final byte startLineSep = System.getProperty("line.separator").getBytes()[0];
	
	private final List<MappedByteBuffer> buffers = new ArrayList<MappedByteBuffer>();
	private final long fileSize; 
    
	
	public MemoryMappedLineSeeker(File sortedFile) throws IOException {
		FileChannel channel = (new FileInputStream(sortedFile)).getChannel();
        long start = 0, length = 0;
        for (long index = 0; start + length < channel.size(); index++) {
            if ((channel.size() / PAGE_SIZE) == index)
                length = (channel.size() - index *  PAGE_SIZE) ;
            else
                length = PAGE_SIZE;
            start = index * PAGE_SIZE;
            
            buffers.add((int)index, channel.map(FileChannel.MapMode.READ_ONLY, start, length));
        }
        fileSize = sortedFile.length();   
	}
	
	/**
	 * Returns the position of the first character in the line pointed to by position
	 * @param position a position anywhere in the file
	 * @return the position that that line starts with
	 */
	public long getStartOfLine(long position) {
		char candidate = getChar(position);
		while(position != 0) {
			position--;
			candidate = getChar(position);
			if(candidate == finalLineSep) return position +1;
		}
		
		return 0;
	}

    private char getChar(long bytePosition) {
        int page  = (int) (bytePosition / PAGE_SIZE);
        int index = (int) (bytePosition % PAGE_SIZE);
        return (char) buffers.get(page).get(index);
    }


	public String getString(long position) {
		StringBuilder sb = new StringBuilder();
		if(position < fileSize) {
			char c = getChar(position);
			
			while(c != startLineSep) {
				sb.append(c);
				if(position == fileSize-1) break;
				c = getChar(++position);
			}
		}
		return sb.toString();
	}
	   

}

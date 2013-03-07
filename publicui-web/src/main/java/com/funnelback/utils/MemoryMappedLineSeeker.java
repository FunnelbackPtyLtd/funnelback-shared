package com.funnelback.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class MemoryMappedLineSeeker implements PanLookSeeker {
    private static final long PAGE_SIZE = Integer.MAX_VALUE;
    private final byte finalLineSep; 
    private final byte startLineSep; 
    @Getter private final long sizeOfLineSep;
    
    private final List<MappedByteBuffer> buffers = new ArrayList<MappedByteBuffer>();
    private final long fileSize;
    
    // Hold these so that they can be closed.
    private final FileChannel channel;
    private final FileInputStream fileInputStream; 
    
    
    public MemoryMappedLineSeeker(File sortedFile, byte [] lineSepBytes) throws IOException {
        finalLineSep = lineSepBytes[lineSepBytes.length -1];
        startLineSep = lineSepBytes[0];
        sizeOfLineSep = lineSepBytes.length;
                
        fileInputStream = new FileInputStream(sortedFile);
        channel = fileInputStream.getChannel();
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
    @Override
    public long getStartOfLine(long position) {
        byte candidate = getChar(position);
        while(position != 0) {
            position--;
            candidate = getChar(position);
            if(candidate == finalLineSep) return position +1;
        }
        
        return 0;
    }

    private byte getChar(long bytePosition) {
        int page  = (int) (bytePosition / PAGE_SIZE);
        int index = (int) (bytePosition % PAGE_SIZE);
        return buffers.get(page).get(index);
    }

    @Override
    public String getString(long position) {
        long start = position;
        if(position < fileSize) {
            byte c = getChar(position);            
            while(c != startLineSep) {
                if(position == fileSize -1) {
                    position++;
                    break;
                }
                c = getChar(++position);
            }
        }
        byte[] str = new byte[(int) (position - start)];
        for(int i = 0; i < position - start;i++) {
            str[i] = getChar(start+i);
        }
        return new String(str);
    }

    @Override
    public long length() {
        return fileSize;
    }

    @Override
    public void close() throws IOException {
        channel.close();
        fileInputStream.close();
    }       

}

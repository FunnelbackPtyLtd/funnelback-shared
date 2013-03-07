package com.funnelback.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;



public class MemoryMapperPanLook implements PanLook{

    ArrayList<String> matches = new ArrayList<String>();
    private final PanLookSeeker seeker;

    public MemoryMapperPanLook(PanLookSeeker seeker, String prefix) throws IOException {
        this.seeker = seeker;
        
        // If the file is empty, there will be no matches
        if(seeker.length() == 0) return; 
        
        // Get the first line that starts with prefix
        long aLineThatStartsWith = findStartOfLineThatBeginsWith(prefix,0,seeker.length()-1);
        
        long firstLineThatStartsWith = aLineThatStartsWith;
        long pos = aLineThatStartsWith;
        if(aLineThatStartsWith != -1) {
            // rewind til we find the first line that doesn't start with prefix;
            while(pos != 0) {
                pos = seeker.getStartOfLine(pos - seeker.getSizeOfLineSep() - 1);
                if( ! seeker.getString(pos).startsWith(prefix)) break;
                else firstLineThatStartsWith = pos;
            }
            // now add all lines that start with prefix to the matches array
            long offset = 0;
            String line = seeker.getString(firstLineThatStartsWith + offset);
            do {    
                matches.add(line);
                offset += line.getBytes().length  + seeker.getSizeOfLineSep();
                line = seeker.getString(firstLineThatStartsWith + offset);
            } while(line.startsWith(prefix) || line.startsWith(prefix.replaceAll(" ","_")));
        }
    }
    
    private long findStartOfLineThatBeginsWith(String prefix,long bottom, long top) {
        long halfWay = bottom + (top -bottom) / 2;
        long lineStart = seeker.getStartOfLine(halfWay);
        String thisLine = seeker.getString(lineStart);
        if(thisLine.startsWith(prefix)) {
            // we've found a match! 
            return lineStart;
        } else if(thisLine.compareTo(prefix) < 0) {
            // prefix is bigger than this line
            if(halfWay == top || halfWay == bottom) {
                // binary search has no more divisions to make; we didn't find a match
                return -1;            
            }
            // keep searching further up the file
            return findStartOfLineThatBeginsWith(prefix,halfWay,top);
        } else if(thisLine.compareTo(prefix) > 0) {
            // prefix is smaller than this line
            if(halfWay == top || halfWay == bottom)  {
                // binary search has no more divisions to make;  we didn't find a match
                return -1;
            }
            // keep searching further down the file
            return findStartOfLineThatBeginsWith(prefix,bottom,halfWay);
            
        } else {
            // This is just here to allow compilation, because
            // the compiler can't tell that one of the three if() paths
            // above must happen.
            throw new RuntimeException("Binary search failure in MemoryMapperPanLook - should never happen");
        }
    }

    
    @Override
    public Iterator<String> iterator() {
        return matches.iterator();
    }

    @Override
    public void close() throws IOException {
        seeker.close();
    }
    
}


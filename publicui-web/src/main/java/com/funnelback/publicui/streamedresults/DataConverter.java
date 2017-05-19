package com.funnelback.publicui.streamedresults;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;



public interface DataConverter<T> {
    
    /**
     * The Content type of the returned data.
     *  
     * @return
     */
    public String getContentType();
    
    public T createWritter(OutputStream outputStream) throws IOException;

    public void writeHead(List<String> fieldNames, T writter) throws IOException ;
    
    public void writeRecord(List<String> fieldNames, List<Object> values, T writter) throws IOException;
    
    public void writeSeperator(T writter) throws IOException ;
    
    public void writeFooter(T writter) throws IOException ;
    
    public void finished(T writter) throws IOException;
    
}

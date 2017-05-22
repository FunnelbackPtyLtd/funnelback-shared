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

    public void writeHead(List<String> fieldNames, T writer) throws IOException ;
    
    public void writeRecord(List<String> fieldNames, List<Object> values, T writer) throws IOException;
    
    public void writeSeperator(T writer) throws IOException ;
    
    public void writeFooter(T writer) throws IOException ;
    
    public void finished(T writer) throws IOException;
    
}

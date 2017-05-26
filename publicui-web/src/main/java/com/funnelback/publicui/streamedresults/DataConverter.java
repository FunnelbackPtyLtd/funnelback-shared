package com.funnelback.publicui.streamedresults;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


/**
 * 
 * 
 * <p>Note that data converter must be re-entrent</p>
 *
 * @param <T>
 */
public interface DataConverter<T> {
    
    /**
     * The Content type of the returned data.
     * <p>This will set on the HTTP header content-type.</p>
     * @return
     */
    public String getContentType();
    
    /**
     * Creates the writer that will be passed back to the data converter for the below methods.
     * This is typically used to store the context or a stream that other methods can write to. 
     * 
     * @param outputStream The outputStream where the converted data should be written to.
     * @return
     * @throws IOException
     */
    public T createWriter(OutputStream outputStream) throws IOException;

    /**
     * Writes the head of the data type to the given writer.
     * 
     * <p>For CSV this might write the first CSV line which consists of the heading
     * of each columns</p>
     *  
     * @param fieldNames The names of the fields that will be written out.
     * @param writer The writer returned from {@link DataConverter#createWriter(OutputStream)}
     * @throws IOException
     */
    public void writeHead(List<String> fieldNames, T writer) throws IOException ;
    
    /**
     * Writes the given values (fields) to the given writer.
     * 
     * @param fieldNames The names of the fields that will be written out.
     * @param values The values of the fields that have been requested. These may be null
     * so a null check may be needed.
     * @param writer The writer returned from {@link DataConverter#createWriter(OutputStream)}
     * @throws IOException
     */
    public void writeRecord(List<String> fieldNames, List<Object> values, T writer) throws IOException;
    
    /**
     * Writes the record separator, called when a previous record has been written and another one will be written.
     * 
     * @param writer The writer returned from {@link DataConverter#createWriter(OutputStream)}
     * @throws IOException
     */
    public void writeSeperator(T writer) throws IOException ;
    
    /**
     * Writes the footer of the data type.
     * 
     * <p>For JSON this might be the close ']' of the array.</p>
     * 
     * @param writer The writer returned from {@link DataConverter#createWriter(OutputStream)}
     * @throws IOException
     */
    public void writeFooter(T writer) throws IOException ;
    
    /**
     * Marks that we are done using writer and if necassery it should be closed.
     * 
     * @param writer The writer returned from {@link DataConverter#createWriter(OutputStream)}
     * @throws IOException
     */
    public void finished(T writer) throws IOException;
    
}

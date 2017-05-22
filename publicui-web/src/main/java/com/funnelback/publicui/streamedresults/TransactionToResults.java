package com.funnelback.publicui.streamedresults;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;

import com.funnelback.common.Reference;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.streamedresults.datafetcher.XJPathResultDataFetcher;
import com.funnelback.publicui.streamedresults.outputstreams.CloseIgnoringOutputStream;

/**
 * 
 * <p>The OutputStream on the HTTP Response will not be closed by this</p>
 *
 */
public class TransactionToResults implements Closeable {

    private final DataConverter<Object> dataConverter;
    private final List<CompiledExpression> xpaths;
    private final List<String> fieldNames;
    private final AtomicBoolean isFirstTransaction = new AtomicBoolean(true);
    private final AtomicBoolean isFirstResult = new AtomicBoolean(true);
    private final  HttpServletResponse response;
    private final  XJPathResultDataFetcher resultDataFetcher;
    private Object writter;
    
    
    public TransactionToResults(DataConverter<Object> dataConverter, List<CompiledExpression> xpaths,
        List<String> fieldNames, HttpServletResponse response, XJPathResultDataFetcher resultDataFetcher) {
        super();
        this.dataConverter = dataConverter;
        this.xpaths = xpaths;
        this.fieldNames = fieldNames;
        this.response = response;
        this.resultDataFetcher = resultDataFetcher;
    }
    
    
    void onFirstTransaction(SearchTransaction transaction) {
        // On the first transaction tell our client how many results they can expect.
        // they can use that to verify if something went wrong.
        response.setHeader("X-Funnelback-Total-Matching", 
            transaction.getResponse().getResultPacket().getResultsSummary().getTotalMatching() + "");
        
        // Set the content type based on the data Converter
        response.setContentType(dataConverter.getContentType());
        
        try {
            // Create the writter that will be passed to the dataConverter. 
            // must be done to prevent some dataConverters from trying to close the stream to early.
            writter = dataConverter.createWritter(new CloseIgnoringOutputStream(response.getOutputStream()));
            
            // Pass the user set field names to the writter, at this point
            // a CSV writer might write the header of the CSV file using the field names.
            dataConverter.writeHead(fieldNames, writter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void onEachTransaction(Reference<SearchTransaction> transactionRef) {
        // Get the search transaction from the reference then null it so we are the only ones with a 
        // reference.
        SearchTransaction transaction = transactionRef.getValue();
        transactionRef.setValue(null);
        
        // We do something special on the first transaction.
        if(isFirstTransaction.get()) {
            onFirstTransaction(transaction);
            isFirstTransaction.set(false);
        }
        
        // Gets the results out of the transaction then null the transaction itself to ensure
        // we don't have any other paths to the list of Results that would prevent GC
        List<Result> results = transaction.getResponse().getResultPacket().getResults();
        transaction = null;
       
        try {
            // Write the results out to the user.
            if(!writeResults(results, dataConverter, writter, fieldNames, xpaths, isFirstResult.get())) {
                isFirstResult.set(false);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void close() throws IOException {
        dataConverter.writeFooter(writter);
        dataConverter.finished(writter);
    }
    

    public <T> boolean writeResults(List<Result> results, 
        DataConverter<T> dataConverter, 
        T writter,
        List<String> fieldNames,
        List<CompiledExpression> expressions,
        boolean isFirst) throws IOException {

        // It is likely the case that the request fields produces a list of objects
        // that are smaller in size than the entire Result object. Thus we convert the list
        // of Results to just what is request and NULL out the Results so they can be GCed
        // We do this now because we don't need to wait for the user to download anything
        // before we can start having the GC collect objects.
        // The clickTrackingUrl and cacheUrl accounted for 78% of the size of the Results,
        // if that is not requested we can drop the memory requirements considerably.
        List<List<Object>> valuesForResults = gcFriendlyListTraverser(results, 
            (result) -> resultDataFetcher.fetchFeilds(fieldNames, expressions, result));

        gcFriendlyListTraverser(valuesForResults, (values) -> {
            try {
                dataConverter.writeRecord(fieldNames, values, writter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        return isFirst;
    }

    /**
     * Traverses over a list applying converter to each element and returns a new list with
     * the converted elements.
     * This takes special care to null out the converted elements asap to ensure the memory
     * usage is as small as possible.
     * 
     * @param listTOProcess
     * @param converter
     * @return
     */
    <E,T> List<T> gcFriendlyListTraverser(List<E> listTOProcess, Function<E, T> converter) {
        // The array of ptrs wont be very big it is the object themselves that is big. 
        List<T> convertedList = new ArrayList<>(listTOProcess.size());
        for(int i = 0; i < listTOProcess.size(); i++) {
            convertedList.add(converter.apply(listTOProcess.get(i)));

            // NUll out the object we are done with so the GC may collect it.
            listTOProcess.set(i, null);
        }

        return convertedList;
    }
}

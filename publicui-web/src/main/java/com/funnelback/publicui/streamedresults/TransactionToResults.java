package com.funnelback.publicui.streamedresults;

import static lombok.AccessLevel.PACKAGE;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jxpath.CompiledExpression;

import com.funnelback.common.Reference;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.streamedresults.datafetcher.XJPathResultDataFetcher;
import com.funnelback.publicui.streamedresults.outputstreams.CloseIgnoringOutputStream;

import lombok.Getter;
import lombok.Setter;

/**
 * Converts SearchTransactions to results in some form defined by the DataConverter.
 * 
 * <p>This expects to be given multiple SearchTransactions which this will accept
 * and fetch out the the requested fields of each Result (based on xPaths) and then
 * write those fields to the {@link HttpServletResponse#getOutputStream()} in the format
 * the DataConverter implements.</p>
 * 
 * <p>The OutputStream on the HTTP Response will not be closed by this</p>
 *
 */
public class TransactionToResults implements Closeable {

    
    @Getter(PACKAGE) private final DataConverter<Object> dataConverter;
    @Getter(PACKAGE) private final List<CompiledExpression> xpaths;
    @Getter(PACKAGE) private final List<String> fieldNames;
    
    /** Used to detmine if it is looking at the firstTransaction */
    @Getter(PACKAGE) private final AtomicBoolean isFirstTransaction = new AtomicBoolean(true);
    
    /** Used to determine if it is looking at the very first Result */
    @Getter(PACKAGE) private final AtomicBoolean isFirstResult = new AtomicBoolean(true);
    
    @Getter(PACKAGE) private final  HttpServletResponse response;
    
    @Getter(PACKAGE) private final  XJPathResultDataFetcher resultDataFetcher;
    
    /** The writer that must be passed to the dataConverter */
    @Getter(PACKAGE) @Setter(PACKAGE) private Object writer;
    
    
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
        addHeadersFromFirstTransaction(transaction);
        
        // Set the content type based on the data Converter
        response.setContentType(dataConverter.getContentType());
        
        try {
            // Create the writer that will be passed to the dataConverter. 
            // must be done to prevent some dataConverters from trying to close the stream to early.
            writer = dataConverter.createWriter(new CloseIgnoringOutputStream(response.getOutputStream()));
            
            // Pass the user set field names to the writer, at this point
            // a CSV writer might write the header of the CSV file using the field names.
            dataConverter.writeHead(fieldNames, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    void addHeadersFromFirstTransaction(SearchTransaction firstTransaction) {
        // On the first transaction tell our client how many results they can expect.
        // they can use that to verify if something went wrong, or to work how much data
        // they are going to get back.
        if(Optional.ofNullable(firstTransaction).map(SearchTransaction::getResponse)
            .map(SearchResponse::getResultPacket)
            .map(ResultPacket::getResultsSummary)
            .isPresent()) {
            response.setHeader("X-Funnelback-Total-Matching", 
                firstTransaction.getResponse().getResultPacket().getResultsSummary().getTotalMatching() + "");
            response.setHeader("X-Funnelback-Total-Matching-Is-Estimated-Counts", 
                firstTransaction.getResponse().getResultPacket().getResultsSummary().getEstimatedCounts() + "");
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
            if(!writeResults(results, writer, isFirstResult.get())) {
                isFirstResult.set(false);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Finishes off writing the data but does not close the OutputStream of the HTTP response.
     */
    @Override
    public void close() throws IOException {
        dataConverter.writeFooter(writer);
        dataConverter.finished(writer);
    }
    

    /**
     * Passes each result to the dataConverter so that it may be written out.
     * 
     * @param results
     * @param dataConverter
     * @param writer
     * @param fieldNames
     * @param expressions
     * @param isFirst 
     * @return the value of isFirst that should be passed in on the next call.
     * @throws IOException
     */
    <T> boolean writeResults(List<Result> results,
        T writer,
        boolean isFirst) throws IOException {

        // It is likely the case that the request fields produces a list of objects
        // that are smaller in size than the entire Result object. Thus we convert the list
        // of Results to just what is request and NULL out the Results so they can be GCed
        // We do this now because we don't need to wait for the user to download anything
        // before we can start having the GC collect objects.
        // The clickTrackingUrl and cacheUrl accounted for 78% of the size of the Results,
        // if that is not requested we can drop the memory requirements considerably.
        
        // First convert the Result to a List of fields that where requested by the xPaths
        List<List<Object>> valuesForResults = gcFriendlyListTraverser(results, 
            (result) -> resultDataFetcher.fetchFieldValues(xpaths, result));
        
        AtomicBoolean isFirstResult = new AtomicBoolean(isFirst);

        // now pass that list of values requested to the dataConverter so that it might
        // write the values out in some format (e.g. CSV).
        gcFriendlyListTraverser(valuesForResults, (values) -> {
            try {
                
                // Write the separator if results have been written before.
                if(!isFirstResult.get()) {
                    dataConverter.writeSeperator(writer);
                }
                
                dataConverter.writeRecord(fieldNames, values, writer);
                isFirstResult.set(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        return isFirstResult.get();
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

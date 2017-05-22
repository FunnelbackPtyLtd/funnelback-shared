package com.funnelback.publicui.streamedresults;

import java.util.List;

import com.funnelback.publicui.search.model.padre.Result;

/**
 * Fetches the requested fields from the Result object.
 *
 * @param <T>
 */
public interface ResultDataFetcher<T> {

    /**
     * 
     * @param fields an xPath representation of the fields under Result object that are wanted.
     * @return A potentially interpreted object of the xPaths that will be passed to fetchFields.
     */
    public T parseFields(List<String> fields);
    
    /**
     * Fetches the values at the given xPaths denoted in what was given to parseFields from the result object
     * 
     * @param context the result of parseFields().
     * @param result The result to get the fields out of.
     * @return the object at the xPaths as given to parseFields() for the given result.
     */
    public List<Object> fetchFeilds(T context, Result result);
    
}

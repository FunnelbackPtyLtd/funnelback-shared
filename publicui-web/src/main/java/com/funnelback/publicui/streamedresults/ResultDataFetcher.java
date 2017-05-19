package com.funnelback.publicui.streamedresults;

import java.util.List;

import com.funnelback.publicui.search.model.padre.Result;

public interface ResultDataFetcher<T> {

    public T parseFields(List<String> fields);
    
    public List<Object> fetchFeilds(List<String> fieldNames, T context, Result result);
    
}

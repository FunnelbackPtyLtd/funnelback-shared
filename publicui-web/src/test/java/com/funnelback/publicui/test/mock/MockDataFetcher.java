package com.funnelback.publicui.test.mock;

import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.lifecycle.data.AbstractDataFetcher;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class MockDataFetcher extends AbstractDataFetcher {

    @Getter @Setter
    private boolean traversed = false;
    
    @Setter
    private boolean throwError = false;
    
    @Override
    public void fetchData(SearchTransaction searchTransaction) throws DataFetchException {
        traversed = true;
        if (throwError) {
            throw new DataFetchException(MockDataFetcher.class.getName(), null);
        }
    }
    
    

}

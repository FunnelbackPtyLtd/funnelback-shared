package com.funnelback.publicui.search.lifecycle.data.fetchers.padre;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.xml.XmlParsingException;

/**
 * <p>Implementation of {@link AbstractPadreForking} that runs a query
 * without faceted navigation constraints and by setting the number
 * of documents returned to zero.</p>
 * 
 * <p>This is intended to run as a second query to gather the full facet
 * tree in addition to the main query.</p>
 */
@Component
public class FacetedNavPadreForking extends AbstractPadreForking {

    @Override
    protected String getQueryString(SearchTransaction transaction) {
        return new PadreQueryStringBuilder(transaction.getQuestion(), false).buildQueryString();
    }

    @Override
    protected void updateTransaction(SearchTransaction transaction, ExecutionReturn padreOutput) throws XmlParsingException {
        throw new IllegalStateException("Not yet implemented");
    }

}

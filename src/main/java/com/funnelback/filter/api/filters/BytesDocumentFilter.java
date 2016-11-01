package com.funnelback.filter.api.filters;

import com.funnelback.filter.api.FilterContext;
import com.funnelback.filter.api.FilterResult;
import com.funnelback.filter.api.documents.BytesDocument;
import com.funnelback.filter.api.documents.FilterableDocument;

/**
 * Filters a document where the content is converted to a byte[].
 * 
 * <p>The document will only call {@link #filterAsBytesDocument(BytesDocument, FilterContext)}
 * if {@link #canFilter(FilterableDocument, FilterContext)} returns true.</p>
 * 
 *
 */
public interface BytesDocumentFilter extends Filter {

    /**
     * Checks if the document can be filtered.
     * 
     * <p>This provides the filter an opportunity to avoid the filtering method
     * from being called. This can provide some speed up by avoiding a conversion
     * of the document to {@link BytesDocument} when the document will not be filtered.</p>
     * 
     * <p>Typically this method is used to inspect non content parts of the document
     * to determine if the document looks like something that can be filtered. For example
     * we may check that the document has a mime type of application/pdf before we attempt
     * to do any filtering of the document.</p> 
     * 
     * <p>The result of this method determines if the filter method is called, 
     * it is acceptable for this method to return <code>true</code> while
     * the filter method could return <code>FilterResult.skipped()</code>. As
     * it may not be known if the filter should be skipped until the document
     * contents are inspected.</p>
     * 
     * @param document which may have its meta data and other non content parts inspected
     * to determine if an attempt should be made to filter the document.  
     * @param context under which the filter is running.
     * @return {@link PreFilterCheck#ATTEMPT_FILTER} if the filter method should be called 
     * otherwise {@link PreFilterCheck#SKIP_FILTER}, to skip this Filter.
     */
    public PreFilterCheck canFilter(FilterableDocument document, FilterContext context);
    
    /**
     * Filters the {@link BytesDocument}
     * <p>Called when {@link #canFilter(FilterableDocument, FilterContext)} returns
     * {@link PreFilterCheck#ATTEMPT_FILTER}</p> 
     * 
     * @param document to be filtered, which has been converted into a RawFilterableDocument
     * @param context under which the filter is running. 
     * @return the result of executing this filter, the result may be {@link FilterResult#skipped()}.
     */
    public FilterResult filterAsBytesDocument(BytesDocument document, FilterContext context);
    
    /**
     * Filter method responsible for calling {@link #canFilter(FilterableDocument, FilterContext)} and {@link #filterAsBytesDocument(BytesDocument, FilterContext)}
     * 
     * <p>Typically this method should not be overridden.</p>
     * {@inheritDoc}
     */
    @Override
    public default FilterResult filter(FilterableDocument document, FilterContext context) {
        if(this.canFilter(document, context) == PreFilterCheck.ATTEMPT_FILTER) {
            return this.filterAsBytesDocument(BytesDocument.from(document), context);
        }
        return FilterResult.skipped();
    }
}

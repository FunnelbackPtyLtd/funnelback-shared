package com.funnelback.filter.api.filters;

import com.funnelback.filter.api.FilterContext;
import com.funnelback.filter.api.FilterResult;
import com.funnelback.filter.api.documents.FilterableDocument;
import com.funnelback.filter.api.documents.NoContentDocument;
import com.funnelback.filter.api.documents.StringDocument;

/**
 * Filter a document where the content is converted to a String.
 * 
 * <p>The document will only call {@link #filterAsStringDocument(StringDocument, FilterContext)} 
 * if both {@link #canFilter(FilterableDocument, FilterContext)} returns true 
 * and the document can be converted into a String.</p>
 * 
 * <p>After a document has been converted into a StringDocument, the encoding 
 * of the raw bytes will be UTF-8, if the document defines the encoding type it
 * must be changed to UTF-8.</p>
 *
 */
public interface StringDocumentFilter extends Filter {

    /**
     * Checks if the document can be filtered.
     * 
      <p>This provides the filter an opportunity to avoid the filtering method
     * from being called. This can provide some speed up by avoiding a conversion
     * of the document to {@link StringDocument} when the document will not be 
     * filtered.</p>
     * 
     * <p>Typically this method is used to inspect non content parts of the document
     * to determine if the document looks like something that can be filtered. For example
     * we may check that the document has a mime type of text/text before we attempt
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
    public PreFilterCheck canFilter(NoContentDocument document, FilterContext context);
    
    /**
     * Filters the {@link StringDocument}
     * 
     * <p>Called when {@link #canFilter(FilterableDocument, FilterContext)} returns
     * {@link PreFilterCheck#ATTEMPT_FILTER}</p> 
     * @param document to be filtered where the document content has been converted into a String
     * @param context under which the filter is running. 
     * @return the result of executing this filter, the result may be {@link FilterResult#skipped()}.
     */
    public FilterResult filterAsStringDocument(StringDocument document, FilterContext context);
    
    /**
     * Filter method responsible for calling {@link #canFilter(FilterableDocument, FilterContext)} and {@link #filterAsStringDocument(StringDocument, FilterContext)}
     * 
     * <p>Typically this method should not be overridden.</p>
     * {@inheritDoc}
     */
    public default FilterResult filter(FilterableDocument document, FilterContext context) {
        if(this.canFilter(document, context) == PreFilterCheck.ATTEMPT_FILTER) {
            return StringDocument
                .from(document).map(doc -> this.filterAsStringDocument(doc, context))
                .orElse(FilterResult.skipped());
        }
        return FilterResult.skipped();
    }
    
}

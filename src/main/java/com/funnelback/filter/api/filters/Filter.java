package com.funnelback.filter.api.filters;

import com.funnelback.filter.api.FilterContext;
import com.funnelback.filter.api.FilterResult;
import com.funnelback.filter.api.documents.FilterableDocument;

/**
 * Filters a document.
 * 
 * <p> A Filter can take a single document change it and return a new document by returning:
 * a {@link FilterResult}. A filter result can either consist of zero documents (removing the 
 * document possibly stopping it from being stored, one document that may have been the result 
 * of editing the given document, many documents which may be the result of splitting the
 * given document or {@link FilterResult#skipped()} to mark that this filter is making no
 * changes and should be considered skipped.</p>
 * 
 * <p>Typically it is simpler to implement {@link StringDocumentFilter} or {@link BytesDocumentFilter } 
 * than it is to implement this.</p>
 *
 */
public interface Filter extends NamedFilter {

    /**
     * Filter a document.
     * 
     * <p>Typically the interfaces {@link BytesDocumentFilter} or {@link StringDocumentFilter}
     * are implemented instead of this to make getting access to the document content easier.
     * This filter should be used when other filters do not make sense such as a filter which
     * does not care about the content.</p>
     * 
     * <p>Filtering only throws unchecked exceptions. In the case a unchecked exception is
     * thrown filtering for that document is stopped and the document may or not be stored.
     * When an exception is raised in a filter the filter can choose to:</p>
     * <ul>
     * <li>Re-throw the exception as a unchecked exception, ideally as a {@link FilterException}.</li>
     * <li>Return {@link FilterResult#isSkipped()} to indicate that the filter should be considered skipped, in
     * the case of a choice filter this will cause it to try the next filter.</li>
     * <li>Ignore the exception and return any other type of {@link FilterResult} with or without changes
     * to the given document.</li>
     * </ul>
     * 
     * If a checked Exception 
     * is raised the filter should catch the exception and typically return the original document 
     * or return {@link FilterResult#skipped()}.
     * 
     * @param document The document to be filtered. 
     * @param context the filter is running in. 
     * @return A FilterResilt which comes from one of the static methods on FilterResult.
     * @throws RuntimeException when a programming error occurs and the filtering of the given
     * document should be considered a failure. Within gathering this may stop the document from
     * being stored, however other documents will continue to be filtered. 
     * @throws FilterException when an error occurs during the filtering of the document, and the 
     * document should not be filtered any further. Within gathering this may stop the document 
     * from being stored, howeber other documents will contine to be filtered.
     */
    FilterResult filter(FilterableDocument document, FilterContext context) throws RuntimeException, FilterException;
    
}

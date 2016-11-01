package com.funnelback.filter.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.funnelback.filter.api.documents.FilterableDocument;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * The result of filtering a document.
 * <p>Can either be FilterResult.skipped() if filtering was not done or 
 * FilterResult.of() if the documents where filtered</p>
 *
 */
@EqualsAndHashCode
public final class FilterResult {

    private static FilterResult SKIPPED = new FilterResult(true, Collections.emptyList());
    
    /**
     * Marks a filter as being skipped.
     * 
     * <p>A skipped filter tells other filters (ChoiceFilter and ChainFilter)
     * that this filter was unable to run. A skipped status lets the choice filter
     * choose a different filter to run. A skipped status causes the chain filter to
     * pass the input document to the next filter in the chain.</p>
     * 
     * @return a skipped FilterResult.
     */
    public static FilterResult skipped() {
        return SKIPPED;
    }
    
    /**
     * Returned by a filter when it wants to delete the given document.
     * 
     * <p>This is used to claim that the filer ran and decided to remove the given
     * document. This is the same as passing an empty list to of().</p>
     * @return a FilterResult containing zero documents.
     */
    public static FilterResult delete() {
        return new FilterResult();
    }
    
    /**
     * Returned by a filter when it has filtered a document into one documents.
     * 
     * <p>This is used to claim that the filter ran and the given document is
     * the result of the filter running. This may be called with the original 
     * unmodified document to show that the filter ran and stopping the choice
     * filter from choosing another filter.</p>
     * 
     * @param filteredDocument the resulting document from filtering.
     * @return a FilterResult containing one document.
     */
    public static FilterResult of(FilterableDocument filteredDocument) {
        return new FilterResult(filteredDocument);
    }
    

    /**
     * Returned by a filter when it has filtered a document into many documents.
     * 
     * <p>This is used to claim that that filter ran and produced the given
     * collection of documents. The collection of documents can be empty showing
     * the filter ran yet removed the input document.</p>
     * 
     * @param filteredDocuments the result of filtering a document which can be zero or
     * more documents.
     * @return a FilterResult containing the given list of documents.
     */
    public static FilterResult of(Collection<? extends FilterableDocument> filteredDocuments) {
        return new FilterResult(filteredDocuments);
    }
    
    @Getter private boolean skipped;
    
    @Getter private List<FilterableDocument> filteredDocuments;
    
    private FilterResult(FilterableDocument filterableDocument) {
        this.skipped = false;
        this.filteredDocuments = new ArrayList<>();
        this.filteredDocuments.add(filterableDocument);
    }
    
    private FilterResult() {
        this.skipped = false;
        this.filteredDocuments = new ArrayList<>();
    }
    
    private FilterResult(boolean skipped, List<FilterableDocument> filteredDocuments) {
        this.skipped = skipped;
        this.filteredDocuments = filteredDocuments;
    }
    
    private FilterResult(Collection<? extends FilterableDocument> filteredDocs) {
        this.skipped = false;
        this.filteredDocuments = new ArrayList<>(filteredDocs);
    }
    
}

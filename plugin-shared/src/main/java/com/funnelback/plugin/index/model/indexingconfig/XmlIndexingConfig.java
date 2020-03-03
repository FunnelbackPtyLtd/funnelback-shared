package com.funnelback.plugin.index.model.indexingconfig;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class XmlIndexingConfig {

    /**
     * When empty the field 'whenNoContentPaths' describes what is 
     * indexed, when non empty only the text within the given paths are indexed.
     */
    private List<ContentPath> contentPaths = new ArrayList<>();
    
    /**
     * Defines where the documents are within the XML, this can be
     * used to split XML documents. Like urlPaths it is recommended to do this 
     * within a filter.
     */
    private List<DocumentPath> documentPaths = new ArrayList<>();
    
    /**
     * The type at this path (e.g. HTML, PDF, DOC) will be used 
     * by the query process to report the original document type. The last file 
     * type found will be used.
     */
    private List<FileTypePath> fileTypePaths = new ArrayList<>();
    
    /**
     * Maps an element withing an XML document which contains a 
     * (XML escaped) document that may itself be HTML/XML/text. e.g. 
     * /root/html could be a path to an element which contains HTML. The indexer 
     * will index that document as though it is HTML.
     */
    private List<InnerDocumentPath> innerDocumentPaths = new ArrayList<>();
    
    /**
     * The URL at this path will be used as the documents URL. This will 
     * typically cause cached copies to no longer work, this can not be used with 
     * Push collections, this path must come before inner HTML documents with links. 
     * It is recommended that filtering be used to change the URL instead to avoid 
     * those issues
     */
    private List<UrlPath> urlPaths = new ArrayList<>();
    
    /**
     * May be set null.
     */
    @Setter
    private WhenNoContentPathsAreSet whenNoContentPathsAreSet;
    
}

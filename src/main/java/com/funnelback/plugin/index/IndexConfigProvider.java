package com.funnelback.plugin.index;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import java.io.InputStream;

import com.funnelback.plugin.index.model.indexingconfig.XmlIndexingConfig;
import com.funnelback.plugin.index.model.querycompletion.QueryCompletionCSV;

/**
 * An interface that my be implemented in a plugin to control indexing.
 *
 */
public interface IndexConfigProvider {

    /**
     * Return external metadata in the raw format as defined in documentation.
     * 
     * Invalid formats are likely to cause silent errors.
     * 
     * TODO define how this will be combined.
     */
    public default InputStream externalMetadata(IndexConfigProviderContext context) {
        return null;
    }
    
    /**
     * Supply additional metadata mappings from the plugin.
     * 
     * 
     * Returns a List of Maps where each Map has:
     * name: the name of the metadata class e.g. "a"
     * type: The type of the metadata class, will be used if the type
     * has not been previously declared. The type must be one of:
     * 'TEXT_NOT_INDEXED_AS_DOCUMENT_CONTENT', 'TEXT_INDEXED_AS_DOCUMENT_CONTENT', 
     * 'GEOSPATIAL', 'NUMERICAL', 'SECURITY', 'DATE'.
     * sources: Must be a list of maps.
     * 
     * And each source map must contain:
     * sourceType: Defines where the metadata comes from must be one of
     * 'XML', 'HTML_OR_HTTP_HEADERS'
     * locator: Defines the location from where the metadata comes from.
     * 
     * To map metadata "author" from HTML documents to metadata class "a"
     * and map XML path /root/secure to 'SECURITY' field 'S':
     * 
     * List.of(
     *     Map.of("name", "a",
     *         "type", "TEXT_INDEXED_AS_DOCUMENT_CONTENT",
     *         "sources", List.of(
     *                              Map.of("sourceType", "HTML_OR_HTTP_HEADERS", "locator", "author"))
     *                         ),
     *     Map.of("name", "S",
     *         "type", "SECURITY",
     *         "sources", List.of(
     *                              Map.of("sourceType", "XML", "locator", "/root/secure"))
     *                         )
     *     );
     * 
     * Note that this model is a sub set of the model returned by the API, refer to the API
     * for further details. The response of the API can be returned here so long as it is 
     * converted into standard java types.
     * 
     * Should any conflicts arise, the plugin will have lower priority and so will be unable
     * to override anything previously set. In the case that a metadata class is already mapped
     * sources for that metadata class will be appended to the existing metadata class unless the 
     * source is already in use. In the future this may change and a single locator may be used for 
     * multiple metadata classes.
     * 
     * @return
     */
    public default List<Map<String, Object>> metadataMappings(IndexConfigProviderContext context) {
        
        return List.of();
    }
    
    // IGNORE
//    /**
//     * Supply additional configuration used for XML processing.
//     * 
//     * The map may contain any of the following keys and values which will be appended 
//     * to the existing XML configuration assuming the path is already not in use.
//     * The keys are a String and the values for these are all  List of Maps
//     * where the map contains a key "path" and the value is the wanted path.
//     * 
//     * contentPaths: When empty the field 'whenNoContentPaths' describes what is 
//     *  indexed, when non empty only the text within the given paths are indexed.
//     * documentPaths: Defines where the documents are within the XML, this can be
//     *  used to split XML documents. Like urlPaths it is recommended to do this 
//     *  within a filter.
//     *  
//     * fileTypePaths: The type at this path (e.g. HTML, PDF, DOC) will be used 
//     *  by the query process to report the original document type. The last file 
//     *  type found will be used.
//     *  
//     * innerDocumentPaths: Maps an element withing an XML document which contains a 
//     *  (XML escaped) document that may itself be HTML/XML/text. e.g. 
//     *  /root/html could be a path to an element which contains HTML. The indexer 
//     *  will index that document as though it is HTML.
//     *  
//     * urlPaths: The URL at this path will be used as the documents URL. This will 
//     * typically cause cached copies to no longer work, this can not be used with 
//     * Push collections, this path must come before inner HTML documents with links. 
//     * It is recommended that filtering be used to change the URL instead to avoid 
//     * those issues
//     * 
//     * Additionally the outer map may also specify 'whenNoContentPathsAreSet',
//     * unlike other keys if this is set it will override what has been previously set.
//     * 
//     * whenNoContentPathsAreSet: efines what should be indexed as document content when 
//     * no paths are mapped as 'ContentPaths'. The two options available are 
//     * 'INDEX_ALL_UNMAPPED_AS_CONTENT' whichwill index all unmapped paths (e.g. paths 
//     * not used as metadata, url or inner documents) as document content. The other 
//     * option 'DONT_INDEX_UNMAPPED_AS_CONTENT' which will NOT index any paths that are 
//     * left unmapped. Note that if some path is mapped as a 'ContentPath', then this 
//     * setting has no effect.
//     * 
//     * Example:
//     * Map.of(
//     *      "contentPaths", List.of(Map.of("path", "/root/content")),
//     *      "documentPaths", List.of(Map.of("path", "/root/documents")),
//     *      "fileTypePaths", List.of(Map.of("path", "/root/mime")),
//     *      "innerDocumentPaths", List.of(Map.of("path", "/root/html")),
//     *      "urlPaths", List.of(Map.of("path", "/root/urls")),
//     *      "whenNoContentPathsAreSet", "INDEX_ALL_UNMAPPED_AS_CONTENT");
//     *      
//     * Note that this model is a sub set of the model returned by the API, refer to the API
//     * for further details. The response of the API can be returned here so long as it is 
//     * converted into standard java types.
//     * 
//     * @return
//     */
    
    /**
     * Supply additional configuration used for XML processing.
     * 
     * The contentPaths, documentPaths, fileTypePaths, innerDocumentPaths and
     * urlPaths will be appended to the existing XML indexing configuration. The
     * whenNoContentPathsAreSet field however will replace any existing value set,
     * this it is not required that the pugin sets this.
     * 
     * Example:
     * <code>
     * import com.funnelback.plugin.index.model.indexingconfig.*;
     * 
     * XmlIndexingConfig xmlIndexingConfig = new XmlIndexingConfig();
     * xmlIndexingConfig.getContentPaths().add(new ContentPath(""));
     * xmlIndexingConfig.getDocumentPaths().add(new DocumentPath(""));
     * xmlIndexingConfig.getFileTypePaths().add(new FileTypePath(""));
     * xmlIndexingConfig.getInnerDocumentPaths().add(new InnerDocumentPath(""));
     * 
     * // This plugin requires that all unmapped content is indexed as document content.
     * // If your plugin doesn't care what this is set to leave it null
     * xmlIndexingConfig.setWhenNoContentPathsAreSet(WhenNoContentPathsAreSet.INDEX_ALL_UNMAPPED_AS_CONTENT);
     * </code>
     * 
     * Note that this model is a sub set of the model returned by the API, refer to the API
     * for further details.
     * 
     * @return The XmlIndexingConfig to use.
     */
    public default XmlIndexingConfig xmlIndexingConfig(IndexConfigProviderContext context) {
        return new XmlIndexingConfig();
    }
    
    /**
     * Supply additional exact match URLs to kill.
     * 
     * Like kill_exact.cfg
     * 
     * See also TODO some API.
     * 
     * A Stream can be made from a list if the list is small for example:
     * <code>
     *     return List.of("http://example.com/index.html").stream();
     * </code>
     * 
     * @return a Stream of URLs to kill by "exact" match.
     * 
     */
    public default Stream<String>  killByExactMatch(IndexConfigProviderContext context) {
        return Stream.of();
    }
    
    /**
     * Supply additional partial match URLs to kill.
     * 
     * Like kill_partial.cfg
     * s
     * See also TODO some API.
     * 
     * A Stream can be made from a list if the list is small for example:
     * <code>
     *     return List.of("http://example.com/").stream();
     * </code>
     * 
     * @return a Stream of URLs to kill by "exact" match.
     * 
     */
    public default Stream<String>  killByPartialMatch(IndexConfigProviderContext context) {
        return Stream.of();
    }
    
    /**
     * Supply additional faceted navigation configuration.
     * 
     * Additional faceted navigation is supplied as a JSON similar to the API.
     * 
     * 
     * 
     * @return 
     */
    public default String extraFacetedNavigation(IndexConfigProviderContext context) {
        return null;
    }
    
    /**
     * Define profiles for which query completion will be supplied
     * 
     * This expects to return the data for all profiles rather than just a single.
     * This is done as typically the CSV can be large and is often shared between multiple 
     * profiles.
     *   
     * 
     * @param contextForAllProfiles The context for all profiles which exists.
     * @return A list of QueryCompletionCSV objects each of which contain the profiles
     * it should apply to along with the CSV for example: 
     * <code>
     * List.of(new QueryCompletionCSV(List.of("_default", "news"),  () -> {return new MyQueryCompletionCSVInputStream();}));
     * </code>
     * 
     */
    public default List<QueryCompletionCSV> queryCompletionCSVForProfile(List<IndexConfigProviderContext> contextForAllProfiles) {
        return List.of();
    }
}

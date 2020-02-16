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
     * FUN-13612
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
     * // If your plugin doesn't care what this is set to leave it null.
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
     * Supply a stream of URLs to kill by exact match.
     * 
     * The behaviour of the returned URLs matches the behaviour as if they where
     * appended to kill_exact.cfg.
     * 
     * A Stream can be made from a list if the list is small for example:
     * <code>
     *     return List.of("http://example.com/index.html").stream();
     * </code>
     * 
     * @return a Stream of URLs to kill by "exact" match.
     * 
     */
    public default Stream<String> killByExactMatch(IndexConfigProviderContext context) {
        return Stream.of();
    }
    
    /**
     * Supply a stream of URLs to kill by partial match.
     * 
     * The behaviour of the returned URLs matches the behaviour as if they where
     * appended to kill_partial.cfg.
     * 
     * A Stream can be made from a list if the list is small for example:
     * <code>
     *     return List.of("http://example.com/").stream();
     * </code>
     * 
     * @return a Stream of URLs to kill by "partial" match.
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
     * This expects to return a list <code>[]</code> of Facets. The id,
     * lastModified and created fields do not need to be set.
     * 
     * It may be easier to design the facets you want using the UI, then use the API
     * to get out each facet you want:
     * GET /faceted-navigation/v2/collections/{collection}/profiles/{profile}/facet/{id}/views/{view}
     * The value wanted is what is set in the <code>data</code> field. Then string the facet 
     * definitions together as a JSON array.  
     * 
     * 
     * @return A JSON list of facet definitions to use. 
     */
    public default String extraFacetedNavigation(IndexConfigProviderContext context) {
        return null;
    }
    
    /**
     * Supply query completion CSV files to use on profiles.
     * 
     * The QueryCompletionCSV supports defining a single CSV "file" for many profiles. The
     * CSV is actually given as a supplier of a InputStream. This means for example you could 
     * have some profiles get their query completion CSV by contacting a remote server.
     *   
     * 
     * @param contextForAllProfiles The context for all profiles which exists.
     * @return A list of QueryCompletionCSV objects each of which contain the profiles
     * it should apply to along with the CSV. For example, to apply the CSV from a remote
     * web server to the profiles "_default" and "news" you could do something similar to:
     * <code>
     * List.of(
     *  new QueryCompletionCSV(List.of("_default", "news"),  () -> {return new URLFetchingInputStream("https://example.com/);})
     *  );
     * </code>
     * 
     */
    public default List<QueryCompletionCSV> queryCompletionCSVForProfile(List<IndexConfigProviderContext> contextForAllProfiles) {
        return List.of();
    }
}

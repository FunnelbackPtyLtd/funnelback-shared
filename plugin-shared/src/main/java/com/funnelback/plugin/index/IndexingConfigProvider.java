package com.funnelback.plugin.index;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import java.io.InputStream;

import com.funnelback.plugin.index.consumers.GscopeByQueryConsumer;
import com.funnelback.plugin.index.consumers.GscopeByRegexConsumer;
import com.funnelback.plugin.index.model.indexingconfig.XmlIndexingConfig;
import com.funnelback.plugin.index.model.querycompletion.QueryCompletionCSV;

/**
 * An interface that my be implemented in a plugin to control indexing.
 *
 */
public interface IndexingConfigProvider {

    /**
     * Return external metadata in the raw format as defined in documentation.
     * 
     * Invalid formats are likely to cause indexing errors.
     * 
     * Each external metadata provided by plugins and from conf, do no interact with 
     * each other. This means if two plugins provided an entry for the same URL pattern
     * and the same metadata, both values would be applied. e.g.
     * Plugin 1 provides:
     * <code>
     * http://example.com/a foo:bar
     * </code>
     * plugin 2 provides:
     * <code>
     * http://example.com/a foo:foobar
     * </code>
     * 
     * A document like:
     * <code>
     * http://example.com/a
     * </code>
     * Would have both metadata values "bar" and "foobar" in the metadata class "foo".
     * 
     * 
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
     * thus the plugin may leave whenNoContentPathsAreSet as null to not overwrite
     * what it is set to.
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
     * Supply gscopes that must be set on a document when a regular expression matches the URL.
     * 
     * For example, to set the gscope 'isDocument' for all documents within:
     * 'example.com/documents/ set:
     * 
     * <code>
     * consumer.accept("isDocument", "example\\.com/documents/"); 
     * </code>
     * 
     * Note that the regex special character '.' is escaped with '\' which is a java character
     * which must be escaped with another '\' thus '\\.'.
     * 
     * The consumer may be called multiple times to configure multiple gscopes to be 
     * set by various regular expressions. For example:
     * 
     * <code>
     * consumer.accept("cat", ".*cat.*");
     * consumer.accept("cat", ".*kitty.*");
     * consumer.accept("dog", ".*dog.*");
     * </code>
     * 
     * @param consumer Accepts gscopes that will be set when the URL matches the regex.
     */
    public default void supplyGscopesByRegex(GscopeByRegexConsumer consumer) {
        
    }
    
    /**
     * Supply gscopes that must be set on a document when a query matches the document.
     * 
     * For example, to set the gscope 'isDocument' for all documents matching the query:
     * "word document"
     * 
     * <code>
     * consumer.accept("isDocument", "word document"); 
     * </code>
     * 
     * Multiple queries can be supplied and query language may be used. For example, 
     * set gscope 'org' on documents containing the term 'enterprise' or 'organisation', 
     * and gscope 'public' on documents containing both 'public' and 'internet'.
     * 
     * <code>
     * consumer.accept("org", "[enterprise organisation]"); 
     * // Use mandatory inclusion operator to ensure both terms are present.
     * consumer.accept("public", "|public |internet"); 
     * </code>
     * 
     * @param consumer Accepts gscopes that will be set then the document matches the query.
     */
    public default void supplyGscopesByQuery(GscopeByQueryConsumer consumer) {
        
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

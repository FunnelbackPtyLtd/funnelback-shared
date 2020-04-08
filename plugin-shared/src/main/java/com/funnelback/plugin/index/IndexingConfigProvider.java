package com.funnelback.plugin.index;

import java.util.List;
import java.util.Map;

import java.io.InputStream;

import com.funnelback.plugin.index.consumers.ExternalMetadataConsumer;
import com.funnelback.plugin.index.consumers.GscopeByQueryConsumer;
import com.funnelback.plugin.index.consumers.GscopeByRegexConsumer;
import com.funnelback.plugin.index.consumers.KillByExactMatchConsumer;
import com.funnelback.plugin.index.consumers.KillByPartialMatchConsumer;
import com.funnelback.plugin.index.model.indexingconfig.XmlIndexingConfig;
import com.funnelback.plugin.index.model.querycompletion.QueryCompletionCSV;

/**
 * An interface that my be implemented in a plugin to control indexing.
 *
 */
public interface IndexingConfigProvider {

    /**
     * Allows supplying external metadata.
     * 
     * Example:
     * <code>
     * // Apply metadata to documents with URLs starting with https://foo.com/documents/
     * consumer.addMetadataToPrefix("https://foo.com/documents/", ImmutableListMultimap.of(
     *     "type", "doc", 
     *     "notes", "informative",
     *     "notes", "boring"));
     * 
     * // Apply metadata to documents with URLs starting with https://foo.com/videos/
     * ListMultimap<String, String> metadata = ArrayListMultimap.create();
     * metadata.put("type", "video");
     * metadata.put("notes", "sometimes informative");
     * consumer.addMetadataToPrefix("https://foo.com/videos/", metadata);
     * </code>
     * 
     * This would result in a URL such as https://foo.com/documents/apollo11owenermanual.pdf 
     * as having the metadata: type=doc, notes=informative and notes=boring. The document 
     * would match the following queries:
     * <p><ul>
     * <li>type:doc
     * <li>notes:informative
     * <li>notes:boring
     * </ul><p>
     * 
     * Each external metadata provided by plugins and from conf, do no interact with 
     * each other. This means if two plugins provided an entry for the same URL pattern
     * and the same metadata, both values would be applied. e.g.
     * Plugin 1 provides:
     * <code>
     * consumer.accept("http://example.com/a", ImmutableListMultimap.of("foo", "bar"));
     * </code>
     * plugin 2 provides:
     * <code>
     * consumer.accept("http://example.com/a", ImmutableListMultimap.of("foo", "foobar"));
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
    public default InputStream externalMetadata(IndexConfigProviderContext context, ExternalMetadataConsumer consumer) {
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
     * Supply URLs to kill by exact match.
     * 
     * The behaviour of the returned URLs matches the behaviour as if they where
     * appended to kill_exact.cfg.
     * 
     * Example:
     * To kill both documents "http://example.com/list.xml" and 
     * "http://example.com/notthisone.html".
     * 
     * <code>
     * consumer.killByExactMatch("http://example.com/list.xml");
     * consumer.killByExactMatch("http://example.com/notthisone.html");
     * </code>
     * 
     */
    public default void killByExactMatch(IndexConfigProviderContext context, KillByExactMatchConsumer consumer) {
        consumer.killByExactMatch("http://example.com/list.xml");
        consumer.killByExactMatch("http://example.com/notthisone.html");
    }
    
    /**
     * Supply URLs to kill by partial match.
     * 
     * The behaviour of the returned URLs matches the behaviour as if they where
     * appended to kill_partial.cfg.
     * 
     * Example:
     * Kill URLs in example.com that are under the "/beta/" or "/invalid/" paths.
     * <code>
     * consumer.killByPartialMatch("https://example.com/beta/");
     * consumer.killByPartialMatch("https://example.com/invalid/");
     * </code>
     * 
     */
    public default void killByPartialMatch(IndexConfigProviderContext context, KillByPartialMatchConsumer consumer) {   
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
     * consumer.applyGscopeWhenRegexMatches("cat", ".*cat.*");
     * consumer.applyGscopeWhenRegexMatches("cat", ".*kitty.*");
     * consumer.applyGscopeWhenRegexMatches("dog", ".*dog.*");
     * </code>
     * 
     * @param consumer Accepts gscopes that will be set when the URL matches the regex.
     */
    public default void supplyGscopesByRegex(IndexConfigProviderContext context, GscopeByRegexConsumer consumer) {
        
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
     * consumer.applyGscopeWhenQueryMatches("org", "[enterprise organisation]"); 
     * // Use mandatory inclusion operator to ensure both terms are present.
     * consumer.applyGscopeWhenQueryMatches("public", "|public |internet"); 
     * </code>
     * 
     * @param consumer Accepts gscopes that will be set then the document matches the query.
     */
    public default void supplyGscopesByQuery(IndexConfigProviderContext context, GscopeByQueryConsumer consumer) {
        
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

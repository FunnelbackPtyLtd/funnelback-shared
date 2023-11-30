package com.funnelback.plugin.index;

import com.funnelback.plugin.index.consumers.*;
import com.funnelback.plugin.index.model.indexingconfig.XmlIndexingConfig;
import com.funnelback.plugin.index.model.metadatamapping.MetadataSourceType;
import com.funnelback.plugin.index.model.metadatamapping.MetadataType;

import java.util.List;

/**
 * An interface that may be implemented in a plugin to control indexing.
 *
 */
public interface IndexingConfigProvider {

    /**
     * Allows supplying external metadata.
     * 
     * Example:
     * <pre>{@code 
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
     * }</pre>
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
     * <pre>{@code 
     * consumer.addMetadataToPrefix("http://example.com/a", ImmutableListMultimap.of("foo", "bar"));
     * }</pre>
     * plugin 2 provides:
     * <pre>{@code 
     * consumer.addMetadataToPrefix("http://example.com/a", ImmutableListMultimap.of("foo", "foobar"));
     * }</pre>
     * 
     * A document like:
     * <pre>{@code 
     * http://example.com/a
     * }</pre>
     * Would have both metadata values "bar" and "foobar" in the metadata class "foo".
     * 
     * 
     */
    public default void externalMetadata(IndexConfigProviderContext context, ExternalMetadataConsumer consumer) {
    }
    
    /**
     * Supply additional metadata mappings from the plugin.
     * 
     * 
     * Metadata must be mapped into a metadata class e.g. "a", in general these must be no longer than 
     * 64 chars and must be alpha numeric.
     * Each metadata class has a type and this is defined by the enum {@link MetadataType}.
     * A source of the metadata must also be defined, this is done by defining a source type and locator.
     * A sourceType is defined by the enum {@link MetadataSourceType}.
     * A locator is used to find the metadata within the source e.g. XML or HTML_OR_HTTP_HEADERS.
     * 
     * To map HTML metadata "author" and "publisher" to metadata class "a"
     * and map XPath /root/secure to 'SECURITY' field 'S':
     * 
     * <pre>{@code 
     * consumer.map("a", MetadataType.TEXT_INDEXED_AS_DOCUMENT_CONTENT, MetadataSourceType.HTML_OR_HTTP_HEADERS, "author");
     * consumer.map("a", MetadataType.TEXT_NOT_INDEXED_AS_DOCUMENT_CONTENT, MetadataSourceType.HTML_OR_HTTP_HEADERS, "publisher");
     * consumer.map("S", MetadataType.SECURITY, MetadataSourceType.HTML_OR_HTTP_HEADERS, "/root/secure");
     * }</pre>
     * 
     * 
     * Should any conflicts arise, the first definition will be accepted. In the above example metadata class
     * "a" would be set to "MetadataType.TEXT_INDEXED_AS_DOCUMENT_CONTENT" unless "a" was previously set to
     * some other type.
     * 
     * The same applies for MetadataSourceType and the locator, the first definition of a locator will win thus for:
     * <pre>{@code 
     * consumer.map("x", MetadataType.TEXT_INDEXED_AS_DOCUMENT_CONTENT, MetadataSourceType.HTML_OR_HTTP_HEADERS, "author");
     * consumer.map("y", MetadataType.TEXT_INDEXED_AS_DOCUMENT_CONTENT, MetadataSourceType.HTML_OR_HTTP_HEADERS, "author");
     * }</pre>
     * "author" from HTML metadata or HTTP headers will be written into metadata "x" only. This may change in the future.
     * 
     * Plugins always have a lower priority then what is set in a collection's configuration.
     * 
     */
    public default void metadataMappings(IndexConfigProviderContext context, MetadataMappingConsumer consumer) {
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
     * <pre>{@code 
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
     * }</pre>
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
     * <pre>{@code 
     * consumer.killByExactMatch("http://example.com/list.xml");
     * consumer.killByExactMatch("http://example.com/notthisone.html");
     * }</pre>
     * 
     */
    public default void killByExactMatch(IndexConfigProviderContext context, KillByExactMatchConsumer consumer) {
    }
    
    /**
     * Supply URLs to kill by partial match.
     * 
     * The behaviour of the returned URLs matches the behaviour as if they where
     * appended to kill_partial.cfg.
     * 
     * Example:
     * Kill URLs in example.com that are under the "/beta/" or "/invalid/" paths.
     * <pre>{@code 
     * consumer.killByPartialMatch("https://example.com/beta/");
     * consumer.killByPartialMatch("https://example.com/invalid/");
     * }</pre>
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
     * <pre>{@code 
     * consumer.applyGscopeWhenRegexMatches("isDocument", "example\\.com/documents/"); 
     * }</pre>
     * 
     * Note that the regex special character '.' is escaped with '\' which is a java character
     * which must be escaped with another '\' thus '\\.'.
     * 
     * The consumer may be called multiple times to configure multiple gscopes to be 
     * set by various regular expressions. For example:
     * 
     * <pre>{@code 
     * consumer.applyGscopeWhenRegexMatches("cat", ".*cat.*");
     * consumer.applyGscopeWhenRegexMatches("cat", ".*kitty.*");
     * consumer.applyGscopeWhenRegexMatches("dog", ".*dog.*");
     * }</pre>
     * 
     * @param consumer Accepts gscopes that will be set when the URL matches the regex.
     */
    public default void supplyGscopesByRegex(IndexConfigProviderContext context, GscopeByRegexConsumer consumer) {
        
    }

    /**
     * Supply the QIE weight to a given URL.
     *
     * For example, to set the QIE weight 0.3 for URL 'www.example.com' call:
     *
     * <pre>{@code
     * consumer.applyQieWhenUrlMatches(0.3, "www.example.com");
     * }</pre>
     *
     * The consumer may be called multiple times to configure multiple QIE weights to be
     * set by various URLs. For example:
     *
     * <pre>{@code
     * consumer.applyQieWhenUrlMatches(0.3, "www.example.com");
     * consumer.applyQieWhenUrlMatches(0.6, "www.example1.com");
     * consumer.applyQieWhenUrlMatches(0.1, "www.example2.com");
     * }</pre>
     *
     * The QIE value is limited within the [0, 1] range. Any floating point value beyond this range will throw an `IllegalArgumentException`. For example, for the QIE value set to 1.3, the exception message will be:
     *
     * "Invalid QIE value: 1.3. Its value shall be  0.0 - 1.0."
     *
     * @param consumer Accepts only the first QIE weight that will be set when multiple QIE weights were set to the same URL.
     */
    public default void supplyQieByURL(IndexConfigProviderContext context, QieByUrlConsumer consumer) {

    }

    /**
     * Supply QIE weight that must be set on a document when the URL matches a query.
     *
     * For example, to set the QIE weight 0.3 for all documents mapping matching query
     * 'example' call:
     *
     * <pre>{@code
     * consumer.applyQieWhenQueryMatches(0.3, "example");
     * }</pre>
     *
     * The consumer may be called multiple times to configure multiple QIE weight to be
     * set by various queries. For example:
     *
     * <pre>{@code
     * consumer.applyQieWhenQueryMatches(0.3, "cat");
     * consumer.applyQieWhenQueryMatches(0.6, "kitty");
     * consumer.applyQieWhenQueryMatches(0.1, "dog");
     * }</pre>
     *
     * The QIE value is limited within the [0, 1] range. Any floating point value beyond this range will throw an `IllegalArgumentException`. For example, for the QIE value set to 1.3, the exception message will be:
     *
     * "Invalid QIE value: 1.3. Its value shall be  0.0 - 1.0."
     *
     * @param consumer Accepts only the first QIE weight that will be set when the URL matches the query.
     */
    public default void supplyQieByQuery(IndexConfigProviderContext context, QieByQueryConsumer consumer) {

    }

    /**
     * Supply queries to return a list of URLs to kill
     *
     * For example, to kill all documents matching query
     * 'example' call:
     *
     * <pre>{@code
     * consumer.killByQueryMatch("example");
     * }</pre>
     *
     * Multiple queries can be supplied and query language may be used. For example,
     *
     * kill documents containing the term 'enterprise' or 'organisation',
     * kill documents containing both 'public' and 'internet'.
     *
     * <pre>{@code
     * consumer.killByQueryMatch("[enterprise organisation]");
     * // Use mandatory inclusion operator to ensure both terms are present.
     * consumer.killByQueryMatch("|public |internet");
     * }</pre>
     *
     * @param consumer Accept the query with a given consumer.
     */
    default void killByQueryMatch(IndexConfigProviderContext context,KillByQueryMatchConsumer consumer) {

    }
    
    /**
     * Supply gscopes that must be set on a document when a query matches the document.
     * 
     * For example, to set the gscope 'isDocument' for all documents matching the query:
     * "word document"
     * 
     * <pre>{@code 
     * consumer.applyGscopeWhenQueryMatches("isDocument", "word document"); 
     * }</pre>
     * 
     * Multiple queries can be supplied and query language may be used. For example, 
     * set gscope 'org' on documents containing the term 'enterprise' or 'organisation', 
     * and gscope 'public' on documents containing both 'public' and 'internet'.
     * 
     * <pre>{@code 
     * consumer.applyGscopeWhenQueryMatches("org", "[enterprise organisation]"); 
     * // Use mandatory inclusion operator to ensure both terms are present.
     * consumer.applyGscopeWhenQueryMatches("public", "|public |internet"); 
     * }</pre>
     * 
     * @param consumer Accepts gscopes that will be set then the document matches the query.
     */
    public default void supplyGscopesByQuery(IndexConfigProviderContext context, GscopeByQueryConsumer consumer) {
        
    }
    
    /**
     * Supply auto completion entries to use on profiles.
     *
     * The method is supplied with a list of contexts, one for each profile which can accept
     * auto completion data, and must provide the consumer with any desired auto-completion
     * entries for each of the given profiles before returning.
     *
     * Note that with large sets of entries and/or many different profiles there is a performance benefit to:
     * - Providing entries which apply to a set profiles over providing the same entries repeatedly for each profile
     *   individually.
     * - Providing all entries for a given set of profiles before providing entries for a new set of profiles.
     *   i.e. try to avoid supplying an entry for profile `a`, then one for `b` ...so on through the alphabet...
     *   and then providing another entry for `a`. Instead provide all the entries for `a`,
     *   then all the entries for `b` and so on.
     *
     * The following example provides a single entry with the trigger `funnelback` for the profile `profileId`.
     * <pre>{@code
     *     consumer.applyAutoCompletionEntryToProfiles(AutoCompletionEntry.builder().trigger("funnelback").build(), Set.of("profileId"));
     * }</pre>
     *
     * The following example converts the given list of contexts into the complete set of profiles, which may be helpful
     * if all profiles should share the same auto-completion entries.
     * <pre>{@code
     *  Set<String> profiles = contextForProfilesThatRunThisPlugin.stream().flatMap(i -> i.getProfileWithView().stream())
     *      .collect(Collectors.toSet());
     * }</pre>
     *
     * @param contextForProfilesThatRunThisPlugin A list of the contexts for each profile which can accept auto-completion entries
     * @param consumer Accepts AutoCompletionEntry objects and a set of profiles to which each should apply
     */
    public default void supplyAutoCompletionEntriesForProfiles(List<IndexConfigProviderContext> contextForProfilesThatRunThisPlugin,
        AutoCompletionConsumer consumer) {
    }

}

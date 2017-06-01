package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;

import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.common.url.VFSURLUtils;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>{@link CategoryDefinition} based on an URL prefix.<p>
 * 
 * <p>Every different URL after the prefix will generate a new value.</p>
 * 
 * @since 11.0
 */
@Log4j2
public class URLFill extends CategoryDefinition implements MetadataBasedCategory {
    
    /**
     * Minimum value for <code>-count_urls</code> option. This option is 1-based,
     * where 1 will cause PADRE to return a list of hostname only URLs (e.g.
     * <code>http://exampe.org</code>)
     */
    private final static int COUNT_URLS_START_VALUE = 1;
    
    /**
     * <p>How much to increment <code>-count_urls</code> to get the data we need.</p>
     * 
     * <p>This is "2" because given a url <code>http://example.org/folder1/folder2/folder3/pag.html</code>
     * and the current scope <code>http://example.org/folder1/</code>:
     * <ul>
     *  <li>With 1 we only get <code>http://example.org/folder1/folder2</code> so <code>folder2</code>.
     *  will be considered a "page", not a possible sub-folder</li>
     *  <li>With 2 we get <code>http://example.org/folder1/folder2/folder3</code>, allowing us to see
     *  that <code>folder2</code> is actually a folder and include it in the facet values</li>
     * </ul>
     * 
     * <p>See {@link #COUNT_URLS_START_VALUE} and the implementation details of PADRE
     * <code>-count_urls</code> option.</p>
     * 
     */
    private final static int COUNT_URLS_INCREMENT = 2;
    
    /**
     * Default number of path segments to count when no facet value is selected
     */
    public final static int DEFAULT_SEGMENTS = 1;
    
    private final static Function<String, String> fixURL = (url) -> {
        // Strip 'http://' prefixes as PADRE strips them.
        url = url.replaceFirst("^http://", "");
        
        if (url.startsWith(VFSURLUtils.WINDOWS_URL_PREFIX)) {
            // Windows style url \\server\share\folder\file.ext
            // Convert to smb://... so that URLs returned by PADRE will match
          return url = VFSURLUtils.SystemUrlToVFSUrl(url);
        } else {
            return url;
        }
    };
    
    public URLFill(String url) {
        super(url);
        this.setData(url); // We reset data as we need to ensure the data is set correctly
    }

    /** Identifier used in query string parameter. */
    private static final String TAG = "url";
    
    /** URLs are indexed in the metadata class <tt>v</tt> */
    private static final String MD = "v";
    
    /** {@inheritDoc} */
    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    public List<CategoryValue> computeValues(final SearchTransaction st) {
        List<CategoryValue> categories = new ArrayList<CategoryValue>();

        
        String url = data;

        // Find out which URL is currently selected. By default
        // it's the root folder specified in the faceted nav. config.
        String currentConstraint = url;
        List<String> currentConstraints = st.getQuestion().getSelectedCategoryValues().get(getQueryStringParamName());
        if (currentConstraints != null) {
            if (currentConstraints.size() > 1) {
                log.warn("More than one URL constraint was selected: '"+StringUtils.join(currentConstraints, ",")
                    +"'. This is not supported, only '" + currentConstraints.get(0) + "' will be considered");
            }
            currentConstraint = currentConstraints.get(0);
        }
        
        
        // Fix URLs and lower case them as comparisons are case-insensitive.
        url = fixURL.apply(url).toLowerCase();
        currentConstraint = fixURL.apply(currentConstraint).toLowerCase();
        
        for (Entry<String, Integer> entry: st.getResponse().getResultPacket().getUrlCounts().entrySet()) {
            // Do not toLowerCase() here, we still want the original data from Padre
            // with the correct case to display
            String item = entry.getKey().replaceFirst("^http://", "");
            int count = entry.getValue();
            
            if (item.toLowerCase().startsWith(url)
                    // Only consider parent items (which are de facto selected)
                    // and 1-depth children (Padre may return deeper children)
                    && getDepth(currentConstraint, item.toLowerCase()) <= 1) {
                
                // 'v' metadata value is the URI only, without
                // the host or protocol.
                String vValue = item.replaceFirst("(\\w+://)?[^/]*/", "");

                String relativeItem = item.substring(url.length());

                // Display only the folder name
                String label = relativeItem.substring(relativeItem.lastIndexOf('/')+1);
                
                // FUN-7440:
                // - The 'label' needs to be decoded as we want do present a nice name for
                //   folders in the facet list (e.g. "With Spaces" rather than "With%20Spaces"
                // - The 'item' needs to be decoded as well. It get converted into a v:...
                //   metadata query and that will only work if it's decoded (See FUN-7440 comments)
                categories.add(new CategoryValue(
                        URLDecoder.decode(relativeItem, "UTF-8"),
                        URLDecoder.decode(label, "UTF-8"),
                        count,
                        URLEncoder.encode(getQueryStringParamName(), "UTF-8")
                            + "=" + URLEncoder.encode(vValue, "UTF-8"),
                        getMetadataClass(),
                        // A URL fill value is selected if it's a parent or equal of the current constraint,
                        // because the parents had to be traversed to reach the current constraint.
                        // e.g with smb:///server/folder1/folder2/file3.txt the values
                        // "folder1" and "folder2" were selected to reach "file3.txt"
                        // So set selected=true to all parents and current values, but not
                        // for deeper ones
                        getDepth(currentConstraint, item.toLowerCase()) <= 0));
            }
        }
        return categories;
    }
    
    /**
     * Get the depth of a URL, from the current constraint.
     *  
     * @param currentConstraint Currently constraint URL, such as <tt>folder1/folder2/</tt>
     * @param checkUrl URL to check, absolute (Ex: <tt>smb://server/folder1/folder2/file3.txt</tt>)
     * @return the depth of the check URL compared to the current constraint. Will be negative if
     * the check URL is a parent, zero if the check URL is identical to the contraint, or positive
     * if the check URL is deeper (with the value indicating how deep)
     */
    static int getDepth(String currentConstraint, String checkUrl) {
        if (currentConstraint == null || checkUrl == null) {
            return -1;
        }
        
        int i = checkUrl.indexOf(currentConstraint);
        if (i > -1) {
            // Remove head + currentConstraint
            String part = checkUrl.substring(i + currentConstraint.length());
            if (part.startsWith("/")) { part = part.substring(1); }
            if (part.length() > 0) {
                // We have at least one part below our constraint
                return 1 + StringUtils.countMatches(part, "/");
            } else {
                // No part after the constraint, so we're identical
                return 0;
            }
        }
        
        return -1;        
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Note that this category definition will use a special
     * tag <tt>url</tt> instead of directly the metadata <tt>v</tt> on which
     * URLs are mapped internally. For example: <tt>f.Category|url</tt> instead
     * of <tt>f.Category|v</tt>.</p>
     * 
     * <p><tt>v</tt> can't be used otherwise we won't be able to distinguish
     * between an <em>URL</em> type category definition and a <em>Metadata field</em>
     * type definition.</p>
     */
    @Override
    public String getQueryStringParamName() {
        return RequestParameters.FACET_PREFIX + facetName + CategoryDefinition.QS_PARAM_SEPARATOR + TAG;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(String value, String extraParams) {
        return TAG.equals(extraParams);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getMetadataClass() {
        return MD;
    }
    
    /** {@inheritDoc} */
    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    public String getQueryConstraint(String value) {
        // FUN-7440: The value (path constraint) needs to be URL decoded here
        // because PADRE will actually strip out punctuation from the query
        // e.g. v:"with%20spaces" will be processed as v:"with 20spaces", where what
        // we want is "v:with spaces"
        return  MD + ":\"" + URLDecoder.decode(value, "UTF-8") + "\"";
    }

    /** {@inheritDoc} */
    @Override
    public void setData(String data) {
        // Always ensure a trailing slash for the facet
        // breadcrumb to work correctly
        if (data.endsWith("/")) {
            super.setData(data);
        } else {
            super.setData(data+"/");
        }
    }
    
    public Stream<String> getSelectedValues(SearchQuestion question) {
        if (question.getSelectedFacets().contains(facetName)) {
            return question.getSelectedCategoryValues().get(getQueryStringParamName()).stream();
        }
        return Stream.empty();
         
    }

    @Override
    public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
        
        // Find maxmimum number of segments among all selected values as well as the URL prefix.
        int maxSegments = Stream.concat(getSelectedValues(question), Stream.of(fixURL.apply(this.data)))
            .map(URLFill::countSegments)
            .reduce(Integer::max)
            .orElse(DEFAULT_SEGMENTS);
        
        return Collections.singletonList(new QueryProcessorOption<Integer>(
            QueryProcessorOptionKeys.COUNT_URLS,
            COUNT_URLS_START_VALUE + maxSegments + COUNT_URLS_INCREMENT));
        
    }

    /**
     * <p>Counts the number of segments in the path component of a URL substring</p>
     */
    public static int countSegments(String urlSubstring) {
        // Strip protocol
        urlSubstring = urlSubstring.replaceAll("^.+://", "");
        
        if (urlSubstring.endsWith("/")) {
            // Strip any trailing '/' for consistency
            urlSubstring = urlSubstring.substring(0, urlSubstring.length() - 1);
        }
        
        // Strip anchor, query
        urlSubstring = StringUtils.substringBefore(urlSubstring, "#");
        urlSubstring = StringUtils.substringBefore(urlSubstring, "?");
        
        //minus one so we don't count the host in the segments
        return urlSubstring.split("/").length - 1;
    }

}

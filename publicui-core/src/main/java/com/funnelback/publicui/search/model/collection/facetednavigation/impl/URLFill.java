package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

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

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * <p>{@link CategoryDefinition} based on an URL prefix.<p>
 * 
 * <p>Every different URL after the prefix will generate a new value.</p>
 * 
 * @since 11.0
 */
@Log4j2
public class URLFill extends CategoryDefinition implements MetadataBasedCategory {
    
    /** Default value for -count_urls when no facet value is selected */
    public final static int DEFAULT_COUNT_URLS = 1;

    private final List<QueryProcessorOption<?>> defaultQpOptions;
    
    public URLFill(String url) {
        super(url);
        this.setData(url); // We reset data as we need to ensure the data is set correctly
        defaultQpOptions = Collections.singletonList(new QueryProcessorOption<Integer>(QueryProcessorOptionKeys.COUNT_URLS, DEFAULT_COUNT_URLS));
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

        // Strip 'http://' prefixes as PADRE strips them.
        String url = data.replaceFirst("^http://", "");

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
        
        if (url.startsWith(VFSURLUtils.WINDOWS_URL_PREFIX)) {
            // Windows style url \\server\share\folder\file.ext
            // Convert to smb://... so that URLs returned by PADRE will match
            url = VFSURLUtils.SystemUrlToVFSUrl(url);
        }
        
        if (currentConstraint.startsWith(VFSURLUtils.WINDOWS_URL_PREFIX)) {
            currentConstraint = VFSURLUtils.SystemUrlToVFSUrl(currentConstraint);
        }
        
        // Comparisons are case-insensitive
        url = url.toLowerCase();
        currentConstraint = currentConstraint.toLowerCase();
        
        for (Entry<String, Integer> entry: st.getResponse().getResultPacket().getUrlCounts().entrySet()) {
            // Do not toLowerCase() here, we still want the original data from Padre
            // with the correct case to display
            String item = entry.getKey().replaceFirst("^http://", "");
            int count = entry.getValue();
            
            if (item.toLowerCase().startsWith(url)
                    && isOneLevelDeeper(currentConstraint, item.toLowerCase())) {
                // 'v' metadata value is the URI only, without
                // the host or protocol.
                String vValue = item.replaceFirst("(\\w+://)?[^/]*/", "");
                
                item = item.substring(url.length());

                // Display only the folder name
                String label = item.substring(item.lastIndexOf('/')+1);
                
                // FUN-7440:
                // - The 'label' needs to be decoded as we want do present a nice name for
                //   folders in the facet list (e.g. "With Spaces" rather than "With%20Spaces"
                // - The 'item' needs to be decoded as well. It get converted into a v:...
                //   metadata query and that will only work if it's decoded (See FUN-7440 comments)
                categories.add(new CategoryValue(
                        URLDecoder.decode(item, "UTF-8"),
                        URLDecoder.decode(label, "UTF-8"),
                        count,
                        URLEncoder.encode(getQueryStringParamName(), "UTF-8")
                            + "=" + URLEncoder.encode(vValue, "UTF-8"),
                        getMetadataClass(),
                        // URL fill values are never selected because they're a hierarchy
                        // with only one value at each level. As a result the currently
                        // "selected" path segment is never present in the list of categories,
                        // only the children segments are. As soon as a child is selected, it
                        // becomes the "current", and the new list contains only its childs, etc.
                        false));
            }
        }
        return categories;
    }
    
    /**
     * Checks if an URL is contained in the current constraints and is only
     * one level deeper than the current constraints.
     *  
     * @param currentConstraint Currently constraint URL, such as <tt>folder1/folder2/</tt>
     * @param checkUrl URL to check, absolute (Ex: <tt>smb://server/folder/folder2/file3.txt</tt>)
     * @return true if the URL is in the current constraints, false otherwise
     */
    private boolean isOneLevelDeeper(String currentConstraint, String checkUrl) {
        if (currentConstraint == null || checkUrl == null) {
            return false;
        }
        
        int i = checkUrl.indexOf(currentConstraint);
        if (i > -1) {
            // Remove head + currentConstraint
            String part = checkUrl.substring(i + currentConstraint.length());
            if (part.startsWith("/")) { part = part.substring(1); }
            return (part.length() > 0 && StringUtils.countMatches(part, "/") < 1);
        }
        
        return false;        
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

    @Override
    public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
        if (question.getSelectedFacets().contains(facetName)) {
            // Find maxmimum number of segments among all selected values
            int maxSegments = question.getSelectedCategoryValues().get(getQueryStringParamName())
                .stream()
                .map(URLFill::countSegments)
                .reduce(Integer::max)
                .orElse(DEFAULT_COUNT_URLS);
            
            return Collections.singletonList(new QueryProcessorOption<Integer>(QueryProcessorOptionKeys.COUNT_URLS, maxSegments+1));
        } else {
            return defaultQpOptions;
        }
    }

    /**
     * <p>Counts the number of segments in the path component of a URL substring</p>
     */
    public static int countSegments(String urlSubstring) {
        if (urlSubstring.endsWith("/")) {
            // Strip any trailing '/' for consistency
            urlSubstring = urlSubstring.substring(0, urlSubstring.length() - 1);
        }
        
        // Strip protocol
        urlSubstring = urlSubstring.replaceAll("^.+://", "");
        
        // Strip host
        urlSubstring = StringUtils.substringAfter(urlSubstring, "/");
        
        // Strip anchor, query
        urlSubstring = StringUtils.substringBefore(urlSubstring, "#");
        urlSubstring = StringUtils.substringBefore(urlSubstring, "?");
        
        return urlSubstring.split("/").length;
    }

}

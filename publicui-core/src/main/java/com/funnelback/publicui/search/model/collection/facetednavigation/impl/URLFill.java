package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.funnelback.common.function.StreamUtils;
import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.common.url.VFSURLUtils;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.base.Predicates;

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
public class URLFill extends CategoryDefinition {
    
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
    
    private static Function<String, String> fixURL = (url) -> {
        // Strip 'http://' prefixes as PADRE strips them.
        
        
        if (url.startsWith(VFSURLUtils.WINDOWS_URL_PREFIX)) {
            // Windows style url \\server\share\folder\file.ext
            // Convert to smb://... so that URLs returned by PADRE will match
          url = VFSURLUtils.SystemUrlToVFSUrl(url);
          if(url.endsWith("//")) {
              url = url.substring(0, url.length()-1);
          }
        }
        
        
        if(url.toLowerCase().startsWith("smb://")) {
            url = url.toLowerCase();
        } else {
            // lowercase the domain and schema
            int schemeSepStart = url.indexOf("://");
            if(schemeSepStart == -1) {
                // sometimes we wont have http:// because padre drops it in a bunch of places this
                // makes this code risky assume everythig up to the first / is the domain
                schemeSepStart = 0;
            }
            
            // find where the path starts
            int pathStart = url.indexOf('/', schemeSepStart + 3);
            if(pathStart == -1) {
                pathStart = url.length();
            }
            
            String schemeAndDomain = url.substring(0, pathStart);
            String path = url.substring(pathStart, url.length());
            url = schemeAndDomain.toLowerCase() + path;
        }
        
        
        url = url.replaceFirst("^http://", "");
        
        return url;
    };
    
    public URLFill(String url) {
        super(url);
        this.setData(url); // We reset data as we need to ensure the data is set correctly
    }

    /** Identifier used in query string parameter. */
    public static final String TAG = "url";
    
    
    /** {@inheritDoc} */
    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    public List<CategoryValueComputedDataHolder> computeData(final SearchTransaction st, FacetDefinition facetDefinition) {
        
        // Note that Date facets are incompatible with getting data from the Unscoped query.
        // this is because Date facets by nature force hierarchy where data from the unscoped
        // query implies no hierarchy
        
        
        
        String url = fixURL.apply(data);

        // Find out which URL is currently selected. By default
        // it's the root folder specified in the faceted nav. config.
        
        // Holds either the current constraint (which will just be a path section)
        // or if no constraint is actually set (from the URL) then pretend the
        // constraint is the user set url.
        Optional<String> actualCurrentConstraint = getCurrentConstraint(st.getQuestion());
        String currentConstraint = actualCurrentConstraint.orElse(url);
        
        // Find out what the selected items are, we will remove from this list as we find 
        // the items from the padre result packet. Anything left over will be faked with a 
        // zero count so the user can unselect it.
        Set<String> selectedItems = new HashSet<>(getSelectedItems(actualCurrentConstraint, url));
        
        List<Pair<Integer, CategoryValueComputedDataHolder>> depthAndValues = new ArrayList<>();
        
        // It is possible thay fixing the URL reduces different URLs to be considered the same
        // e.g. smb://foo/AA/bar.doc and smb://foo/Aa/plop.doc both files are actually
        // in the same location. The down side of doing this is we never preserve case.
        Map<String, Integer> urlEntries = new HashMap<>(); 
        st.getResponse().getResultPacket().getUrlCounts().entrySet()
            .stream()
            .map(e -> Pair.of(fixURL.apply(e.getKey()), e.getValue()))
            .forEach(e -> urlEntries.merge(e.getKey(), e.getValue(), Integer::sum));
        
        for (Entry<String, Integer> entry : urlEntries.entrySet()) {
            // Do not toLowerCase() here, we still want the original data from Padre
            // with the correct case to display
            String item = entry.getKey();
            int count = entry.getValue();
            workoutValue(item, count, url, currentConstraint).ifPresent(depthAndValues::add);
            selectedItems.remove(item);
        }
        
        for(String s : selectedItems) {
            workoutValue(s, 0, url, currentConstraint).ifPresent(depthAndValues::add);
        }
        
        
        // Order the values from lowest depth to highest, we want a before a/b which is before a/b/c
        // This is needed for sorting by selected first to ensure the drill down looks correct.
        Collections.sort(depthAndValues, 
            Comparator.<Pair<Integer, CategoryValueComputedDataHolder>, Integer>comparing(Pair::getLeft));
        
        return depthAndValues.stream()
                .map(Pair::getRight)
                .collect(Collectors.toList());
    }
    
    /**
     * Use this to get the single constraint from the list of currentConstraints
     * 
     * At this point if the response packet URLs are respected we should never have
     * more than one constraint. Although it might happen from bad input, we take the first
     * 
     * @param currentConstraints
     * @return
     */
    protected Optional<String> getCurrentConstraint(SearchQuestion sq) {
        List<String> currentConstraints = sq.getSelectedCategoryValues().get(getQueryStringParamName());
        if (currentConstraints != null) {
            if (currentConstraints.size() > 1) {
                log.warn("More than one URL constraint was selected: '"+StringUtils.join(currentConstraints, ",")
                    +"'. This is not supported, only '" + currentConstraints.get(0) + "' will be considered");
            }
            return Optional.ofNullable(currentConstraints.get(0));
        }
        return Optional.empty();
    }
    
    public String fixedUrl() {
        return fixURL.apply(data);
    }
    
    /**
     * By looking at the URL the user sets on the facet definition and the current constraint
     * this will return the selected items that padre may return if the result set has documents 
     * under those directories.
     * e.g. if the url is foo.com/1/ and the curentConstraint is 1/bar/foo then this will return
     * foo.com/1/bar and foo.com/1/bar/foo
     * 
     * @param currentConstraint the current constraint which will be some path under the users URL
     * @param url the prefix set by the user that all URLs must be undert the constraint is deeper under 
     * this e.g. if url is http://foo.com/ and constraint is bar then we would see URLs under
     * http://foo.com/bar/
     * @return
     */
    public static List<String> getSelectedItems(Optional<String> currentConstraintOpt, String url) {
        if(!currentConstraintOpt.isPresent()) {
            return new ArrayList<>();
        }
        String currentConstraint = currentConstraintOpt.get();
        
        // The path of the url is in the constraint when selected we need to strip
        // that from the constraint
        String prefix = toUrlValue(fixURL.apply(url));
        if(!currentConstraint.startsWith(prefix)) {
            // I think this happens only when the facet definition changes while someone is using the facet.
            // or when the facet is not selected.
            return new ArrayList<>();
        }
        
        // Remove the prefix from the current constraint
        currentConstraint = currentConstraint.substring(prefix.length(), currentConstraint.length());
        
        List<String> currentFolderConstraints = StreamUtils.ofNullable(currentConstraint.split("/"))
            .filter(Predicates.not(String::isEmpty))
            .collect(Collectors.toList());
        
        List<String> selectedItems = new ArrayList<>();
        String urlPrefix = fixURL.apply(url);
        if(urlPrefix.endsWith("/")) {
            urlPrefix = urlPrefix.substring(0, urlPrefix.length()-1);
        }
        for(int i = 0; i < currentFolderConstraints.size(); i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(urlPrefix);
            
            for(int j = 0; j <= i; j++) {
                sb.append("/");
                sb.append(currentFolderConstraints.get(j));
            }
            selectedItems.add(fixURL.apply(sb.toString()));
        }
        
        return selectedItems;
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
    
    private static String toUrlValue(String item) {
        // 'v' metadata value is the URI only, without
        // the host or protocol.
        return item.replaceFirst("(\\w+://)?[^/]*/", "");
    }
    
    /**
     * 
     * @param item comes from padre's URL counts.
     * @param count
     * @param url
     * @param currentConstraint
     * @return
     * @throws UnsupportedEncodingException
     */
    private Optional<Pair<Integer, CategoryValueComputedDataHolder>> workoutValue(String item, 
                    int count, 
                    String url, 
                    String currentConstraint) 
            throws UnsupportedEncodingException {
        
        if (item.startsWith(url)
                // Only consider parent items (which are de facto selected)
                // and 1-depth children (Padre may return deeper children)
                && getDepth(currentConstraint, item) <= 1) {
            
            String vValue = toUrlValue(item);
    
            String relativeItem = item.substring(url.length());
    
            // Display only the folder name
            String label = relativeItem.substring(relativeItem.lastIndexOf('/')+1);
            
            // FUN-7440:
            // - The 'label' needs to be decoded as we want do present a nice name for
            //   folders in the facet list (e.g. "With Spaces" rather than "With%20Spaces"
            // - The 'item' needs to be decoded as well. It get converted into a v:...
            //   metadata query and that will only work if it's decoded (See FUN-7440 comments)
            
            // Don't use the depth value as it can be negative.
            int sortOrder = StringUtils.countMatches(vValue, "/");
            
            return Optional.of(Pair.of(sortOrder, 
                new CategoryValueComputedDataHolder(
                    URLDecoder.decode(relativeItem, "UTF-8"),
                    URLDecoder.decode(label, "UTF-8"),
                    count,
                    "", // Constraint is a prefix, this is not used so it doesn't matter.
                    // A URL fill value is selected if it's a parent or equal of the current constraint,
                    // because the parents had to be traversed to reach the current constraint.
                    // e.g with smb:///server/folder1/folder2/file3.txt the values
                    // "folder1" and "folder2" were selected to reach "file3.txt"
                    // So set selected=true to all parents and current values, but not
                    // for deeper ones
                    getDepth(currentConstraint, item) <= 0,
                    getQueryStringParamName(),
                    vValue
                    )));
        }
        return Optional.empty();
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
    public String getQueryStringCategoryExtraPart() {
        return TAG;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(String value, String extraParams) {
        return TAG.equals(extraParams);
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
        int maxSegments = Integer.max(countSegments(fullCurrentConstraintForCounting(question)), 0);
        
        List<QueryProcessorOption<?>> qpOptions = new ArrayList<>();
        qpOptions.add(new QueryProcessorOption<Integer>(
            QueryProcessorOptionKeys.COUNT_URLS,
            COUNT_URLS_START_VALUE + maxSegments + COUNT_URLS_INCREMENT));
        facetScopeToRestrictTo(question).ifPresent(qpOptions::add);
        return Collections.unmodifiableList(qpOptions);
    }
    
    protected Optional<QueryProcessorOption<?>> facetScopeToRestrictTo(SearchQuestion question) {
        return getCurrentConstraint(question)
            .map(this::joinConstraintToUserPrefix)
            .map(prefix -> new QueryProcessorOption<>("fscope", prefix));
    }
    
    /**
     * Must return a string which will have the number of segments counted to 
     * determine how deep -count_urls needs to be.
     * 
     * returned value must include the host.
     * @param sq
     * @return
     */
    protected String fullCurrentConstraintForCounting(SearchQuestion sq) {
        return joinConstraintToUserPrefix(getCurrentConstraint(sq).orElse(""));
    }
    
    /**
     * Joins the selected constraint (which is a path under the user URL prefix) to the URL
     * prefix.
     * 
     * This takes care of slashes and makes some effort to ensure that we don't join with
     * double slashes and we have a trailing slash to ensure the prefix check works.
     *
     * @param constraint
     * @return
     */
    String joinConstraintToUserPrefix(String constraint) {
        // URL always includes up to the host
        String url = fixedUrl();
        String sep = "/";
        
        if(url.endsWith("/") || constraint.startsWith("/")) {
            sep = "";
        }
        
        if(url.endsWith("/") && constraint.startsWith("/")) {
            constraint = constraint.substring(1);
        }
        
        // Add trailing "/" so that it is easier for padre to work out which URLs
        // are below the folder.
        if(!constraint.endsWith("/")) {
            constraint = constraint + "/";
        }
        
        return url + sep + constraint;
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

    @Override
    public boolean allValuesDefinedByUser() {
        return false;
    }

    @Override
    public boolean selectedValuesAreNested() {
        return true;
    }
    

}

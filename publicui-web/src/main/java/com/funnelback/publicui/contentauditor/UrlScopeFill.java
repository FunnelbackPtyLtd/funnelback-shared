package com.funnelback.publicui.contentauditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.URLFill;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.Getter;
import lombok.ToString;

/**
 * A custom facet type for content auditor which allows for a URL based
 * drill-down through the domain segments and then the directory structure.
 * 
 * We avoid ever showing a single 'next-level' option by automatically drilling
 * the category options down to the next level at which there will be more than
 * one category available.
 * 
 * Facet selections are done using padre's scope query processor option (see {@link
 * com.funnelback.publicui.search.lifecycle.input.processors.FacetedNavigation})
 * which I suspect may not be as efficient as we might ideally like here.
 */
@Controller
public class UrlScopeFill extends URLFill {
        
        public UrlScopeFill(String URL) {
        super(URL);
        this.setData(URL);
    }

        /** Identifier used in query string parameter. */
        // Note - this tag matches a special case in the funnelback_classic.ftl Category macro
        // so you need to make changes there if you change it.
        private static final String TAG = "url";

        @Override
        public List<CategoryValueComputedDataHolder> computeData(final SearchTransaction st, FacetDefinition facetDefinition) {
            List<CategoryValueComputedDataHolder> categories = new ArrayList<>();
            
            // Find out the currently selected value and its depth
            String currentlySelectedValue = st.getQuestion().getInputParameterMap().get(this.getQueryStringParamName());
            if (currentlySelectedValue == null) {
                currentlySelectedValue = "";
            }
            int currentDepth = countSegments(currentlySelectedValue);
            
            SegmentCountTable segmentCounts = new SegmentCountTable();
            
            for (Entry<String, Integer> entry : st.getResponse().getResultPacket().getUrlCounts().entrySet()) {
                SimpleParsedUri uri = new SimpleParsedUri(entry.getKey());
                int count = entry.getValue();

                updateSegmentCounts(segmentCounts, uri, count);
            }
            
            // Find the smallest number of segments with more than one possible value
            Integer numSegmentsToUse = Integer.MAX_VALUE;
            for (Entry<Integer, Map<String, Integer>> entry : segmentCounts.getSegmentCounts().entrySet()) {
                if (entry.getKey() < numSegmentsToUse && entry.getValue().size() > 1 && entry.getKey() > currentDepth) {
                    numSegmentsToUse = entry.getKey();
                }
            }
            
            if (numSegmentsToUse != Integer.MAX_VALUE) {
                // We found some level  to return
                for (Entry<String, Integer>  entry : segmentCounts.getSegmentCounts().get(numSegmentsToUse).entrySet()) {
                    categories.add(new CategoryValueComputedDataHolder(
                        entry.getKey(),
                        entry.getKey(),
                        entry.getValue(),
                        entry.getKey(),
                        // URLScope fill values are never selected because they're a hierarchy
                        // with only one value at each level. As a result the currently
                        // "selected" path segment is never present in the list of categories,
                        // only the children segments are. As soon as a child is selected, it
                        // becomes the "current", and the new list contains only its childs, etc.
                        false,
                        getQueryStringParamName(),
                        entry.getKey()));
                }
            } else {
                // No levels returned. Use the deepest level we were able to get from
                // PADRE url-count so that the user can still drill-down to it to access sub-folders
                segmentCounts.getSegmentCounts().keySet()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .ifPresent(maxDepth -> {
                        if (maxDepth > currentDepth) {
                            for (Entry<String, Integer>  entry : segmentCounts.getSegmentCounts().get(maxDepth).entrySet()) {
                                categories.add(new CategoryValueComputedDataHolder(
                                    entry.getKey(),
                                    entry.getKey(),
                                    entry.getValue(),
                                    entry.getKey(),
                                    false,
                                    getQueryStringParamName(),
                                    entry.getKey()));
                            }
                        }
                        
                    });
                
            }

            return categories;
        }

        /** Updates segment counts for a hostname */
        private void updateSegmentCounts(SegmentCountTable segmentCounts, SimpleParsedUri uri, Integer count) {
            // Do the hostname
            if (uri.getPath().isEmpty()) {
                for (String hostnameSuffix : uri.getHostnameSuffixes()) {
                    // We want to always have exactly one trailing slash
                    hostnameSuffix = hostnameSuffix + "/";

                    segmentCounts.add(hostnameSuffix, countSegments(hostnameSuffix), count);
                }
            }

            // Do the path
            String pathPrefix = uri.getPath();
            
            if (!pathPrefix.isEmpty()) {
                // We want to always have exactly one trailing slash
                String key = uri.getHostname() + "/" + pathPrefix + (pathPrefix.endsWith("/") ? "" : "/");
                
                segmentCounts.add(key, countSegments(key), count);
            }

        }
        
        /**
         * Counts the number of segments (levels of domain and path) in a URL substring
         */
        public static int countSegments(String urlSubstring) {
            int result = 0;
            
            if (urlSubstring.endsWith("/")) {
                // Strip any trailing '/' for consistency
                urlSubstring = urlSubstring.substring(0, urlSubstring.length() - 1);
            }
            
            String hostname = "";
            String path = "";
            if (urlSubstring.contains("/")) {
                int index = urlSubstring.indexOf("/");
                hostname = urlSubstring.substring(0, index);
                path = urlSubstring.substring(index + 1);
                result += StringUtils.countMatches(path, "/") + 1;
            } else {
                hostname = urlSubstring;
            }
            
            if (!hostname.isEmpty()) {
                result += StringUtils.countMatches(hostname, ".") + 1;
            }
            
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public String getQueryStringCategoryExtraPart() {
            return TAG;
        }

        /** {@inheritDoc} */
        @Override
        public boolean matches(String value, String extraParams) {
            return TAG.equals(extraParams);
        }

        /** TODO */
        public String getScopeConstraint() {
            return this.getData();
        }
        
        @Override
        public String getQueryConstraint(String value) {
            // No query constraint, scoping is done
            // Via QPOs (-scope=)
            return "";
        }
        
        @Override
        public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
            // Make a copy of the list as the super one is immutable
            List<QueryProcessorOption<?>> qpOptions = new ArrayList<>(super.getQueryProcessorOptions(question));
            if (question.getSelectedFacets().contains(facetName)) {
                question.getSelectedCategoryValues().get(getQueryStringParamName())
                    .stream()
                    .forEach(value -> qpOptions.add(new QueryProcessorOption<String>(QueryProcessorOptionKeys.SCOPE , value)));
            }
            
            return qpOptions;
        }        

        @ToString
        public class SegmentCountTable {
            @Getter
            Map<Integer, Map<String, Integer>> segmentCounts = new HashMap<Integer, Map<String, Integer>>();
            
            public void add(String segment, int depth, int count) {
                if (!segmentCounts.containsKey(depth)) {
                    segmentCounts.put(depth, new HashMap<String, Integer>());
                }
                
                if (!segmentCounts.get(depth).containsKey(segment)) {
                    segmentCounts.get(depth).put(segment, count);
                } else {
                    segmentCounts.get(depth).put(segment, segmentCounts.get(depth).get(segment) + count);
                }
            }
        }
        
        /**
         * A very basic URI parsing class to deal simplistically with the (possibly invalid)
         * URLs padre might give us.
         */
        @ToString
        public static class SimpleParsedUri {
            
            public SimpleParsedUri (String uri) {
                String protocolHostnameSeparator = "://";
                String hostnamePathSeparator = "/";

                if (!uri.contains(protocolHostnameSeparator)) {
                    // Restore the protocol padre drops by default
                    uri = "http" + protocolHostnameSeparator + uri;
                }
                
                int protocolStart = 0;
                int protocolEnd = uri.indexOf(protocolHostnameSeparator);
                
                this.protocol = uri.substring(protocolStart, protocolEnd);
                
                String remainingAfterProtocol = uri.substring(protocolEnd + protocolHostnameSeparator.length());
                if (!remainingAfterProtocol.contains(hostnamePathSeparator)) {
                    remainingAfterProtocol = remainingAfterProtocol + hostnamePathSeparator;
                }
                
                int hostnameStart = 0;
                int hostnameEnd = remainingAfterProtocol.indexOf(hostnamePathSeparator);
                
                this.hostname = remainingAfterProtocol.substring(hostnameStart, hostnameEnd);
                
                int pathStart = hostnameEnd + hostnamePathSeparator.length();
                int pathEnd = remainingAfterProtocol.length();
                
                this.path = remainingAfterProtocol.substring(pathStart, pathEnd);
            }
            
            @Getter
            private String protocol;
            
            @Getter
            private String hostname;
            
            @Getter
            private String path;
            
            /**
             * Get the '.' separated suffixes of the hostname.
             * 
             * e.g. www.funnelback.com -&gt; com, funnelback.com, www.funnelback.com
             */
            public Set<String> getHostnameSuffixes() {
                Set<String> result = new HashSet<>();
                StringBuffer buffer = new StringBuffer();
                
                List<String> suffixes = Arrays.asList(hostname.split(Pattern.quote(".")));
                Collections.reverse(suffixes);
                
                for (String segment : suffixes) {
                    buffer.insert(0, segment);
                    result.add(buffer.toString());
                    buffer.insert(0, ".");
                }
                    
                return result;
            }
            
            /**
             * Get the '/' separated prefixes of the path.
             * 
             * e.g. example.com/search/enterprise/whitepaper.html -&gt;
             *     search, search/enterprise, search/enterprise/whitepapter.html
             */
            public Set<String> getPathPrefixes() {
                Set<String> result = new HashSet<>();
                StringBuffer buffer = new StringBuffer();
                
                for(String segment : path.split("/")) {
                    if (segment.isEmpty()) {
                        continue;
                    }
                    if (!buffer.toString().isEmpty()) {
                        buffer.append("/");
                    }
                    buffer.append(segment);
                    
                    result.add(buffer.toString());
                }
                
                return result;
            }
        }
    }

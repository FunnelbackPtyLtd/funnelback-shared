package com.funnelback.publicui.contentauditor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * TODO - Put this in a sensible package
 * 
 * TODO - Document
 */
@Controller
public class UrlScopeFill extends CategoryDefinition {
        
        public UrlScopeFill(String URL) {
        super(URL);
        this.setData(URL);
    }

        /** Identifier used in query string parameter. */
        // Note - this tag matches a special case in the funnelback_classic.ftl Category macro
        // so you need to make changes there if you change it.
        private static final String TAG = "url";

        @Override
        @SneakyThrows(UnsupportedEncodingException.class)
        public List<CategoryValue> computeValues(final SearchTransaction st) {
            List<CategoryValue> categories = new ArrayList<CategoryValue>();
            
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
                    categories.add(new CategoryValue(
                        entry.getKey(),
                        entry.getKey(),
                        entry.getValue(),
                        URLEncoder.encode(getQueryStringParamName(), "UTF-8")
                            + "=" + URLEncoder.encode(entry.getKey(), "UTF-8"),
                        entry.getKey(),
                        FacetedNavigationUtils.isCategorySelected(this, st.getQuestion().getSelectedCategoryValues(), entry.getKey())));
                }
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
                String key = uri.getHostname() + "/" + pathPrefix + "/";
                
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
        public String getQueryStringParamName() {
            return RequestParameters.FACET_PREFIX + facetName + CategoryDefinition.QS_PARAM_SEPARATOR + TAG;
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

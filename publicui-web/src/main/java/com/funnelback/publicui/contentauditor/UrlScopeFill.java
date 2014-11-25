package com.funnelback.publicui.contentauditor;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * TODO - Put this in a sensible package
 * 
 * TODO - Document
 */
@Controller
@Log4j
public class UrlScopeFill extends CategoryDefinition {
        
        /** Identifier used in query string parameter. */
        // Note - this tag matches a special case in the funnelback_classic.ftl Category macro
        // so you need to make changes there if you change it.
        private static final String TAG = "url";

        @Override
        @SneakyThrows(UnsupportedEncodingException.class)
        public List<CategoryValue> computeValues(final SearchTransaction st) {
            List<CategoryValue> categories = new ArrayList<CategoryValue>();
            
            Map<Integer, Map<String, Integer>> prefixCounts = new HashMap<Integer, Map<String, Integer>>();
            
            for (Entry<String, Integer> entry : st.getResponse().getResultPacket().getUrlCounts().entrySet()) {
//                URI uri;
                URL url;
                try {
                    if (entry.getKey().contains("://")) {
//                        uri = new URI(entry.getKey());
                        url = URL.parse(entry.getKey());
                    } else {
                        // Padre strips the protocol if it's http :(
//                        uri = new URI("http://" + entry.getKey());
                        url = URL.parse("http://" + entry.getKey());
                    }
//                } catch (URISyntaxException e) {
//                    // TODO - Is this possible? Decide how to deal with it.
//                    throw new RuntimeException(e);
                } catch (GalimatiasParseException e) {
                    // TODO - Is this possible? Decide how to deal with it.
                    throw new RuntimeException(e);
                }
                
                String hostname;
                String path;
                if (url.isHierarchical()) {
                    hostname = url.host().toHumanString();
                    path = url.path();                    
                } else {
                    String schemeData = url.schemeData();
                    Matcher m = Pattern.compile("//(?<hostname>((\\w+).)+\\w)/(?<path>.*)").matcher(schemeData);
                    if (m.matches()) {
                        hostname = m.group("hostname");
                        path = m.group("path");
                    } else {
                        hostname = "";
                        path = url.schemeData();
                    }
                }
                int count = entry.getValue();
                updateHostnamePrefixCounts(prefixCounts, hostname, count);
                updatePathPrefixCounts(prefixCounts, hostname, path, count);
            }

            // Find the smallest number of segments with more than one possible value
            Integer numSegmentsToUse = Integer.MAX_VALUE;
            for (Entry<Integer, Map<String, Integer>> entry : prefixCounts.entrySet()) {
                if (entry.getKey() < numSegmentsToUse && entry.getValue().size() > 1) {
                    numSegmentsToUse = entry.getKey();
                }
            }
            
            if (numSegmentsToUse != Integer.MAX_VALUE) {
                // We found some level  to return
                for (Entry<String, Integer>  entry : prefixCounts.get(numSegmentsToUse).entrySet()) {
                    categories.add(new CategoryValue(
                        entry.getKey(),
                        entry.getKey(),
                        entry.getValue(),
                        URLEncoder.encode(getQueryStringParamName(), "UTF-8")
                            + "=" + URLEncoder.encode(entry.getKey(), "UTF-8"),
                        entry.getKey()));
                }
            }

            return categories;
        }

        /** TODO */
        private void updateHostnamePrefixCounts(Map<Integer, Map<String, Integer>> prefixCounts, String hostname, Integer count) {
            int position = hostname.indexOf('.', 0);
            while (position != -1) {
                String domainSuffix = hostname.substring(position + 1);
                int segments = StringUtils.countMatches(domainSuffix, ".") + 1;
                
                if (!prefixCounts.containsKey(segments)) {
                    prefixCounts.put(segments, new HashMap<String, Integer>());
                }
                
                if (!prefixCounts.get(segments).containsKey(domainSuffix)) {
                    prefixCounts.get(segments).put(domainSuffix, count);
                } else {
                    prefixCounts.get(segments).put(domainSuffix, prefixCounts.get(segments).get(domainSuffix) + count);
                }
                
                position = hostname.indexOf('.', position + 1);
            }
        }

        /** TODO */
        private void updatePathPrefixCounts(Map<Integer, Map<String, Integer>> prefixCounts, String hostname, String path, Integer count) {
            int hostnameSegments = StringUtils.countMatches(hostname, ".") + 1;

            int position = path.indexOf('/', 0);
            while (position != -1) {
                String pathPrefix = path.substring(0, position);
                String key = hostname + pathPrefix;

                int segments = hostnameSegments + StringUtils.countMatches(pathPrefix, "/");
                
                if (!prefixCounts.containsKey(segments)) {
                    prefixCounts.put(segments, new HashMap<String, Integer>());
                }
                
                if (!prefixCounts.get(segments).containsKey(key)) {
                    prefixCounts.get(segments).put(key, count);
                } else {
                    prefixCounts.get(segments).put(key, prefixCounts.get(segments).get(key) + count);
                }
                
                position = path.indexOf('/', position + 1);
            }
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
    }

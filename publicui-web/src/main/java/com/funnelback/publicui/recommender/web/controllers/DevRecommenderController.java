package com.funnelback.publicui.recommender.web.controllers;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.compare.SortType;
import com.funnelback.publicui.recommender.utils.HTMLUtils;
import com.funnelback.publicui.recommender.utils.RecommenderUtils;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.reporting.recommender.tuple.PreferenceTuple;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

/**
 * Controller for developer access to Recommender system.
 */

@Controller
@RequestMapping("/recommender")
public class DevRecommenderController {
    public static final String RECOMMENDER_PREFIX = DefaultValues.ModernUI.CONTEXT_PATH + "recommender/";
    private static final String RECOMMENDATIONS_DOC_HEADER = "<html><head><title>Recommendations</title><head><body><h1>Recommendations</h1>";
    private static final String SESSIONS_DOC_HEADER = "<html><head><title>Sessions</title><head><body><h1>Sessions</h1>";
    private static final String SCOPE = "handbook.curtin.edu.au/units,courses.curtin.edu.au/course_overview";
    private static final String CURTIN = "Curtin University";
    private static final String SOURCE = CURTIN;
    private static final String CURTIN_SEARCH_PREFIX =
            "http://127.0.0.1:8080/s/search.json?collection=test-curtin-courses";
    private static final String SEARCH_URL = CURTIN_SEARCH_PREFIX;
    private static final int DEFAULT_MAX_RECOMMENDATIONS = 10;

    @Autowired
    @Setter
    private ConfigRepository configRepository;

    /**
     * Return a HTML page displaying the recommendations for each result for the given query, collection & scope.
     *
     * @param query              search term(s) (required)
     * @param collection         collection ID (required)
     * @param scope              comma separated list of scope(s) to apply to suggestions (may be null or empty)
     * @param maxRecommendations maximum number of recommendations to display for each item (less than 1 means unlimited)
     * @param dsort              descending sort parameter (optional)
     * @param asort              ascending sort parameter (optional)
     * @param metadataClass      metadata field ID if sorting on a metadata field (optional)
     * @return HTML page with recommendations
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = {"/" + RecommenderController.SEARCH_RECOMMENDATIONS_HTML}, method = RequestMethod.GET)
    public String searchRecommendations(@RequestParam("query") String query,
                                        @RequestParam("collection") String collection,
                                        @RequestParam("scope") String scope,
                                        @RequestParam("maxRecommendations") int maxRecommendations,
                                        @RequestParam(value = "dsort", required = false) String dsort,
                                        @RequestParam(value = "asort", required = false) String asort,
                                        @RequestParam(value = "metadataClass", required = false) String metadataClass)
            throws UnsupportedEncodingException {
        Comparator<Recommendation> comparator;
        List<Map<String, Object>> results = null;
        String searchService;

        if (metadataClass != null || ("").equals(metadataClass)) {
            if (!DocInfoQuery.isValidMetadataClass(metadataClass)) {
                return HTMLUtils.getErrorPage(RECOMMENDATIONS_DOC_HEADER, "metadataClass parameter value is invalid: "
                        + metadataClass);
            }
        }

        if (scope != null && !("").equals(scope)) {
            String utf8Scope = URLEncoder.encode(scope, "utf-8");
            searchService = new String((SEARCH_URL + "&scope=" + utf8Scope));
        } else {
            searchService = SEARCH_URL;
        }

        try {
            comparator = SortType.getComparator(asort, dsort, metadataClass);
        } catch (Exception exception) {
            System.out.println("searchRecommendations(): " + exception);
            return HTMLUtils.getErrorPage(RECOMMENDATIONS_DOC_HEADER, exception.toString());
        }

        StringBuffer buf = new StringBuffer();
        buf.append(RECOMMENDATIONS_DOC_HEADER);

        com.funnelback.publicui.search.model.collection.Collection collectionRef
                = configRepository.getCollection(collection);

        if (collectionRef != null) {
            Config collectionConfig = collectionRef.getConfiguration();
            long startTime = System.currentTimeMillis();

            try {
                results = HTMLUtils.getResults(query, searchService);
            } catch (IOException exception) {
                System.out.println(exception);
            }
            long timeTaken = (System.currentTimeMillis() - startTime);

            if (results != null && !results.isEmpty()) {
                Set<String> originalResults = new HashSet<>();

                buf.append("<p>Original results for query: ");
                buf.append(HTMLUtils.getSearchLink(searchService, query, HTMLUtils.ResultFormat.html));
                buf.append(" (" + HTMLUtils.getSearchLink(searchService, query, HTMLUtils.ResultFormat.json) + ") ");
                buf.append("<small>(query time: " + timeTaken + "ms)</small></p>\n");

                for (Map<String, Object> result : results) {
                    String resultURL = (String) result.get("liveUrl");
                    String title = (String) result.get("title");
                    resultURL = resultURL.trim();
                    title = title.trim();

                    String encodedCollection = HTMLUtils.getEncodedParameter("collection", collection);
                    String encodedScope = HTMLUtils.getEncodedParameter("scope", scope);
                    String encodedDSort = HTMLUtils.getEncodedParameter("dsort", dsort);
                    String encodedASort = HTMLUtils.getEncodedParameter("asort", asort);
                    String encodedMetadataClass = HTMLUtils.getEncodedParameter("metadataClass", metadataClass);

                    buf.append("<ul><li><a href=\"" + resultURL + "\">" + title
                            + "</a> ");

                    String encodedResultURL = URLEncoder.encode(resultURL, "utf-8");
                    buf.append("[<a href=\"" + RECOMMENDER_PREFIX + RecommenderController.SIMILAR_ITEMS_JSON + "?seedItem="
                            + encodedResultURL + encodedCollection + encodedScope
                            + "&maxRecommendations=" + maxRecommendations + encodedDSort + encodedASort
                            + encodedMetadataClass + "\">JSON</a>] \n");
                    buf.append("[<a href=\"" + RECOMMENDER_PREFIX + RecommenderController.SESSIONS_HTML + "?itemName="
                            + encodedResultURL + "&seedItem=" + encodedResultURL + encodedCollection
                            + "\">Sessions</a>]</li>\n");

                    List<Recommendation> recommendations =
                            RecommenderUtils.getRecommendationsForItem(resultURL, collectionConfig, scope, maxRecommendations);
                    List<Recommendation> sortedRecommendations
                            = RecommenderUtils.sortRecommendations(recommendations, comparator);
                    buf.append(HTMLUtils.getHTMLRecommendations(sortedRecommendations, resultURL, collection,
                            scope, maxRecommendations, dsort, asort, metadataClass));
                    buf.append("</ul>\n");
                    originalResults.add(resultURL);
                }

                timeTaken = (System.currentTimeMillis() - startTime);
                buf.append("<p>Recommendation time: " + timeTaken + "ms</p>\n");
            } else if (results == null) {
                buf.append("<p>Error getting results from search service.</p>");
            } else {
                buf.append("<p>No results found for query: " + query + " </p>");
            }
        } else {
            return HTMLUtils.getErrorPage(RECOMMENDATIONS_DOC_HEADER, "Invalid collection: " + collection);
        }

        buf.append("</body></html>");
        return buf.toString();
    }

    /**
     * Return a HTML page displaying sessions which included the given item (clicked URL).
     *
     * @param itemName   Name of item e.g. URL address
     * @param seedItem   The seed which led to this item being recommended. Could be a query or a URL.
     * @param collection name of collection
     * @return HTML page displaying session information.
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = {"/" + RecommenderController.SESSIONS_HTML}, method = RequestMethod.GET)
    public String sessions(@RequestParam("itemName") String itemName,
                           @RequestParam("seedItem") String seedItem,
                           @RequestParam("collection") String collection) throws Exception {
        StringBuffer buf = new StringBuffer();
        String searchURL = SEARCH_URL.replaceAll("\\.json", "\\.html");

        com.funnelback.publicui.search.model.collection.Collection collectionRef
                = configRepository.getCollection(collection);

        if (collectionRef != null) {
            Config collectionConfig = collectionRef.getConfiguration();
            buf.append(getSessionsHeader(itemName, seedItem, collectionConfig));
            Set<List<PreferenceTuple>> sessions
                    = com.funnelback.publicui.recommender.utils.RecommenderUtils.getSessions(itemName, collectionConfig);

            if (sessions != null && !sessions.isEmpty()) {
                for (List<PreferenceTuple> session : sessions) {
                    String sessionID = session.get(0).getUserID();
                    String host = session.get(0).getHost();

                    buf.append("<h2>Session: " + sessionID + " Host: " + host + "</h2><ul>\n");

                    List<String> urls = new ArrayList<>();
                    for (PreferenceTuple preference : session) {
                        urls.add(preference.getItemID());
                    }

                    Map<URI, DocInfo> docInfoMap = RecommenderUtils.getDocInfoResult(urls, collectionConfig).asMap();

                    if (docInfoMap != null && !docInfoMap.isEmpty()) {
                        for (PreferenceTuple preference : session) {
                            String url = preference.getItemID();
                            String address = url;
                            String query = preference.getQuery();
                            String title = url;
                            float qieScore = -1;
                            URI uri = new URI(url);
                            DocInfo docInfo = docInfoMap.get(uri);

                            if (docInfo != null) {
                                title = docInfo.getTitle();
                                qieScore = docInfo.getQieScore();
                            }

                            if (url.equals(itemName)) {
                                url = "<font color=\"green\">" + url + "</font>";
                            } else if (url.equals(seedItem)) {
                                url = "<font color=\"red\">" + url + "</font>";
                            }

                            buf.append("<li><a href=\"" + address + "\">" + title + "</a> <small>"
                                    + url + " Date: " + preference.getDate() + " Query: <a href=\"" + searchURL
                                    + URLEncoder.encode(query, "utf-8") + "\">" + query + "</a> QIE Score: "
                                    + qieScore + "</small></li>\n");
                        }
                    } else {
                        buf.append("<li>No document information available from index for URLs in this session</li>");
                    }

                    buf.append("</ul>");
                }
            } else {
                buf.append("<p>No sessions found.</p>");
            }
        }

        buf.append("</ul></body></html>");
        return buf.toString();
    }

    /**
     * Return a header for the sessions page for the given input parameters.
     * @param itemName   Name of item e.g. URL address
     * @param seedItem   The seed which led to this item being recommended. Could be a query or a URL.
     * @param collectionConfig Collection Config object
     * @return String containing HTML for sessions page header.
     */
    private String getSessionsHeader(String itemName, String seedItem, Config collectionConfig) {
        StringBuffer buf = new StringBuffer();
        buf.append(SESSIONS_DOC_HEADER);

        String keyTitle = RecommenderUtils.getTitle(itemName, collectionConfig);

        if ("".equals(keyTitle)) {
            keyTitle = itemName;
        }

        buf.append("<p>Sessions with two or more clicks for item: <a href=\""
                + itemName + "\">" + keyTitle + "</a></p>");

        buf.append("<p>Item that appears in each session is highlighted in <font color=\"green\">green</font>.</p>");

        if (!itemName.equals(seedItem)) {
            buf.append("<p>Item it was recommended for is highlighted in <font color=\"red\">red</font>.</p>");
        }

        return buf.toString().trim();
    }

    /**
     * Display a HTML form for entering an item to get recommendations for.
     *
     * @param collection collection ID
     * @return HTML page with entry form.
     */
    @ResponseBody
    @RequestMapping(value = {"/" + RecommenderController.ITEM_ENTRY_HTML}, method = RequestMethod.GET)
    public String itemEntry(@RequestParam("collection") String collection) throws UnsupportedEncodingException {
        StringBuffer buf = new StringBuffer();

        buf.append(RECOMMENDATIONS_DOC_HEADER);
        buf.append("<form action=\"" + RECOMMENDER_PREFIX + RecommenderController.SIMILAR_ITEMS_JSON + "\" method=\"GET\">");
        buf.append("<label for=\"seedItem\">Recommend URLs for URL: </label>\n" +
                "<input id=\"seedItem\" class=\"text\" type=\"text\" title=\"Item\" name=\"seedItem\" size=\"70\"/>" +
                "<input type=\"hidden\" name=\"collection\" value=\"" + URLEncoder.encode(collection, "utf-8") + "\">\n"
                + "<input type=\"hidden\" name=\"maxRecommendations\" value=\"" + DEFAULT_MAX_RECOMMENDATIONS + "\">"
                + "<input type=\"hidden\" name=\"scope\" value=\"" + SCOPE + "\"><br>"
                + "<input type=\"submit\" value=\"Submit\" /></form></body></html>");
        return buf.toString();
    }

    /**
     * Display a HTML form for entering a query to get recommendations for.
     *
     * @param collection collection ID
     * @return HTML page with entry form.
     */
    @ResponseBody
    @RequestMapping(value = {"/" + RecommenderController.QUERY_ENTRY_HTML}, method = RequestMethod.GET)
    public String queryEntry(@RequestParam("collection") String collection) throws UnsupportedEncodingException {
        StringBuffer buf = new StringBuffer();

        buf.append(RECOMMENDATIONS_DOC_HEADER);
        buf.append("<h3>Source: " + SOURCE + "</h3>\n");
        buf.append("<form action=\"" + RECOMMENDER_PREFIX + RecommenderController.SEARCH_RECOMMENDATIONS_HTML
                + "\" method=\"GET\">");
        buf.append("<label for=\"query\">Query: </label>" +
                "<input id=\"query\" class=\"text\" type=\"text\" title=\"Query\" name=\"query\" size=\"70\"/>" +
                "<input type=\"hidden\" name=\"collection\" value=\"" + URLEncoder.encode(collection, "utf-8") + "\">" +
                "<input type=\"hidden\" name=\"scope\" value=\"" + SCOPE + "\">");
        buf.append("<br><br>Sort Descending: " + HTMLUtils.getSortRadioButtons(SortType.Parameter.dsort));
        buf.append("<br><br>Sort Ascending: " + HTMLUtils.getSortRadioButtons(SortType.Parameter.asort));

        buf.append("<br><br>Optional metadata class to sort on: <select name=\"metadataClass\">");
        for (String id : DocInfoQuery.ALL_METADATA) {
            String selected = "";

            if (id.equals(SortType.DEFAULT_METADATA_CLASS)) {
                selected = "selected";
            }
            buf.append("<option value=\"" + id + "\" " + selected + ">" + id + "</option>");
        }

        buf.append("</select><br>");

        buf.append("<br><br>Maxiumum Recommendations: <select name=\"maxRecommendations\">");
        for (int i = 1; i <= 10; i++) {
            String selected = "";

            if (i == DEFAULT_MAX_RECOMMENDATIONS) {
                selected = "selected";
            }
            buf.append("<option value=\"" + i + "\" " + selected + ">" + i + "</option>");
        }

        buf.append("</select><br><br>");
        buf.append("<input type=\"submit\" value=\"Submit\"/> <input type=\"reset\" value=\"Reset\">");
        buf.append("</form></body></html>");
        return buf.toString();
    }
}

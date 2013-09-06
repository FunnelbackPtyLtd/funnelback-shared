package com.funnelback.publicui.recommender.web.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;

import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfoQuery;
import com.funnelback.publicui.recommender.FBRecommender;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.SortType;
import com.funnelback.publicui.recommender.compare.MetaDataComparator;
import com.funnelback.publicui.recommender.tuple.ItemTuple;
import com.funnelback.publicui.recommender.tuple.PreferenceTuple;
import com.funnelback.publicui.recommender.utils.HTMLUtils;
import com.funnelback.publicui.recommender.utils.ItemUtils;
import com.funnelback.publicui.recommender.utils.RecommenderUtils;

@Controller
@RequestMapping("/recommender")
public class DevRecommenderController {

    private static final String RECOMMENDATIONS_DOC_HEADER = "<html><head><title>Recommendations</title><head><body><h1>Recommendations</h1>";
    private static final String SESSIONS_DOC_HEADER = "<html><head><title>Sessions</title><head><body><h1>Sessions</h1>";
    private static final String SCOPE = "handbook.curtin.edu.au/units,courses.curtin.edu.au/course_overview";
    private static final String CURTIN = "Curtin University";
    private static final String SOURCE = CURTIN;
    private static final String CURTIN_SEARCH_PREFIX =
            "http://127.0.0.1:8080/s/search.json?collection=test-curtin-courses";
    private static final String SEARCH_URL = CURTIN_SEARCH_PREFIX;
	private static FBRecommender fbRecommender;
    private static final int DEFAULT_MAX_RECOMMENDATIONS = 10;
    
    static {
		// Get a single instance of the Recommender.
        fbRecommender = FBRecommender.getInstance();
	}

	
	@Resource(name="jsonView")
	private View view;
	
    /**
     * Return a HTML page displaying the recommendations for each result for the given query, collection & scope.
     *
     * @param query search term(s) (required)
     * @param collection collection ID (required)
     * @param scope comma separated list of scope(s) to apply to suggestions (may be null or empty)
     * @param maxRecommendations maximum number of recommendations to display for each item (less than 1 means unlimited)
     * @param dsort descending sort parameter (optional)
     * @param asort ascending sort parameter (optional)
     * @param metadataClass metadata field ID if sorting on a metadata field (optional)
     * @return HTML page with recommendations
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value={"/" + RecommenderController.searchRecommendationsHtml}, method = RequestMethod.GET)
    public String searchRecommendations(@RequestParam("query") String query,
    		@RequestParam("collection") String collection,
    		@RequestParam("scope") String scope,
    		@RequestParam("maxRecommendations") int maxRecommendations,
    		@RequestParam("dsort") String dsort,
    		@RequestParam("asort") String asort,
    		@RequestParam("metadataClass") String metadataClass)
                                            throws UnsupportedEncodingException {
        Comparator<Recommendation> comparator;
        List<Map<String, Object>> results = null;
        String searchService;
        String encodedScope;

        // Validate/normalize input parameters
        if (query == null || ("").equals(query)) {
            return HTMLUtils.getErrorPage(RECOMMENDATIONS_DOC_HEADER, "Query term(s) must be provided.");
        }

        if (collection == null || ("").equals(collection)) {
            return HTMLUtils.getErrorPage(RECOMMENDATIONS_DOC_HEADER, "collection parameter must be provided.");
        }

        if (metadataClass != null || ("").equals(metadataClass)) {
            if (!DocInfoQuery.isValidMetadataClass(metadataClass)) {
                return HTMLUtils.getErrorPage(RECOMMENDATIONS_DOC_HEADER, "metadataClass parameter value is invalid: "
                    + metadataClass);
            }
        }

        if (scope != null && !("").equals(scope)) {
            encodedScope = URLEncoder.encode(scope, "utf-8");
            searchService = new String((SEARCH_URL + "&scope=" + encodedScope));
        }
        else {
            searchService = SEARCH_URL;
        }

        try {
            comparator = SortType.getComparator(asort, dsort, metadataClass);
        }
        catch (Exception exception) {
            System.out.println("searchRecommendations(): " + exception);
            return HTMLUtils.getErrorPage(RECOMMENDATIONS_DOC_HEADER, exception.toString());
        }

        if (!(comparator instanceof MetaDataComparator))  {
            metadataClass = "";
        }

        long startTime = System.currentTimeMillis();

        try {
            results = RecommenderUtils.getResults(query, searchService);
        }
        catch (IOException exception) {
            System.out.println(exception);
        }
        long timeTaken = (System.currentTimeMillis() - startTime);

        StringBuffer buf = new StringBuffer();
        buf.append(RECOMMENDATIONS_DOC_HEADER);

        if (results != null && !results.isEmpty()) {
            Set<String> originalResults = new HashSet<>();

            buf.append("<p>Original results for query: ");
            buf.append(HTMLUtils.getSearchLink(searchService, query, HTMLUtils.ResultFormat.html));
            buf.append(" (" + HTMLUtils.getSearchLink(searchService, query, HTMLUtils.ResultFormat.json) +") ");
            buf.append("<small>(query time: " + timeTaken + "ms)</small></p>\n");

            for (Map<String, Object> result : results) {
                String resultURL = (String) result.get("liveUrl");
                String title = (String) result.get("title");
                resultURL = resultURL.trim();
                title = title.trim();

                buf.append("<ul><li><a href=\"" + resultURL + "\">" + title
                        + "</a> ");

                if (fbRecommender.knownItem(resultURL)) {
                    String encodedResultURL = URLEncoder.encode(resultURL, "utf-8");
                    buf.append("[<a href=\"" + RecommenderController.RECOMMENDER_PREFIX + RecommenderController.sessionsHtml + "?itemName="
                            + encodedResultURL + "&seedItem=" + encodedResultURL + "&collection=" + collection
                            + "&minClicks=" + RecommenderController.MIN_CLICKS_PER_SESSION + "\">Sessions</a>]</li>\n");
                } else {
                    buf.append(" [Item was not clicked on]</li>");
                }

                List<Recommendation> recommendations =
                    RecommenderUtils.getRecommendationsForItem(fbRecommender, resultURL, collection, scope, maxRecommendations);
                List<Recommendation> sortedRecommendations
                    = RecommenderUtils.sortRecommendations(recommendations, comparator);
                buf.append(HTMLUtils.getHTMLRecommendations(sortedRecommendations, resultURL, collection,
                        scope, maxRecommendations, dsort, asort, metadataClass));
                buf.append("</ul>\n");
                originalResults.add(resultURL);
            }

            timeTaken = (System.currentTimeMillis() - startTime);
            buf.append("<p>Recommendation time: " + timeTaken + "ms</p>\n");
        }
        else if (results == null) {
            buf.append("<p>Error getting results from search service.</p>");
        }
        else {
            buf.append("<p>No results found for query: " + query + " </p>");
        }

        buf.append("</body></html>");
        return buf.toString();
    }

    /**
     * Return a HTML page displaying sessions which included the given item (clicked URL).
     *
     * @param itemName Name of item e.g. URL address
     * @param seedItem The seed which led to this item being recommended. Could be a query or a URL.
     * @param collection
     * @return HTML page displaying session information.
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value={"/" + RecommenderController.sessionsHtml}, method = RequestMethod.GET)
    public static String sessions(@RequestParam("itemName") String itemName,
    		@RequestParam("seedItem") String seedItem,
    		@RequestParam("collection") String collection,
    		@RequestParam("minClicks") int minClicks) throws Exception {
        StringBuffer buf = new StringBuffer();
        String searchURL = SEARCH_URL.replaceAll("\\.json", "\\.html");
        buf.append(SESSIONS_DOC_HEADER);

        String keyTitle = ItemUtils.getTitle(itemName, collection);
        if ("".equals(keyTitle)) {
            keyTitle = itemName;
        }

        buf.append("<p>Sessions with " + minClicks + " or more clicks for: <a href=\""
                + itemName + "\">" + keyTitle + "</a></p>");

        Set<List<PreferenceTuple>> sessions = fbRecommender.getSessions(itemName);

        if (sessions != null && !sessions.isEmpty()) {
            for(List<PreferenceTuple> session : sessions) {
                if (session.size() >= minClicks) {
                    String sessionID = session.get(0).getUserID();
                    String host = session.get(0).getHost();

                    buf.append("<h2>Session: " + sessionID + " Host: " + host + "</h2><ul>\n");

                    List<ItemTuple> items = ItemUtils.getItemsFromPreferences(session);
                    Map<URI, DocInfo> docInfoMap
                            = ItemUtils.getDocInfoMap(items, collection, SortType.DEFAULT_METADATA_CLASS);

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

                            if (url.equals(seedItem)) {
                                url = "<font color=\"red\">" + url + "</font>";
                            }

                            buf.append("<li><a href=\"" + address + "\">" + title + "</a> <small>"
                                    + url + " Date: " + preference.getDate() + " Query: <a href=\"" + searchURL
                                    + URLEncoder.encode(query, "utf-8") + "\">" + query + "</a> QIE Score: "
                                    + qieScore + "</small></li>\n");
                        }
                    }
                    else {
                        buf.append("<li>No document information available from index for URLs in this session</li>");
                    }

                    buf.append("</ul>");
                }
            }
        }
        else {
            buf.append("<p>No sessions found.</p>");
        }

        buf.append("</ul></body></html>");
        return buf.toString();
    }

    /**
     * Display a HTML form for entering an item to get recommendations for.
     * @param collection collection ID
     * @return HTML page with entry form.
     */
    @ResponseBody
    @RequestMapping(value={"/" + RecommenderController.itemEntryHtml}, method = RequestMethod.GET)
    public String itemEntry(@RequestParam("collection") String collection) throws UnsupportedEncodingException {
        StringBuffer buf = new StringBuffer();
        
        buf.append(RECOMMENDATIONS_DOC_HEADER);
        buf.append("<form action=\"" + RecommenderController.RECOMMENDER_PREFIX + RecommenderController.similarItemsJson + "\" method=\"GET\">");
        buf.append("<label for=\"seedItem\">Recommend URLs for URL: </label>\n" +
                "<input id=\"seedItem\" class=\"text\" type=\"text\" title=\"Item\" name=\"seedItem\" size=\"70\"/>" +
                "<input type=\"hidden\" name=\"collection\" value=\"" + URLEncoder.encode(collection, "utf-8") + "\">\n"
                + "<input type=\"hidden\" name=\"maxRecommendations\" value=\"" + DEFAULT_MAX_RECOMMENDATIONS + "\">"
                + "<input type=\"hidden\" name=\"sort\" value=\"" + SortType.Type.cooccurrence + "\"><br>"
                + "<input type=\"hidden\" name=\"scope\" value=\"" + SCOPE + "\"><br>"
                + "<input type=\"submit\" value=\"Submit\" /></form></body></html>");
        return buf.toString();
    }
    
    /**
     * Display a HTML form for entering a query to get recommendations for.
     * @param collection collection ID
     * @return HTML page with entry form.
     */
    @ResponseBody
    @RequestMapping(value={"/" + RecommenderController.queryEntryHtml}, method = RequestMethod.GET)
    public String queryEntry(@RequestParam("collection") String collection) throws UnsupportedEncodingException {
        StringBuffer buf = new StringBuffer();
        
        buf.append(RECOMMENDATIONS_DOC_HEADER);
        buf.append("<h3>Source: " + SOURCE + "</h3>\n");
        buf.append("<form action=\"" + RecommenderController.RECOMMENDER_PREFIX + RecommenderController.searchRecommendationsHtml + "\" method=\"GET\">");
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
            buf.append("<option value=\"" + id + "\" " + selected +  ">" + id + "</option>");
        }

        buf.append("</select><br>");

        buf.append("<br><br>Maxiumum Recommendations: <select name=\"maxRecommendations\">");
        for (int i = 1; i <= 10; i++) {
            String selected = "";

            if (i == DEFAULT_MAX_RECOMMENDATIONS) {
                selected = "selected";
            }
            buf.append("<option value=\"" + i + "\" " + selected +  ">" + i + "</option>");
        }

        buf.append("</select><br><br>");
        buf.append("<input type=\"submit\" value=\"Submit\"/> <input type=\"reset\" value=\"Reset\">");
        buf.append("</form></body></html>");
        return buf.toString();
    }


}

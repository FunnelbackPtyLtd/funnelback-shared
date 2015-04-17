package com.funnelback.publicui.recommender.web.controllers;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.Recommender;
import com.funnelback.publicui.recommender.dao.RecommenderDAO;
import com.funnelback.publicui.recommender.dataapi.DataAPI;
import com.funnelback.publicui.recommender.utils.HTMLUtils;
import com.funnelback.publicui.recommender.utils.SearchUtils;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.reporting.recommender.tuple.PreferenceTuple;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Controller for developer access to Recommender system.
 * NB: Since this is for developers only it currently has no unit tests exercising it.
 * TODO FUN-5961: Move this into another "developer tools" WAR file.
 * @author fcrimmins@funnelback.com
 */

@Controller
@RequestMapping("/recommender")
@Log4j2
public class DevRecommenderController {

    public static final String SEARCH_RECOMMENDATIONS_HTML = "searchRecommendations.html";
    public static final String QUERY_ENTRY_HTML = "queryEntry.html";
    public static final String ITEM_ENTRY_HTML = "itemEntry.html";
    public static final String SESSIONS_HTML = "sessions.html";
    public static final String RECOMMENDER_PREFIX = DefaultValues.ModernUI.CONTEXT_PATH + "recommender/";
    private static final String RECOMMENDATIONS_DOC_HEADER = "<html><head><title>Recommendations</title><head><body><h1>Recommendations</h1>";
    private static final String SESSIONS_DOC_HEADER = "<html><head><title>Sessions</title><head><body><h1>Sessions</h1>";
    private static final int DEFAULT_MAX_RECOMMENDATIONS = 10;
    private static final String CURTIN_SCOPE = "handbook.curtin.edu.au/units,courses.curtin.edu.au/course_overview";
    private static final String CURTIN_COLLECTION = "test-curtin-courses";
    private static final String SEARCH_JSON = "search.json?collection=";

    @Autowired
    @Setter
    private ConfigRepository configRepository;

    @Autowired
    @Setter
    private DataAPI dataAPI;

    @Autowired
    @Setter
    private RecommenderDAO recommenderDAO;
    
    /**
     * Return a HTML page displaying the recommendations for each result for the given query, collection &amp; scope.
     * @param query              search term(s) (required)
     * @param collection         collection ID (required)
     * @param scope              comma separated list of scope(s) to apply to suggestions (may be null or empty)
     * @param maxRecommendations maximum number of recommendations to display for each item (less than 1 means unlimited)
     * @return HTML page with recommendations
     */
    @ResponseBody
    @RequestMapping(value = {"/" + SEARCH_RECOMMENDATIONS_HTML}, method = RequestMethod.GET)
    public String searchRecommendations(HttpServletRequest request,
                                        @RequestParam("query") String query,
                                        @RequestParam("collection") String collection,
                                        @RequestParam("scope") String scope,
                                        @RequestParam("maxRecommendations") int maxRecommendations)
            throws UnsupportedEncodingException, MalformedURLException {
        List<Map<String, Object>> results = null;
        URL requestURL = new URL(request.getRequestURL().toString());
        String searchServiceAddress = "http://" + requestURL.getAuthority() + "/s/"
                + SEARCH_JSON + collection;

        StringBuffer buf = new StringBuffer();
        buf.append(RECOMMENDATIONS_DOC_HEADER);

        com.funnelback.publicui.search.model.collection.Collection collectionRef
                = configRepository.getCollection(collection);

        if (collectionRef != null) {
            long startTime = System.currentTimeMillis();
            SearchUtils searchUtils = new SearchUtils(collectionRef.getConfiguration());

            try {
                results = searchUtils.getResults(query, searchServiceAddress, scope, DEFAULT_MAX_RECOMMENDATIONS);
            } catch (IOException exception) {
                log.error(exception);
            }
            long timeTaken = (System.currentTimeMillis() - startTime);

            if (results != null && !results.isEmpty()) {
                Set<String> originalResults = new HashSet<>();

                buf.append("<p>Original results for query: ");
                buf.append(HTMLUtils.getSearchLink(searchServiceAddress, query, HTMLUtils.ResultFormat.html));
                buf.append(" (" + HTMLUtils.getSearchLink(searchServiceAddress, query, HTMLUtils.ResultFormat.json) + ") ");
                buf.append("<small>(query time: " + timeTaken + "ms)</small></p>\n");

                for (Map<String, Object> result : results) {
                    String resultURL = (String) result.get("liveUrl");
                    String title = (String) result.get("title");
                    resultURL = resultURL.trim();
                    title = title.trim();

                    String encodedCollection = HTMLUtils.getEncodedParameter("collection", collection);
                    String encodedScope = HTMLUtils.getEncodedParameter("scope", scope);

                    buf.append("<ul><li><a href=\"" + resultURL + "\">" + title
                            + "</a> ");

                    String encodedResultURL = URLEncoder.encode(resultURL, "utf-8");
                    buf.append("[<a href=\"" + RECOMMENDER_PREFIX + RecommenderController.SIMILAR_ITEMS_JSON + "?seedItem="
                            + encodedResultURL + encodedCollection + encodedScope
                            + "&maxRecommendations=" + maxRecommendations + "\">JSON</a>] \n");
                    buf.append("[<a href=\"" + RECOMMENDER_PREFIX + SESSIONS_HTML + "?itemName="
                            + encodedResultURL + "&seedItem=" + encodedResultURL + encodedCollection
                            + "\">Sessions</a>]</li>\n");

                    try {
                        Recommender recommender =
                                new Recommender(collectionRef, dataAPI, recommenderDAO, resultURL, "", configRepository);

                        List<Recommendation> recommendations =
                                recommender.getRecommendationsForItem(resultURL, scope,
                                        maxRecommendations, com.funnelback.reporting.recommender.tuple.ItemTuple.Source.DEFAULT);
                        buf.append(HTMLUtils.getHTMLRecommendations(recommendations, resultURL, collection,
                                scope, maxRecommendations));	
                    }
                    catch (IllegalStateException exception) {
                    	log.warn(exception);
                    }
 
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
     * @param itemName   Name of item e.g. URL address
     * @param seedItem   The seed which led to this item being recommended. Could be a query or a URL.
     * @param collection name of collection
     * @return HTML page displaying session information.
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = {"/" + SESSIONS_HTML}, method = RequestMethod.GET)
    public String sessions(@RequestParam("itemName") String itemName,
                           @RequestParam("seedItem") String seedItem,
                           @RequestParam("collection") String collection) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        String searchServiceAddress = SEARCH_JSON + collection;
        searchServiceAddress = searchServiceAddress.replaceAll("\\.json", "\\.html");

        Collection collectionRef
                = configRepository.getCollection(collection);

        if (collectionRef != null) {      	
            try {
                Recommender recommender =
                        new Recommender(collectionRef, dataAPI, recommenderDAO, itemName, "", configRepository);
                Config collectionConfig = recommender.getCollectionConfig();
                
                stringBuffer.append(getSessionsHeader(itemName, seedItem, collectionConfig));
                Set<List<PreferenceTuple>> sessions = recommender.getSessions(itemName);
                
                if (sessions != null && !sessions.isEmpty()) {
                    for (List<PreferenceTuple> session : sessions) {
                        String sessionID = session.get(0).getUserID();
                        String host = session.get(0).getHost();

                        stringBuffer.append("<h2>Session: " + sessionID + " Host: " + host + "</h2><ul>\n");

                        List<String> urls = new ArrayList<>();
                        for (PreferenceTuple preference : session) {
                            urls.add(preference.getItemID());
                        }

                        Map<URI, DocInfo> docInfoMap = dataAPI.getDocInfoResult(urls, collectionConfig).asMap();

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

                                stringBuffer.append("<li><a href=\"" + address + "\">" + title + "</a> <small>"
                                        + url + " Date: " + preference.getDate() + " Query: <a href=\"" + searchServiceAddress
                                        + URLEncoder.encode(query, "utf-8") + "\">" + query + "</a> QIE Score: "
                                        + qieScore + "</small></li>\n");
                            }
                        } else {
                            stringBuffer.append("<li>No document information available from index for URLs in this session</li>");
                        }

                        stringBuffer.append("</ul>");
                    }
                } else {
                    stringBuffer.append("<p>No sessions found.</p>");
                }              
            }
            catch (IllegalStateException exception) {
            	log.warn(exception);
                stringBuffer.append("<p>Unable to get a valid collection.</p>");
            }           
        }

        stringBuffer.append("</ul></body></html>");
        return stringBuffer.toString();
    }

    /**
     * Return a header for the sessions page for the given input parameters.
     * @param itemName         Name of item e.g. URL address
     * @param seedItem         The seed which led to this item being recommended. Could be a query or a URL.
     * @param collectionConfig Collection Config object
     * @return String containing HTML for sessions page header.
     */

    private String getSessionsHeader(String itemName, String seedItem, Config collectionConfig) {
        StringBuffer buf = new StringBuffer();
        buf.append(SESSIONS_DOC_HEADER);

        String keyTitle = dataAPI.getTitle(itemName, collectionConfig);

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
     * @param collection collection ID
     * @return HTML page with entry form.
     */
    @ResponseBody
    @RequestMapping(value = {"/" + ITEM_ENTRY_HTML}, method = RequestMethod.GET)
    public String itemEntry(@RequestParam("collection") String collection) throws UnsupportedEncodingException {
        StringBuffer buf = new StringBuffer();
        String scope = "";

        if (collection.equals(CURTIN_COLLECTION)) {
            scope = CURTIN_SCOPE;
        }

        buf.append(RECOMMENDATIONS_DOC_HEADER);
        buf.append("<form action=\"" + RECOMMENDER_PREFIX + RecommenderController.SIMILAR_ITEMS_JSON + "\" method=\"GET\">");
        buf.append("<label for=\"seedItem\">Recommend URLs for URL: </label>\n" +
                "<input id=\"seedItem\" class=\"text\" type=\"text\" title=\"Item\" name=\"seedItem\" size=\"70\"/>" +
                "<input type=\"hidden\" name=\"collection\" value=\"" + URLEncoder.encode(collection, "utf-8") + "\">\n"
                + "<input type=\"hidden\" name=\"maxRecommendations\" value=\"" + DEFAULT_MAX_RECOMMENDATIONS + "\">"
                + "<input type=\"hidden\" name=\"scope\" value=\"" + scope + "\"><br>"
                + "<input type=\"submit\" value=\"Submit\" /></form></body></html>");
        return buf.toString();
    }

    /**
     * Display a HTML form for entering a query to get recommendations for.
     * @param collection collection ID
     * @return HTML page with entry form.
     */
    @ResponseBody
    @RequestMapping(value = {"/" + QUERY_ENTRY_HTML}, method = RequestMethod.GET)
    public String queryEntry(@RequestParam("collection") String collection) throws UnsupportedEncodingException {
        StringBuffer buf = new StringBuffer();
        String scope = "";

        if (collection.equals(CURTIN_COLLECTION)) {
            scope = CURTIN_SCOPE;
        }

        buf.append(RECOMMENDATIONS_DOC_HEADER);
        buf.append("<form action=\"" + RECOMMENDER_PREFIX + SEARCH_RECOMMENDATIONS_HTML
                + "\" method=\"GET\">");
        buf.append("<label for=\"query\">Query: </label>" +
                "<input id=\"query\" class=\"text\" type=\"text\" title=\"Query\" name=\"query\" size=\"70\"/>" +
                "<input type=\"hidden\" name=\"collection\" value=\"" + URLEncoder.encode(collection, "utf-8") + "\">" +
                "<input type=\"hidden\" name=\"scope\" value=\"" + scope + "\">");

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
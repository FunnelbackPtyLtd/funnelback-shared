package com.funnelback.publicui.recommender.utils;

import com.funnelback.common.utils.ObjectMapperSingleton;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.compare.SortType;
import com.funnelback.publicui.recommender.web.controllers.RecommenderController;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Utilities for generating HTML output.
 * @author fcrimmins@funnelback.com
 */
public final class HTMLUtils {
    private static final Logger logger = Logger.getLogger(HTMLUtils.class);
    public static final int MIN_CLICKS_PER_SESSION = 2;

    /**
     * Get search results for the given query and search URL. Each result is a Map which can be queried
     * like: result.get("liveUrl") or result.get("title");
     *
     * @param query         search terms.
     * @param searchService search service URL
     * @return List of results from the search engine result packet.
     * @throws Exception
     */
    public static List<Map<String, Object>> getResults(String query, String searchService) throws IOException {
        List<Map<String, Object>> results = null;
        HttpURLConnection urlConnection = null;
        ObjectMapper mapper = ObjectMapperSingleton.getInstance();

        if (query != null && !query.trim().equals("")) {
            try {
                URL url = new URL(searchService + "&query=" + URLEncoder.encode(query, "utf-8"));
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                // Drill down through the JSON to get to the result packet
                Map<String, Object> jsonMap = mapper.readValue(in, Map.class);
                Map<String, Object> response = (Map<String, Object>) jsonMap.get("response");
                Map<String, Object> resultPacket = (Map<String, Object>) response.get("resultPacket");
                results = (List<Map<String, Object>>) resultPacket.get("results");
            } catch (NullPointerException nullPointerException) {
                logger.error("FBRecommenderREST.getResults(): " + nullPointerException);
            } catch (UnsupportedEncodingException exception) {
                logger.error("FBRecommenderREST.getResults(): " + exception);
            } finally {
                urlConnection.disconnect();
            }
        }

        return results;
    }

    public static enum ResultFormat {
        html("html"),
        json("json");

        private final String type;

        private ResultFormat(final String value) {
        	this.type = value;
		}

        @Override
        public String toString() {
            return type;
        }
    }

    // Private constructor to avoid unnecessary instantiation of the class
    private HTMLUtils() {
    }

    /**
     * Return an error page which displays the given header and message.
     * @param header HTML header code
     * @param message error message to display
     * @return HTML error page string
     */
    public static String getErrorPage(String header, String message) {
        StringBuffer buf = new StringBuffer();
        buf.append(header);
        buf.append("<h2>Error</h2>");
        buf.append("<p>" + message + "</p>");
        buf.append("</body></html>");
        return buf.toString();
    }

    /**
     * Return an encoded version of the given parameter in "name=value" form
     * @param name name of parameter
     * @param value value of parameter
     * @return encoded version or the empty string
     * @throws UnsupportedEncodingException
     */
    public static String getEncodedParameter(String name, String value) throws UnsupportedEncodingException {
        String parameter = "";

        if (value != null && !("").equals(value)) {
            parameter = "&" + name + "=" + URLEncoder.encode(value, "utf-8");
        }

        return parameter;
    }

    /**
     * Return a HTML version of the given list of recommendations, suitable for inclusion in a web page.
     *
     * @param recommendations List of {@link com.funnelback.publicui.recommender.Recommendation}s
     * @param seedItem seed item that recommendations are for
     * @param collection collection ID (required)
     * @param scope comma separated list of scope(s) to apply to suggestions (may be null or empty)
     * @param maxRecommendations maximum number of recommendations to display for each item (less than 1 means unlimited)
     * @param dsort descending sort parameter (optional)
     * @param asort ascending sort parameter (optional)
     * @param metadataClass metadata field ID if sorting on a metadata field (optional)
     */
    public static String getHTMLRecommendations(List<Recommendation> recommendations, String seedItem,
            String collection, String scope, int maxRecommendations,
            String dsort, String asort, String metadataClass) {
        StringBuffer buf = new StringBuffer();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        String timeStamp = "N/A";
        int numRecommendations = 0;
        String encodedMetadataClass;

        if (maxRecommendations < 1) {
            maxRecommendations = 10;
        }

        if (recommendations != null && recommendations.size() > 0) {
            buf.append("<ul>\n");

            try {
                collection = getEncodedParameter("collection", collection);
                scope = getEncodedParameter("scope", scope);
                seedItem = getEncodedParameter("seedItem", seedItem);
                dsort = getEncodedParameter("dsort", dsort);
                asort = getEncodedParameter("asort", asort);
                encodedMetadataClass = getEncodedParameter("metadataClass", metadataClass);

                for (Recommendation recommendation : recommendations) {
                    String item = recommendation.getItemID();
                    String title = recommendation.getTitle();
                    Date date = recommendation.getDate();
                    float confidence = recommendation.getConfidence();
                    float qieScore = recommendation.getQieScore();
                    String metaData = recommendation.getMetaData().get(metadataClass);

                    if (metaData != null) {
                        metaData = StringEscapeUtils.escapeHtml(metaData);
                        metaData = " <b>Metadata class " + metadataClass + "</b>: " + metaData;
                    }
                    else {
                        metaData = "";
                    }

                    if (date != null) {
                        timeStamp = df.format(date);
                    }

                    String encodedItem = URLEncoder.encode(item, "utf-8");

                    String similarLink =  RecommenderController.SIMILAR_ITEMS_JSON + "?seedItem="
                            + encodedItem + collection + scope + "&maxRecommendations="
                            + maxRecommendations + dsort + asort + encodedMetadataClass;

                    String sessionsLink = RecommenderController.SESSIONS_HTML + "?itemName=" + encodedItem
                            + seedItem + collection + "&minClicks=" + MIN_CLICKS_PER_SESSION;

                    buf.append("<li><a href=\"" + item + "\">" + title + "</a> <small>"
                            + item + "</small> [<a href=\"" + similarLink + "\">JSON</a>] "
                            + "[<a href=\"" + sessionsLink + "\">Sessions</a>] ");
                    buf.append("<ul><li><small> <b>Confidence</b>: "
                            + confidence + " <b>Date</b>: " + timeStamp
                            + " <b>QIE Score</b>: " + qieScore
                            + metaData + "</small></li></ul></li>\n");
                    numRecommendations++;
                }
                buf.append("</ul>");
            }
            catch (UnsupportedEncodingException exception) {

            }
        }

        if (numRecommendations == 0) {
            buf.append("<ul><li>No recommendations found.</li></ul>\n");
        }

        return buf.toString();
    }

    /**
     * Return a HTML hyperlink based on the given service URL, query and format.
     * @param searchService URL for search service
     * @param query query term(s)
     * @param format format e.g. {@link ResultFormat}
     * @return HTML hyperlink
     * @throws UnsupportedEncodingException
     */
    public static String getSearchLink(String searchService, String query, ResultFormat format)
            throws UnsupportedEncodingException {
        StringBuffer buf = new StringBuffer();
        String searchURL = searchService;
        String anchorText = "JSON";

        if (format.equals(ResultFormat.html)) {
            searchURL = searchService.replaceAll("\\.json", "\\.html");
            anchorText = query;
        }

        buf.append("<a href=\"" + searchURL + "&query=" + URLEncoder.encode(query, "utf-8")
                + "\">" + anchorText + "</a>");

        return buf.toString().trim();
    }

    /**
     * Return a string representing a set of HTML form radio buttons for the given sort parameter name.
     * @param parameter name of parameter (e.g. asort or dsort)
     * @return HTML string
     */
    public static String getSortRadioButtons(SortType.Parameter parameter) {
        StringBuffer buf = new StringBuffer();


        for (SortType.Type sortType : SortType.Type.values())  {
            String humanReadable = StringUtils.capitalize(sortType.toString());
            buf.append("<input type=\"radio\" name=\"" + parameter.toString() + "\" value=\""
                    + sortType + "\"> " +  humanReadable + " \n");
        }

        return buf.toString().trim();
    }
}
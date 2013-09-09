package com.funnelback.publicui.recommender.utils;

import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.SortType;
import com.funnelback.publicui.recommender.web.controllers.RecommenderController;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Utilities for generating HTML output.
 * @author fcrimmins@funnelback.com
 */
public final class HTMLUtils {
    public static final int MIN_CLICKS_PER_SESSION = 2;

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
                    long popularity = recommendation.getPopularity();
                    float confidence = recommendation.getConfidence();
                    float qieScore = recommendation.getQieScore();
                    String format = recommendation.getFormat();
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

                    String similarLink =  RecommenderController.similarItemsJson + "?seedItem="
                            + encodedItem + collection + scope + "&maxRecommendations="
                            + maxRecommendations + dsort + asort + encodedMetadataClass;

                    String sessionsLink = RecommenderController.sessionsHtml + "?itemName=" + encodedItem
                            + seedItem + collection + "&minClicks=" + MIN_CLICKS_PER_SESSION;

                    buf.append("<li><a href=\"" + item + "\">" + title + "</a> <small>"
                            + item + "</small> [<a href=\"" + similarLink + "\">JSON</a>] "
                            + "[<a href=\"" + sessionsLink + "\">Sessions</a>] <small> <b>Confidence</b>: "
                            + confidence + " <b>Date</b>: " + timeStamp + " <b>Popularity</b>: " + popularity
                            + " <b>QIE Score</b>: " + qieScore + " <b>Format</b>: " + format
                            + metaData + "</small></li>\n");
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
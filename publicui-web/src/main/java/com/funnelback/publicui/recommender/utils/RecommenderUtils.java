package com.funnelback.publicui.recommender.utils;

import com.funnelback.common.utils.FileUtils;
import com.funnelback.common.utils.ObjectMapperSingleton;
import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.publicui.recommender.FBRecommender;
import com.funnelback.publicui.recommender.Recommendation;
import com.funnelback.publicui.recommender.tuple.ItemTuple;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Miscellaneous recommender utility methods.
 * @author fcrimmins@funnelback.com
 */
public final class RecommenderUtils {

    // Private constructor to avoid unnecessary instantiation of the class
    private RecommenderUtils() {
    }

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
                System.out.println("FBRecommenderREST.getResults(): " + nullPointerException);
            } catch (UnsupportedEncodingException exception) {
                System.out.println("FBRecommenderREST.getResults(): " + exception);
            } finally {
                urlConnection.disconnect();
            }
        }

        return results;
    }

    /**
     * Sort the given list of recommendations using the specified {@link Comparator}
     * @param recommendations list of {@link Recommendation}s
     * @param comparator The comparator to apply
     * @return sorted list
     */
    public static List<Recommendation> sortRecommendations(List<Recommendation> recommendations,
                                                           Comparator<Recommendation> comparator) {
        List<Recommendation> sortedRecommendations;

        if (comparator != null && recommendations != null && !recommendations.isEmpty()) {
            int size = recommendations.size();
            Recommendation[] recommendationsArray = recommendations.toArray(new Recommendation[size]);
            Arrays.sort(recommendationsArray, comparator);
            sortedRecommendations = new ArrayList<>(Arrays.asList(recommendationsArray));
        }
        else {
            sortedRecommendations = recommendations;
        }

        return sortedRecommendations;
    }

    /**
     * Write out a checkpointed version of the given object to the given file.
     * @param checkpointFile File to checkpoint to
     * @param object Object to checkpoint
     */
    public static void writeCheckpoint(File checkpointFile, Object object) {
        FileUtils.checkpoint(object, checkpointFile);
        System.out.println("Checkpointed to file: " + checkpointFile);
    }

    /**
     * Return a List of {@link com.funnelback.publicui.recommender.Recommendation}'s for the given item name.
     * @param recommender Recommender to get recommendations from
     * @param itemName name of item
     * @param collection collection that item is believed to exist in
     * @param scope comma separated list of items scopes
     * @param maxRecommendations maximum number of recommendations to display (less than 1 means unlimited)
     * @return List of recommendations (which may be empty).
     */
    public static List<Recommendation> getRecommendationsForItem(FBRecommender recommender, String itemName,
                                                                 String collection, String scope,
                                                                 int maxRecommendations) {
        List<Recommendation> recommendations = new ArrayList<>();
        List<ItemTuple> items = null;
        List<String> scopes = new ArrayList<>();

        if (scope != null && !("").equals(scope)) {
            scopes = Arrays.asList(scope.split(","));
        }

        try {
            items = recommender.recommendThingsForItem(itemName, maxRecommendations, scopes);
        }
        catch (Exception exception) {
            System.out.println(exception);
        }

        if (items != null && items.size() > 0) {
            Map<String,ItemTuple> map = new HashMap<>();
            for (ItemTuple i : items) map.put(i.getItemID(),i);

            List<DocInfo> dis = ItemUtils.getDocInfoList(items, collection);

            if (dis != null) {
                for(DocInfo docInfo : dis) {
                    URI uri = docInfo.getUri();
                    String itemID = uri.toString();
                    long popularity = recommender.getNumPreferences(itemID);
                    float confidence = map.get(itemID).getScore();
                    Recommendation recommendation = new Recommendation(itemID, confidence, popularity, docInfo);
                    recommendations.add(recommendation);
                }
            }
        }

        return recommendations;
    }
}

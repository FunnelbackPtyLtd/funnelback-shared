package com.funnelback.publicui.recommender;

import com.funnelback.common.config.Config;
import com.funnelback.publicui.recommender.utils.RecommenderUtils;
import com.funnelback.publicui.search.model.padre.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from the recommendations system.
 */
@AllArgsConstructor
public class RecommendationResponse {
    public static enum Source {
        CLICKS("CLICKS"),
        EXPLORE("EXPLORE"),
        NONE("NONE");

        private final String source;

        private Source(final String value) {
        	this.source = value;
		}

        @Override
        public String toString() {
            return source;
        }
    }

    public static enum Status {
        OK("OK"),
        SEED_NOT_FOUND("SEED_NOT_FOUND"),
        NO_SUGGESTIONS_FOUND("NO_SUGGESTIONS_FOUND"),
        ERROR("ERROR");

        private final String status;

        private Status(final String value) {
        	this.status = value;
		}

        @Override
        public String toString() {
            return status;
        }
    }

    @Getter
    private Status status;

    @Getter
    private String seedItem;

    @Getter
   	private String collection;

    @Getter
   	private String scope;

    @Getter
   	private int maxRecommendations;

    @Getter
   	private String sourceCollection;

    @Getter
   	private Source source;

    @Setter
    @Getter
   	private long timeTaken;

    @Getter
   	private List<Recommendation> recommendations;

    /**
     * Return a RecommendationResponse built from the given list of results (which came from an 'explore:url' query.
     *
     * @param seedItem seed URL
     * @param results list of results from explore query
     * @param collectionConfig collection config object
     * @param requestCollection name of the collection that the original recommendation request was made to
     * @param scope scope parameter (may be empty)
     * @param maxRecommendations maximum number of recommendations to return
     * */
    public static RecommendationResponse fromResults(String seedItem, List<Result> results,
                                                     Config collectionConfig, String requestCollection,
                                                     String scope, int maxRecommendations){
		List<Recommendation> recommendations;
        List<String> urls = new ArrayList<>();

        for (Result result : results) {
            String url = result.getDisplayUrl();
            urls.add(url);
        }

        if (scope == null) {
            scope = "";
        }

        recommendations
                = RecommenderUtils.decorateURLRecommendations(urls, null, collectionConfig, maxRecommendations);

        if (recommendations != null && recommendations.size() > 0) {
            return new RecommendationResponse(Status.OK, seedItem, requestCollection, scope, maxRecommendations,
                    collectionConfig.getCollectionName(), Source.EXPLORE, -1, recommendations);
        }
        else {
            return new RecommendationResponse(Status.NO_SUGGESTIONS_FOUND, seedItem, requestCollection, scope,
                    maxRecommendations, collectionConfig.getCollectionName(), Source.NONE, -1, null);
        }
	}
}

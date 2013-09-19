package com.funnelback.publicui.recommender;

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
        clicks("recency"),
        explore("explore"),
        purchases("purchases"),
        none("none");

        private final String source;

        private Source(final String value) {
        	this.source = value;
		}

        @Override
        public String toString() {
            return source;
        }
    }

    @Getter
    private String seedItem;

	@Getter
	private List<Recommendation> recommendations;

    @Getter
   	private Source source;

    @Setter
    @Getter
   	private long timeTaken;

	public static RecommendationResponse fromResults(String seedItem, List<Result> results){
		List<Recommendation> recommendations = new ArrayList<>();
		for (Result result : results) {
			recommendations.add(Recommendation.fromResult(result));
		}
		
		return new RecommendationResponse(seedItem, recommendations, Source.explore, -1);
	}
}

package com.funnelback.publicui.recommender;

import com.funnelback.reporting.recommender.tuple.ItemTuple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * A response from the Recommender System.
 * @author fcrimmins@funnelback.com
 */
@AllArgsConstructor
public class RecommendationResponse {
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
    private ItemTuple.Source source;

    @Setter
    @Getter
    private long timeTaken;

    @Getter
    private List<Recommendation> recommendations;
}

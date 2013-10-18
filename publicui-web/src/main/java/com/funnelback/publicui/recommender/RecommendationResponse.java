package com.funnelback.publicui.recommender;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * A response from the Recommender System.
 */
@AllArgsConstructor
public class RecommendationResponse {
    public static enum Source {
        CLICKS("CLICKS"),
        EXPLORE("EXPLORE"),
        DEFAULT("DEFAULT"),
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
}

package com.funnelback.publicui.recommender;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Response from the recommendations system.
 */
@AllArgsConstructor
public class RecommendationResponse {

	@Getter
	private List<Recommendation> recommendations;

}

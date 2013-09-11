package com.funnelback.publicui.recommender;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.funnelback.publicui.search.model.padre.Result;

/**
 * Response from the recommendations system.
 */
@AllArgsConstructor
public class RecommendationResponse {

	@Getter
	private List<Recommendation> recommendations;
	
	public static RecommendationResponse fromResults(List<Result> results){
		List<Recommendation> recommendations = new ArrayList<>();
		for (Result result : results) {
			recommendations.add(Recommendation.fromResult(result));
		}
		
		return new RecommendationResponse(recommendations);
	}

}

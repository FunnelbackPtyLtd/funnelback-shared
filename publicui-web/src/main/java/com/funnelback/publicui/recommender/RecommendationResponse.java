package com.funnelback.publicui.recommender;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class RecommendationResponse {

	@Getter
	private List<Recommendation> recommendations;

}

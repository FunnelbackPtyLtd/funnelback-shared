package com.funnelback.publicui.search.model.padre;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Search explain plan used in the Content Optimiser.
 * 
 * @since 11.0
 */
@RequiredArgsConstructor
public class Explain {

		@Getter public final float finalScore;  
		@Getter public final int consat;
		@Getter public final float lenratio;
		@Getter public final Map<String,Float> featureScores;
		
		/** Constants for the PADRE XML result packet tags. */
		public static final class Schema {
			
			public static final String CONSAT = "consat";
			public static final String FINAL_SCORE = "final_score";
			public static final String LENRATIO = "lenratio";
			public static final String COOLER_SCORES = "cooler_scores";
			public static final String FEATURE = "feature";
		}
}


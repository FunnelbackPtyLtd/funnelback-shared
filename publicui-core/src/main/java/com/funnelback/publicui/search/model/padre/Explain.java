package com.funnelback.publicui.search.model.padre;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Search explain plan used in the Content Optimiser.
 * 
 * @since 11.0
 */
@NoArgsConstructor
@AllArgsConstructor
public class Explain {

		/**
		 * The final score for this result
		 */
		@Getter public float finalScore;  
		
		/**
		 * The number of constraints this result satisfied
		 */
		@Getter public int consat;
		
		/**
		 * The length ratio of this document relative to the average document, measured in content words 
		 */
		@Getter public float lenratio;
		
		/**
		 * a Map of float scores for each ranking feature.&nbsp;Feature names are specified by their unique short name.
		 */
		@Getter public Map<String,Float> featureScores;
		
		/** Constants to enable extracting the Explain data from the PADRE XML result packet tags. */
		public static final class Schema {
			public static final String CONSAT = "consat";
			public static final String FINAL_SCORE = "final_score";
			public static final String LENRATIO = "lenratio";
			public static final String COOLER_SCORES = "cooler_scores";
			public static final String FEATURE = "feature";
		}
}


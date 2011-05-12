package com.funnelback.publicui.search.model.padre;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Explain {

		@Getter public final float finalScore;  
		@Getter public final int consat;
		@Getter public final float lenratio;
		@Getter public final Map<String,Float> featureScores;
		
		public static final class Schema {
			
			public static final String CONSAT = "consat";
			public static final String FINAL_SCORE = "final_score";
			public static final String LENRATIO = "lenratio";
			public static final String COOLER_SCORES = "cooler_scores";
			public static final String FEATURE = "feature";
		}
}


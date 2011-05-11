package com.funnelback.publicui.search.model.padre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Explain {

		@Getter public final float finalScore;  
		
		public static final class Schema {
			
			public static final String CONSAT = "consat";
			public static final String FINAL_SCORE = "final_score";
			public static final String LENRATIO = "lenratio";
			public static final String COOLER_SCORES = "cooler_scores";
			public static final String FEATURE = "feature";
		}
}


package com.funnelback.publicui.search.service;

import java.util.List;

import com.funnelback.publicui.search.model.transaction.Suggestion;


public interface Suggester {

	public enum Sort {
		Weight(0), Length(1), Alphabetic(2);
		
		private final int value;
		
		private Sort(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static Sort valueOf(int i) {
			switch (i) {
			case 0:
				return Weight;
			case 1:
				return Length;
			case 2:
				return Alphabetic;
			default:
				throw new IllegalArgumentException();
			}			
		}
	}
	
	public enum AutoCMode {
		Yes("yes"), No("no"), TryIt("tryit");
		
		private final String value;
		
		private AutoCMode(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
		public static AutoCMode valueOf(int i) {
			switch (i) {
			case 0:
				return Yes;
			case 1:
				return No;
			case 2:
				return TryIt;
			default:
				throw new IllegalArgumentException();
			}
		}
	}
	
	public List<Suggestion> suggest(String collectionId, String partialQuery, int numSuggestions, Sort sort, float alpha, AutoCMode autoCMode);
	
}

package com.funnelback.publicui.search.model.transaction;

import java.nio.ByteBuffer;
import java.util.Comparator;

import com.funnelback.publicui.search.model.padre.PadreNative;

public class Suggestion {
	public final float weight;
	public final String suggestion;
		
	public Suggestion(float weight, String suggestion) {
		this.weight = weight;
		this.suggestion = suggestion;
	}
	
	public static Suggestion fromBytes(ByteBuffer buf) {
		float weight = buf.getFloat();
		char length = (char) buf.get();
		char[] letters = new char[length];
		for (int i=0; i<length; i++) {
			letters[i] = (char) buf.get();
		}
		buf.position(buf.position()+PadreNative.SizeOf.SUGGEST_T-(PadreNative.SizeOf.FLOAT + PadreNative.SizeOf.CHAR+length));

		
		return new Suggestion(weight, new String(letters));
	}
	
	@Override
	public String toString() {
		return suggestion + " ("+weight+")";
	}
	
	public double score(float alpha, int inputQueryLength) {
		return (alpha*(double)weight) + (1.0-alpha) / ((double)suggestion.length()-inputQueryLength);
	}
	
	public static class ByWeightComparator implements Comparator<Suggestion> {

		private final float alpha;
		private final int inputQueryLength;
		
		public ByWeightComparator(float alpha, int inputQueryLength) {
			this.alpha = alpha;
			this.inputQueryLength = inputQueryLength;
		}
		
		@Override
		public int compare(Suggestion s1, Suggestion s2) {
			if (s2.score(alpha, inputQueryLength) < s1.score(alpha, inputQueryLength)) return -1;
			if (s2.score(alpha, inputQueryLength) > s1.score(alpha, inputQueryLength)) return 1;
			return s2.suggestion.compareTo(s1.suggestion);
		}		
	}
}

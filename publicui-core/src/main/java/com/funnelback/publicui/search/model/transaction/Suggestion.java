package com.funnelback.publicui.search.model.transaction;

import java.util.Comparator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Suggestion {
    
    @RequiredArgsConstructor
    public enum DisplayType {
        Unknown(""), Text("T"), HTML("H"), Javascript("C"), JSON("J"), Experimental("X");
        
        public final String value;
        
        public static DisplayType fromValue(String value) {
            for(DisplayType dt: DisplayType.values()) {
                if (dt.value.equals(value)) {
                    return dt;
                }
            }
            throw new IllegalArgumentException(value);
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    @RequiredArgsConstructor
    public enum ActionType {
        Unknown(""), Javascript("C"), URL("U"), Query("Q"), Experimental("X");
        
        public final String value;
        
        public static ActionType fromValue(String value) {
            for(ActionType at: ActionType.values()) {
                if (at.value.equals(value)) {
                    return at;
                }
            }
            throw new IllegalArgumentException(value);
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    @Getter @Setter private int length;
    @Getter @Setter private String key;
    @Getter @Setter private float weight;
    @Getter @Setter private String display;
    @Getter @Setter private DisplayType displayType;
    @Getter @Setter private String category;
    @Getter @Setter private String categoryType;
    @Getter @Setter private String action;
    @Getter @Setter private ActionType actionType;
        
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    public double score(float alpha, int inputQueryLength) {
        return (alpha*(double)weight) + (1.0-alpha) / ((double)display.length()-inputQueryLength);
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
            return s2.display.compareTo(s1.display);
        }        
    }
}

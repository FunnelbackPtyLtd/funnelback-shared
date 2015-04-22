package com.funnelback.publicui.search.model.curator.trigger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lombok.extern.log4j.Log4j2;

/**
 * Enumeration of different string matching types available within Curator triggers.
 */
@Log4j2
public enum StringMatchType {

    /**
     * Returns true if the haystack and the needle contain the same sequence of
     * characters.
     */
    EXACT {
        @Override
        public Boolean matches(String needle, String haystack) {
            return haystack.equals(needle);
        }
    },
    /**
     * Returns true if the lower-cased (in default locale) haystack contains the
     * same sequence of characters as the lower-cased (in default locale)
     * needle.
     */
    CASE_INSENSITIVE_EXACT {
        @Override
        public Boolean matches(String needle, String haystack) {
            return haystack.equalsIgnoreCase(needle);
        }
    },
    /**
     * Returns true if the haystack contains the needle.
     */
    SUBSTRING {
        @Override
        public Boolean matches(String needle, String haystack) {
            return haystack.contains(needle);
        }
    },
    /**
     * Returns true if the lower-cased (in default locale) haystack contains the
     * lower-cased (in default locale) needle.
     */
    CASE_INSENSITIVE_SUBSTRING {
        @Override
        public Boolean matches(String needle, String haystack) {
            return haystack.toLowerCase().contains(needle.toLowerCase());
        }
    },
    /**
     * Returns true if haystack matches the regular expression defined by
     * needle.
     * 
     * Note, needle expression must match the entire haystack.
     */
    REGULAR_EXPRESSION {
        @Override
        public Boolean matches(String needle, String haystack) {
            try {
                return haystack.matches(needle);
            } catch (Exception e) {
                log.error("Invalid regular expression", e);
                return false;
            }
        }
    },
    /**
     * Returns true if each word (regular expression word boundary separated) in the needle occurs
     * in the set of words (regular expression word boundary separated) in the haystack,
     * regardless of order.
     */
    ALL_WORDS_CONTAINED {
        @Override
        public Boolean matches(String needle, String haystack) {
            Set<String> needleWords = new HashSet<String>();
            needleWords.addAll(Arrays.asList(needle.split("\\b")));

            Set<String> haystackWords = new HashSet<String>();
            haystackWords.addAll(Arrays.asList(haystack.split("\\b")));
            
            return haystackWords.containsAll(needleWords);
        }
    },
    /**
     * Returns true if each word (regular expression word boundary separated) in the needle occurs
     * in the set of words (regular expression word boundary separated) in the haystack,
     * ignoring case differences and regardless of order.
     */
    CASE_INSENSITIVE_ALL_WORDS_CONTAINED {
        @Override
        public Boolean matches(String needle, String haystack) {
            return ALL_WORDS_CONTAINED.matches(needle.toLowerCase(), haystack.toLowerCase());
        }
    },
    /**
     * Returns true if the needle is greater than the haystack after both are
     * converted to floats with Float.parseFloat.
     */
    NUMERIC_GREATER_THAN {
        @Override
        public Boolean matches(String needle, String haystack) {
            try {
                return Float.parseFloat(needle) > Float.parseFloat(haystack);
            } catch (NumberFormatException e) {
                log.error("Invalid string for numeric comparison", e);
                return false;
            }
        }
    },
    /**
     * Returns true if the needle is less than the haystack after both are
     * converted to floats with Float.parseFloat.
     */
    NUMERIC_LESS_THAN {
        @Override
        public Boolean matches(String needle, String haystack) {
            try {
                return Float.parseFloat(needle) < Float.parseFloat(haystack);
            } catch (NumberFormatException e) {
                log.error("Invalid string for numeric comparison", e);
                return false;
            }
        }
    };

    /**
     * Returns whether needle is present in haystack for a given type of string
     * match.
     */
    public abstract Boolean matches(String needle, String haystack);
}

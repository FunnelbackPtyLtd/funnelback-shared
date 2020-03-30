package com.funnelback.common.filter.jsoup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

/**
 * This can be used when testing Jsoup filters that need a {@link SetupContext}.
 * 
 * This supports setting the search home, the collection name as well as mocking
 * setting keys in collection.cfg.
 * 
 * Example:
 * <code>
 * MockJsoupSetupContext setupContext = new MockJsoupSetupContext();
 * // Simulate setting keys in collection.cfg
 * setupContext.getConfigSettings().put(&#x22;myfilter.enable.this&#x22;, &#x22;true&#x22;);
 * setupContext.getConfigSettings().put(&#x22;myfilter.enable.that&#x22;, &#x22;false&#x22;);
 * </code>
 * 
 *
 */
public class MockJsoupSetupContext implements SetupContext {

    @Getter @Setter private File searchHome;

    @Getter @Setter private String collectionName;
    
    /**
     * This can be used to mock setting values in collection.cfg.
     * 
     * Must not contain environment prefixed keys, no key should start with 'env.'
     * 
     * Variable expansion will not be done e.g. $SEARCH_HOME will not be expanded.
     * In test set the values to exactly what you expect them to be.
     * 
     */
    @Getter private Map<String, String> configSettings = new HashMap<>();
    
    @Override
    public String getConfigSetting(String key) {
        return configSettings.get(key);
    }

    @Override
    public Set<String> getConfigKeysWithPrefix(String prefix) {
        return configSettings.keySet().stream().filter((a) -> a.startsWith(prefix)).collect(Collectors.toSet());
    }

    @Override
    public Map<String, List<String>> getConfigKeysMatchingPattern(String pattern) {
        return getAllKeysMatchingPattern(pattern, configSettings.keySet());
    }

    @Override
    public File getCollectionConfigFile(String filename) {
        throw new RuntimeException("Not mocked.");
    }

    /**
     * Copied from common WildCardKeyMatcher
     * 
     * @param pattern
     * @param escapedKeyWithParameters
     * @return
     * @throws IllegalArgumentException
     */
    private Optional<List<String>> configKeyMatches(String pattern, String escapedKeyWithParameters) 
            throws IllegalArgumentException {
        // Could have used something like:
        // key.matches(wildCardKey.replace("*", "[\\.^]*"))
        // but it seems that regexes tend to come back and bit us 
        // as they tend to cause poor user experience.
        int wildCardOffset = 0;
        int keyOffset = 0;
        
        List<String> parameters = new ArrayList<>();
        
        
        // simulate last char being a dot so that a key like "*.bar" works as a wild card key
        // or "*" matches everything
        char lastChar = '.';
        while(wildCardOffset < pattern.length()) {
            char wildCardChar = pattern.charAt(wildCardOffset);
            
            
            boolean isWildCard = false;
            boolean isPrefixMatch = false;
            
            if(lastChar == '.' && wildCardChar == '*') {
                boolean isLastChar = wildCardOffset == pattern.length() -1;
                if(isLastChar) { // pattern ends with ".*"
                    isPrefixMatch = true;
                } else {
                    char nextChar = pattern.charAt(wildCardOffset + 1);
                    if(nextChar == '.') { // we have found a ".*."
                        isWildCard = true;
                    }
                }
            }
            
            // we need this incase we have a wild card which is going to match the param
            // that is the empty string.
            if(keyOffset >= escapedKeyWithParameters.length() && !isPrefixMatch && !isWildCard) {
                break;
            }
            
            if(isPrefixMatch) {
                String restOfKey = escapedKeyWithParameters.substring(keyOffset);
                parameters.add(restOfKey);
                keyOffset += restOfKey.length();
                wildCardOffset++;
            } else if(isWildCard) {
                wildCardOffset++;
                int patternNextLitStart = wildCardOffset + 1;
                int patternDotAfterNextLit = pattern.indexOf('.', patternNextLitStart);
                String nextDotAndLiteral = null;
                if(patternDotAfterNextLit == -1) {
                    nextDotAndLiteral = pattern.substring(patternNextLitStart-1);
                } else {
                    nextDotAndLiteral = pattern.substring(patternNextLitStart-1, patternDotAfterNextLit);
                }
                
                if(".*".equals(nextDotAndLiteral)) {
                    throw new IllegalArgumentException("Patterns may not contain consecutive wild cards: Pattern: '"
                        + pattern + "' and Key: '" + escapedKeyWithParameters + "'");
                }
                
                // Now found the literal from the pattern in the given key starting from where 
                // we are up to in the given key.
                
                int indexOfNextLitInKey = escapedKeyWithParameters.indexOf(nextDotAndLiteral, keyOffset);
                if(indexOfNextLitInKey == -1) return Optional.empty();
                
                String param = escapedKeyWithParameters.substring(keyOffset, indexOfNextLitInKey);
                parameters.add(param);
                keyOffset = indexOfNextLitInKey + nextDotAndLiteral.length();
                
                wildCardOffset += nextDotAndLiteral.length(); 
            } else {
                if(wildCardChar != escapedKeyWithParameters.charAt(keyOffset)) return Optional.empty();
                wildCardOffset++;
                keyOffset++;
            }
            lastChar = wildCardChar;
        }
        
        
        if(wildCardOffset == pattern.length() && keyOffset == escapedKeyWithParameters.length()) {
            return Optional.of(parameters);
        }
        return Optional.empty();
    }
    
    /**
     * Copied and modified from WildCardKeyMatcher, this does not deal woth config envoronments.
     * 
     * @param pattern
     * @param hayStack
     * @return
     */
    private Map<String, List<String>> getAllKeysMatchingPattern(String pattern, Collection<String> hayStack) {
        Map<String, List<String>> map = new HashMap<>();
        
        for(String escapedKeyWithParameters : hayStack) {
            Optional<List<String>> res = configKeyMatches(pattern, escapedKeyWithParameters);
            if(res.isPresent()) {
                map.put(escapedKeyWithParameters, res.get());
            }
        }
        return map;
    }
}

package com.funnelback.publicui.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PadreSFEquivalentRegexMatch {

    
    public Set<String> filterToMatchingMetadataClassnames(List<String> metadataClassNames, List<String> regexes) {
        Set<String> matchingClasses = new HashSet<>();
        Set<String> nonMatchingClasses = new HashSet<>(metadataClassNames);
        
        for(String regex : regexes) {
            if(nonMatchingClasses.isEmpty()) {
                break;
            }
            Pattern p = Pattern.compile(regex);
            List<String> metadataClassesAdded = new ArrayList<>();
            for(String mdClass : nonMatchingClasses) {
                Matcher m = p.matcher(mdClass);
                if(m.find()) {
                    String matched = m.group();
                    if(matched.equals(mdClass)) {
                        matchingClasses.add(mdClass);
                        metadataClassesAdded.add(mdClass);
                    }
                }
            }
            matchingClasses.stream().forEach(nonMatchingClasses::remove);
            
        }
        
        return matchingClasses;
    }
}

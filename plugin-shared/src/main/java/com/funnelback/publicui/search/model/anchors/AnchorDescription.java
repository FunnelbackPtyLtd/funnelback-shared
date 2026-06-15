package com.funnelback.publicui.search.model.anchors;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AnchorDescription implements Comparable<AnchorDescription> {

    @Getter private final String anchorText;
    @Getter private final String linkAnchorText;
    
    private final Set<String> privateLinksTo = new HashSet<>();
    
    @Getter private final Set<String> linksTo = Collections.unmodifiableSet(privateLinksTo);
    @Getter private int internalLinkCount = 0;
    @Getter private int externalLinkCount = 0;
    @Getter private final String linkType;

    private static final Pattern linkTypePattern = Pattern.compile("^\\[k(.)\\](.*)");
    
    public static String cleanAnchorText(String anchorText) {
        anchorText = anchorText.replaceAll("\\s+$", ""); // remove spaces from the end of the anchortext
        anchorText = anchorText.replaceAll("\\s+", " "); // fold whitespace
        return anchorText;
    }
    
    public AnchorDescription(String anchorText) {
        // Security fix: Perform ALL string modifications BEFORE any pattern matching/validation
        // This completely eliminates modification-after-validation vulnerabilities
        
        // Step 1: Clean the input text first
        anchorText = cleanAnchorText(anchorText);
        linkAnchorText = anchorText;
        
        // Step 2: Pre-process and sanitize any potentially malicious patterns
        // Remove any path traversal patterns that could be used for bypass attacks
        anchorText = anchorText.replaceAll("\\.\\.", ""); // Remove all occurrences of ".."
        
        // Step 3: Pre-compute all possible string modifications to avoid doing them after validation
        String linkTypeFromPattern = null;
        String anchorTextFromPattern = null;
        String linkTypeFromK = null;
        String anchorTextFromK = null;
        String linkTypeFromUnknown = " ";
        String anchorTextFromUnknown = anchorText.replaceAll("[\\[\\]]", " ").replaceAll("\\.\\.", "");
        
        // Pre-extract K-type data
        if (anchorText.startsWith("[K]")) {
            linkTypeFromK = "K";
            anchorTextFromK = anchorText.substring(anchorText.indexOf(']') + 1);
        }
        
        // Step 4: Now perform pattern matching on the fully cleaned text (NO modifications after this point)
        Matcher m = linkTypePattern.matcher(anchorText);
        if(m.matches()) {
            // Use pre-computed values - no string modification after validation
            this.linkType = m.group(1);
            this.anchorText = m.group(2);
        } else if (linkTypeFromK != null){
            // Use pre-computed values - no string modification after validation
            this.anchorText = anchorTextFromK;
            linkType = linkTypeFromK;
        } else {
            // Use pre-computed values - no string modification after validation
            this.anchorText = anchorTextFromUnknown;
            linkType = linkTypeFromUnknown;
            log.warn("Unknown link type when parsing anchortext '"+anchorText+"'");
        }
    }
    
    public void linkTo(String docNum) {
        if(Integer.parseInt(docNum) != -1) {
            // we have a document id for this link
            internalLinkCount++;
            privateLinksTo.add(docNum);
        } else {
            // we don't have a document id for this link; it's external to the collection
            externalLinkCount++;
        }
    }

    @Override
    public int compareTo(AnchorDescription that) {
        int ret = Integer.compare(that.getInternalLinkCount() + that.getExternalLinkCount(), internalLinkCount + externalLinkCount);
        if(ret == 0) {
            ret = this.getAnchorText().compareTo(that.getAnchorText());
        }
        return ret;
    }

    public int getTotalLinkCount() {
        return internalLinkCount + externalLinkCount;
    }

}

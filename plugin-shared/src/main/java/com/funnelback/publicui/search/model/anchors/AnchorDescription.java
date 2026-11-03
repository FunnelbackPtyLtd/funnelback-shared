package com.funnelback.publicui.search.model.anchors;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String normalizedAnchorText = cleanAnchorText(anchorText);
        linkAnchorText = normalizedAnchorText;
        // Pre-compute unknown-link-type text before pattern matching (FindSecBugs MODIFICATION_AFTER_VALIDATION).
        String unknownLinkTypeAnchorText = normalizedAnchorText.replaceAll("[\\[\\]]", " ");
        Matcher m = linkTypePattern.matcher(normalizedAnchorText);
        if(m.matches()) {
            // this is an anchortext with a link type
            this.linkType = m.group(1);
            this.anchorText = m.group(2);
        } else if (normalizedAnchorText.startsWith("[K]")){
            this.anchorText = normalizedAnchorText.substring(normalizedAnchorText.indexOf(']') +1);    
            linkType = "K";
        } else {
            // this is an anchortext with an unknown link type
            this.anchorText = unknownLinkTypeAnchorText;
            linkType = " ";
            log.warn("Unkown link type when parsing anchortext '"+normalizedAnchorText+"'");
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

package com.funnelback.publicui.search.model.anchors;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

@Log4j
public class AnchorDescription implements Comparable<AnchorDescription> {

    @Getter private final String anchorText;
    @Getter private final String linkAnchorText;
    
    private final Set<String> privateLinksTo = new HashSet<String>();
    
    @Getter private final Set<String> linksTo = Collections.unmodifiableSet(privateLinksTo);
    @Getter private int internalLinkCount = 0;
    @Getter private int externalLinkCount = 0;
    @Getter private final String linkType;

    private static Pattern linkTypePattern = Pattern.compile("^\\[k(.)\\](.*)");
    
    public static String cleanAnchorText(String anchorText) {
        anchorText = anchorText.replaceAll("\\s+$", ""); // remove spaces from the end of the anchortext
        anchorText = anchorText.replaceAll("\\s+", " "); // fold whitespace
        return anchorText;
    }
    
    public AnchorDescription(String anchorText) {
        anchorText = cleanAnchorText(anchorText);
        linkAnchorText = anchorText;
        Matcher m = linkTypePattern.matcher(anchorText);
        if(m.matches()) {
            // this is an anchortext with a link type
            this.linkType = m.group(1);
            this.anchorText = m.group(2);
        } else if (anchorText.startsWith("[K]")){
            this.anchorText = anchorText.substring(anchorText.indexOf(']') +1);    
            linkType = "K";
        } else {
            // this is an anchortext with an unknown link type
            this.anchorText = anchorText.replaceAll("[\\[\\]]", " "); // strip '[' and ']' characters    
            linkType = " ";
            log.warn("Unkown link type when parsing anchortext '"+anchorText+"'");
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
        int ret = new Integer(that.getInternalLinkCount() + that.getExternalLinkCount()).compareTo(internalLinkCount + externalLinkCount);
        if(ret == 0) {
            ret = this.getAnchorText().compareTo(that.getAnchorText());
        }
        return ret;
    }

    public int getTotalLinkCount() {
        return internalLinkCount + externalLinkCount;
    }

}

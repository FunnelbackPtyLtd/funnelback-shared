package com.funnelback.publicui.search.model.anchors;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class AnchorDetail {

    public static final int MAX_URLS_PER_PAGE = 50;
    
    @Getter private final List<String> urls = new ArrayList<String>();
    @Getter private final String linkAnchortext;
    @Getter private final String anchortext;
    @Getter @Setter private int size;
    @Getter @Setter private int start;
    @Getter @Setter private int end;

    public AnchorDetail(String anchorText) {
        linkAnchortext = anchorText;
        if(anchorText.indexOf(']') != -1) {
            this.anchortext = anchorText.substring(anchorText.indexOf(']')+1);
        } else {
            this.anchortext = anchorText;
        }
    }
    
}

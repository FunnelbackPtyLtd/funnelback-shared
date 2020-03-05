package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Quick links, associated to a {@link Result}.
 * 
 * @since 11.0
 */
@AllArgsConstructor
@NoArgsConstructor
public class QuickLinks {

    /**
     * Domain of the quick links. Note: This is not a domain only, but could be
     * a partial URL such as: www.domain.com/folder1/folder2/
     */
    @Getter @Setter private String domain;

    /** List of quick links */
    @Getter
    private final List<QuickLink> quickLinks = new ArrayList<QuickLink>();

    /**
     * <p>A single quick link.</p>
     * 
     * <p>Belongs to a {@link QuickLinks} associated to a
     * search result.</p>
     */
    @AllArgsConstructor
    public static class QuickLink {
        @Getter @Setter private String text;
        @Getter @Setter private String url;
    }
    
}

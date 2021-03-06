package com.funnelback.publicui.search.model.curator.data;

import lombok.EqualsAndHashCode;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.Exhibit;

/**
 * <p>
 * An 'advert' for a URL to be displayed within search results.
 * </p>
 * 
 * <p>
 * It is normally expected that this should be displayed differently to normal
 * search results (otherwise PromoteUrl may be more appropriate).
 * </p>
 * 
 * <p>
 * The URL to be advertised need not exist in the current collection.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class UrlAdvert implements Exhibit {
    /** The title (in HTML) of the advert. */
    @Getter
    @Setter
    private String titleHtml;

    /** The URL to be displayed for the advert. */
    @Getter
    @Setter
    private String displayUrl;

    /** The URL to which the user should be taken if the advert is clicked. */
    @Getter
    @Setter
    private String linkUrl;

    /** The description (in HTML) body of the advert. */
    @Getter
    @Setter
    private String descriptionHtml;

    /**
     * Any additional properties associated with the advert (for example CSS
     * styling related information).
     */
    @Getter
    @Setter
    private Map<String, Object> additionalProperties;

    /**
     * A category for the advert which may be used by an ftl file to display
     * different types of adverts in different ways.
     */
    @Getter
    @Setter
    private String category;
}

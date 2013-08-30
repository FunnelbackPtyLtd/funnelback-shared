package com.funnelback.publicui.search.model.curator.data;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.funnelback.publicui.search.model.curator.Exhibit;

/**
 * Message represents a 'message' added by curator to be displayed within the
 * search results.
 */
@AllArgsConstructor @NoArgsConstructor
public class Message implements Exhibit {

    /** The HTML content of the message to display. */
    @Getter
    @Setter
    private String messageHtml;

    /**
     * Any additional properties associated with the message (for example CSS
     * styling related information).
     */
    @Getter
    @Setter
    private Map<String, String> additionalProperties;

    /**
     * A category for the message which may be used by an ftl file to display
     * different types of messages in different ways.
     */
    @Getter
    @Setter
    private String category;

}

package com.funnelback.publicui.search.model.transaction.session;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;

import com.funnelback.publicui.search.model.padre.Result;

import lombok.Getter;
import lombok.Setter;

/**
 * A single entry in the user's click history
 * 
 * @since 12.5
 */
@Entity
public class ClickHistory extends SessionResult {

    /** Date when the click was performed */
    @Getter @Setter
    private Date clickDate;
    
    /**
     * <p>Query that returned this result</p>
     * <p>Might be null if there was no referer available.</p>
     **/
    @Getter @Setter
    private String query;
    
    /**
     * Metadata values for the result
     */
    @Getter
    @ElementCollection
    @MapKeyColumn(name = "key")
    @CollectionTable(name="ClickHistoryMetadata", joinColumns = {
        @JoinColumn(name="userId"),
        @JoinColumn(name="collection"),
        @JoinColumn(name="indexUrl")
        })
    private final Map<String, String> metaData = new HashMap<>();

    /**
     * Builds a {@link ClickHistory} from a {@link Result}
     * @param r The {@link Result} to build from
     * @return A {@link ClickHistory}
     */
    public static ClickHistory fromResult(Result r) {
        ClickHistory ch = new ClickHistory();
        ch.setCollection(r.getCollection());
        ch.setIndexUrl(URI.create(r.getIndexUrl()));
        ch.setTitle(r.getTitle());
        ch.setSummary(r.getSummary());
        ch.getMetaData().putAll(r.getMetaData());
        
        return ch;

    }

}

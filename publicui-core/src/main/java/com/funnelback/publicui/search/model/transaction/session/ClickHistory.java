package com.funnelback.publicui.search.model.transaction.session;

import java.net.URI;
import java.util.Date;

import javax.persistence.Entity;

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
     * Creates a {@link ClickHistory} from a {@link com.funnelback.publicui.search.model.padre.Result}
     * @param r {@link com.funnelback.publicui.search.model.padre.Result} to clone
     * @return A {@link ClickHistory} with copied fields
     */
    public static ClickHistory fromResult(Result r) {
        ClickHistory h = new ClickHistory();
        h.setCollection(r.getCollection());
        h.setIndexUrl(URI.create(r.getIndexUrl()));
        h.setTitle(r.getTitle());
        h.setSummary(r.getSummary());
        h.getMetaData().putAll(r.getMetaData());

        return h;
    }

}

package com.funnelback.publicui.recommender;

import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.reporting.recommender.tuple.ItemTuple;
import lombok.Getter;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
/**
 * A recommendation in the Recommender System.
 * @author fcrimmins@funnelback.com
 */
public class Recommendation {
    /**
     * The canonical name or ID for the item e.g. URL address, musician name, product ID etc.
     */
    @Getter
    private String itemID = "";

    /**
     * Source of this recommendation.
     */
    @Getter
    private ItemTuple.Source source;

    /**
        * The title of the item e.g. title of web page etc.
        */
    @Getter
    private String title = "";

    /**
        * The date of the item (which may be null). The semantics of the date will depend on
     * the type of item, but usually defaults to Last-Modified-Date.
        */
    @Getter
    private Date date;

    /**
        * The QIE score of the item (-1 if not available/applicable).
        */
    @Getter
    private float qieScore = -1;

    /**
     * Any metadata associated with the item being recommended.
     */
    @Getter
    private Map<String, List<String>> metaData;

    /**
     * Description taken from metadata.
     */
    @Getter
    private String description = "";

    /**
     * Format taken from metadata.
     */
    @Getter
    private String format = "";

    /**
     * The frequency of occurrence of this item (e.g. number of times it appeared in different data sources).
     */
    @Getter
    private int frequency = 0;

    public Recommendation(ItemTuple itemTuple, DocInfo docInfo) {
        this.itemID = itemTuple.getItemID();
        this.source = itemTuple.getSource();
        this.title = docInfo.getTitle();
        this.date = docInfo.getDate();
        this.qieScore = docInfo.getQieScore();
        this.metaData = docInfo.getMetaData();
        this.frequency = itemTuple.getFrequency();

        if (metaData.containsKey("c")) {
            String value = metaData.get("c").get(0);
    
            if (value != null) {
                this.description = StringEscapeUtils.escapeHtml4(value);
            }
        }

        if (metaData.containsKey("f")) {
            String value = metaData.get("f").get(0);
    
            if (value != null) {
                this.format = StringEscapeUtils.escapeHtml4(value);
            }
        }
    }
}
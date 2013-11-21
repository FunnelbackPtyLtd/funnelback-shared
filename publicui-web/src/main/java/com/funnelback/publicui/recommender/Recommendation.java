package com.funnelback.publicui.recommender;

import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.reporting.recommender.tuple.ItemTuple;
import lombok.Getter;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.Date;
import java.util.Map;
/**
 * A recommendation in the recommender system.
 * @author fcrimmins@funnelback.com
 */
public class Recommendation {
	/**
	 * The canonical name or ID for the item e.g. URL address, musician name, product ID etc.
	 */
    @Getter
    private String itemID = "";

    /**
   	 * The confidence value assigned to this recommendation.
   	 */
    @Getter
    private float confidence = 0;

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
    private Map<String, String> metaData;

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
     * Source of this recommendation.
     */
    @Getter
    private ItemTuple.Source source;

    public Recommendation(String item, float confidence, DocInfo docInfo, ItemTuple.Source source) {
        this.itemID = item;
        this.confidence = confidence;
        this.title = docInfo.getTitle();
        this.date = docInfo.getDate();
        this.qieScore = docInfo.getQieScore();
        this.metaData = docInfo.getMetaData();
        this.source = source;

        String value = metaData.get("c");

        if (value != null) {
            this.description = StringEscapeUtils.escapeHtml(value);
        }

        value = metaData.get("f");

        if (value != null) {
            this.format = StringEscapeUtils.escapeHtml(value);
        }
    }
}
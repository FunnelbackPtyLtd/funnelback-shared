package com.funnelback.publicui.recommender;

import com.funnelback.dataapi.connector.padre.docinfo.DocInfo;
import com.funnelback.publicui.search.model.padre.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.Date;
import java.util.Map;
/**
 * A recommendation in the recommender system.
 * @author fcrimmins@funnelback.com
 */
@Data
@AllArgsConstructor
public class Recommendation {
	/**
	 * The canonical name or ID for the item e.g. URL address, musician name, product ID etc.
	 */
    private String itemID = "";

    /**
   	 * The confidence value assigned to this recommendation.
   	 */
    private float confidence = 0;

    /**
   	 * The title of the item e.g. title of web page etc.
   	 */
    private String title = "";

    /**
   	 * The date of the item (which may be null). The semantics of the date will depend on
     * the type of item, but usually defaults to Last-Modified-Date.
   	 */
    private Date date;

    /**
   	 * The QIE score of the item (-1 if not available/applicable).
   	 */
    private float qieScore = -1;

    /**
     * Any metadata associated with the item being recommended.
     */
    private Map<String, String> metaData;

    /**
     * Description taken from metadata.
     */
    private String description = "";

    /**
     * Format taken from metadata.
     */
    private String format = "";

    public Recommendation(String item, float confidence, DocInfo docInfo) {
        this.itemID = item;
        this.confidence = confidence;
        this.title = docInfo.getTitle();
        this.date = docInfo.getDate();
        this.qieScore = docInfo.getQieScore();
        this.metaData = docInfo.getMetaData();

        String value = metaData.get("c");

        if (value != null) {
            this.description = StringEscapeUtils.escapeHtml(value);
        }

        value = metaData.get("f");

        if (value != null) {
            this.format = StringEscapeUtils.escapeHtml(value);
        }
    }
    
    public static Recommendation fromResult(Result result){
    	String format = "";
    	if (null != result.getMetaData().get("f"))
    		format = StringEscapeUtils.escapeHtml(result.getMetaData().get("f"));

            // This call uses the @AllArgsConstructor annotation, which fills in the object fields in order
            Recommendation recommendation = new Recommendation(result.getDisplayUrl(),
    			0,//result.getExplain().getFinalScore(), 
    			result.getTitle(), 
    			result.getDate(), 
    			0,
    			result.getMetaData(), 
    			result.getSummary(), 
    			format);
    	return recommendation;
    }
}
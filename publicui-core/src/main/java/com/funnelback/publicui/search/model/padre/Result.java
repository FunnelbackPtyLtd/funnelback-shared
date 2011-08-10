package com.funnelback.publicui.search.model.padre;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A single search result.
 * 
 * @since 11.0
 */
@ToString
@AllArgsConstructor
public class Result implements ResultType {

	/**
	 * Date format used in PADRE results.
	 */
	public static final String DATE_PATTERN = "d MMM yyyy";

	/**
	 * Prefix for Metadata fields in the MetaData Map.
	 */
	public static final String METADATA_PREFIX = Schema.METADATA + "_";
	
	/** Rank of the result (From 1 to n) */
	@Getter @Setter private Integer rank;
	/** Score of the result (From 1000 to 0) */
	@Getter @Setter private Integer score;
	/** Title of the result */
	@Getter @Setter private String title;
	
	/**
	 * <p>ID of the collection to which this result belongs.</p>
	 * 
	 * <p>This is usually the same ID as the collection being
	 * searched, except for Meta collections where results can
	 * come from different sub-collections.</p>
	 */
	@Getter @Setter private String collection;
	
	/**
	 * <p>For meta collections it's the internal component
	 * number of the sub-collection this result is coming
	 * from.</p>
	 * 
	 * <p>For non-meta collections it's always zero.</p>
	 * 
	 * @see Result#collection
	 */
	@Getter @Setter private Integer component;
	
	/** URL to access the search result. */
	@Getter @Setter private String liveUrl;
	
	/** Query-biased summary */
	@Getter @Setter private String summary;
	
	/** URL to access the cached version of the result. */
	@Getter @Setter private String cacheUrl;
	
	/** Date of the search result */
	@Getter @Setter private Date date;
	
	/**
	 * Size of the file corresponding to the search
	 * results, in bytes.
	 */
	@Getter @Setter private Integer fileSize;
	
	/**
	 * File type of the result, usually the file extension
	 * ("pdf", "xls", "html", ...)
	 */
	@Getter @Setter private String fileType;
	
	/**
	 * Tier number to which this search results belongs.
	 * 
	 * @see {@link ResultPacket#getResultsWithTierBars()}
	 */
	@Getter @Setter private Integer tier;
	
	/**
	 * Internal document number of the result
	 * in the index.
	 */
	@Getter @Setter private Integer docNum;
	
	/**
	 * Distance in kilometres of this search result
	 * from the origin set when the query is run.
	 */
	@Getter @Setter private Float kmFromOrigin;
	
	/**
	 * <p>Map containing the metadata fields of the results.</p>
	 * 
	 * <p>The key is the letter the metadata is mapped on.</p>
	 * 
	 * @see <code>metamap.cfg</code>, <code>xml.cfg</code>
	 */
	@Getter @Setter private Map<String, String> metaData;
	
	/** Quick links associated with the result. */
	@Getter @Setter private QuickLinks quickLinks;
	
	/**
	 * <p>URL to display for the result.</p>
	 * 
	 * <p>The {@link #liveUrl} will be replaced by a click tracking URL
	 * if click tracking is enabled. This field can be used to display
	 * the initial live URL of the document to the user.</p>
	 * 
	 * <p>The {@link #liveUrl} is copied on this field by default.</p>
	 **/
	@Getter @Setter private String displayUrl;
		
	/** URL for click tracking. */
	@Getter @Setter private String clickTrackingUrl;

	/**
	 * Explain data used in the Content Optimiser.
	 */
	@Getter @Setter private Explain explain;
	
	/**
	 * Original URL from the index, taken from indexUrl before any transformation.
	 */
	@Getter final private String indexUrl;

	/**
	 * Custom data placeholder allowing any arbitrary data to be
	 * stored by hook scripts.
	 */
	@Getter private final Map<String, Object> customData = new HashMap<String, Object>();
	
	/** Constants for the PADRE XML result packet tags. */
	public static final class Schema {
		
		public static final String RESULT = "result";
		
		public static final String RANK = "rank";
		public static final String SCORE = "score";
		public static final String TITLE = "title";
		public static final String COLLECTION = "collection";
		public static final String COMPONENT = "component";
		public static final String LIVE_URL = "live_url";
		public static final String SUMMARY = "summary";
		public static final String CACHE_URL = "cache_url";		
		public static final String DATE = "date";
		public static final String FILESIZE = "filesize";
		public static final String FILETYPE = "filetype";
		public static final String TIER = "tier";
		public static final String DOCNUM = "docnum";
		public static final String KM_FROM_ORIGIN = "km_from_origin";
		public static final String EXPLAIN = "explain";
		public static final String METADATA = "md";
		public static final String ATTR_METADATA_F = "f";
	}
}


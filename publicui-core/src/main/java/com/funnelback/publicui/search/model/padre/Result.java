package com.funnelback.publicui.search.model.padre;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A single search result
 */
@ToString
@AllArgsConstructor
public class Result implements ResultType {

	/**
	 * Date format used in padre results
	 */
	public static final String DATE_PATTERN = "d MMM yyyy";

	/**
	 * Prefix for Metadata fields in the MetaData Map.
	 */
	public static final String METADATA_PREFIX = Schema.METADATA + "_";
	
	@Getter @Setter private Integer rank;
	@Getter @Setter private Integer score;
	@Getter @Setter private String title;
	@Getter @Setter private String collection;
	@Getter @Setter private Integer component;
	@Getter @Setter private String liveUrl;
	@Getter @Setter private String summary;
	@Getter @Setter private String cacheUrl;
	@Getter @Setter private Date date;
	@Getter @Setter private Integer fileSize;
	@Getter @Setter private String fileType;
	@Getter @Setter private Integer tier;
	@Getter @Setter private Integer docNum;
	@Getter @Setter private Float kmFromOrigin;
	@Getter @Setter private Map<String, String> metaData;
	@Getter @Setter private QuickLinks quickLinks;
	
	/** By default filled with liveUrl, but will be updated at a later stage */
	@Getter @Setter private String displayUrl;
		
	/** Filled at a later stage */
	@Getter @Setter private String clickTrackingUrl;

	@Getter @Setter private Explain explain;
	
	/**
	 * Original URL from the index, taken from indexUrl before any transformation.
	 */
	@Getter final private String indexUrl;

	/**
	 * Custom data place holder for custom processors and
	 * hooks. Anything can be put there by users.
	 */
	@Getter private final Map<String, Object> customData = new HashMap<String, Object>();
	
	/**
	 * Represents XML Schema
	 *
	 */
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


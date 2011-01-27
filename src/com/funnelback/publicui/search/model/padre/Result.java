package com.funnelback.publicui.search.model.padre;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class Result {

	/**
	 * Date format used in padre results
	 */
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("d MMM yyyy");

	/**
	 * Prefix for Metadata fields in the MetaData Map.
	 */
	public static final String METADATA_PREFIX = Schema.METADATA + "_";
	
	@Getter private Integer rank;
	@Getter private Integer score;
	@Getter private String title;
	@Getter private String collection;
	@Getter private Integer component;
	@Getter @Setter private String liveUrl;
	@Getter private String summary;
	@Getter private String cacheUrl;
	@Getter private Date date;
	@Getter private Integer fileSize;
	@Getter private String fileType;
	@Getter private Integer tier;
	@Getter private Integer docNum;
	@Getter private Map<String, String> metaData;
	
	/** By default filled with liveUrl, but will be updated at a later stage */
	@Getter private String displayUrl;

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
		public static final String METADATA = "md";				
	}
}

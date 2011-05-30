package com.funnelback.publicui.search.model.padre;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Details (header) of a result packet
 */
@ToString
@AllArgsConstructor
public class Details {

	public static final String UPDATED_DATE_PATTERN = "EEE MMM dd HH:mm:ss yyyy";
	
	@Getter @Setter private String padreVersion;
	@Getter @Setter private String collectionSize;
	@Getter @Setter private Date collectionUpdated;
	
	public static final class Schema {
		public static final String DETAILS = "details";
		
		public static final String PADRE_VERSION = "padre_version";
		public static final String COLLECTION_SIZE = "collection_size";
		public static final String COLLECTION_UPDATED = "collection_updated";
	}
	
}

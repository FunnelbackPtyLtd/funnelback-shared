package com.funnelback.publicui.search.model.padre;

import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Details (header) of a result packet
 */
@ToString
@RequiredArgsConstructor
public class Details {

	@Getter private final String padreVersion;
	@Getter private final String collectionSize;
	@Getter private final Date collectionUpdated;
	
	public static final class Schema {
		public static final String DETAILS = "details";
		
		public static final String PADRE_VERSION = "padre_version";
		public static final String COLLECTION_SIZE = "collection_size";
		public static final String COLLECTION_UPDATED = "collection_updated";
	}
	
}

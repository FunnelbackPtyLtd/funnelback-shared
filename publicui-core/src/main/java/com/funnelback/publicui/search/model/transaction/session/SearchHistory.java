package com.funnelback.publicui.search.model.transaction.session;

import java.net.URL;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A single entry in the {@link SearchUser} search history
 * 
 * @since v12.4
 */
@ToString
public class SearchHistory {

	/** Collection identifier for this search event */
	@Getter @Setter private String collectionId;
	
	/** Date when the search was performed */
	@Getter @Setter private Date searchDate;
	
	/** Original query as entered by the user */
	@Getter @Setter private String originalQuery;
	
	/** Query as processed by the query processor */
	@Getter @Setter private String queryAsProcessed;
	
	/** Total number of results returned */
	@Getter @Setter private int totalMatching;
	
	/** Starting page offset */
	@Getter @Setter private int currStart;
	
	/** Number of results per page */
	@Getter @Setter private int numRanks;
	
	/** URL used to perform the search */
	@Getter @Setter private URL searchUrl;

}

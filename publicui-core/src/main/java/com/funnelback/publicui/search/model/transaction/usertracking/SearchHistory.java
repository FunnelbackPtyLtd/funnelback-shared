package com.funnelback.publicui.search.model.transaction.usertracking;

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

	@Getter @Setter private SearchUser user;
	@Getter @Setter private Date searchDate;
	@Getter @Setter private String originalQuery;
	@Getter @Setter private String queryAsProcessed;
	@Getter @Setter private int totalMatching;
	@Getter @Setter private int currStart;
	@Getter @Setter private int numRanks;
	@Getter @Setter private URL searchUrl;

}

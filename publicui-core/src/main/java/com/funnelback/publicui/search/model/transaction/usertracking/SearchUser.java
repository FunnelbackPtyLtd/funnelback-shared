package com.funnelback.publicui.search.model.transaction.usertracking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * User performing the search.
 * 
 * @since v12.4
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SearchUser {

	@Getter @Setter private String id;

}

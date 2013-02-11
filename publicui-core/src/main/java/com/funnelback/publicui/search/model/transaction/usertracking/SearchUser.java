package com.funnelback.publicui.search.model.transaction.usertracking;

import java.util.ArrayList;
import java.util.List;

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

	/** Unique identifier of the user */
	@Getter @Setter private String id;
	
}

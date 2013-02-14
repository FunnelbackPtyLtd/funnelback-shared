package com.funnelback.publicui.search.model.transaction.session;

import java.io.Serializable;

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
public class SearchUser implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** Unique identifier of the user */
	@Getter @Setter private String id;
	
}

package com.funnelback.publicui.search.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Error occuring during a {@link SearchTransaction}
 */
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class SearchError {

	public enum Reason {
		InvalidCollection,
		MissingParameter,
		InputProcessorError,
		DataFetchError,
		Unknown;
	}

	@Getter	private final Reason reason;
	@Getter @Setter private Object additionalData;
	
}

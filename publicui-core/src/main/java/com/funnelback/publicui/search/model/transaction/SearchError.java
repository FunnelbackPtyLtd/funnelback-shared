package com.funnelback.publicui.search.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.padre.Error;

/**
 * <p>Errors occurring during a {@link SearchTransaction}.</p>
 * 
 * <p>This class contains error that happened during input parameter
 * processing or result transformation. If the query processor returned
 * an error it can be found under {@link Error}
 * 
 * @since 11.0
 */
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class SearchError {

	/**
	 * Generic reasons of errors.
	 */
	public enum Reason {
		InvalidCollection,
		MissingParameter,
		InputProcessorError,
		DataFetchError,
		OutputProcessorError,
		Unknown;
	}

	/**
	 * Reason for this error.
	 */
	@Getter	private final Reason reason;
	
	/**
	 * The original {@link Exception} that occured,
	 * if available.
	 */
	@Getter @Setter private Exception additionalData;
	
}

package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * A supplementary query, in the query blending mechanism.
 * 
 * @since 11.0
 */
@AllArgsConstructor
public class QSup {

	/** Query blending sources */
	public enum Source {
		/** US / UK conflation */ USUK,
		/** Spelling suggestions */ SPEL,
		/** Synonyms */ SYNS,
		/** Unknown */ Unknown;
	}
	
	/** Source of this additional query */
	@Getter @Setter private Source src = Source.Unknown;
	
	/** Query terms */
	@Getter @Setter private String query;
	
}

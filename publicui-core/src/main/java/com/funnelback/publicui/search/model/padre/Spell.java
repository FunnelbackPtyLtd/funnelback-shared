package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A spelling suggestion
 */
@AllArgsConstructor
@ToString
public class Spell {

	@Getter @Setter private String url;
	@Getter @Setter private String text;

	public static final class Schema {

		public static final String SPELL = "spell";

		public static final String URL = "url";
		public static final String TEXT = "text";
	}
}

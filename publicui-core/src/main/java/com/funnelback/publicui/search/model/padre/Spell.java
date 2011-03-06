package com.funnelback.publicui.search.model.padre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A spelling suggestion
 */
@RequiredArgsConstructor
@ToString
public class Spell {

	@Getter private final String url;
	@Getter private final String text;

	public static final class Schema {

		public static final String SPELL = "spell";

		public static final String URL = "url";
		public static final String TEXT = "text";
	}
}

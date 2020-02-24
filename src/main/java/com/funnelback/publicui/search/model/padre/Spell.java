package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>A spelling suggestion</p>
 * 
 * @since 11.0
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Spell {

    @Getter @Setter private String url;
    @Getter @Setter private String text;

    /** Constants for the PADRE XML result packet tags. */
    public static final class Schema {

        public static final String SPELL = "spell";

        public static final String URL = "url";
        public static final String TEXT = "text";
    }
}
